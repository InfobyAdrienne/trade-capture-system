package com.technicalchallenge.specifications;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Path;

public class GenericRsqlSpecBuilder<T> {

  public Specification<T> createSpecification(ComparisonNode node) {
    return (root, query, cb) -> {
      // Handle nested properties
      Path<?> path = root;
      for (String property : node.getSelector().split("\\.")) {
        path = path.get(property);
      }

      String operator = node.getOperator().getSymbol();
      Object argument = node.getArguments().get(0);

      switch (operator) {
        case "==" -> {
          return cb.equal(path, argument);
        }
        case "!=" -> {
          return cb.notEqual(path, argument);
        }
        case "=ge=" -> {
          return cb.greaterThanOrEqualTo(path.as(String.class), argument.toString());
        }
        case "=le=" -> {
          return cb.lessThanOrEqualTo(path.as(String.class), argument.toString());
        }
        default -> throw new UnsupportedOperationException("Operator not supported: " + operator);
      }
    };
  }
}
