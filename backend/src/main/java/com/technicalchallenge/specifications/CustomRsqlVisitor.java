package com.technicalchallenge.specifications;

import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

public class CustomRsqlVisitor<T> implements RSQLVisitor<Specification<T>, Void> {

    private GenericRsqlSpecBuilder<T> builder;

    public CustomRsqlVisitor() {
        builder = new GenericRsqlSpecBuilder<T>();
    }

    @Override
    public Specification<T> visit(AndNode node, Void param) {
        return node.getChildren().stream()
                   .map(n -> n.accept(this))
                   .reduce(Specification::and)
                   .orElse(null);
    }

    @Override
    public Specification<T> visit(OrNode node, Void param) {
        return node.getChildren().stream()
                   .map(n -> n.accept(this))
                   .reduce(Specification::or)
                   .orElse(null);
    }

    @Override
    public Specification<T> visit(ComparisonNode node, Void param) {
        return builder.createSpecification(node);
    }
}
