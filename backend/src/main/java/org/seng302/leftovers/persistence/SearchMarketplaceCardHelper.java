package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.List;

public class SearchMarketplaceCardHelper {

    private SearchMarketplaceCardHelper() {}

    /**
     * Specification generator for finding marketplace cards with the given keywords
     * @param keywords List of keywords to filter by
     * @param isOr Whether to accept cards with all or at least 1 matching keyword
     * @return Generated specification
     */
    public static Specification<MarketplaceCard> cardHasKeywords(List<Keyword> keywords, boolean isOr) {
        return (root, query, criteriaBuilder) -> {
            Subquery<Long> subquery = query.subquery(Long.class);

            Root<MarketplaceCard> subqueryRoot = subquery.from(MarketplaceCard.class);
            Join<MarketplaceCard, Keyword> join = subqueryRoot.join("keywords");

            subquery.select(criteriaBuilder.count(subqueryRoot));
            subquery.where(criteriaBuilder.equal(root, subqueryRoot), join.in(keywords));

            if (isOr) {
                return criteriaBuilder.greaterThan(subquery, 0L);
            } else {
                return criteriaBuilder.equal(subquery, keywords.size());
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
