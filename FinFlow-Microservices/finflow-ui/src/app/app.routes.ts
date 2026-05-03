import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard, applicantGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },

  // Applicant routes
  { path: 'dashboard', loadComponent: () => import('./features/applicant/dashboard/dashboard.component').then(m => m.DashboardComponent), canActivate: [authGuard, applicantGuard] },
  { path: 'applications', loadComponent: () => import('./features/applicant/my-applications/my-applications.component').then(m => m.MyApplicationsComponent), canActivate: [authGuard, applicantGuard] },
  { path: 'applications/new', loadComponent: () => import('./features/applicant/application-wizard/application-wizard.component').then(m => m.ApplicationWizardComponent), canActivate: [authGuard, applicantGuard] },
  { path: 'applications/edit/:id', loadComponent: () => import('./features/applicant/application-wizard/application-wizard.component').then(m => m.ApplicationWizardComponent), canActivate: [authGuard, applicantGuard] },
  { path: 'applications/:id', loadComponent: () => import('./features/applicant/application-detail/application-detail.component').then(m => m.ApplicationDetailComponent), canActivate: [authGuard, applicantGuard] },

  // Profile
  { path: 'profile', loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent), canActivate: [authGuard] },

  // Admin routes
  { path: 'admin', loadComponent: () => import('./features/admin/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent), canActivate: [authGuard, adminGuard] },
  { path: 'admin/applications', loadComponent: () => import('./features/admin/all-applications/all-applications.component').then(m => m.AllApplicationsComponent), canActivate: [authGuard, adminGuard] },
  { path: 'admin/applications/:id', loadComponent: () => import('./features/admin/application-review/application-review.component').then(m => m.ApplicationReviewComponent), canActivate: [authGuard, adminGuard] },
  { path: 'admin/users', loadComponent: () => import('./features/admin/user-management/user-management.component').then(m => m.UserManagementComponent), canActivate: [authGuard, adminGuard] },
  { path: 'admin/reports', loadComponent: () => import('./features/admin/reports/reports.component').then(m => m.ReportsComponent), canActivate: [authGuard, adminGuard] },

  { path: '**', redirectTo: '/login' }
];
