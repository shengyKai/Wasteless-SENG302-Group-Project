package org.seng302.leftovers.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.exceptions.SearchFormatException;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.persistence.SpecificationsBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides static methods to help the controller classes return results from queries to the database.
 */
public class SearchHelper {

    private static final int DEFAULT_RESULTS_PER_PAGE = 15;
    private static final List<String> USER_ORDER_BY_OPTIONS = List.of("userID", "firstName", "middleName", "lastName", "nickname", "email");
    private static final Logger logger = LogManager.getLogger(SearchHelper.class);

    private enum PredicateType {
        OR, AND
    }

    /**
     * This method takes an ordered list of User objects  and returns a list of the user objects which should appear on
     * a single page to be displayed on client side of the application. The page number and number of results per page
     * can be specified or left as null, in which case the default values will be used for these parameters.
     * @param queryResults An ordered list of User objects resulting from a query of the database.
     * @param requestedPageOrNull The page number in the results which has been requested. Defaults to 1.
     * @param resultsPerPageOrNull The number of results which will be returned. Defaults to 15.
     * @return An ordered list of Users with length resultsPerPage.
     */
    public static <T>List<T> getPageInResults(List<T> queryResults, Integer requestedPageOrNull, Integer resultsPerPageOrNull) {

        int resultsPerPage = getResultsPerPageInt(resultsPerPageOrNull);
        int requestedPage = getRequestedPageInt(requestedPageOrNull);

        int numResults = queryResults.size();
        int maxPageNum = (int) Math.ceil((double) numResults / resultsPerPage);
        int pageToReturn = setPageToReturn(requestedPage, maxPageNum);

        int fromIndex = (pageToReturn - 1) * resultsPerPage;
        int toIndex = Math.min(fromIndex + resultsPerPage, numResults);
        return queryResults.subList(fromIndex, toIndex);
    }

    /**
     * Creates a request for a specific page when sorting by the provided sort
     * @param requestedPage Page index to start from (1 based indexing)
     * @param resultsPerPage Maximum number of results per page
     * @param sort Sorting function to use
     * @return PageRequest for the given parameters
     */
    public static PageRequest getPageRequest(Integer requestedPage, Integer resultsPerPage, Sort sort) {
        return PageRequest.of(getRequestedPageInt(requestedPage) - 1, getResultsPerPageInt(resultsPerPage), sort);
    }

    /**
     * Normalises the results per page to a valid results per page value.
     * If null or less then one returns 15
     * @param resultsPerPage A integer for the number of results per page or null
     * @return An int representing the requested number of results per page.
     */
    private static int getResultsPerPageInt(Integer resultsPerPage) {
        if (resultsPerPage == null || resultsPerPage < 1) {
            return DEFAULT_RESULTS_PER_PAGE;
        } else {
            return resultsPerPage;
        }
    }

    /**
     * Normalises the requested page to a valid page value.
     * If null or less then one returns 1
     * @param requestedPage A integer for the number of results per page or null
     * @return An int representing the requested number of results per page.
     */
    private static int getRequestedPageInt(Integer requestedPage) {
        if (requestedPage == null || requestedPage < 1) {
            return 1;
        } else {
            return requestedPage;
        }
    }

    /**
     * Determine the page number which should be returned by getPageInResults. Sets page number to 1 if it is zero, a
     * negative number or null (meaning no requested page number has been provided in the client request). Set it to the
     * highest possible page number if the page number requested is above this.
     * @param requestedPage The page number requested by the user.
     * @param maxPageNum The highest page number which is possible for the given number of results.
     * @return The page number for which results should be returned by getPageInResults
     */
    private static int setPageToReturn(Integer requestedPage, int maxPageNum) {
        int page;
        if (requestedPage == null || requestedPage < 1) {
            page = 1;
        } else if (requestedPage > maxPageNum) {
            page = Math.max(maxPageNum, 1);
        } else {
            page = requestedPage;
        }
        return page;
    }

