package org.seng302.leftovers.service.search;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.controllers.UserController;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

public class SearchQueryParser {

    static final Logger logger = LogManager.getLogger(SearchQueryParser.class);

    enum PredicateType {
        OR, AND
    }

    /**
     * Object representing a query that is not yet combined into a single spec.
     * @param <T> Entity type to apply spec on
     */
    @Data
    static class SearchQuery<T> {
        private final List<Specification<T>> searchSpecs;
        private final List<PredicateType> predicateTypes;
    }

    /**
     * This method parses a list of search tokens taken from a search string entered by a user. If the token is in
     * quotation marks, a specification which will only match attributes which are exactly the same as that token is
     * constructed and added to searchSpecs. If the token is a word that is not in quotes, and is not 'AND' or 'OR',
     * a specification which will match attributes of which contain this token without case sensitivity is constructed.
     * 'AND' and 'OR' tokens are used to determine what predicate should be used to chain specifications, with AND being
     * the default if no predicate token is present.
     * @param searchTokens A list of single words or phrases in quotes from the user's search string.
     * @param fieldNames A list of field names to compare against
     * @return SearchQuery is a list of specifications matching a field and a parallel list of predicate types.
     */
    static <T> SearchQuery<T> parseSearchTokens(List<String> searchTokens, List<String> fieldNames) {
        List<Specification<T>> searchSpecs = new ArrayList<>();
        List<PredicateType> predicateTypesByIndex = new ArrayList<>();
        int i = 0;
        while (i < searchTokens.size()) {
            String token = searchTokens.get(i);
            if ((token.startsWith("\"") || token.startsWith("'")) && token.length() > 1) {
                String termWithoutQuotes = token.substring(1, token.length() - 1);
                searchSpecs.add(SearchSpecConstructor.buildPartialMatchSpec(termWithoutQuotes, fieldNames));

            } else if (!(token.equalsIgnoreCase("and") || token.equalsIgnoreCase("or"))) {
                searchSpecs.add(SearchSpecConstructor.buildPartialMatchSpec(token, fieldNames));
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
            ValidationResponseException searchFormatException = new ValidationResponseException("No valid search terms in query.");
            logger.error(searchFormatException.getMessage());
            throw(searchFormatException);
        }

        return new SearchQuery<>(searchSpecs, predicateTypesByIndex);
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
    static List<String> splitSearchStringIntoTerms(String searchString) {
        if (searchString.isBlank()) {
            ValidationResponseException searchFormatException = new ValidationResponseException("Search query cannot be blank.");
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
                ValidationResponseException searchFormatException = new ValidationResponseException("Search string contains opening quote but " +
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
     * @return String of concatenated list items
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
     * @return List of users
     */
    public static List<User> getSearchResultsOrderedByRelevance(String originalSearchQuery, UserRepository userRepository, Boolean reverse) {
        Sort idSort = UserController.getSort("userID", false);

        String fullMatchSomeTermsQuery = getFullMatchesQueryString(originalSearchQuery);
        String fullMatchAllTermsQuery = getQueryStringWithoutOr(fullMatchSomeTermsQuery);

        Specification<User> fullMatchAllTermsSpec = SearchSpecConstructor.constructUserSpecificationFromSearchQuery(fullMatchAllTermsQuery);
        Specification<User> fullMatchSomeTermsSpec = SearchSpecConstructor.constructUserSpecificationFromSearchQuery(fullMatchSomeTermsQuery);
        Specification<User> partialMatchSomeTermsSpec = SearchSpecConstructor.constructUserSpecificationFromSearchQuery(originalSearchQuery);

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
