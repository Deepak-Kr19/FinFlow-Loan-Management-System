import { Component } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { ToastComponent } from './shared/components/toast/toast.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, LayoutComponent, ToastComponent],
  template: `
    <app-toast />
    @if (showLayout()) {
      <div class="app-layout">
        <app-layout />
        <main class="main-content fade-in">
          <router-outlet />
        </main>
      </div>
    } @else {
      <router-outlet />
    }
  `
})
export class AppComponent {
  constructor(private auth: AuthService, private router: Router) {}

  showLayout(): boolean {
    const url = this.router.url;
    return this.auth.hasToken() && !url.startsWith('/login') && !url.startsWith('/register');
  }
}
