package org.seng302.Tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.Entities.User;
import org.seng302.Exceptions.SearchFormatException;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.UserRepository;
import org.seng302.Persistence.UserSpecificationsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserSearchHelperTest {

    /**
     * List of users to be used in testsing of getPageInResults.
     */
    private List<User> pagingUserList;

    /**
     * List of users to be saved to the repository and used for testing of getSort.
     */
    private List<User> savedUserList;

    /**
     * Repository storing user entities.
     */
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    /**
     * Speification for repository queries.
     */
    private Specification<User> spec;

    /**
     * Read user info from file UserSearchHelperTestData1.csv, use this information to construct User objects and add them to userList
     * @throws ParseException
     * @throws IOException
     */
    @BeforeEach
    public void setUp() throws ParseException, IOException {
        UserSpecificationsBuilder builder = new UserSpecificationsBuilder()
                .with("firstName", ":", "andy", true)
                .with("middleName", ":", "andy", true)
                .with("lastName", ":", "andy", true)
                .with("nickname", ":", "andy", true);
        spec = builder.build();

        pagingUserList = readUserFile("src//test//testFiles//UserSearchHelperTestData1.csv");
        savedUserList = readUserFile("src//test//testFiles//UserSearchHelperTestData2.csv");

        businessRepository.deleteAll();
        userRepository.deleteAll();
        for (User user : savedUserList) {
            userRepository.save(user);
        }
    }

    private List<User> readUserFile(String filepath) throws IOException {
        List<User> userList = new ArrayList<>();
        String row;
        BufferedReader csvReader = new BufferedReader(new FileReader(filepath));
        while ((row = csvReader.readLine()) != null) {
            try {
                String[] userData = row.split(",");
                User user = new User.Builder().withFirstName(userData[0]).withMiddleName(userData[1]).withLastName(userData[2]).withNickName(userData[3])
                        .withEmail(userData[4]).withPassword(userData[5]).withAddress(userData[6]).withDob(userData[7]).build();
                userList.add(user);
            } catch (Exception e) {

            }
        }
        csvReader.close();
        return userList;
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and a non-null requested
     * page number which is above one and below the maximum, it will return the requested page with the requested number
     * of users on it.
     */
    @Test
    public void getPageInResultsValidResultsPerPageValidRequestedPageTest() {
        List<User> result = UserSearchHelper.getPageInResults(pagingUserList, "2", "10");
        assertArrayEquals(pagingUserList.subList(10, 20).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and null as the page number,
     * it will return the first page in the results with the requested number of users on it.
     */
    @Test
    public void getPageInResultsValidResultsPerPageNullRequestedPageTest() {
        List<User> result = UserSearchHelper.getPageInResults(pagingUserList, null, "10");
        assertArrayEquals(pagingUserList.subList(0, 10).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and a negative number as
     * the page number, it will return the first page in the results with the requested number of users on it.
     */
    @Test
    public void getPageInResultsValidResultsPerPageNegativeRequestedPageTest() {
        List<User> result = UserSearchHelper.getPageInResults(pagingUserList, "-3", "10");
        assertArrayEquals(pagingUserList.subList(0, 10).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and a number above the maximum as
     * the page number, it will return the last page in the results when the results are divided according to the requested
     * number of results per page.
     */
    @Test
    public void getPageInResultsValidResultsPerPageAboveMaximumRequestedPageTest() {
        List<User> result = UserSearchHelper.getPageInResults(pagingUserList, "100", "10");
        assertArrayEquals(pagingUserList.subList(20, 26).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with null as the number of results per page and a non-null requested
     * page number which is above zero and below the maximum, it will return the requested page with 15 users per page.
     */
    @Test
    public void getPageInResultsNegativeResultsPerPageValidRequestedPageTest() {
        List<User> result = UserSearchHelper.getPageInResults(pagingUserList, "1", "-100");
        assertArrayEquals(pagingUserList.subList(0, 15).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with negative number as the number of results per page and a non-null
     * requested page number which is above zero and below the maximum, it will return the requested page with 15 users per page.
     */
    @Test
    public void getPageInResultsNullResultsPerPageValidRequestedPageTest() {
        List<User> result = UserSearchHelper.getPageInResults(pagingUserList, "1", null);
        assertArrayEquals(pagingUserList.subList(0, 15).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and a the final page as the
     * requested page number, but there are not enough users to fill the last page, it will return the final page with
     * with less than the requested number of users on it.
     */
    @Test
    public void getPageInResultsValidResultsPerPageFinalRequestedPageTest() {
        List<User> result = UserSearchHelper.getPageInResults(pagingUserList, "3", "10");
        assertArrayEquals(pagingUserList.subList(20, 26).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and a non-null requested
     * page number, but queryResults is an empty list, it will return an empty list.
     */
    @Test
    public void getPageInResultsQueryResultsEmptyTest() {
        List<User> emptyList = new ArrayList<User>();
        List<User> result = UserSearchHelper.getPageInResults(emptyList, "1", "10");
        assertArrayEquals(emptyList.toArray(), result.toArray());
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is null, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's id number.
     */
    @Test
    public void getSortOrderByNullTest()  {
        Sort userSort = UserSearchHelper.getSort(null, null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        Long previousId = firstUser.getUserID();
        for (int i = 1; i < queryResults.size(); i++) {
            Long currentId = queryResults.get(i).getUserID();
            assertTrue(currentId > previousId);
            previousId = currentId;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is firstName, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's firstName parameter.
     */
    @Test
    public void getSortOrderByFirstNameTest() {
        Sort userSort = UserSearchHelper.getSort("firstName", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousFirstName = firstUser.getFirstName();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentFirstName = queryResults.get(i).getFirstName();
            assertTrue(currentFirstName.compareTo(previousFirstName) >= 0);
            previousFirstName = currentFirstName;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is middleName, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's middleName parameter.
     */
    @Test
    public void getSortOrderByMiddleNameTest() {
        Sort userSort = UserSearchHelper.getSort("middleName", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousMiddleName = firstUser.getMiddleName();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentMiddleName = queryResults.get(i).getMiddleName();
            assertTrue(currentMiddleName.compareTo(previousMiddleName) >= 0);
            previousMiddleName = currentMiddleName;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is lastName, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's lastName parameter.
     */
    @Test
    public void getSortOrderByLastNameTest() {
        Sort userSort = UserSearchHelper.getSort("lastName", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousLastName = firstUser.getLastName();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentLastName = queryResults.get(i).getLastName();
            assertTrue(currentLastName.compareTo(previousLastName) >= 0);
            previousLastName = currentLastName;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is nickname, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's nickname parameter.
     */
    @Test
    public void getSortOrderByNicknameTest() {
        Sort userSort = UserSearchHelper.getSort("nickname", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousNickname = firstUser.getNickname();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentNickname = queryResults.get(i).getNickname();
            assertTrue(currentNickname.compareTo(previousNickname) >= 0);
            previousNickname = currentNickname;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is email, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's email parameter.
     */
    @Test
    public void getSortOrderByEmailTest() {
        Sort userSort = UserSearchHelper.getSort("email", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousEmail = firstUser.getEmail();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentEmail = queryResults.get(i).getEmail();
            assertTrue(currentEmail.compareTo(previousEmail) >= 0);
            previousEmail = currentEmail;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is firstName and the reverse parameter is true,
     * it returns a Sort which when applied to a query of UserRepository causes the results to be in reverse order
     * by the user's firstName parameter.
     */
    @Test
    public void getSortOrderByFirstNameReverseTrueTest() {
        Sort userSort = UserSearchHelper.getSort("firstName", "true");
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousFirstName = firstUser.getFirstName();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentFirstName = queryResults.get(i).getFirstName();
            assertTrue(currentFirstName.compareTo(previousFirstName) <= 0);
            previousFirstName = currentFirstName;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is email and the reverse parameter is false,
     * it returns a Sort which when applied to a query of UserRepository causes the results to be in alphabetical order
     * by the user's email parameter.
     */
    @Test
    public void getSortOrderByEmailReverseFalseTest() {
        Sort userSort = UserSearchHelper.getSort("email", "false");
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousEmail = firstUser.getEmail();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentEmail = queryResults.get(i).getEmail();
            assertTrue(currentEmail.compareTo(previousEmail) >= 0);
            previousEmail = currentEmail;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is address and the reverse parameter is null,
     * it returns a Sort which when applied to a query of UserRepository causes the results to be in alphabetical order
     * by the user's address parameter.
     */
    @Test
    public void getSortOrderByHomeAddressTest() {
        Sort userSort = UserSearchHelper.getSort("address", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousAddress = firstUser.getAddress();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentAddress = queryResults.get(i).getAddress();
            assertTrue(currentAddress.compareTo(previousAddress) >= 0);
            previousAddress = currentAddress;
        }
    }

    /**
     * Verify that when getSort is called with a parameter for orderBy which does not appear in orderByOptions, it returns
     * a Sort which when applied to a query of UserRepository causes the results to be in numerical order by the user's
     * id number.
     */
    @Test
    public void getSortOrderByInvalidOptionTest() {
        Sort userSort = UserSearchHelper.getSort("dateOfBirth", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        Long previousId = firstUser.getUserID();
        for (int i = 1; i < queryResults.size(); i++) {
            Long currentId = queryResults.get(i).getUserID();
            assertTrue(currentId > previousId);
            previousId = currentId;
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with an empty string as the argument,
     * as SearchFormatException is thrown.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryEmptyStringTest() {
        assertThrows(SearchFormatException.class, () -> {
            UserSearchHelper.constructUserSpecificationFromSearchQuery("");
        });
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in double
     * quotes as its argument, it returns a specification which matches Users in the UserRepository which have a name
     * that exactly matches that word.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryDoubleQuotesExactMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("\"Carl\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals("Carl", matches.get(0).getFirstName());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in single
     * quotes as its argument, it returns a specification which matches Users in the UserRepository which have a name
     * that exactly matches that word.
     */
    @Test
    public void constructUserSpecificationFromSearchQuerySingleQuotesExactMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("'Petra'");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals("Petra", matches.get(0).getMiddleName());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in double
     * quotes as its argument, it returns a specification which does not match users in the repository whose name is
     * a partial match for that word.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryDoubleQuotesPartialMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("\"Car\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in single
     * quotes as its argument, it returns a specification which does not match users in the repository whose name is
     * a partial match for that word.
     */
    @Test
    public void constructUserSpecificationFromSearchQuerySingleQuotesPartialMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("'etra'");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in double
     * quotes as its argument, it returns a specification which does not match users in the repository whose name does
     * not fully or partially match that word.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryDoubleQuotesNoMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("\"zzz\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in single
     * quotes as its argument, it returns a specification which does not match users in the repository whose name does
     * not fully or partially match that word.
     */
    @Test
    public void constructUserSpecificationFromSearchQuerySingleQuotesNoMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("'X'");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in double
     * quotes as its argument, it returns a specification which does not match users in the repository whose name is a
     * substring of the argument
     */
    @Test
    public void constructUserSpecificationFromSearchQueryDoubleQuotesNameSubstringTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("\"Carlos\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in single
     * quotes as its argument, it returns a specification which does not match users in the repository whose name is a
     * substring of the argument
     */
    @Test
    public void constructUserSpecificationFromSearchQuerySingleQuotesNameSubstringTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("'PPPPetra'");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in double
     * quotes as its argument, it returns a specification does not match users in the repository who have an name which
     * is the same as the argument but in a different case
     */
    @Test
    public void constructUserSpecificationFromSearchQueryDoubleQuotesDifferentCaseMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("\"carl\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in single
     * quotes as its argument, it returns a specification which does not match users in the repository who have a name
     * which is the same argument but in a different case
     */
    @Test
    public void constructUserSpecificationFromSearchQuerySingleQuotesDifferentCaseMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("'PetRA'");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word which is not in quotes
     * as its arguement, it returns a specification which matches users with a name which is an exact match for that word.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryNoQuotesExactMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("Andy");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(7, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().equals("Andy") ||
                        user.getMiddleName().equals("Andy") ||
                        user.getLastName().equals("Andy") ||
                        user.getNickname().equals("Andy"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word which is not in quotes
     * as its arguement, it returns a specification which matches users with a name which is a partial match for that word.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryNoQuotesPartialMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("ndy");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(7, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().contains("ndy") ||
                    user.getMiddleName().contains("ndy") ||
                    user.getLastName().contains("ndy") ||
                    user.getNickname().contains("ndy"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word without
     * quotes as its argument, it returns a specification which does not match users in the repository whose name does
     * not fully or partially match that word.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryNoQuotesNoMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("q");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word without
     * quotes as its argument, it returns a specification which does not match users in the repository whose name is a
     * substring of the argument
     */
    @Test
    public void constructUserSpecificationFromSearchQueryNoQuotesNameSubstringTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("Andyyyyy");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word without
     * quotes as its argument, it returns a specification will match users in the repository who have an name which
     * is the same as the argument but in a different case
     */
    @Test
    public void constructUserSpecificationFromSearchQueryNoQuotesDifferentCaseMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("ANDY");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(7, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().equalsIgnoreCase("ANDY") ||
                    user.getMiddleName().equalsIgnoreCase("ANDY") ||
                    user.getLastName().equalsIgnoreCase("ANDY") ||
                    user.getNickname().equalsIgnoreCase("ANDY"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with just the word 'and' as its argument,
     * a SearchFormatException is thrown.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryJustAndTest() {
        assertThrows(SearchFormatException.class, () -> {
            UserSearchHelper.constructUserSpecificationFromSearchQuery("and");
        });
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'and' as its
     * argument, it returns a specification which matches users which contain both those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryLowerAndBothMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("andy and \"Graham\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().equalsIgnoreCase("andy") &&
                    user.getLastName().equals("Graham"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'AND' as its
     * argument, it returns a specification which matches users which contain both those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryUpperAndBothMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("andy AND \"Graham\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().equalsIgnoreCase("andy") &&
                    user.getLastName().equals("Graham"));
        }
    }


    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'and' as its
     * argument, it returns a specification which doesn't match users which contain only one of those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryAndOneMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("andy and \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'and' as its
     * argument, it returns a specification which doesn't match users which contain neither of those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryAndNoMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("tomato and \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with just the word and 'OR' as its argument,
     * a SearchFormatException is thrown.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryJustOrTest() {
        assertThrows(SearchFormatException.class, () -> {
            UserSearchHelper.constructUserSpecificationFromSearchQuery("OR");
        });
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'or' as its
     * argument, it returns a specification which matches users which contain both those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryBothMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("peter or \"Graham\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        User user = matches.get(0);
        assertTrue(user.getMiddleName().equalsIgnoreCase("peter"));
        assertTrue(user.getLastName().equals("Graham"));
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'or' as its
     * argument, it returns a specification which matches users which contain only one of those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryLowerOrOneMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("peter or \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        User user = matches.get(0);
        assertTrue(user.getMiddleName().equalsIgnoreCase("peter"));
        assertFalse(user.getFirstName().equals("Potato") || user.getMiddleName().equals("Potato") ||
                user.getLastName().equals("Potato") || user.getNickname().equals("Potato"));
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'Or' as its
     * argument, it returns a specification which matches users which contain one of those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryMixedCaseOneMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("peter Or \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        User user = matches.get(0);
        assertTrue(user.getMiddleName().equalsIgnoreCase("peter"));
        assertFalse(user.getFirstName().equals("Potato") || user.getMiddleName().equals("Potato") ||
                user.getLastName().equals("Potato") || user.getNickname().equals("Potato"));
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'or' as its
     * argument, it returns a specification which doesn't match users which contain neither of those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryOrNoMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("tomato or \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms not joined by a predicate as its
     * argument, it returns a specification which matches users which contain both those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryNoPredicateBothMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("andy \"Graham\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().equalsIgnoreCase("andy") &&
                    user.getLastName().equals("Graham"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms not joined by a predicate as its
     * argument, it returns a specification which doesn't match users which contain only one of those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryNoPredicateOneMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("andy \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms not joined by a predicate as its
     * argument, it returns a specification which doesn't match users which contain neither of those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryNoPredicateNoMatchTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("tomato \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'or' as its
     * argument, and there is additional whitespace between the words in the query, it still returns a specification which
     * matches users which contain only one of those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryOrExtraWhitespaceTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("peter         or      \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        User user = matches.get(0);
        assertTrue(user.getMiddleName().equalsIgnoreCase("peter"));
        assertFalse(user.getFirstName().equals("Potato") || user.getMiddleName().equals("Potato") ||
                user.getLastName().equals("Potato") || user.getNickname().equals("Potato"));
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'and' as its
     * argument, and there is additional whitespace between the words in the query, it returns a specification which
     * doesn't match users which contain only one of those terms in their names.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryAndExtraWhitespaceTest() {
        Specification<User> specification = UserSearchHelper.constructUserSpecificationFromSearchQuery("andy   and        \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with a string containing an opening quote
     * but no closing quote, a SearchFormatException is thrown.
     */
    @Test
    public void constructUserSpecificationFromSearchQueryOpeningQuoteTest() {
        assertThrows(SearchFormatException.class, () -> {
            UserSearchHelper.constructUserSpecificationFromSearchQuery("\"hello");
        });
    }

    /**
     * Verify that getQueryStringWithoutOr replaces lower case 'or' in the query string.
     */
    @Test
    public void getQueryStringWithoutOrLowerCaseTest() {
        assertEquals("this and that", UserSearchHelper.getQueryStringWithoutOr("this or that"));
    }

    /**
     * Verify that getQueryStringWithoutOr replaces upper case 'OR' in the query string.
     */
    @Test
    public void getQueryStringWithoutOrUpperCaseTest() {
        assertEquals("ME and YOU", UserSearchHelper.getQueryStringWithoutOr("ME OR YOU"));
    }

    /**
     * Verify that getQueryStringWithoutOr doesn't replace or if it is in quotes.
     */
    @Test
    public void getQueryStringWithoutOrQuotesTest() {
        assertEquals("chicken 'or' beef", UserSearchHelper.getQueryStringWithoutOr("chicken 'or' beef"));
    }

    /**
     * Verify that getQueryStringWithoutOr doesn't replace the sequence 'or' within a word
     */
    @Test
    public void getQueryStringWithoutOrWithinWordTest() {
        assertEquals("corn horn and orchard", UserSearchHelper.getQueryStringWithoutOr("corn horn and orchard"));
    }

    /**
     * Verify that getFullMatchesQueryString puts all words inside quotes when there are no quotes present in the string.
     */
    @Test
    public void getFullMatchesQueryStringNoQuotesTest() {
        assertEquals("\"apple\" \"banana\" \"carrot\"", UserSearchHelper.getFullMatchesQueryString("apple banana carrot"));
    }

    /**
     * Verify that getFullMatchesQueryString doesn't put predicates into quotes when they are present within a string.
     */
    @Test
    public void getFullMatchesQueryStringPredicateTest() {
        assertEquals("\"Tom\" and \"Dick\" OR \"Harry\"", UserSearchHelper.getFullMatchesQueryString("Tom and Dick OR Harry"));
    }

    /**
     * Verify that getFullMatchesQueryString doesn't put quotes around parts of the string that are already in quotes.
     */
    @Test
    public void getFullMatchesQueryStringQuotesTest() {
        assertEquals("'Wow!' \"Amazing!\" \"Incredible!\"", UserSearchHelper.getFullMatchesQueryString("'Wow!' Amazing! \"Incredible!\""));
    }

    /**
     * Verify that the list of users returned by getSearchResultsOrderedByRelevance will be in the correct relevance
     * order if they all have different levels of relevance.
     */
    @Test
    public void getSearchResultsOrderedByRelevanceCorrectRelevanceOrderTest() throws ParseException {
        userRepository.deleteAll();
        User donaldDuck = new User.Builder().withFirstName("Donald").withLastName("Duck").withAddress("1313 Webfoot Walk, Duckburg, Calisota")
                .withDob("1934-06-09").withEmail("donald.duck@waddlemail.com").withPassword("HonkHonk").build();
        User donaldSmith = new User.Builder().withFirstName("Donald").withLastName("Smith").withAddress("92 Clyde Road, Ilam, Christchurch")
                .withDob("1994-03-08").withEmail("donald.smith@gmail.com").withPassword("123456789").build();
        User lucyMcDonald = new User.Builder().withFirstName("Lucy").withLastName("McDonald").withAddress("39 Riccarton Road, Riccarton, Christchurch")
                .withDob("2000-11-21").withEmail("lucymcdonald@hotmail.com").withPassword("password").build();
        userRepository.save(lucyMcDonald);
        userRepository.save(donaldDuck);
        userRepository.save(donaldSmith);
        List<User> result = UserSearchHelper.getSearchResultsOrderedByRelevance("Donald or Duck", userRepository);

        assertEquals("Donald", result.get(0).getFirstName());
        assertEquals("Duck", result.get(0).getLastName());
        assertEquals("Donald", result.get(1).getFirstName());
        assertEquals("Smith", result.get(1).getLastName());
        assertEquals("Lucy", result.get(2).getFirstName());
        assertEquals("McDonald", result.get(2).getLastName());
    }

    /**
     * Verify that when getSearchResultsOrderedByRelevance is called but the users matching the search query all have
     * the same level of relevance, they are ordered by their id number.
     */
    @Test
    public void getSearchResultsOrderedByRelevanceCorrectIdOrderTest() {
        List<User> result = UserSearchHelper.getSearchResultsOrderedByRelevance("andy", userRepository);
        User firstUser = result.get(0);
        Long previousId = firstUser.getUserID();
        for (int i = 1; i < result.size(); i++) {
            Long currentId = result.get(i).getUserID();
            assertTrue(currentId > previousId);
            previousId = currentId;
        }
    }

    /**
     * Verify that when getSearchUserResultsOrderedByRelevance is called with a search query that will match
     * users with different levels of relevance, there is no duplication of users in the results.
     */
    @Test
    public void getSearchResultsOrderedByRelevanceNoDuplicationTest() {
        List<User> result = UserSearchHelper.getSearchResultsOrderedByRelevance("a or Donna or Percy", userRepository);
        HashSet<Long> ids = new HashSet<>();
        for (User user : result) {
            assertFalse(ids.contains(user.getUserID()));
            ids.add(user.getUserID());
        }
    }



}