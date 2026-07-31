package com.ricardofiorini.hub.service;

import com.ricardofiorini.hub.dto.TicketDtos.*;
import com.ricardofiorini.hub.model.Channel;
import com.ricardofiorini.hub.model.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TicketServiceIT {

    @Autowired
    private TicketService ticketService;

    @Test
    void deveCriarTicketComMensagemInicial() {
        CreateTicketRequest request = new CreateTicketRequest(
                "Problema no login", "Maria Souza", "maria@email.com",
                Channel.EMAIL, "Não consigo acessar minha conta");

        TicketResponse response = ticketService.createTicket(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(TicketStatus.ABERTO);
        assertThat(response.messages()).hasSize(1);
        assertThat(response.messages().get(0).content()).isEqualTo("Não consigo acessar minha conta");
    }

    @Test
    void deveListarTicketsFiltradosPorCanal() {
        ticketService.createTicket(new CreateTicketRequest(
                "Dúvida sobre plano", "João Lima", "joao@email.com",
                Channel.CHAT, "Qual o valor do plano premium?"));

        Pageable pageable = PageRequest.of(0, 10);
        var page = ticketService.listTickets(null, Channel.CHAT, pageable);

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).channel()).isEqualTo(Channel.CHAT);
    }

    @Test
    void deveAtualizarStatusDoTicket() {
        TicketResponse created = ticketService.createTicket(new CreateTicketRequest(
                "Erro no pagamento", "Ana Paula", "ana@email.com",
                Channel.FORMULARIO_WEB, "Meu cartão foi recusado"));

        TicketResponse updated = ticketService.updateTicket(created.id(),
                new UpdateTicketRequest(TicketStatus.EM_ATENDIMENTO, null, null));

        assertThat(updated.status()).isEqualTo(TicketStatus.EM_ATENDIMENTO);
    }
}
