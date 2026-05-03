import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { DocumentService } from '../../../core/services/document.service';
import { ToastService } from '../../../core/services/toast.service';
import { StatusTimelineComponent } from '../../../shared/components/status-timeline/status-timeline.component';
import { LoanApplication } from '../../../core/models/application.model';
import { Document } from '../../../core/models/document.model';

@Component({
  selector: 'app-application-review',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, StatusTimelineComponent],
  template: `
    <div class="fade-in">
      <div class="page-header"><h1>Review Application #{{ appId }}</h1><p>Verify documents and make a decision</p></div>
      @if (loading()) { <div class="loading-overlay"><div class="spinner"></div></div> }
      @else if (app()) {
        <app-status-timeline [currentStatus]="app()!.status" />
        <div class="grid-2">
          <div>
            <div class="card" style="margin-bottom:16px;"><div class="card-header"><h2>Personal</h2></div><pre class="dp">{{ fmt(app()!.personalDetails) }}</pre></div>
            <div class="card" style="margin-bottom:16px;"><div class="card-header"><h2>Employment</h2></div><pre class="dp">{{ fmt(app()!.employmentDetails) }}</pre></div>
            <div class="card" style="margin-bottom:16px;"><div class="card-header"><h2>Loan</h2></div><pre class="dp">{{ fmt(app()!.loanDetails) }}</pre></div>
            <div class="card">
              <div class="card-header"><h2>📎 Documents</h2></div>
              @for (d of docs(); track d.id) {
                <div class="doc-row">
                  <div><strong>{{ d.type }}</strong><p>{{ getFileName(d.filePath) }}</p></div>
                  <div style="display:flex;align-items:center;gap:8px;">
                    <button class="btn btn-sm btn-secondary" (click)="downloadDoc(d)">📥 View</button>
                    <span class="badge" [ngClass]="'badge-' + d.status.toLowerCase()">{{ d.status }}</span>
                    @if (d.status === 'PENDING') {
                      <button class="btn btn-sm btn-success" (click)="verifyDoc(d.id, 'VERIFIED')">✓</button>
                      <button class="btn btn-sm btn-danger" (click)="verifyDoc(d.id, 'REJECTED')">✕</button>
                    }
                  </div>
                </div>
              } @empty { <p style="color:var(--text-dim);font-size:.85rem;">No documents uploaded</p> }
            </div>
          </div>
          <div>
            <div class="card dec-card">
              <div class="card-header"><h2>🛡 Decision</h2></div>
              <form [formGroup]="decisionForm">
                <div class="form-group"><label>Decision</label>
                  <div class="dec-btns">
                    <button type="button" class="dec-btn ap" [class.sel]="decisionForm.get('decision')?.value==='APPROVED'" (click)="decisionForm.patchValue({decision:'APPROVED'})">✅ Approve</button>
                    <button type="button" class="dec-btn rj" [class.sel]="decisionForm.get('decision')?.value==='REJECTED'" (click)="decisionForm.patchValue({decision:'REJECTED'})">❌ Reject</button>
                  </div>
                </div>
                <div class="form-group"><label>Remarks</label><textarea class="form-control" formControlName="remarks" placeholder="Provide your reasoning..." rows="4"></textarea></div>
                <button class="btn btn-primary btn-block" (click)="submitDecision()" [disabled]="decisionForm.invalid || submitting()">
                  @if (submitting()) { Submitting... } @else { Submit Decision }
                </button>
              </form>
            </div>
            <button class="btn btn-secondary" style="margin-top:16px;" (click)="goBack()">← Back</button>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .dp { color:var(--text-muted);font-size:.85rem;line-height:1.7;white-space:pre-wrap;font-family:'Inter',sans-serif; }
    .doc-row { display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid var(--border); }
    .doc-row:last-child { border-bottom:none; }
    .doc-row strong { font-size:.82rem;color:#fff; }
    .doc-row p { font-size:.72rem;color:var(--text-dim);margin-top:2px; }
    .dec-card { position:sticky;top:32px; }
    .dec-btns { display:flex;gap:10px; }
    .dec-btn {
      flex:1;padding:14px;border-radius:10px;border:1px solid var(--border);
      background:var(--bg-tertiary);color:var(--text-muted);font-family:'Inter',sans-serif;
      font-size:.88rem;font-weight:600;cursor:pointer;transition:all .2s;
    }
    .dec-btn.ap.sel { background:var(--green-bg);border-color:var(--green);color:var(--green); }
    .dec-btn.rj.sel { background:var(--red-bg);border-color:var(--red);color:var(--red); }
    .dec-btn:hover { background:var(--bg-card-hover); }
  `]
})
export class ApplicationReviewComponent implements OnInit {
  appId = 0;
  app = signal<LoanApplication | null>(null);
  docs = signal<Document[]>([]);
  loading = signal(true);
  submitting = signal(false);
  decisionForm: FormGroup;

  constructor(private admin: AdminService, private docSvc: DocumentService, private toast: ToastService, private route: ActivatedRoute, private router: Router, fb: FormBuilder) {
    this.decisionForm = fb.group({ decision: ['', Validators.required], remarks: ['', Validators.required] });
  }

  ngOnInit(): void {
    this.appId = +this.route.snapshot.paramMap.get('id')!;
    this.admin.getAllApplications().subscribe({
      next: apps => { const a = apps.find((x: any) => x.id === this.appId); if (a) { this.app.set(a); } this.loading.set(false); }
    });
    this.docSvc.getByApplicationId(this.appId).subscribe({ next: d => this.docs.set(d) });
  }

  verifyDoc(id: number, status: string): void {
    this.docSvc.verify(id, status).subscribe({
      next: updated => { this.docs.update(ds => ds.map(d => d.id === id ? updated : d)); this.toast.success(`Document ${status.toLowerCase()}`); },
      error: () => this.toast.error('Verification failed')
    });
  }

  submitDecision(): void {
    if (this.decisionForm.invalid) return;
    this.submitting.set(true);
    const { decision, remarks } = this.decisionForm.value;
    this.admin.makeDecision(this.appId, decision, remarks).subscribe({
      next: () => { this.toast.success(`Application ${decision.toLowerCase()}!`); this.router.navigate(['/admin/applications']); },
      error: () => { this.submitting.set(false); this.toast.error('Decision failed'); }
    });
  }

  fmt(s: string): string {
    try { const o = JSON.parse(s); return Object.entries(o).map(([k, v]) => `${k}: ${v}`).join('\n'); } catch { return s; }
  }

  getFileName(path: string): string {
    const name = path.split('/').pop() || path.split('\\').pop() || path;
    return name.includes('_') ? name.substring(name.indexOf('_') + 1) : name;
  }

  downloadDoc(d: any): void { this.docSvc.download(d.id, this.getFileName(d.filePath)); }

  goBack(): void { this.router.navigate(['/admin/applications']); }
}
