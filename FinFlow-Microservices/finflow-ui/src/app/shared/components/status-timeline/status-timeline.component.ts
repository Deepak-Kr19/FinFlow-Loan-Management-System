import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApplicationStatus } from '../../../core/models/application.model';

/**
 * Visual status timeline showing the loan application lifecycle.
 * Highlights the current step and marks completed steps.
 */
@Component({
  selector: 'app-status-timeline',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="timeline">
      @for (step of steps; track step.key; let i = $index) {
        <div class="step" [class.active]="step.key === currentStatus" [class.completed]="isCompleted(i)" [class.rejected]="currentStatus === 'REJECTED' && step.key === 'REJECTED'">
          <div class="step-dot">
            @if (isCompleted(i)) { ✓ }
            @else if (step.key === currentStatus) { ● }
            @else if (currentStatus === 'REJECTED' && step.key === 'REJECTED') { ✕ }
          </div>
          <div class="step-label">{{ step.label }}</div>
        </div>
        @if (i < steps.length - 1) {
          <div class="step-line" [class.filled]="isCompleted(i)"></div>
        }
      }
    </div>
  `,
  styles: [`
    .timeline { display: flex; align-items: center; padding: 20px 0; gap: 0; }
    .step {
      display: flex; flex-direction: column; align-items: center; gap: 8px; min-width: 80px;
    }
    .step-dot {
      width: 32px; height: 32px; border-radius: 50%;
      background: var(--bg-tertiary); border: 2px solid var(--border);
      display: flex; align-items: center; justify-content: center;
      font-size: 0.7rem; color: var(--text-dim); font-weight: 700;
      transition: all 0.3s;
    }
    .step-label { font-size: 0.72rem; color: var(--text-dim); text-align: center; font-weight: 500; }
    .step-line {
      flex: 1; height: 2px; background: var(--border); min-width: 30px;
      margin-bottom: 24px; transition: background 0.3s;
    }
    .step-line.filled { background: var(--green); }
    .step.completed .step-dot { background: var(--green-bg); border-color: var(--green); color: var(--green); }
    .step.completed .step-label { color: var(--green); }
    .step.active .step-dot { background: var(--accent-glow); border-color: var(--accent); color: var(--accent-light); box-shadow: 0 0 12px var(--accent-glow); }
    .step.active .step-label { color: var(--accent-light); font-weight: 600; }
    .step.rejected .step-dot { background: var(--red-bg); border-color: var(--red); color: var(--red); }
    .step.rejected .step-label { color: var(--red); font-weight: 600; }
  `]
})
export class StatusTimelineComponent {
  @Input() currentStatus: ApplicationStatus = 'Draft';

  steps = [
    { key: 'Draft', label: 'Draft' },
    { key: 'Submitted', label: 'Submitted' },
    { key: 'APPROVED', label: 'Approved' },
    { key: 'REJECTED', label: 'Rejected' },
  ];

  private statusOrder = ['Draft', 'Submitted', 'APPROVED'];

  isCompleted(index: number): boolean {
    if (this.currentStatus === 'REJECTED') {
      return index <= 1; // Draft and Submitted are completed when rejected
    }
    const currentIndex = this.statusOrder.indexOf(this.currentStatus);
    return index < currentIndex;
  }
}
