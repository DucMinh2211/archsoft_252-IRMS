package com.irms.table.service;

import com.irms.table.domain.Reservation;
import com.irms.table.domain.ReservationStatus;
import com.irms.table.domain.RestaurantTable;
import com.irms.table.exception.TableBusinessException;
import com.irms.table.repository.ReservationRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationPolicyTest {

    @Test
    void validateCanAssign_ShouldAllowPendingOrConfirmed() {
        ReservationPolicy policy = new ReservationPolicy(mock(ReservationRepository.class));

        assertDoesNotThrow(() -> policy.validateCanAssign(reservation(ReservationStatus.PENDING)));
        assertDoesNotThrow(() -> policy.validateCanAssign(reservation(ReservationStatus.CONFIRMED)));
        assertThrows(TableBusinessException.class, () -> policy.validateCanAssign(reservation(ReservationStatus.SEATED)));
    }

    @Test
    void validateNoOverlap_ShouldIgnoreCurrentReservationAndRejectOthers() {
        UUID reservationId = UUID.randomUUID();
        UUID otherReservationId = UUID.randomUUID();
        RestaurantTable table = new RestaurantTable();
        table.setId(UUID.randomUUID());
        table.setTableNumber("T1");

        Reservation current = reservation(ReservationStatus.PENDING);
        current.setId(reservationId);
        current.setReservationTime(LocalDateTime.now());

        Reservation same = reservation(ReservationStatus.CONFIRMED);
        same.setId(reservationId);
        Reservation other = reservation(ReservationStatus.CONFIRMED);
        other.setId(otherReservationId);

        ReservationRepository repository = mock(ReservationRepository.class);
        when(repository.findOverlappingReservations(table.getId(), current.getReservationTime(), current.getReservationTime().plusMinutes(current.getExpectedDurationMinutes())))
                .thenReturn(new java.util.ArrayList<>(List.of(same)));

        ReservationPolicy policy = new ReservationPolicy(repository);
        assertDoesNotThrow(() -> policy.validateNoOverlap(current, table, reservationId));

        when(repository.findOverlappingReservations(table.getId(), current.getReservationTime(), current.getReservationTime().plusMinutes(current.getExpectedDurationMinutes())))
                .thenReturn(new java.util.ArrayList<>(List.of(other)));
        assertThrows(TableBusinessException.class, () -> policy.validateNoOverlap(current, table, reservationId));
    }

    private Reservation reservation(ReservationStatus status) {
        Reservation reservation = new Reservation();
        reservation.setStatus(status);
        reservation.setExpectedDurationMinutes(120);
        return reservation;
    }
}
