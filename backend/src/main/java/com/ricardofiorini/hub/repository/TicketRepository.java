package com.ricardofiorini.hub.repository;

import com.ricardofiorini.hub.model.Channel;
import com.ricardofiorini.hub.model.Ticket;
import com.ricardofiorini.hub.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    Page<Ticket> findByChannel(Channel channel, Pageable pageable);

    Page<Ticket> findByStatusAndChannel(TicketStatus status, Channel channel, Pageable pageable);
}
