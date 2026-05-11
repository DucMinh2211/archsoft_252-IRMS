package com.irms.table.service;

import com.irms.table.domain.ReservationStatus;
import com.irms.table.domain.SeatingSource;
import com.irms.table.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReservationSeatingSourceHandler implements SeatingSourceHandler {

    private final ReservationRepository reservationRepository;

    @Override
    public SeatingSource source() {
        return SeatingSource.RESERVATION;
    }

    @Override
    public void handle(UUID sourceId) {
        if (sourceId == null) {
            return;
        }

        reservationRepository.findById(sourceId).ifPresent(reservation -> {
            reservation.setStatus(ReservationStatus.SEATED);
            reservationRepository.save(reservation);
        });
    }
}
