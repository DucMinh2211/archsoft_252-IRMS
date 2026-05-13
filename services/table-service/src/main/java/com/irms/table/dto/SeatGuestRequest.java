package com.irms.table.dto;

import com.irms.table.domain.SeatingSource;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SeatGuestRequest {

    @NotNull(message = "ID bàn không được để trống")
    private UUID tableId;

    // Nguồn khách: RESERVATION, WAITLIST hoặc WALK_IN
    private SeatingSource source;

    // ID của Reservation hoặc WaitlistEntry tương ứng (nullable nếu source = WALK_IN)
    private UUID sourceId;
}