    /**
     * Gets the sort direction based on whether reverse is selected.
     * If reverse is null then a default sort order is returned (Ascending)
     * @param reverse Whether sort needs to be reversed from default
     * @return Sort order based in reverse
     */
    public static Sort.Direction getSortDirection(Boolean reverse) {
        return Boolean.TRUE.equals(reverse) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    /**
     * This method constructs a Sort object to be passed into a query for searching the UserRepository. The attribute
     * which Users should be sorted by and whether that order should be reversed are specified.
     * @param orderBy The attribute which query results will be ordered by.
     * @param reverse Results will be in descending order if true, ascending order if false or null.
     * @return A Sort which can then be applied to queries of the UserRepository.
     */
    public static Sort getSort(String orderBy, Boolean reverse) {
        if (orderBy == null || !USER_ORDER_BY_OPTIONS.contains(orderBy)) {
            orderBy = "userID";
        }

        return Sort.by(getSortDirection(reverse), orderBy);
    }

    /**
     * Specification for a user that is not the DGAA
     * @return Specification matching any user except DGAA
     */
    public static Specification<User> isNotDGAASpec() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("role"), "defaultGlobalApplicationAdmin");
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
        List<String> searchTokens = splitSearchStringIntoTerms(searchQuery);

        var fieldNames = Arrays.asList("firstName", "lastName", "nickname", "middleName");

        List<Specification<User>> searchSpecs = new ArrayList<>();
        List<PredicateType> predicateTypesByIndex = new ArrayList<>();
        parseSearchTokens(searchTokens, searchSpecs, predicateTypesByIndex, fieldNames);

