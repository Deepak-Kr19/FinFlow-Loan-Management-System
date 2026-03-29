package com.capg.apigateway.filter;

import com.capg.apigateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global authentication filter for the API Gateway.
 * Intercepts every incoming request and performs JWT validation
 * for secured routes (non-public endpoints).
 *
 * Workflow:
 * 1. Check if the route is secured (using RouteValidator)
 * 2. If open route (login, signup, swagger) → pass through
 * 3. If secured route → extract and validate JWT from Authorization header
 * 4. On valid JWT → inject X-User-Id and X-User-Role headers for downstream services
 * 5. On invalid JWT → return 401 Unauthorized
 *
 * Order: -1 (runs before all other filters)
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        log.info("Incoming request: {} {} ", method, path);

        // Check if this route requires authentication
        if (validator.isSecured.test(request)) {

            // Extract the Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null) {
                log.warn("Missing Authorization header for secured route: {} {}", method, path);
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            // Strip "Bearer " prefix to get the raw JWT token
            if (authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            }

            try {
                // Validate the JWT signature and expiration
                jwtUtil.validateToken(authHeader);

                // Extract userId and role from JWT claims
                io.jsonwebtoken.Claims claims = jwtUtil.extractAllClaims(authHeader);
                String userId = String.valueOf(claims.get("userId"));
                String role = String.valueOf(claims.get("role"));

                log.info("JWT validated — userId: {}, role: {}, routing to: {}", userId, role, path);

                // Inject user identity headers for downstream microservices
                // These headers are used by services instead of parsing the JWT themselves
                request = exchange.getRequest()
                        .mutate()
                        .header("X-User-Id", userId)    // User's database ID
                        .header("X-User-Role", role)    // User's role (ROLE_APPLICANT, ROLE_ADMIN)
                        .build();

            } catch (Exception e) {
                log.error("JWT validation failed for {} {}: {}", method, path, e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        } else {
            log.debug("Open route — skipping auth: {} {}", method, path);
        }

        // Continue the filter chain with the (possibly modified) request
        return chain.filter(exchange.mutate().request(request).build());
    }

    /**
     * Returns an error response to the client, terminating the filter chain.
     * @param exchange   the current server web exchange
     * @param httpStatus the HTTP status code to return (e.g., 401)
     */
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    /**
     * Filter execution order. Returns -1 to ensure this filter runs
     * before all other gateway filters (route-specific filters).
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
