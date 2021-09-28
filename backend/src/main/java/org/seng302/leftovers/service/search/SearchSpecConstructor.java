package org.seng302.leftovers.service.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.dto.product.ProductFilterOption;
import org.seng302.leftovers.dto.saleitem.SaleListingSearchDTO;
import org.seng302.leftovers.dto.user.UserRole;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.SpecificationsBuilder;
import org.seng302.leftovers.persistence.SearchCriteria.Pred;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class SearchSpecConstructor {
    /**
     * Specification for a user that is not the DGAA
     * @return Specification matching any user except DGAA
     */
    public static Specification<User> isNotDGAASpec() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("role"), UserRole.DGAA);
    }

    /**
     * This method parses a search query to construct a specification which will match only those users which match the
     * search query by calling private methods.
     *
     * It takes a searchQuery and calls splitSearchStringIntoTerms to divide it into its individual tokens. These tokens
     * are either single words or multiple words joined by single or double quotes.
     *
     * parseUserSearchTokens is then called, which uses the tokens to build a list of specifications and a list of
     * predicates types. The specifications will match uses which contain a certain term, and the predicate types indicate
     * whether these specifications are joined by logical AND or logical OR.
     *
     * Then buildCompoundSpecification is called which combines the individual specifications using the predicates to
     * create one specification which can be used to query the User repository for users which match the search query.
     *
     * Finally DGAA accounts are filtered out
     * @param searchQuery A query entered by the user for searching for users within the database.
     * @return A specification which matches the user's search query.
     */
    public static Specification<User> constructUserSpecificationFromSearchQuery(String searchQuery) {
        List<String> searchTokens = SearchQueryParser.splitSearchStringIntoTerms(searchQuery);

        var fieldNames = Arrays.asList("firstName", "lastName", "nickname", "middleName");
        SearchQueryParser.SearchQuery<User> parsedQuery = SearchQueryParser.parseSearchTokens(searchTokens, fieldNames);
        return buildCompoundSpecification(parsedQuery)
                .and(isNotDGAASpec());
    }

    /**
     * This method returns a specification which will match businesses according to a given search query and business
     * type. If one of these two parameters is null then it will be ignored and the provided field will be used to find
     * matching businesses. If bother parameters are provided then a specification for only businesses that match both
     * will be returned. If neither is provided then an exception will be thrown.
     * @param searchQuery A search query provided by the user, used to find matching business names.
     * @param businessType A business type provided by the user, used to find businesses with matching type.
     * @param minPoints  A minimum points boundary provided by user to find business with more than or equal to the minPoints
     * @return A specification for businesses matching the provided search query and type.
     */
    public static Specification<Business> constructSpecificationFromBusinessSearch(String searchQuery, BusinessType businessType, Integer minPoints) {
        if (searchQuery == null && businessType == null && minPoints == null) {
            ValidationResponseException exception =
                    new ValidationResponseException("Provide either a search query or business type or minPoints to find matching businesses");
            SearchQueryParser.logger.error(exception.getMessage());
            throw exception;
        }
        if (searchQuery == null) {
            return constructBusinessSpecificationFromType(businessType).and(constructBusinessSpecificationFromPoints(minPoints));
        }
        if (businessType == null) {
            return constructBusinessSpecificationFromSearchQuery(searchQuery).and(constructBusinessSpecificationFromPoints(minPoints));
        }
        if (minPoints == null) {
            return constructBusinessSpecificationFromSearchQuery(searchQuery).and(constructBusinessSpecificationFromType(businessType));
        }
        return constructBusinessSpecificationFromSearchQuery(searchQuery)
                .and(constructBusinessSpecificationFromType(businessType)
                .and(constructBusinessSpecificationFromPoints(minPoints)));
    }

    /**
     * Creates a specification matching a product that is in a business and matches given searchQuery over the provided columns.
     * @param business Business to select products from
     * @param searchQuery Query to filter product by
     * @param options Columns to apply the query over
     * @return User product search specification
     */
    public static Specification<Product> constructSpecificationFromProductSearch(Business business, String searchQuery, Set<ProductFilterOption> options) {
        return productBusinessSpecification(business).and(productFilterSpecification(searchQuery, options));
    }

    /**
     * Creates a specification matching a product that matches the given searchQuery over the provided columns.
     * If no columns are provided, then all columns are tested.
     * Does not filter products by business
     *
     * @param searchQuery User provided search query
     * @param options Set of columns to filter by
     * @return Specification for filtering products by user search query
     */
    public static Specification<Product> productFilterSpecification(String searchQuery, Set<ProductFilterOption> options) {
        List<String> searchTokens = SearchQueryParser.splitSearchStringIntoTerms(searchQuery);

        if (options.isEmpty()) {
            options = Set.of(ProductFilterOption.NAME);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> columnNames = options.stream().map(option -> objectMapper.convertValue(option, String.class)).collect(Collectors.toList());

        return buildCompoundSpecification(SearchQueryParser.parseSearchTokens(searchTokens, columnNames));
    }

    /**
     * Creates a specification matching all products that belong to the provided business
     * @param business Business to filter products by
     * @return Specification filtering products to the business
     */
    public static Specification<Product> productBusinessSpecification(Business business) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("business"), business);
    }

    /**
     * This method returns a specification for the Business entity which will match Business objects with a name which is a partial match for the given searchTerm.
     * For example, if the search term was 'Tim', the
     * specification would match Businesses with the name 'Tim', 'Tim's BBQ' or 'Tim's garage',
     * Also supports operations AND and OR in the search term. Exact matches are given using quotation marks
     * @param searchQuery A term to find exact matches for.
     * @return A specification which will match Businesses that partially match the given string in the business name.
     */
    private static Specification<Business> constructBusinessSpecificationFromSearchQuery(String searchQuery) {
        List<String> searchTokens = SearchQueryParser.splitSearchStringIntoTerms(searchQuery);
        List<String> fieldNames = Collections.singletonList("name");

        SearchQueryParser.SearchQuery<Business> searchSpecs = SearchQueryParser.parseSearchTokens(searchTokens, fieldNames);
        return buildCompoundSpecification(searchSpecs);
    }

    /**
     * This method returns a specification which will only match businesses with the given string for their business type.
     * An exception will be thrown if an invalid business type is provided.
     * @param businessType The type to match.
     * @return Specification for finding businesses with the given type.
     */
    private static Specification<Business> constructBusinessSpecificationFromType(BusinessType businessType) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("businessType"), businessType);
    }

    private static Specification<Business> constructBusinessSpecificationFromPoints(Integer minPoints) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("businessType"), minPoints);
    }

    /**
     * Returns a specification for Keywords which is used to filter keywords using a search term.
     * @param searchQuery The term to search for
     * @return Specification of type Keyword
     */
    public static Specification<Keyword> constructKeywordSpecificationFromSearchQuery(String searchQuery) {
//        TODO something
        return buildPartialMatchSpec(searchQuery, Collections.singletonList("name"));
    }

    /**
     * Construct a specification to match against Sale items
     * Specification will match when searchQuery matches ANY field of
     * [product name, business name, business address, product manufacturer, product description]
     * AND productName, businessName, businessLocation match their respective fields.
     * When a field is left blank, it is ignored.
     * @param searchQuery Term to match against all fields
     * @param productName Term to match against productName
     * @param businessName Term to match against Business name
     * @param businessLocation Term to match against Business location
     * @return A specification for Sale Items
     */
    public static Specification<SaleItem> constructSaleItemSpecificationFromSearchQueries
            (String searchQuery, String productName, String businessName, String businessLocation) {

        // build a match for specific fields
        Specification<SaleItem> searchSpec = Specification.where(null);
        if (searchQuery != null && !searchQuery.isBlank()) {
            searchSpec = searchSpec.and(constructSaleItemSpecificationFromBasicQuery(searchQuery));
        }
        if (businessName != null && !businessName.isBlank()){
            searchSpec = searchSpec.and(constructSaleItemSpecificationFromBusinessName(businessName));
        }
        if (productName != null && !productName.isBlank()) {
            searchSpec = searchSpec.and(constructSaleItemSpecificationFromProductName(productName));
        }
        if (businessLocation != null && !businessLocation.isBlank()) {
            searchSpec = searchSpec.and(constructSaleItemSpecificationFromBusinessLocation(businessLocation));
        }

        return searchSpec;
    }

    /**
     * Returns a specifications which matches sale items with a given product name
     * @param basicQuery The term to search for
     * @return Specification for SaleItem
     */
    private static Specification<SaleItem> constructSaleItemSpecificationFromBasicQuery(String basicQuery) {
        var allFieldNames = List.of(
                "inventoryItem.product.name",
                "inventoryItem.product.business.name",
                "inventoryItem.product.business.address.country",
                "inventoryItem.product.business.address.city",
                "inventoryItem.product.business.address.region",
                "inventoryItem.product.manufacturer",
                "inventoryItem.product.description",
                "moreInfo");
        List<String> searchTokens = SearchQueryParser.splitSearchStringIntoTerms(basicQuery);

        SearchQueryParser.SearchQuery<SaleItem> searchSpecs = SearchQueryParser.parseSearchTokens(searchTokens, allFieldNames);
        return buildCompoundSpecification(searchSpecs);
    }

    /**
     * Returns a specifications which matches sale items with a given product name
     * @param productName The product name to search for
     * @return Specification for SaleItem
     */
    private static Specification<SaleItem> constructSaleItemSpecificationFromProductName(String productName) {
        List<String> fieldNames = Arrays.asList("inventoryItem.product.name");
        List<String> searchTokens = SearchQueryParser.splitSearchStringIntoTerms(productName);

        SearchQueryParser.SearchQuery<SaleItem> searchSpecs = SearchQueryParser.parseSearchTokens(searchTokens, fieldNames);
        return buildCompoundSpecification(searchSpecs);
    }

    /**
     * Returns a specifications which matches against the name of the business which owns the sale item
     * @param businessName The query to search against business name
     * @return Specification for SaleItem
     */
    private static Specification<SaleItem> constructSaleItemSpecificationFromBusinessName(String businessName) {
        List<String> fieldNames = Arrays.asList(
                "inventoryItem.product.business.name");
        List<String> searchTokens = SearchQueryParser.splitSearchStringIntoTerms(businessName);

        SearchQueryParser.SearchQuery<SaleItem> searchSpecs = SearchQueryParser.parseSearchTokens(searchTokens, fieldNames);
        return buildCompoundSpecification(searchSpecs);
    }

    /**
     * Returns a specifications which matches against the location of the business which owns the sale item
     * The query will match against country, city and region
     * @param location The query to search against location
     * @return Specification for SaleItem
     */
    private static Specification<SaleItem> constructSaleItemSpecificationFromBusinessLocation(String location) {
        List<String> fieldNames = Arrays.asList(
                "inventoryItem.product.business.address.country",
                "inventoryItem.product.business.address.city",
                "inventoryItem.product.business.address.region");
        List<String> searchTokens = SearchQueryParser.splitSearchStringIntoTerms(location);

        SearchQueryParser.SearchQuery<SaleItem> searchSpecs = SearchQueryParser.parseSearchTokens(searchTokens, fieldNames);
        return buildCompoundSpecification(searchSpecs);
    }

    /**
     * This method takes a SearchQuery which is a list of user specifications and a list of predicate types. It combines
     * the individual specifications into one specification depending on the prediate type. The specification at index i
     * in searchSpecs is followed by the predicate type at index i in predicateTypes e.g. for 'a AND b', a would be at
     * index 0 in the list of specifications and AND would be at index 0 in the list of predicate types.
     * @return A compound specification made up of individual specifications linked by predicates.
     */
    private static <T> Specification<T> buildCompoundSpecification(SearchQueryParser.SearchQuery<T> searchQuery) {
        List<Specification<T>> searchSpecs = searchQuery.getSearchSpecs();
        List<SearchQueryParser.PredicateType> predicateTypes = searchQuery.getPredicateTypes();

        Specification<T> result = searchSpecs.get(0);
        for (int i = 1; i < searchSpecs.size(); i++) {
            if (predicateTypes.get(i-1).equals(SearchQueryParser.PredicateType.OR)) {
                result = result.or(searchSpecs.get(i));
            } else {
                result = result.and(searchSpecs.get(i));
            }
        }
        return result;
    }

    /**
     * This method returns a specification for filtering entities which will match objects with fields
     * which is an exact match for the given searchTerm. For example, if the search term was 'Jo', the
     * specification would match Users with the name 'Jo' but not 'jo' or 'Joe',
     * @param searchTerm A term to find exact matches for.
     * @param fieldNames A list of fields to compare
     * @return A specification which will match entities that exactly match the given string in one of the given fields.
     */
    private static <T> Specification<T> buildExactMatchSpec(String searchTerm, List<String> fieldNames) {
        SpecificationsBuilder<T> builder = new SpecificationsBuilder<>();
        for (var field : fieldNames) {
            builder.with(field, Pred.FULL_MATCH, searchTerm, true);
        }
        return builder.build();
    }

    /**
     * This method returns a specification for filtering entities which will match objects with fields
     * which is a partial match for the given searchTerm. For example, if the search term was 'Jo', the
     * specification would match Users with the name 'Jo', 'jo' or 'Joe',
     * @param searchTerm A term to find exact matches for.
     * @param fieldNames A list of fields to compare
     * @return A specification which will match entities that exactly match the given string in one of their given fields.
     */
    public static <T> Specification<T> buildPartialMatchSpec(String searchTerm, List<String> fieldNames) {
        SpecificationsBuilder<T> builder = new SpecificationsBuilder<>();
        for (var field : fieldNames) {
            builder.with(field, Pred.PARTIAL_MATCH, searchTerm, true);
        }
        return builder.build();
    }

    /**
     * This method constructs a specification which will match only those sale items whose business matches the business
     * provided by calling private methods.
     * @param business Business of interest to match with
     * @return A specification for sale items which matches the business
     */
    public static Specification<SaleItem> constructSpecificationFromSaleItemsFilter(Business business) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("inventoryItem").get("product").get("business"), business);
    }

    /**
     * This method constructs a specification which will match only those inventory items whose business matches the business
     * provided by calling private methods.
     * @param business Business of interest to match with
     * @return A specification for inventory items which matches the business
     */
    public static Specification<InventoryItem> constructSpecificationFromInventoryItemsFilter(Business business) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("product").get("business"), business);
    }

    /**
     * Constructs a specification with regards to the "price" field of the SaleItem and matches SaleItems which are between
     * the lower bound and upper bound of prices inclusive.
     * @param lowerBound of the price of the SaleItems
     * @param upperBound of the price of the SaleItems
     * @return A specification for SaleItems which matches the price range
     */
    public static Specification<SaleItem> constructSaleListingSpecificationFromPrice(BigDecimal lowerBound, BigDecimal upperBound) {
        // If both bounds are null, return an specification with no limitations
        if (lowerBound == null && upperBound == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        else if (lowerBound == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), upperBound);
        }
        else if (upperBound == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("price"), lowerBound);
        }
        // If both bounds are present, return a specification between both bounds
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("price"), lowerBound, upperBound);
    }

    /**
     * Constructs a specification with regards to the "closes" field of the SaleItem and matches SaleItems which are between
     * the lower bound and upper bound of closing dates inclusive.
     * @param lowerBound of the closing date of the SaleItems
     * @param upperBound of the closing date of the SaleItems
     * @return A specification for SaleItems which matches the closing date range
     */
    public static Specification<SaleItem> constructSaleListingSpecificationFromClosingDate(LocalDate lowerBound, LocalDate upperBound) {
        // If both bounds are null, return an specification with no limitations
        if (lowerBound == null && upperBound == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        else if (lowerBound == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("closes"), upperBound);
        }
        else if (upperBound == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("closes"), lowerBound);
        }
        // If both bounds are present, return a specification between both bounds
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("closes"), lowerBound, upperBound);
    }

    /**
     * Constructs a specification with regards to the "businessType" field of the business that owns the SaleItems
     * and matches SaleItems which are owned by businesses of that type.
     * @param businessTypes a list of strings containing the business types of the business
     * @return A specification for Sale items which matches the business's business type
     */
    public static Specification<SaleItem> constructSaleListingSpecificationFromBusinessType(List<BusinessType> businessTypes) {
        if  (businessTypes == null || businessTypes.isEmpty()) {
            return null; // Null specification matches all business types
        } else {
            return (root, query, criteriaBuilder) -> root.get("inventoryItem").get("product").get("business").get("businessType").in(businessTypes);
        }

    }

    /**
     * Constructs a composite specification with three other smaller specifications containing the price, closing date and
     * business type of sale items.
     * @param saleListingSearchDTO containing the price, closing date and business type of the search specification
     * @return A specification for Sale items which matches the business's price, closing date and business type
     */
    public static Specification<SaleItem> constructSaleListingSpecificationForSearch(SaleListingSearchDTO saleListingSearchDTO) {
        return Specification.where(constructSaleListingSpecificationFromPrice(
                        saleListingSearchDTO.getPriceLowerBound(), saleListingSearchDTO.getPriceUpperBound())).
                and(constructSaleListingSpecificationFromClosingDate(
                        saleListingSearchDTO.getClosingDateLowerBound(), saleListingSearchDTO.getClosingDateUpperBound())).
                and(constructSaleListingSpecificationFromBusinessType(
                        saleListingSearchDTO.getBusinessTypes())).
                and(constructSaleItemSpecificationFromSearchQueries(saleListingSearchDTO.getBasicSearchQuery(), saleListingSearchDTO.getProductSearchQuery(), saleListingSearchDTO.getBusinessSearchQuery(), saleListingSearchDTO.getLocationSearchQuery()));
    }

    /**
     * Constructs a BoughtSaleItem specification that matches all entities that are bought within the bounds of the
     * provided start and end dates+times.
     * @param start Earliest matching sale date+time (inclusive)
     * @param end Latest matching sale date+time (exclusive)
     * @return Specification for bought sale items, sold within a certain interval
     */
    public static Specification<BoughtSaleItem> constructBoughtSaleListingSpecificationFromPeriod(Instant start, Instant end) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("saleDate"), start));
            }
            if (end != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("saleDate"), end));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Constructs a BoughtSaleItem specification that matches all that belong to the provided business
     * @param business Business to filter bought sale items by
     * @return Specification that matches only bought sale items that belong to the business
     */
    public static Specification<BoughtSaleItem> constructBoughtSaleListingSpecificationFromBusiness(Business business) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("product").get("business"), business);
    }
}
