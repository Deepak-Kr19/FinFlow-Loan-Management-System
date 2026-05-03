import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApplicationService } from '../../../core/services/application.service';
import { ToastService } from '../../../core/services/toast.service';
import { LoanApplication } from '../../../core/models/application.model';

@Component({
  selector: 'app-my-applications',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="fade-in">
      <div class="page-header"><h1>My Applications</h1><p>Track all your loan applications</p></div>
      @if (loading()) {
        <div class="loading-overlay"><div class="spinner"></div></div>
      } @else if (apps().length === 0) {
        <div class="empty-state card"><div class="empty-icon">📋</div><h3>No Applications</h3><p>Create your first loan application</p><a routerLink="/applications/new" class="btn btn-primary">➕ New Application</a></div>
      } @else {
        <div class="table-container card" style="padding:0;">
          <table>
            <thead><tr><th>ID</th><th>Loan</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>
              @for (app of apps(); track app.id) {
                <tr>
                  <td style="color:#fff;font-weight:600;">#{{ app.id }}</td>
                  <td>{{ parseSummary(app.loanDetails) }}</td>
                  <td><span class="badge" [ngClass]="'badge-' + app.status.toLowerCase()">{{ app.status }}</span></td>
                  <td><div style="display:flex;gap:6px;">
                    <a [routerLink]="['/applications', app.id]" class="btn btn-sm btn-secondary">View</a>
                    @if (app.status === 'Draft') {
                      <a [routerLink]="['/applications/edit', app.id]" class="btn btn-sm btn-outline">Edit</a>
                      <button class="btn btn-sm btn-primary" (click)="submitApp(app.id)">Submit</button>
                    }
                  </div></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `
})
export class MyApplicationsComponent implements OnInit {
  apps = signal<LoanApplication[]>([]);
  loading = signal(true);
  constructor(private svc: ApplicationService, private toast: ToastService) {}
  ngOnInit(): void { this.load(); }
  load(): void {
    this.svc.getMyApplications().subscribe({
      next: a => { this.apps.set(a); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load'); }
    });
  }
  submitApp(id: number): void {
    this.svc.submit(id).subscribe({
      next: () => { this.toast.success('Submitted!'); this.load(); },
      error: () => this.toast.error('Submit failed')
    });
  }
  parseSummary(d: string): string {
    try { const j = JSON.parse(d); return `${j.loanType} — ₹${Number(j.loanAmount).toLocaleString()}`; }
    catch { return d.slice(0, 50); }
  }
}
