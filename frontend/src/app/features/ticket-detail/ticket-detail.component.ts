import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TicketService } from '../../core/services/ticket.service';
import { TicketDetail, TicketStatus } from '../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ticket-detail.component.html'
})
export class TicketDetailComponent implements OnInit {
  ticket: TicketDetail | null = null;
  newMessage = '';
  statusOptions: TicketStatus[] = ['ABERTO', 'EM_ATENDIMENTO', 'AGUARDANDO_CLIENTE', 'RESOLVIDO', 'FECHADO'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ticketService: TicketService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
  }

  load(id: number): void {
    this.ticketService.getById(id).subscribe(t => this.ticket = t);
  }

  changeStatus(status: TicketStatus): void {
    if (!this.ticket) return;
    this.ticketService.update(this.ticket.id, { status }).subscribe(t => this.ticket = t);
  }

  sendMessage(): void {
    if (!this.ticket || !this.newMessage.trim()) return;
    this.ticketService.addMessage(this.ticket.id, this.newMessage).subscribe(t => {
      this.ticket = t;
      this.newMessage = '';
    });
  }

  back(): void {
    this.router.navigate(['/dashboard']);
  }
}
