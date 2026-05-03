import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Document } from '../models/document.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly API = '/documents';

  constructor(private http: HttpClient) {}

  upload(applicationId: number, type: string, file: File): Observable<Document> {
    const formData = new FormData();
    formData.append('applicationId', applicationId.toString());
    formData.append('type', type);
    formData.append('file', file);
    return this.http.post<Document>(`${this.API}/upload`, formData);
  }

  getByApplicationId(applicationId: number): Observable<Document[]> {
    return this.http.get<Document[]>(`${this.API}/application/${applicationId}`);
  }

  verify(documentId: number, status: string): Observable<Document> {
    const params = new HttpParams().set('status', status);
    return this.http.put<Document>(`${this.API}/${documentId}/verify`, {}, { params });
  }

  /** Download a document file — triggers browser download */
  download(documentId: number, fileName: string): void {
    this.http.get(`${this.API}/${documentId}/download`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        a.click();
        window.URL.revokeObjectURL(url);
      }
    });
  }
}

