import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../models/user.model';

/**
 * Authentication service managing JWT tokens, user session state,
 * and role-based access using Angular Signals.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API = '/auth';
  private readonly TOKEN_KEY = 'finflow_token';
  private readonly USER_KEY = 'finflow_user';

  /** Signal holding current user info (null if not logged in) */
  currentUser = signal<{ userId: number; name: string; email: string; role: string } | null>(
    this.loadUser()
  );

  /** Computed signal: is user authenticated */
  isLoggedIn = computed(() => !!this.currentUser());

  /** Computed signal: current role */
  currentRole = computed(() => this.currentUser()?.role ?? '');

  /** Computed signal: is admin */
  isAdmin = computed(() => this.currentRole() === 'ROLE_ADMIN');

  /** Computed signal: is applicant */
  isApplicant = computed(() => this.currentRole() === 'ROLE_APPLICANT');

  constructor(private http: HttpClient, private router: Router) {}

  /** Register a new user */
  register(req: RegisterRequest): Observable<string> {
    return this.http.post(`${this.API}/signup`, req, { responseType: 'text' });
  }

  /** Login and store JWT token + user info */
  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/login`, req).pipe(
      tap(res => {
        localStorage.setItem(this.TOKEN_KEY, res.token);
        const user = { userId: res.userId, name: res.name, email: res.email, role: res.role };
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        this.currentUser.set(user);
      })
    );
  }

  /** Logout — clear storage and redirect */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  /** Get JWT token from localStorage */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /** Check if token exists */
  hasToken(): boolean {
    return !!this.getToken();
  }

  /** Fetch user profile from the backend */
  getProfile(): Observable<User> {
    return this.http.get<User>(`${this.API}/profile`);
  }

  /** Load user from localStorage on app startup */
  private loadUser(): { userId: number; name: string; email: string; role: string } | null {
    const stored = localStorage.getItem(this.USER_KEY);
    return stored ? JSON.parse(stored) : null;
  }
}
