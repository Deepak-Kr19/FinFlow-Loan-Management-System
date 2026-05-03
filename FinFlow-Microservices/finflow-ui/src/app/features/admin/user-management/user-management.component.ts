import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../core/services/admin.service';
import { ToastService } from '../../../core/services/toast.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fade-in">
      <div class="page-header"><h1>User Management</h1><p>View all registered users</p></div>
      <div class="stat-grid">
        <div class="stat-card"><div class="stat-icon">👥</div><div class="stat-value">{{ users().length }}</div><div class="stat-label">Total</div></div>
        <div class="stat-card"><div class="stat-icon">👤</div><div class="stat-value">{{ countRole('ROLE_APPLICANT') }}</div><div class="stat-label">Applicants</div></div>
        <div class="stat-card"><div class="stat-icon">🛡</div><div class="stat-value">{{ countRole('ROLE_ADMIN') }}</div><div class="stat-label">Admins</div></div>
      </div>
      @if (loading()) { <div class="loading-overlay"><div class="spinner"></div></div> }
      @else {
        <div class="table-container card" style="padding:0;">
          <table>
            <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th></tr></thead>
            <tbody>
              @for (u of users(); track u.id) {
                <tr>
                  <td style="color:#fff;font-weight:600;">#{{ u.id }}</td>
                  <td>{{ u.name }}</td>
                  <td>{{ u.email }}</td>
                  <td><span class="badge" [class.badge-approved]="u.role==='ROLE_ADMIN'" [class.badge-draft]="u.role!=='ROLE_ADMIN'">{{ u.role === 'ROLE_ADMIN' ? 'Admin' : 'Applicant' }}</span></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `
})
export class UserManagementComponent implements OnInit {
  users = signal<User[]>([]);
  loading = signal(true);
  constructor(private admin: AdminService, private toast: ToastService) {}
  ngOnInit(): void {
    this.admin.getAllUsers().subscribe({
      next: u => { this.users.set(u); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load users'); }
    });
  }
  countRole(r: string): number { return this.users().filter(u => u.role === r).length; }
}
