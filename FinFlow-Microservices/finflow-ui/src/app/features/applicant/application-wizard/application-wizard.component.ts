import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApplicationService } from '../../../core/services/application.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-application-wizard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="fade-in">
      <div class="page-header">
        <h1>{{ isEdit ? 'Edit Application' : 'New Loan Application' }}</h1>
        <p>{{ isEdit ? 'Update your draft application' : 'Complete all 3 steps to create your application' }}</p>
      </div>

      <!-- Step indicator -->
      <div class="wizard-steps">
        @for (s of stepLabels; track s; let i = $index) {
          <div class="ws" [class.active]="step() === i" [class.done]="step() > i" (click)="goToStep(i)">
            <div class="ws-dot">@if (step() > i) { ✓ } @else { {{ i + 1 }} }</div>
            <span>{{ s }}</span>
          </div>
          @if (i < stepLabels.length - 1) { <div class="ws-line" [class.filled]="step() > i"></div> }
        }
      </div>

      <div class="card wizard-card">
        <form [formGroup]="form">
          <!-- Step 1: Personal -->
          @if (step() === 0) {
            <h3>Personal Information</h3>
            <p class="step-desc">Provide your personal details for the loan application</p>
            <div class="form-row">
              <div class="form-group"><label>Full Name</label><input class="form-control" formControlName="fullName" placeholder="John Doe"></div>
              <div class="form-group"><label>Date of Birth</label><input class="form-control" type="date" formControlName="dob"></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>Phone Number</label><input class="form-control" formControlName="phone" placeholder="+91 98765 43210"></div>
              <div class="form-group"><label>PAN Number</label><input class="form-control" formControlName="pan" placeholder="ABCDE1234F"></div>
            </div>
            <div class="form-group"><label>Address</label><textarea class="form-control" formControlName="address" placeholder="Full address with city and pincode" rows="3"></textarea></div>
          }

          <!-- Step 2: Employment -->
          @if (step() === 1) {
            <h3>Employment Details</h3>
            <p class="step-desc">Tell us about your current employment</p>
            <div class="form-row">
              <div class="form-group"><label>Company Name</label><input class="form-control" formControlName="company" placeholder="Capgemini Technologies"></div>
              <div class="form-group"><label>Designation</label><input class="form-control" formControlName="designation" placeholder="Senior Analyst"></div>
            </div>
            <div class="form-row">
              <div class="form-group"><label>Monthly Salary (₹)</label><input class="form-control" type="number" formControlName="salary" placeholder="75000"></div>
              <div class="form-group"><label>Years of Experience</label><input class="form-control" type="number" formControlName="experience" placeholder="5"></div>
            </div>
            <div class="form-group"><label>Employment Type</label>
              <select class="form-control" formControlName="empType">
                <option value="Salaried">Salaried</option>
                <option value="Self-Employed">Self-Employed</option>
                <option value="Freelancer">Freelancer</option>
              </select>
            </div>
          }

          <!-- Step 3: Loan Details -->
          @if (step() === 2) {
            <h3>Loan Requirements</h3>
            <p class="step-desc">Specify your loan requirements</p>
            <div class="form-row">
              <div class="form-group"><label>Loan Amount (₹)</label><input class="form-control" type="number" formControlName="loanAmount" placeholder="5000000"></div>
              <div class="form-group"><label>Tenure (Months)</label><input class="form-control" type="number" formControlName="tenure" placeholder="240"></div>
            </div>
            <div class="form-group"><label>Loan Type</label>
              <select class="form-control" formControlName="loanType">
                <option value="Home Loan">Home Loan</option>
                <option value="Personal Loan">Personal Loan</option>
                <option value="Car Loan">Car Loan</option>
                <option value="Education Loan">Education Loan</option>
                <option value="Business Loan">Business Loan</option>
              </select>
            </div>
            <div class="form-group"><label>Purpose</label><textarea class="form-control" formControlName="purpose" placeholder="Describe the purpose of this loan" rows="3"></textarea></div>
          }
        </form>

        <div class="wizard-actions">
          <button class="btn btn-secondary" (click)="prevStep()" [disabled]="step() === 0">← Previous</button>
          @if (step() < 2) {
            <button class="btn btn-primary" (click)="nextStep()">Next Step →</button>
          } @else {
            <button class="btn btn-success" (click)="onSubmit()" [disabled]="submitting()">
              @if (submitting()) { <span class="spinner" style="width:16px;height:16px;border-width:2px;"></span> Saving... }
              @else { ✓ {{ isEdit ? 'Update' : 'Create' }} Application }
            </button>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .wizard-steps { display: flex; align-items: center; margin-bottom: 24px; }
    .ws {
      display: flex; align-items: center; gap: 8px; cursor: pointer;
      font-size: 0.82rem; color: var(--text-dim); font-weight: 500;
      transition: color 0.2s;
    }
    .ws-dot {
      width: 28px; height: 28px; border-radius: 50%;
      background: var(--bg-tertiary); border: 2px solid var(--border);
      display: flex; align-items: center; justify-content: center;
      font-size: 0.72rem; font-weight: 700; transition: all 0.3s;
    }
    .ws.active .ws-dot { background: var(--accent-glow); border-color: var(--accent); color: var(--accent-light); box-shadow: 0 0 10px var(--accent-glow); }
    .ws.active { color: var(--accent-light); }
    .ws.done .ws-dot { background: var(--green-bg); border-color: var(--green); color: var(--green); }
    .ws.done { color: var(--green); }
    .ws-line { flex: 1; height: 2px; background: var(--border); margin: 0 12px; margin-bottom: 0; transition: background 0.3s; }
    .ws-line.filled { background: var(--green); }
    .wizard-card { max-width: 720px; }
    .wizard-card h3 { font-size: 1.1rem; font-weight: 700; color: #fff; margin-bottom: 4px; }
    .step-desc { font-size: 0.84rem; color: var(--text-muted); margin-bottom: 24px; }
    .wizard-actions { display: flex; justify-content: space-between; margin-top: 28px; padding-top: 20px; border-top: 1px solid var(--border); }
  `]
})
export class ApplicationWizardComponent implements OnInit {
  form: FormGroup;
  step = signal(0);
  submitting = signal(false);
  isEdit = false;
  editId: number | null = null;
  stepLabels = ['Personal Details', 'Employment', 'Loan Details'];

  constructor(private fb: FormBuilder, private appService: ApplicationService, private toast: ToastService, private route: ActivatedRoute, private router: Router) {
    this.form = this.fb.group({
      fullName: ['', Validators.required], dob: ['', Validators.required],
      phone: ['', Validators.required], pan: [''], address: ['', Validators.required],
      company: ['', Validators.required], designation: ['', Validators.required],
      salary: ['', Validators.required], experience: ['', Validators.required], empType: ['Salaried'],
      loanAmount: ['', Validators.required], tenure: ['', Validators.required],
      loanType: ['Home Loan'], purpose: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.editId = +id;
      this.appService.getById(this.editId).subscribe({
        next: (app) => {
          try {
            const p = JSON.parse(app.personalDetails);
            const e = JSON.parse(app.employmentDetails);
            const l = JSON.parse(app.loanDetails);
            this.form.patchValue({ ...p, ...e, ...l });
          } catch { /* fields are plain text — use as-is */ }
        }
      });
    }
  }

  goToStep(i: number): void { if (i <= this.step()) this.step.set(i); }
  nextStep(): void { if (this.step() < 2) this.step.update(s => s + 1); }
  prevStep(): void { if (this.step() > 0) this.step.update(s => s - 1); }

  onSubmit(): void {
    const v = this.form.value;
    const req = {
      personalDetails: JSON.stringify({ fullName: v.fullName, dob: v.dob, phone: v.phone, pan: v.pan, address: v.address }),
      employmentDetails: JSON.stringify({ company: v.company, designation: v.designation, salary: v.salary, experience: v.experience, empType: v.empType }),
      loanDetails: JSON.stringify({ loanAmount: v.loanAmount, tenure: v.tenure, loanType: v.loanType, purpose: v.purpose })
    };
    this.submitting.set(true);
    const obs = this.isEdit ? this.appService.update(this.editId!, req) : this.appService.create(req);
    obs.subscribe({
      next: () => { this.toast.success(this.isEdit ? 'Application updated!' : 'Application created!'); this.router.navigate(['/applications']); },
      error: () => { this.submitting.set(false); this.toast.error('Failed to save application'); }
    });
  }
}
