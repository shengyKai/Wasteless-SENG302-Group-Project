package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.List;

/**
 * Defines specifications for searching MarketplaceCard entities
 */
public class SearchMarketplaceCardHelper {

    private SearchMarketplaceCardHelper() {}

    /**
     * Specification generator for finding marketplace cards with the given keywords
     * If no keywords are provided then the resulting specification will match all marketplace cards
     * @param keywords List of keywords to filter by
     * @param isOr Whether to accept cards with all or at least 1 matching keyword
     * @return Generated specification
     */
    public static Specification<MarketplaceCard> cardHasKeywords(List<Keyword> keywords, boolean isOr) {
        return (root, query, criteriaBuilder) -> {
            if (keywords.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            Subquery<Long> subquery = query.subquery(Long.class); // Query for the number of matching keywords

            Root<MarketplaceCard> subqueryRoot = subquery.from(MarketplaceCard.class);
            Join<MarketplaceCard, Keyword> join = subqueryRoot.join("keywords");

            subquery.select(criteriaBuilder.count(subqueryRoot));
            subquery.where(criteriaBuilder.equal(root, subqueryRoot), join.in(keywords));

            if (isOr) {
                return criteriaBuilder.greaterThanOrEqualTo(subquery, 1L); // At least one match
            } else {
                return criteriaBuilder.greaterThanOrEqualTo(subquery, (long)keywords.size()); // All match
            }
        };
    }

    /**
     * Specification for a marketplace card in the provided section
     * @param section Section to filter cards by
     * @return Generated specification that filters by section
     */
    public static Specification<MarketplaceCard> cardIsInSection(MarketplaceCard.Section section) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("section"), section);
    }
}
