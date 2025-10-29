package com.technicalchallenge.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.technicalchallenge.service.TradeSearchCriteria;
import com.technicalchallenge.model.Trade;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Expression;

public class TradeSpecifications {

    public static Specification<Trade> build(TradeSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getCounterpartyName() != null) {
                predicates.add(cb.like(
                        cb.lower(root.get("counterparty").get("name")),
                        "%" + criteria.getCounterpartyName().toLowerCase() + "%"));
            }

            if (criteria.getCounterpartyId() != null) {
                predicates.add(cb.equal(root.get("counterparty").get("id"), criteria.getCounterpartyId()));
            }

            if (criteria.getBookName() != null) {
                predicates.add(cb.like(
                        cb.lower(root.get("book").get("bookName")),
                        "%" + criteria.getBookName().toLowerCase() + "%"));
            }

            if (criteria.getBookId() != null) {
                predicates.add(cb.equal(root.get("book").get("id"), criteria.getBookId()));
            }

            if (criteria.getTraderUserId() != null) {
                predicates.add(cb.equal(root.get("traderUser").get("id"), criteria.getTraderUserId()));
            }

            if (criteria.getTraderUserName() != null) {
                Expression<String> fullName = cb.concat(
                        cb.concat(cb.coalesce(root.get("traderUser").get("firstName"), ""), " "),
                        cb.coalesce(root.get("traderUser").get("lastName"), ""));
                predicates.add(cb.like(
                        cb.lower(fullName),
                        "%" + criteria.getTraderUserName().toLowerCase() + "%"));
            }

            if (criteria.getTradeStatus() != null) {
                predicates.add(cb.equal(root.get("tradeStatus").get("tradeStatus"), criteria.getTradeStatus()));
            }

            // Date range filtering using tradeDate and maturityDate
            // tradeDate looks for on or after the specified date
            if (criteria.getTradeDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("tradeDate"), criteria.getTradeDate()));
            }
            
            // maturityDate looks for trades maturing on or before the specified date
            if (criteria.getMaturityDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("tradeMaturityDate"), criteria.getMaturityDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
