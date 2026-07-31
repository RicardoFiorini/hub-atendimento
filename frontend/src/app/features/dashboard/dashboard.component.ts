import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';
import { TicketSummary, TicketStatus, Channel } from '../../core/models/ticket.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  tickets: TicketSummary[] = [];
  statusFilter: TicketStatus | '' = '';
  channelFilter: Channel | '' = '';
  loading = false;

  statusOptions: TicketStatus[] = ['ABERTO', 'EM_ATENDIMENTO', 'AGUARDANDO_CLIENTE', 'RESOLVIDO', 'FECHADO'];
  channelOptions: Channel[] = ['EMAIL', 'CHAT', 'FORMULARIO_WEB', 'TELEFONE'];

  constructor(
    private ticketService: TicketService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  get agentName(): string | null {
    return this.auth.getAgentName();
  }

  load(): void {
    this.loading = true;
    this.ticketService.list(
      this.statusFilter || undefined,
      this.channelFilter || undefined
    ).subscribe({
      next: (page) => {
        this.tickets = page.content;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  openTicket(id: number): void {
    this.router.navigate(['/tickets', id]);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
