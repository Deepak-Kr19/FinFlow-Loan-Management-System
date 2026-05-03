import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoanApplication, ApplicationRequest } from '../models/application.model';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private readonly API = '/applications';

  constructor(private http: HttpClient) {}

  create(req: ApplicationRequest): Observable<LoanApplication> {
    return this.http.post<LoanApplication>(this.API, req);
  }

  getMyApplications(): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(`${this.API}/my`);
  }

  getById(id: number): Observable<LoanApplication> {
    return this.http.get<LoanApplication>(`${this.API}/${id}`);
  }

  update(id: number, req: ApplicationRequest): Observable<LoanApplication> {
    return this.http.put<LoanApplication>(`${this.API}/${id}`, req);
  }

  submit(id: number): Observable<string> {
    return this.http.post(`${this.API}/${id}/submit`, {}, { responseType: 'text' });
  }

  getStatus(id: number): Observable<string> {
    return this.http.get(`${this.API}/${id}/status`, { responseType: 'text' });
  }
}
