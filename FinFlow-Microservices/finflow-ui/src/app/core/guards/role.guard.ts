import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.hasToken() && auth.isAdmin()) return true;
  router.navigate(['/dashboard']);
  return false;
};

export const applicantGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.hasToken() && auth.isApplicant()) return true;
  router.navigate(['/admin']);
  return false;
};
