import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApplicationService } from '../../../core/services/application.service';
import { AuthService } from '../../../core/services/auth.service';
import { LoanApplication } from '../../../core/models/application.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="fade-in">
      <div class="page-header">
        <h1>Welcome, {{ auth.currentUser()?.name }} 👋</h1>
        <p>Here's a summary of your loan applications</p>
      </div>

      <div class="stat-grid">
        <div class="stat-card"><div class="stat-icon">📋</div><div class="stat-value">{{ apps().length }}</div><div class="stat-label">Total</div></div>
        <div class="stat-card"><div class="stat-icon">📝</div><div class="stat-value">{{ countStatus('Draft') }}</div><div class="stat-label">Drafts</div></div>
        <div class="stat-card"><div class="stat-icon">📤</div><div class="stat-value">{{ countStatus('Submitted') }}</div><div class="stat-label">Submitted</div></div>
        <div class="stat-card"><div class="stat-icon">✅</div><div class="stat-value">{{ countStatus('APPROVED') }}</div><div class="stat-label">Approved</div></div>
      </div>

      <div class="grid-2">
        <div class="card">
          <div class="card-header"><h2>Quick Actions</h2></div>
          <div class="actions-list">
            <a routerLink="/applications/new" class="action-card">
              <span class="act-icon">➕</span>
              <div><strong>New Application</strong><p>Start a new loan application</p></div>
              <span class="act-arrow">→</span>
            </a>
            <a routerLink="/applications" class="action-card">
              <span class="act-icon">📋</span>
              <div><strong>My Applications</strong><p>Track your existing applications</p></div>
              <span class="act-arrow">→</span>
            </a>
            <a routerLink="/profile" class="action-card">
              <span class="act-icon">👤</span>
              <div><strong>My Profile</strong><p>View your account details</p></div>
              <span class="act-arrow">→</span>
            </a>
          </div>
        </div>
        <div class="card">
          <div class="card-header"><h2>Recent Applications</h2><a routerLink="/applications" class="btn btn-sm btn-secondary">View All</a></div>
          @for (app of apps().slice(0, 4); track app.id) {
            <a [routerLink]="['/applications', app.id]" class="recent-row">
              <div><strong>#{{ app.id }}</strong><p>{{ app.loanDetails | slice:0:40 }}...</p></div>
              <span class="badge" [ngClass]="'badge-' + app.status.toLowerCase()">{{ app.status }}</span>
            </a>
          } @empty {
            <div class="empty-state" style="padding:30px;"><div class="empty-icon">📋</div><p>No applications yet</p></div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .actions-list { display: flex; flex-direction: column; gap: 8px; }
    .action-card {
      display: flex; align-items: center; gap: 14px; padding: 14px;
      border-radius: 10px; background: var(--bg-tertiary); border: 1px solid var(--border);
      transition: all 0.2s; text-decoration: none; color: var(--text);
      &:hover { border-color: var(--accent); transform: translateX(4px); }
      strong { font-size: 0.88rem; color: #fff; display: block; }
      p { font-size: 0.78rem; color: var(--text-dim); margin-top: 2px; }
    }
    .act-icon { font-size: 1.4rem; width: 40px; height: 40px; border-radius: 10px; background: var(--bg-card); display: flex; align-items: center; justify-content: center; }
    .act-arrow { margin-left: auto; color: var(--text-dim); font-size: 1.1rem; }
    .recent-row {
      display: flex; justify-content: space-between; align-items: center;
      padding: 12px 0; border-bottom: 1px solid var(--border);
      text-decoration: none; color: var(--text); transition: all 0.15s;
      &:last-child { border-bottom: none; }
      &:hover { padding-left: 4px; }
      strong { font-size: 0.85rem; color: #fff; }
      p { font-size: 0.75rem; color: var(--text-dim); margin-top: 2px; }
    }
  `]
})
export class DashboardComponent implements OnInit {
  apps = signal<LoanApplication[]>([]);

  constructor(private appService: ApplicationService, public auth: AuthService) {}

  ngOnInit(): void {
    this.appService.getMyApplications().subscribe({ next: (a) => this.apps.set(a) });
  }

  countStatus(s: string): number { return this.apps().filter(a => a.status === s).length; }
}
