package org.seng302.leftovers.service.searchservice;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

public class SearchPageConstructor {
    private static final int DEFAULT_RESULTS_PER_PAGE = 15;

    /**
     * This method takes an ordered list of User objects  and returns a list of the user objects which should appear on
     * a single page to be displayed on client side of the application. The page number and number of results per page
     * can be specified or left as null, in which case the default values will be used for these parameters.
     * @param queryResults An ordered list of User objects resulting from a query of the database.
     * @param requestedPageOrNull The page number in the results which has been requested. Defaults to 1.
     * @param resultsPerPageOrNull The number of results which will be returned. Defaults to 15.
     * @return An ordered list of Users with length resultsPerPage.
     */
    public static <T> List<T> getPageInResults(List<T> queryResults, Integer requestedPageOrNull, Integer resultsPerPageOrNull) {

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
}
