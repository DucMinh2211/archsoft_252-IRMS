package com.irms.order.service;

import com.irms.order.domain.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderSpecificationFactory {

    public Specification<Order> from(OrderSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }
            if (criteria.waiterId() != null) {
                predicates.add(cb.equal(root.get("waiterId"), criteria.waiterId()));
            }
            if (criteria.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.startDate()));
            }
            if (criteria.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.endDate()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
