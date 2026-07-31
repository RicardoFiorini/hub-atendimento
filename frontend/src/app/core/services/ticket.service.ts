import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse, TicketDetail, TicketStatus, Channel } from '../models/ticket.model';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private base = `${environment.apiUrl}/tickets`;

  constructor(private http: HttpClient) {}

  list(status?: TicketStatus, channel?: Channel, page = 0, size = 20): Observable<PageResponse<any>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    if (channel) params = params.set('channel', channel);
    return this.http.get<PageResponse<any>>(this.base, { params });
  }

  getById(id: number): Observable<TicketDetail> {
    return this.http.get<TicketDetail>(`${this.base}/${id}`);
  }

  update(id: number, payload: { status?: TicketStatus; priority?: string; assignedAgentId?: number }): Observable<TicketDetail> {
    return this.http.put<TicketDetail>(`${this.base}/${id}`, payload);
  }

  addMessage(id: number, content: string): Observable<TicketDetail> {
    return this.http.post<TicketDetail>(`${this.base}/${id}/messages`, { content });
  }
}
