import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fade-in">
      <div class="page-header"><h1>Reports</h1><p>System reports and analytics</p></div>
      @if (loading()) { <div class="loading-overlay"><div class="spinner"></div></div> }
      @else if (reports().length === 0) {
        <div class="empty-state card"><div class="empty-icon">📈</div><h3>No Reports Yet</h3><p>Reports will appear here once decisions are made</p></div>
      } @else {
        <div class="table-container card" style="padding:0;">
          <table>
            <thead><tr><th>ID</th><th>Type</th><th>Data</th></tr></thead>
            <tbody>
              @for (r of reports(); track r.id || $index) {
                <tr><td style="color:#fff;font-weight:600;">#{{ r.id }}</td><td>{{ r.type || 'Report' }}</td><td style="max-width:400px;overflow:hidden;text-overflow:ellipsis;">{{ r.data }}</td></tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `
})
export class ReportsComponent implements OnInit {
  reports = signal<any[]>([]);
  loading = signal(true);
  constructor(private admin: AdminService) {}
  ngOnInit(): void {
    this.admin.getReports().subscribe({
      next: r => { this.reports.set(r); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}
