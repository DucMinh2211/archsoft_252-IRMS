package com.irms.order.service;

import com.irms.order.domain.OrderType;
import com.irms.order.dto.TableResponseDTO;
import com.irms.order.exception.BusinessValidationException;
import com.irms.order.infrastructure.client.TableServiceClient;
import org.springframework.stereotype.Component;

@Component
public class OrderTableValidator {

    private final TableServiceClient tableServiceClient;

    public OrderTableValidator(TableServiceClient tableServiceClient) {
        this.tableServiceClient = tableServiceClient;
    }

    public void validateFor(OrderCreateInput input) {
        if (input.type() != OrderType.DINE_IN) {
            return;
        }
        if (input.tableId() == null) {
            throw new BusinessValidationException("Table ID is required for DINE_IN orders.");
        }

        TableResponseDTO table = tableServiceClient.getTable(input.tableId());
        if (!"AVAILABLE".equals(table.getStatus()) && !"OCCUPIED".equals(table.getStatus())) {
            // Warning or error depending on strictness. Let's assume OCCUPIED is fine for adding an order.
        }
    }
}
