package org.seng302.leftovers.persistence;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.Transient;
import javax.persistence.criteria.*;

/**
 * Defines a specification for type User
 * Used to create search predicates
 * Can be chained together to form a specification
 */
public class SearchSpecification<T> implements Specification<T> {

    @Transient
    private SearchCriteria criteria;

    /**
     * Specification
     * @param criteria The search criteria for this predicate
     */
    public SearchSpecification(SearchCriteria criteria){
        this.criteria = criteria;
    }

    /**
     * Constructs a predicate of type User
     * @param root Criteria root
     * @param query The search criteria
     * @param builder Criteria Builder
     * @return A predicate matching the search criteria
     */
    @Override
    public Predicate toPredicate
            (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        Path<String> key = getPath(root, criteria.getKey());
        if (criteria == null) {
            return builder.conjunction();
        }
        else if (criteria.getOperation().equalsIgnoreCase(">")) {
            return builder.greaterThanOrEqualTo(
                    key, criteria.getValue().toString());
        }
        else if (criteria.getOperation().equalsIgnoreCase("<")) {
            return builder.lessThanOrEqualTo(
                    key, criteria.getValue().toString());
        }
        else if (criteria.getOperation().equalsIgnoreCase(":")) {
            if (key.getJavaType() == String.class) {
                return builder.like(
                        builder.lower(key), "%" + criteria.getValue().toString().toLowerCase() + "%");
            }
        }
        else if (criteria.getOperation().equalsIgnoreCase("=") &&
                key.getJavaType() == String.class) {
            return builder.like(
                    key, criteria.getValue().toString());
        }
        return null;
    }

    /**
     * Given a predicate root, and input path separated by periods, returns the path of the last attribute
     * @param root The root
     * @param input Path of period separated attributes
     * @return Path of last attribute of input
     */
    private <Y> Path<Y> getPath(Root<T> root, String input) {
        var items = input.split("\\.");
        Path<Y> path = root.get(items[0]);
        for (int i = 1; i < items.length; i++) {
            path = path.get(items[i]);
        }
        return path;
    }
}

