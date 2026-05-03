import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="auth-page">
      <div class="auth-bg-orb orb-1"></div>
      <div class="auth-card">
        <div class="auth-header">
          <div class="auth-logo">🏦</div>
          <h1>Create Account</h1>
          <p>Join FinFlow to manage your loans</p>
        </div>
        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label for="name">Full Name</label>
            <input id="name" type="text" class="form-control" formControlName="name" placeholder="John Doe">
            @if (form.get('name')?.invalid && form.get('name')?.touched) {
              <div class="form-error">Name is required</div>
            }
          </div>
          <div class="form-group">
            <label for="email">Email Address</label>
            <input id="email" type="email" class="form-control" formControlName="email" placeholder="you@company.com">
            @if (form.get('email')?.invalid && form.get('email')?.touched) {
              <div class="form-error">Valid email is required</div>
            }
          </div>
          <div class="form-group">
            <label for="password">Password</label>
            <input id="password" type="password" class="form-control" formControlName="password" placeholder="Minimum 6 characters">
            @if (form.get('password')?.invalid && form.get('password')?.touched) {
              <div class="form-error">Minimum 6 characters</div>
            }
          </div>
          <div class="form-group">
            <label for="role">Role</label>
            <select id="role" class="form-control" formControlName="role">
              <option value="ROLE_APPLICANT">Applicant</option>
              <option value="ROLE_ADMIN">Admin</option>
            </select>
          </div>
          <button type="submit" class="btn btn-primary btn-block btn-lg" [disabled]="loading()">
            @if (loading()) { <span class="spinner" style="width:18px;height:18px;border-width:2px;"></span> Creating... }
            @else { Create Account }
          </button>
        </form>
        <div class="auth-footer">Already have an account? <a routerLink="/login">Sign in</a></div>
      </div>
    </div>
  `,
  styles: [`
    .auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: var(--bg-primary); position: relative; overflow: hidden; }
    .auth-bg-orb { position: absolute; border-radius: 50%; filter: blur(80px); }
    .orb-1 { width: 500px; height: 500px; top: -150px; right: -100px; background: rgba(99,102,241,0.15); }
    .auth-card { position: relative; z-index: 1; width: 420px; background: rgba(255,255,255,0.025); border: 1px solid var(--border); border-radius: var(--radius-xl); padding: 40px; backdrop-filter: blur(24px); animation: fadeIn 0.5s ease; }
    .auth-header { text-align: center; margin-bottom: 28px; }
    .auth-logo { font-size: 2.5rem; margin-bottom: 12px; }
    .auth-header h1 { font-size: 1.5rem; font-weight: 700; color: #fff; }
    .auth-header p { color: var(--text-muted); font-size: 0.88rem; margin-top: 4px; }
    .auth-footer { text-align: center; margin-top: 24px; color: var(--text-dim); font-size: 0.85rem; }
  `]
})
export class RegisterComponent {
  form: FormGroup;
  loading = signal(false);

  constructor(private fb: FormBuilder, private auth: AuthService, private toast: ToastService, private router: Router) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['ROLE_APPLICANT', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.auth.register(this.form.value).subscribe({
      next: () => { this.toast.success('Account created! Please login.'); this.router.navigate(['/login']); },
      error: (err) => { this.loading.set(false); this.toast.error(typeof err.error === 'string' ? err.error : 'Registration failed'); }
    });
  }
}
