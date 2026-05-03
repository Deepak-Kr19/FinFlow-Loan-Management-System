export interface Document {
  id: number;
  applicationId: number;
  type: string;
  filePath: string;
  status: 'PENDING' | 'VERIFIED' | 'REJECTED';
}

export const DOC_TYPES = [
  { value: 'ID_PROOF', label: 'ID Proof' },
  { value: 'ADDRESS_PROOF', label: 'Address Proof' },
  { value: 'SALARY_SLIP', label: 'Salary Slip' },
  { value: 'BANK_STATEMENT', label: 'Bank Statement' },
  { value: 'PAN_CARD', label: 'PAN Card' },
];
