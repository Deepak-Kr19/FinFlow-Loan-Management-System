import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Decision } from '../models/decision.model';
import { LoanApplication } from '../models/application.model';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly API = '/admin';

  constructor(private http: HttpClient) {}

  getAllApplications(): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(`${this.API}/applications`);
  }

  makeDecision(applicationId: number, decision: string, remarks: string): Observable<Decision> {
    const params = new HttpParams().set('decision', decision).set('remarks', remarks);
    return this.http.post<Decision>(`${this.API}/applications/${applicationId}/decision`, {}, { params });
  }

  getReports(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API}/reports`);
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.API}/users`);
  }

  updateUser(id: number, user: any): Observable<any> {
    return this.http.put(`${this.API}/users/${id}`, user);
  }
}
