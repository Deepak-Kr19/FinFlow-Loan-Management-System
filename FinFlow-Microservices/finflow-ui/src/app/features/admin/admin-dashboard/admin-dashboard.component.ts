import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { AuthService } from '../../../core/services/auth.service';
import { LoanApplication } from '../../../core/models/application.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="fade-in">
      <div class="page-header"><h1>Admin Dashboard</h1><p>System overview and application queue</p></div>
      <div class="stat-grid">
        <div class="stat-card"><div class="stat-icon">📋</div><div class="stat-value">{{ apps().length }}</div><div class="stat-label">Total Apps</div></div>
        <div class="stat-card"><div class="stat-icon">⏳</div><div class="stat-value">{{ countStatus('Submitted') }}</div><div class="stat-label">Pending</div></div>
        <div class="stat-card"><div class="stat-icon">✅</div><div class="stat-value">{{ countStatus('APPROVED') }}</div><div class="stat-label">Approved</div></div>
        <div class="stat-card"><div class="stat-icon">❌</div><div class="stat-value">{{ countStatus('REJECTED') }}</div><div class="stat-label">Rejected</div></div>
      </div>
      <div class="grid-2">
        <div class="card">
          <div class="card-header"><h2>Quick Actions</h2></div>
          <div style="display:flex;flex-direction:column;gap:8px;">
            <a routerLink="/admin/applications" class="action-card"><span class="ai">📋</span><div><strong>Review Applications</strong><p>Approve or reject pending applications</p></div></a>
            <a routerLink="/admin/users" class="action-card"><span class="ai">👥</span><div><strong>Manage Users</strong><p>View registered users</p></div></a>
            <a routerLink="/admin/reports" class="action-card"><span class="ai">📈</span><div><strong>Reports</strong><p>View system reports</p></div></a>
          </div>
        </div>
        <div class="card">
          <div class="card-header"><h2>Pending Queue</h2><a routerLink="/admin/applications" class="btn btn-sm btn-secondary">View All</a></div>
          @for (a of pending().slice(0,5); track a.id) {
            <a [routerLink]="['/admin/applications', a.id]" class="pq-row"><div><strong>#{{ a.id }}</strong><p>User #{{ a.userId }}</p></div><span class="badge badge-submitted">Submitted</span></a>
          } @empty { <p style="color:var(--text-dim);text-align:center;padding:20px;">No pending applications</p> }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .action-card { display:flex;align-items:center;gap:12px;padding:12px;border-radius:10px;background:var(--bg-tertiary);border:1px solid var(--border);transition:all .2s;text-decoration:none;color:var(--text); }
    .action-card:hover { border-color:var(--accent);transform:translateX(3px); }
    .action-card strong { font-size:.85rem;color:#fff;display:block; }
    .action-card p { font-size:.75rem;color:var(--text-dim);margin-top:2px; }
    .ai { font-size:1.3rem;width:36px;height:36px;border-radius:9px;background:var(--bg-card);display:flex;align-items:center;justify-content:center; }
    .pq-row { display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid var(--border);text-decoration:none;color:var(--text);transition:all .15s; }
    .pq-row:last-child { border-bottom:none; }
    .pq-row:hover { padding-left:4px; }
    .pq-row strong { font-size:.85rem;color:#fff; }
    .pq-row p { font-size:.72rem;color:var(--text-dim);margin-top:2px; }
  `]
})
export class AdminDashboardComponent implements OnInit {
  apps = signal<LoanApplication[]>([]);
  pending = signal<LoanApplication[]>([]);
  constructor(private admin: AdminService, public auth: AuthService) {}
  ngOnInit(): void {
    this.admin.getAllApplications().subscribe({ next: a => { this.apps.set(a); this.pending.set(a.filter(x => x.status === 'Submitted')); }});
  }
  countStatus(s: string): number { return this.apps().filter(a => a.status === s).length; }
}
