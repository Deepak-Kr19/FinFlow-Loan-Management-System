import { Component } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  template: `
    <div class="toast-stack">
      @for (t of toastService.toasts(); track t.id) {
        <div class="toast toast-{{ t.type }}" (click)="toastService.dismiss(t.id)">
          <span class="t-icon">
            @switch (t.type) {
              @case ('success') { ✅ }
              @case ('error') { ❌ }
              @case ('warning') { ⚠️ }
              @default { ℹ️ }
            }
          </span>
          {{ t.message }}
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-stack { position: fixed; top: 16px; right: 16px; z-index: 9999; display: flex; flex-direction: column; gap: 8px; }
    .toast {
      display: flex; align-items: center; gap: 10px;
      padding: 12px 20px; border-radius: 12px;
      font-size: 0.85rem; font-weight: 500; cursor: pointer;
      backdrop-filter: blur(20px); min-width: 280px;
      animation: slideIn 0.3s ease;
      border: 1px solid var(--border);
    }
    .toast-success { background: rgba(34,197,94,0.12); color: var(--green); border-color: rgba(34,197,94,0.25); }
    .toast-error { background: rgba(239,68,68,0.12); color: var(--red); border-color: rgba(239,68,68,0.25); }
    .toast-warning { background: rgba(245,158,11,0.12); color: var(--orange); border-color: rgba(245,158,11,0.25); }
    .toast-info { background: rgba(99,102,241,0.12); color: var(--accent-light); border-color: rgba(99,102,241,0.25); }
  `]
})
export class ToastComponent {
  constructor(public toastService: ToastService) {}
}
