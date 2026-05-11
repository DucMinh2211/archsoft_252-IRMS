package com.irms.table.service;

import com.irms.table.domain.Reservation;
import com.irms.table.domain.ReservationStatus;
import com.irms.table.dto.ReservationRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationManagementService {
    List<Reservation> getAllReservations();
    List<Reservation> getReservationsByStatus(ReservationStatus status);
    List<Reservation> getReservationsBetween(LocalDateTime from, LocalDateTime to);
    Reservation getReservationById(UUID id);
    Reservation createReservation(ReservationRequest request);
    Reservation confirmReservation(UUID reservationId, UUID tableId);
    Reservation cancelReservation(UUID id);
    Reservation markNoShow(UUID id);
}
