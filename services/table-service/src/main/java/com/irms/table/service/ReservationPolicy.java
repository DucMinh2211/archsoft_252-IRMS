package com.irms.table.service;

import com.irms.table.domain.Reservation;
import com.irms.table.domain.ReservationStatus;
import com.irms.table.domain.RestaurantTable;
import com.irms.table.exception.TableBusinessException;
import com.irms.table.repository.ReservationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class ReservationPolicy {

    private final ReservationRepository reservationRepository;

    public ReservationPolicy(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void validateCanAssign(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new TableBusinessException("Chỉ có thể gán bàn cho đặt bàn ở trạng thái PENDING hoặc CONFIRMED");
        }
    }

    public void validateNoOverlap(Reservation reservation, RestaurantTable table, UUID reservationId) {
        LocalDateTime startTime = reservation.getReservationTime();
        LocalDateTime endTime = startTime.plusMinutes(reservation.getExpectedDurationMinutes());

        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(table.getId(), startTime, endTime);
        overlapping.removeIf(r -> r.getId().equals(reservationId));

        if (!overlapping.isEmpty()) {
            throw new TableBusinessException("Bàn " + table.getTableNumber() + " đã có người đặt trong khung giờ này");
        }
    }

    public void validateCanCancel(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.SEATED) {
            throw new TableBusinessException("Không thể hủy đặt bàn khi khách đã vào chỗ");
        }
    }

    public void validateCanMarkNoShow(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new TableBusinessException("Chỉ có thể đánh dấu No-Show với đặt bàn đã được xác nhận");
        }
    }
}
