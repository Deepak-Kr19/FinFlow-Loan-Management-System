import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { LoanApplication } from '../../../core/models/application.model';

@Component({
  selector: 'app-all-applications',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="fade-in">
      <div class="page-header"><h1>All Applications</h1><p>Review and manage loan applications</p></div>
      <div class="card" style="margin-bottom:16px;padding:14px 18px;">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap;">
          <input type="text" class="form-control" [(ngModel)]="search" placeholder="🔍 Search..." style="max-width:280px;">
          <div style="display:flex;gap:6px;">
            @for (f of filters; track f.key) {
              <button class="btn btn-sm" [class.btn-primary]="filter===f.key" [class.btn-secondary]="filter!==f.key" (click)="filter=f.key">{{ f.label }}</button>
            }
          </div>
        </div>
      </div>
      <div class="table-container card" style="padding:0;">
        <table>
          <thead><tr><th>ID</th><th>User</th><th>Loan</th><th>Status</th><th>Action</th></tr></thead>
          <tbody>
            @for (a of filtered(); track a.id) {
              <tr>
                <td style="color:#fff;font-weight:600;">#{{ a.id }}</td>
                <td>User #{{ a.userId }}</td>
                <td>{{ parseSummary(a.loanDetails) }}</td>
                <td><span class="badge" [ngClass]="'badge-' + a.status.toLowerCase()">{{ a.status }}</span></td>
                <td><a [routerLink]="['/admin/applications', a.id]" class="btn btn-sm btn-primary">Review</a></td>
              </tr>
            } @empty {
              <tr><td colspan="5" style="text-align:center;padding:40px;color:var(--text-dim);">No applications found</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class AllApplicationsComponent implements OnInit {
  apps = signal<LoanApplication[]>([]);
  search = '';
  filter = '';
  filters = [{ key: '', label: 'All' }, { key: 'Submitted', label: 'Pending' }, { key: 'APPROVED', label: 'Approved' }, { key: 'REJECTED', label: 'Rejected' }];

  constructor(private admin: AdminService) {}
  ngOnInit(): void { this.admin.getAllApplications().subscribe({ next: a => this.apps.set(a) }); }

  filtered(): LoanApplication[] {
    return this.apps().filter(a => {
      const ms = !this.filter || a.status === this.filter;
      const mt = !this.search || a.id.toString().includes(this.search) || a.loanDetails?.toLowerCase().includes(this.search.toLowerCase());
      return ms && mt;
    });
  }

  parseSummary(d: string): string {
    try { const j = JSON.parse(d); return `${j.loanType} — ₹${Number(j.loanAmount).toLocaleString()}`; } catch { return d.slice(0, 40); }
  }
}
