import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fade-in">
      <div class="page-header">
        <h1>My Profile</h1>
        <p>View your account information</p>
      </div>
      @if (loading()) {
        <div class="loading-overlay"><div class="spinner"></div></div>
      } @else if (profile()) {
        <div class="profile-grid">
          <div class="card profile-card">
            <div class="profile-avatar">{{ profile()!.name.charAt(0) }}</div>
            <h2>{{ profile()!.name }}</h2>
            <p class="profile-role">
              <span class="badge" [class.badge-approved]="profile()!.role === 'ROLE_ADMIN'" [class.badge-draft]="profile()!.role !== 'ROLE_ADMIN'">
                {{ profile()!.role === 'ROLE_ADMIN' ? 'Administrator' : 'Applicant' }}
              </span>
            </p>
          </div>
          <div class="card">
            <div class="card-header"><h2>Account Details</h2></div>
            <div class="detail-row">
              <div class="detail-label">User ID</div>
              <div class="detail-value">#{{ profile()!.id }}</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">Full Name</div>
              <div class="detail-value">{{ profile()!.name }}</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">Email</div>
              <div class="detail-value">{{ profile()!.email }}</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">Role</div>
              <div class="detail-value">{{ profile()!.role }}</div>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .profile-grid { display: grid; grid-template-columns: 280px 1fr; gap: 20px; }
    .profile-card { text-align: center; padding: 36px 24px; }
    .profile-avatar {
      width: 72px; height: 72px; border-radius: 18px;
      background: linear-gradient(135deg, var(--accent), #7c3aed);
      display: flex; align-items: center; justify-content: center;
      font-size: 1.8rem; font-weight: 800; color: #fff;
      margin: 0 auto 16px;
    }
    .profile-card h2 { font-size: 1.2rem; color: #fff; margin-bottom: 8px; }
    .profile-role { margin-top: 4px; }
    .detail-row {
      display: flex; justify-content: space-between; align-items: center;
      padding: 14px 0; border-bottom: 1px solid var(--border);
      &:last-child { border-bottom: none; }
    }
    .detail-label { font-size: 0.82rem; color: var(--text-dim); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 500; }
    .detail-value { font-size: 0.9rem; color: #fff; font-weight: 500; }
    @media (max-width: 768px) { .profile-grid { grid-template-columns: 1fr; } }
  `]
})
export class ProfileComponent implements OnInit {
  profile = signal<User | null>(null);
  loading = signal(true);

  constructor(private auth: AuthService) {}

  ngOnInit(): void {
    this.auth.getProfile().subscribe({
      next: (u) => { this.profile.set(u); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}
