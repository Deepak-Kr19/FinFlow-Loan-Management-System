import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

/**
 * Functional HTTP interceptor:
 * 1. Attaches JWT Bearer token to all API requests (except login/signup)
 * 2. Globally handles 401 (Unauthorized) — clears token and redirects to login
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('finflow_token');

  let cloned = req;
  if (token && !req.url.includes('/auth/login') && !req.url.includes('/auth/signup')) {
    cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(cloned).pipe(
    catchError(err => {
      if (err.status === 401) {
        localStorage.removeItem('finflow_token');
        localStorage.removeItem('finflow_user');
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};
