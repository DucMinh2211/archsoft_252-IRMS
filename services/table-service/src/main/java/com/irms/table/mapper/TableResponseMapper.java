package com.irms.table.mapper;

import com.irms.table.domain.Reservation;
import com.irms.table.domain.RestaurantTable;
import com.irms.table.domain.WaitlistEntry;
import com.irms.table.dto.ReservationResponse;
import com.irms.table.dto.TableResponse;
import com.irms.table.dto.WaitlistEntryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TableResponseMapper {

    public TableResponse toTableResponse(RestaurantTable table) {
        if (table == null) {
            return null;
        }

        return new TableResponse(
                table.getId(),
                table.getTableNumber(),
                table.getCapacity(),
                table.getStatus(),
                table.getLocation(),
                table.getCurrentOrderId(),
                table.getSeatedAt(),
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }

    public List<TableResponse> toTableResponses(List<RestaurantTable> tables) {
        return tables.stream().map(this::toTableResponse).toList();
    }

    public ReservationResponse toReservationResponse(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        return new ReservationResponse(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getCustomerPhone(),
                reservation.getPartySize(),
                reservation.getReservationTime(),
                reservation.getStatus(),
                reservation.getNotes(),
                toTableResponse(reservation.getTable()),
                reservation.getExpectedDurationMinutes(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }

    public List<ReservationResponse> toReservationResponses(List<Reservation> reservations) {
        return reservations.stream().map(this::toReservationResponse).toList();
    }

    public WaitlistEntryResponse toWaitlistEntryResponse(WaitlistEntry entry) {
        if (entry == null) {
            return null;
        }

        return new WaitlistEntryResponse(
                entry.getId(),
                entry.getCustomerName(),
                entry.getCustomerPhone(),
                entry.getPartySize(),
                entry.getStatus(),
                entry.getEstimatedWaitMinutes(),
                entry.getNotifiedAt(),
                entry.getQueuePosition(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }

    public List<WaitlistEntryResponse> toWaitlistEntryResponses(List<WaitlistEntry> entries) {
        return entries.stream().map(this::toWaitlistEntryResponse).toList();
    }
}