        return buildCompoundSpecification(searchSpecs, predicateTypesByIndex)
                .and(isNotDGAASpec());
    }

    /**
     * This method returns a specification which will match businesses according to a given search query and business
     * type. If one of these two parameters is null then it will be ignored and the provided field will be used to find
     * matching businesses. If bother parameters are provided then a specification for only businesses that match both
     * will be returned. If neither is provided then an exception will be thrown.
     * @param searchQuery A search query provided by the user, used to find matching business names.
     * @param businessType A business type provided by the user, used to find businesses with matching type.
     * @return A specification for businesses matching the provided search query and type.
     */
    public static Specification<Business> constructSpecificationFromBusinessSearch(String searchQuery, String businessType) {
        if (searchQuery == null && businessType == null) {
            SearchFormatException exception =
                    new SearchFormatException("Provide either a search query or business type to find matching businesses");
            logger.error(exception.getMessage());
            throw exception;
        }
        if (searchQuery == null) {
            return constructBusinessSpecificationFromType(businessType);
        }
        if (businessType == null) {
            return constructBusinessSpecificationFromSearchQuery(searchQuery);
        }
        return constructBusinessSpecificationFromSearchQuery(searchQuery)
                .and(constructBusinessSpecificationFromType(businessType));
    }

    public static Specification<Product> constructSpecificationFromProductSearch(Business business, String searchQuery, List<Product> columns) {

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
        List<String> searchTokens = splitSearchStringIntoTerms(searchQuery);
        List<String> fieldNames = Collections.singletonList("name");

        List<Specification<Business>> searchSpecs = new ArrayList<>();
        List<PredicateType> predicateTypesByIndex = new ArrayList<>();
        parseSearchTokens(searchTokens, searchSpecs, predicateTypesByIndex, fieldNames);
        return buildCompoundSpecification(searchSpecs, predicateTypesByIndex);
    }

    /**
     * This method returns a specification which will only match businesses with the given string for their business type.
     * An exception will be thrown if an invalid business type is provided.
     * @param businessType The type to match.
     * @return Specification for finding businesses with the given type.
     */
    private static Specification<Business> constructBusinessSpecificationFromType(String businessType) {
        if (!Business.getBusinessTypes().contains(businessType)) {
            SearchFormatException exception = new SearchFormatException(String.format("\"%s\" is an invalid business type", businessType));
            logger.error(exception.getMessage());
            throw exception;
        }
        return buildExactMatchSpec(businessType, Collections.singletonList("businessType"));
    }
     
    /* Returns a specification for Keywords which is used to filter keywords using a search term.
     * @param searchQuery The term to search for
     * @return Specification of type Keyword
     */
    public static Specification<Keyword> constructKeywordSpecificationFromSearchQuery(String searchQuery) {
        return buildPartialMatchSpec(searchQuery ,Collections.singletonList("name"));
    }

    /**
     * This method takes a list of user specifications and a list of predicate types. It combines the individual
     * specifications into one specification depending on the prediate type. The specification at index i in searchSpecs
     * is followed by the predicate type at index i in predicateTypes e.g. for 'a AND b', a would be at index 0 in the
     * list of specifications and AND would be at index 0 in the list of predicate types.
     * @param searchSpecs A list of N user specifications to be combined.
     * @param predicateTypes A list of N-1 predicate types to use when combining the specifications.
     * @return A compound specification made up of individual specifications linked by predicates.
     */
    private static <T> Specification<T> buildCompoundSpecification(List<Specification<T>> searchSpecs,
                                                                   List<PredicateType> predicateTypes) {
        Specification<T> result = searchSpecs.get(0);
        for (int i = 1; i < searchSpecs.size(); i++) {
            if (predicateTypes.get(i-1).equals(PredicateType.OR)) {
                result = result.or(searchSpecs.get(i));
            } else {
                result = result.and(searchSpecs.get(i));
            }
        }
        return result;
    }

    /**
     * This method parses a list of search tokens taken from a search string entered by a user. If the token is in
     * quotation marks, a specification which will only match attributes which are exactly the same as that token is
     * constructed and added to searchSpecs. If the token is a word that is not in quotes, and is not 'AND' or 'OR',
     * a specification which will match attributes of which contain this token without case sensitivity is constructed.
     * 'AND' and 'OR' tokens are used to determine what predicate should be used to chain specifications, with AND being
     * the default if no predicate token is present.
     * @param searchTokens A list of single words or phrases in quotes from the user's search string.
     * @param searchSpecs An empty list which the specifications will be added to.
     * @param predicateTypesByIndex An empty list which the predicates will be added to.
     * @param fieldNames A list of field names to compare against
     */
    private static <T> void parseSearchTokens(List<String> searchTokens, List<Specification<T>> searchSpecs,
                                              List<PredicateType> predicateTypesByIndex, List<String> fieldNames) {
        int i = 0;
        while (i < searchTokens.size()) {
            String token = searchTokens.get(i);
            if ((token.startsWith("\"") || token.startsWith("'")) && token.length() > 1) {
                String termWithoutQuotes = token.substring(1, token.length() - 1);
                searchSpecs.add(buildPartialMatchSpec(termWithoutQuotes, fieldNames));

            } else if (!(token.equalsIgnoreCase("and") || token.equalsIgnoreCase("or"))) {
                searchSpecs.add(buildPartialMatchSpec(token, fieldNames));
            } else {
                i++;
                continue; // The current token is an operator so skip it
            }
            if (i + 1 < searchTokens.size()) {
                PredicateType predicateType = getPredicateType(searchTokens.get(i + 1));
                predicateTypesByIndex.add(predicateType);
            }
            i++;
        }

        if (searchSpecs.isEmpty()) {
            SearchFormatException searchFormatException = new SearchFormatException("No valid search terms in query.");
            logger.error(searchFormatException.getMessage());
            throw(searchFormatException);
        }

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
            builder.with(field, "=", searchTerm, true);
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
    private static <T> Specification<T> buildPartialMatchSpec(String searchTerm, List<String> fieldNames) {
        SpecificationsBuilder<T> builder = new SpecificationsBuilder<>();
        for (var field : fieldNames) {
            builder.with(field, ":", searchTerm, true);
        }
        return builder.build();
    }

    /**
     * This method returns PredicateType.OR if the given search token matches the string 'or' (case insensitive), or
     * PredicateType.AND if the given search token matches the string 'and'. It also returns PredicateType.AND if the
     * given string does not match either of these cases, as this is the default predicate type.
     * @param searchToken A single token from the search query.
     * @return PredicateType.OR if searchToken is or, PredicateType.AND otherwise.
     */
    private static PredicateType getPredicateType(String searchToken) {
        PredicateType predicateType = PredicateType.AND;
        if (searchToken.equalsIgnoreCase("or")) {
            predicateType = PredicateType.OR;
        }
        return predicateType;
    }

    /**
     * This method separates a search string by its whitespace and then identifies the terms in the string - either
     * individual words or phrases joined with double or single quotes.
     * @param searchString A string to be parsed into an array of individual terms.
     * @return An array containing each term from the search string.
     */
    private static List<String> splitSearchStringIntoTerms(String searchString) {
        if (searchString.isBlank()) {
            SearchFormatException searchFormatException = new SearchFormatException("Search query cannot be blank.");
            logger.error(searchFormatException.getMessage());
            throw(searchFormatException);
        }
        String[] words = searchString.split("[ ]+");
        ArrayList<String> searchTerms = new ArrayList<>();
        int termStartingIndex = 0;
        while (termStartingIndex < words.length) {

            int termEndingIndex = findTermEndingIndex(words, termStartingIndex);

            String[] wordsInSearchTerm = Arrays.copyOfRange(words, termStartingIndex, termEndingIndex+1);
            String joinedTerm = joinStringArrayWithSpace(wordsInSearchTerm);
            searchTerms.add(joinedTerm);

            termStartingIndex = termEndingIndex+1;
        }
        return searchTerms;
    }

    /**
     * This method takes an array of Strings and joins them with a space between strings.
     * @param stringArray An array of strings to be joined.
     * @return A String of words with a space between them.
     */
    private static String joinStringArrayWithSpace(String[] stringArray) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String word : stringArray) {
            joiner.add(word);
        }
        return joiner.toString();
    }

    /**
     * This method finds the index of the final word in a term. If the first word does not start with a quote then
     * the final index will be the same as the starting index. If the term does start with a quote, the array will be
     * searched until a string ending in a quote is found. A ResponseStatusException will be thrown if no string ending
     * in a quote is found.
     * @param words An array of words from which terms need to be identified.
     * @param termStartingIndex The index of the first word in the term.
     * @return The index of the final word in the term.
     */
    private static int findTermEndingIndex(String[] words, int termStartingIndex) {
        int termEndingIndex;

        if (words[termStartingIndex].startsWith("\"") || words[termStartingIndex].startsWith("'")) {
            String openingQuote = words[termStartingIndex].substring(0, 1);
            boolean foundClosingQuote = false;
            termEndingIndex = termStartingIndex;
            while (termEndingIndex < words.length) {
                if (words[termEndingIndex].endsWith(openingQuote)) {
                    foundClosingQuote = true;
                    break;
                }
                termEndingIndex++;
            }
            if (!foundClosingQuote) {
                SearchFormatException searchFormatException = new SearchFormatException("Search string contains opening quote but " +
                        "no closing quote.");
                logger.error(searchFormatException.getMessage());
                throw(searchFormatException);
            }
        } else {
            termEndingIndex = termStartingIndex;
        }

        return termEndingIndex;
    }

    /**
     * This method takes a string, replaces terms that are not in quotes with terms in quotes, and replaces or tokens
     * with and. The resulting query string will only match User entities which fully match each term in the original
     * search query.
     * @param originalSearchQuery A search query entered by the user.
     * @return A string which is like the original query except every or has been replaced with an and.
     */
    public static String getQueryStringWithoutOr(String originalSearchQuery) {
        List<String> originalTokens = splitSearchStringIntoTerms(originalSearchQuery);
        List<String> tokensWithoutOr = new ArrayList<>();
        for (String token : originalTokens) {
            if (token.equalsIgnoreCase("or")) {
                tokensWithoutOr.add("and");
            } else {
                tokensWithoutOr.add(token);
            }
        }
        return listToStringWithSpace(tokensWithoutOr);
    }

    /**
     * This method takes a string and replaces terms that are not in quotes with terms in quotes. The resulting query
     * string will only match User entities whose attributes fully match the terms from the original search query.
     * @param originalSearchQuery A search query entered by the user.
     * @return A string which is like the original search query except all terms are in quotes.
     */
    public static String getFullMatchesQueryString(String originalSearchQuery) {
        List<String> originalTokens = splitSearchStringIntoTerms(originalSearchQuery);
        List<String> fullMatchTokens = new ArrayList<>();
        for (String token : originalTokens) {
            if (token.startsWith("\"") || token.startsWith("'")) {
                fullMatchTokens.add(token);
            } else if (!(token.equalsIgnoreCase("or") || token.equalsIgnoreCase("and"))) {
                fullMatchTokens.add("\"" + token + "\"");
            } else {
                fullMatchTokens.add(token);
            }
        }
        return listToStringWithSpace(fullMatchTokens);
    }

    /**
     * This method takes a list of strings and returns a single string made up of all the individual strings joined with
     * a space character.
     * @param list A list of strings to be joined with spaces.
     * @return
     */
    private static String listToStringWithSpace(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String phrase : list) {
            builder.append(phrase);
            builder.append(" ");
        }
        String stringWithSpaces = builder.toString();
        return stringWithSpaces.substring(0, stringWithSpaces.length() - 1);
    }

    /**
     * This method takes a search query entered by the user. It returns a list of User entities from the given repository
     * which have a first name, middle name, last name or nickname matching the query, ordered by relevance as follows:
     *
     * 1) Users where part of their name is a full match for every term in the query (e.g. for the query 'Donald or Duck',
     * Donald Duck would fall into this category).
     *
     * 2) Users where part of their name is a full match for some of the terms in the query (e.g. for the query 'Donald
     * or Duck', Donald Smith would fall into this category).
     *
     * 3) User where part of their name is a partial match for some to the terms in the query (e.g. for the query 'Donald
     * or Duck', Lucy McDonald would fall into this category).
     *
     * @param originalSearchQuery A search query entered by the user.
     * @param userRepository The repository containing all the User entities.
     * @return
     */
    public static List<User> getSearchResultsOrderedByRelevance(String originalSearchQuery, UserRepository userRepository, Boolean reverse) {
        Sort idSort = getSort("userID", false);

        String fullMatchSomeTermsQuery = getFullMatchesQueryString(originalSearchQuery);
        String fullMatchAllTermsQuery = getQueryStringWithoutOr(fullMatchSomeTermsQuery);

        Specification<User> fullMatchAllTermsSpec = constructUserSpecificationFromSearchQuery(fullMatchAllTermsQuery);
        Specification<User> fullMatchSomeTermsSpec = constructUserSpecificationFromSearchQuery(fullMatchSomeTermsQuery);
        Specification<User> partialMatchSomeTermsSpec = constructUserSpecificationFromSearchQuery(originalSearchQuery);

        List<User> fullMatchesAllTerms = userRepository.findAll(fullMatchAllTermsSpec, idSort);
        List<User> fullMatchesSomeTerms = userRepository.findAll(fullMatchSomeTermsSpec, idSort);
        List<User> partialMatchesSomeTerms = userRepository.findAll(partialMatchSomeTermsSpec, idSort);

        List<User> matchList = new ArrayList<>();
        HashSet<Long> addedIds = new HashSet<>();
        addNewToList(matchList, addedIds, fullMatchesAllTerms);
        addNewToList(matchList, addedIds, fullMatchesSomeTerms);
        addNewToList(matchList, addedIds, partialMatchesSomeTerms);

        if (Boolean.TRUE.equals(reverse)) {
            Collections.reverse(matchList);
        }

        return matchList;
    }

    /**
     * Filters out the DGAA accounts from a list of users
     * @param userList List of users to filter
     * @return Filtered list of users
     */
    public static List<User> removeDGAAAccountFromResults(List<User> userList) {
        return userList
                .stream()
                .filter(user -> !user.getRole().equals("defaultGlobalApplicationAdmin"))
                .collect(Collectors.toList());
    }

    /**
     * This method checks addedIds to see if the given user has already been added to noDuplicatesList. If they have not,
     * the user is added to noDuplicatesList and their id is added to addedIds.
     * @param noDuplicatesList A list which users will be added to if they are not duplicates of users already in the list.
     * @param addedIds A list of ids of users which have been added to the list.
     * @param users A list of users which may be added to the list if they are not in it already.
     */
    private static void addNewToList(List<User> noDuplicatesList, HashSet<Long> addedIds, List<User> users) {
        for (User user : users) {
            if (!addedIds.contains(user.getUserID())) {
                addedIds.add(user.getUserID());
                noDuplicatesList.add(user);
            }
        }
    }





}
