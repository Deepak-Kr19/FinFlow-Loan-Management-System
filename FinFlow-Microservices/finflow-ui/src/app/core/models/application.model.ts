export interface LoanApplication {
  id: number;
  userId: number;
  personalDetails: string;
  employmentDetails: string;
  loanDetails: string;
  status: ApplicationStatus;
}

export interface ApplicationRequest {
  personalDetails: string;
  employmentDetails: string;
  loanDetails: string;
}

export type ApplicationStatus =
  | 'Draft'
  | 'Submitted'
  | 'APPROVED'
  | 'REJECTED';

export const STATUS_FLOW: ApplicationStatus[] = [
  'Draft',
  'Submitted',
  'APPROVED',
];
