package com.irms.table.service;

import com.irms.table.dto.SeatGuestRequest;
import com.irms.table.exception.TableBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SeatingSourceUpdateService {

    private final List<SeatingSourceHandler> seatingSourceHandlers;

    public void update(SeatGuestRequest request) {
        if (request.getSource() == null) {
            return;
        }

        SeatingSourceHandler handler = seatingSourceHandlers.stream()
                .filter(candidate -> candidate.supports(request.getSource()))
                .findFirst()
                .orElseThrow(() -> new TableBusinessException("Nguồn xếp bàn không được hỗ trợ: " + request.getSource()));

        handler.handle(request.getSourceId());
    }
}
