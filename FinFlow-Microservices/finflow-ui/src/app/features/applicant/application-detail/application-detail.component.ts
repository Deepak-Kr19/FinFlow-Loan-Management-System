import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApplicationService } from '../../../core/services/application.service';
import { DocumentService } from '../../../core/services/document.service';
import { ToastService } from '../../../core/services/toast.service';
import { StatusTimelineComponent } from '../../../shared/components/status-timeline/status-timeline.component';
import { LoanApplication } from '../../../core/models/application.model';
import { Document, DOC_TYPES } from '../../../core/models/document.model';

@Component({
  selector: 'app-application-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, StatusTimelineComponent],
  template: `
    <div class="fade-in">
      <div class="page-header"><h1>Application #{{ appId }}</h1><p>View details, timeline, and manage documents</p></div>
      @if (loading()) { <div class="loading-overlay"><div class="spinner"></div></div> }
      @else if (app()) {
        <app-status-timeline [currentStatus]="app()!.status" />
        <div class="grid-2">
          <div>
            <div class="card" style="margin-bottom:16px;"><div class="card-header"><h2>Personal Details</h2></div><pre class="detail-pre">{{ formatJson(app()!.personalDetails) }}</pre></div>
            <div class="card" style="margin-bottom:16px;"><div class="card-header"><h2>Employment Details</h2></div><pre class="detail-pre">{{ formatJson(app()!.employmentDetails) }}</pre></div>
            <div class="card"><div class="card-header"><h2>Loan Details</h2></div><pre class="detail-pre">{{ formatJson(app()!.loanDetails) }}</pre></div>
          </div>
          <div>
            <div class="card">
              <div class="card-header"><h2>📎 Documents</h2></div>
              <form [formGroup]="uploadForm" class="upload-section">
                <div class="form-row">
                  <div class="form-group"><label>Type</label>
                    <select class="form-control" formControlName="docType">
                      @for (t of docTypes; track t.value) { <option [value]="t.value">{{ t.label }}</option> }
                    </select>
                  </div>
                  <div class="form-group"><label>File</label><input type="file" class="form-control" (change)="onFile($event)"></div>
                </div>
                <button class="btn btn-primary btn-sm" (click)="uploadDoc()" [disabled]="!selectedFile || uploading()">{{ uploading() ? 'Uploading...' : '📤 Upload' }}</button>
              </form>
              @if (docs().length > 0) {
                <div class="doc-list">
                  @for (d of docs(); track d.id) {
                    <div class="doc-row">
                      <div><strong>{{ d.type }}</strong><p>{{ getFileName(d.filePath) }}</p></div>
                      <div style="display:flex;align-items:center;gap:8px;">
                        <button class="btn btn-sm btn-secondary" (click)="downloadDoc(d)">📥 View</button>
                        <span class="badge" [ngClass]="'badge-' + d.status.toLowerCase()">{{ d.status }}</span>
                      </div>
                    </div>
                  }
                </div>
              }
            </div>
            <button class="btn btn-secondary" style="margin-top:16px;" (click)="goBack()">← Back</button>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .detail-pre { color: var(--text-muted); font-size: 0.85rem; line-height: 1.7; white-space: pre-wrap; font-family: 'Inter', sans-serif; }
    .upload-section { padding-bottom: 16px; border-bottom: 1px solid var(--border); margin-bottom: 16px; }
    .doc-list { display: flex; flex-direction: column; gap: 0; }
    .doc-row { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--border); }
    .doc-row:last-child { border-bottom: none; }
    .doc-row strong { font-size: 0.82rem; color: #fff; }
    .doc-row p { font-size: 0.72rem; color: var(--text-dim); margin-top: 2px; }
  `]
})
export class ApplicationDetailComponent implements OnInit {
  appId = 0;
  app = signal<LoanApplication | null>(null);
  docs = signal<Document[]>([]);
  loading = signal(true);
  uploading = signal(false);
  selectedFile: File | null = null;
  docTypes = DOC_TYPES;
  uploadForm: FormGroup;

  constructor(private appSvc: ApplicationService, private docSvc: DocumentService, private toast: ToastService, private route: ActivatedRoute, private router: Router, fb: FormBuilder) {
    this.uploadForm = fb.group({ docType: ['ID_PROOF', Validators.required] });
  }

  ngOnInit(): void {
    this.appId = +this.route.snapshot.paramMap.get('id')!;
    this.appSvc.getById(this.appId).subscribe({
      next: a => { this.app.set(a); this.loading.set(false); },
      error: () => { this.toast.error('Not found'); this.router.navigate(['/applications']); }
    });
    this.docSvc.getByApplicationId(this.appId).subscribe({ next: d => this.docs.set(d) });
  }

  onFile(e: Event): void { const i = e.target as HTMLInputElement; if (i.files?.length) this.selectedFile = i.files[0]; }

  uploadDoc(): void {
    if (!this.selectedFile) return;
    this.uploading.set(true);
    this.docSvc.upload(this.appId, this.uploadForm.value.docType, this.selectedFile).subscribe({
      next: d => { this.docs.update(ds => [...ds, d]); this.uploading.set(false); this.selectedFile = null; this.toast.success('Uploaded!'); },
      error: () => { this.uploading.set(false); this.toast.error('Upload failed'); }
    });
  }

  formatJson(s: string): string {
    try { const o = JSON.parse(s); return Object.entries(o).map(([k, v]) => `${k}: ${v}`).join('\n'); }
    catch { return s; }
  }

  getFileName(path: string): string {
    const name = path.split('/').pop() || path.split('\\').pop() || path;
    return name.includes('_') ? name.substring(name.indexOf('_') + 1) : name;
  }

  downloadDoc(d: any): void { this.docSvc.download(d.id, this.getFileName(d.filePath)); }

  goBack(): void { this.router.navigate(['/applications']); }
}
