import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-logo">🏦</span>
        <span class="brand-name">FinFlow</span>
      </div>
      <nav class="nav-links">
        @if (auth.isApplicant()) {
          <a routerLink="/dashboard" routerLinkActive="active" class="nav-link"><span>📊</span> Dashboard</a>
          <a routerLink="/applications" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}" class="nav-link"><span>📋</span> My Applications</a>
          <a routerLink="/applications/new" routerLinkActive="active" class="nav-link"><span>➕</span> New Application</a>
        }
        @if (auth.isAdmin()) {
          <a routerLink="/admin" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}" class="nav-link"><span>📊</span> Dashboard</a>
          <a routerLink="/admin/applications" routerLinkActive="active" class="nav-link"><span>📋</span> Applications</a>
          <a routerLink="/admin/users" routerLinkActive="active" class="nav-link"><span>👥</span> Users</a>
          <a routerLink="/admin/reports" routerLinkActive="active" class="nav-link"><span>📈</span> Reports</a>
        }
        <div class="nav-divider"></div>
        <a routerLink="/profile" routerLinkActive="active" class="nav-link"><span>👤</span> Profile</a>
      </nav>
      <div class="sidebar-footer">
        <div class="user-pill">
          <div class="avatar">{{ auth.currentUser()?.name?.charAt(0) || '?' }}</div>
          <div class="user-meta">
            <div class="user-name">{{ auth.currentUser()?.name }}</div>
            <div class="user-role-tag">{{ auth.isAdmin() ? 'Admin' : 'Applicant' }}</div>
          </div>
        </div>
        <button class="logout-btn" (click)="auth.logout()">Logout</button>
      </div>
    </aside>
  `,
  styles: [`
    .sidebar {
      position: fixed; top: 0; left: 0;
      width: var(--sidebar-w); height: 100vh;
      background: var(--bg-secondary);
      border-right: 1px solid var(--border);
      display: flex; flex-direction: column;
      z-index: 50;
    }
    .brand {
      padding: 24px 20px 20px;
      display: flex; align-items: center; gap: 10px;
      border-bottom: 1px solid var(--border);
    }
    .brand-logo { font-size: 1.5rem; }
    .brand-name {
      font-size: 1.25rem; font-weight: 800;
      background: linear-gradient(135deg, #fff, var(--accent-light));
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
    }
    .nav-links { flex: 1; padding: 16px 12px; display: flex; flex-direction: column; gap: 2px; }
    .nav-link {
      display: flex; align-items: center; gap: 10px;
      padding: 10px 14px; border-radius: 10px;
      color: var(--text-dim); font-size: 0.85rem; font-weight: 500;
      transition: all 0.2s; text-decoration: none;
      span { font-size: 1rem; width: 20px; text-align: center; }
      &:hover { background: var(--bg-card); color: var(--text); }
      &.active { background: var(--accent-glow); color: #fff; font-weight: 600; }
    }
    .nav-divider { height: 1px; background: var(--border); margin: 8px 0; }
    .sidebar-footer { padding: 16px; border-top: 1px solid var(--border); }
    .user-pill {
      display: flex; align-items: center; gap: 10px; margin-bottom: 12px;
    }
    .avatar {
      width: 34px; height: 34px; border-radius: 10px;
      background: linear-gradient(135deg, var(--accent), #7c3aed);
      display: flex; align-items: center; justify-content: center;
      font-weight: 700; font-size: 0.85rem; color: #fff;
    }
    .user-name { font-size: 0.82rem; font-weight: 600; color: #fff; }
    .user-role-tag { font-size: 0.7rem; color: var(--text-dim); text-transform: uppercase; letter-spacing: 0.05em; }
    .logout-btn {
      width: 100%; padding: 8px; border-radius: 8px;
      background: transparent; border: 1px solid var(--border);
      color: var(--text-dim); font-family: 'Inter', sans-serif;
      font-size: 0.8rem; font-weight: 500; cursor: pointer;
      transition: all 0.2s;
      &:hover { background: var(--red-bg); border-color: var(--red); color: var(--red); }
    }
  `]
})
export class LayoutComponent {
  constructor(public auth: AuthService) {}
}
