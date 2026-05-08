package com.irms.kitchen.repository;

import com.irms.kitchen.domain.KitchenTicketItem;
import com.irms.kitchen.domain.StationType;
import com.irms.kitchen.domain.TicketItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KitchenTicketItemRepository extends JpaRepository<KitchenTicketItem, UUID> {
    List<KitchenTicketItem> findByStationAndStatusInOrderByCreatedAtAsc(StationType station, List<TicketItemStatus> statuses);
    List<KitchenTicketItem> findByTicketId(UUID ticketId);
    List<KitchenTicketItem> findByTicket_OrderIdAndMenuItemId(UUID orderId, UUID menuItemId);
}
