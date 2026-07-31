export type Channel = 'EMAIL' | 'CHAT' | 'FORMULARIO_WEB' | 'TELEFONE';
export type TicketStatus = 'ABERTO' | 'EM_ATENDIMENTO' | 'AGUARDANDO_CLIENTE' | 'RESOLVIDO' | 'FECHADO';
export type Priority = 'BAIXA' | 'MEDIA' | 'ALTA' | 'URGENTE';

export interface TicketSummary {
  id: number;
  subject: string;
  customerName: string;
  channel: Channel;
  status: TicketStatus;
  priority: Priority;
  updatedAt: string;
}

export interface Message {
  id: number;
  author: string;
  senderType: 'CUSTOMER' | 'AGENT';
  content: string;
  createdAt: string;
}

export interface TicketDetail {
  id: number;
  subject: string;
  customerName: string;
  customerContact: string;
  channel: Channel;
  status: TicketStatus;
  priority: Priority;
  assignedAgentName: string | null;
  createdAt: string;
  updatedAt: string;
  messages: Message[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}
