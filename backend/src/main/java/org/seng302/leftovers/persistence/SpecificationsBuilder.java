package org.seng302.leftovers.persistence;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A specification builder
 * Builds specifications made from one or more predicates to assist in searching for entities
 */
public class SpecificationsBuilder<T> {

    private final List<SearchCriteria> params;

    public SpecificationsBuilder() {
        params = new ArrayList<>();
    }

    /**
     * Adds a predicate to the specification
     * @param key The column to compare
     * @param operation The compare operation
     * @param value he value to compare against
     * @param isOrPredicate Determines if predicate will be AND / OR
     * @return The builder
     */
    public SpecificationsBuilder<T> with(String key, String operation, Object value, boolean isOrPredicate) {
        params.add(new SearchCriteria(key, operation, value, isOrPredicate));
        return this;
    }

    /**
     * Builds the specification
     * @return A chained set of predicates
     */
    public Specification<T> build() {
        if (params.isEmpty()) {
            return null;
        }

        List<Specification<T>> specs = params.stream()
                .map(SearchSpecification<T>::new)
                .collect(Collectors.toList());

        Specification<T> result = specs.get(0);

        for (int i = 1; i < params.size(); i++) {
            result = params.get(i)
                    .isOrPredicate()
                    ? Specification.where(result)
                    .or(specs.get(i))
                    : Specification.where(result)
                    .and(specs.get(i));
        }
        return result;
    }
}