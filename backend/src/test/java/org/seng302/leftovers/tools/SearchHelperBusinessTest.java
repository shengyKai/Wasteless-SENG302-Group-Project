package org.seng302.leftovers.tools;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.exceptions.SearchFormatException;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SearchHelperBusinessTest {

    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void createBusinesses() {
        User owner = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        owner = userRepository.save(owner);
        Business testBusiness1 = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("Joe's Garage")
                .withPrimaryOwner(owner)
                .build();
        businessRepository.save(testBusiness1);
        Business testBusiness2 = new Business.Builder()
                .withBusinessType("Retail Trade")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("Old Joe's Farm")
                .withPrimaryOwner(owner)
                .build();
        businessRepository.save(testBusiness2);
        Business testBusiness3 = new Business.Builder()
                .withBusinessType("Retail Trade")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("Steves Workshop")
                .withPrimaryOwner(owner)
                .build();
        businessRepository.save(testBusiness3);
    }

    @ParameterizedTest
    @CsvSource({"joe,2", "garage,1", "steve,1"})
    void constructSpecificationFromBusinessSearch_validSearchTerm_matchesBusinessName(String query, int numMatches) {
        createBusinesses();
        var specification = SearchHelper.constructSpecificationFromBusinessSearch(query, null);
        var businesses = businessRepository.findAll(specification);
        assertEquals(numMatches, businesses.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_randomSearchTerm_matchesNoBusinesses() {
        createBusinesses();
        var specification = SearchHelper.constructSpecificationFromBusinessSearch("thereAreNoBusinessesWithThisInTheirName", null);
        var businesses = businessRepository.findAll(specification);
        assertEquals(0, businesses.size());
    }

    @ParameterizedTest
    @CsvSource({"JOE,2", "joe,2"})
    void constructSpecificationFromBusinessSearch_caseIsIgnored(String query, int numMatches) {
        createBusinesses();
        var specification = SearchHelper.constructSpecificationFromBusinessSearch(query, null);
        var businesses = businessRepository.findAll(specification);
        assertEquals(numMatches, businesses.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_EmptyStringTest() {
        assertThrows(SearchFormatException.class, () -> {
            SearchHelper.constructSpecificationFromBusinessSearch("", null);
        });
    }

    @Test
    void constructSpecificationFromBusinessSearch_DoubleQuotesExactMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("\"Joe's Garage\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals("Joe's Garage", matches.get(0).getName());
    }

    @Test
    void constructSpecificationFromBusinessSearch_SingleQuotesExactMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("'Steves Workshop'", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals("Steves Workshop", matches.get(0).getName());
    }


    @Test
    void constructSpecificationFromBusinessSearch_DoubleQuotesPartialMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("\"Joe\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(2, matches.size());
    }


    @Test
    void constructSpecificationFromBusinessSearch_SingleQuotesPartialMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("'Joe'", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(2, matches.size());
    }


    @Test
    void constructSpecificationFromBusinessSearch_DoubleQuotesNoMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("\"zzz\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(0, matches.size());
    }


    @Test
    void constructSpecificationFromBusinessSearch_SingleQuotesNoMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("'X'", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(0, matches.size());
    }


    @Test
    void constructSpecificationFromBusinessSearch_DoubleQuotesDifferentCaseMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("\"joe's garage\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
    }


    @Test
    void constructSpecificationFromBusinessSearch_SingleQuotesDifferentCaseMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("'steves workshop'", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_NoQuotesExactMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("Joe's Garage", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
        for (Business business : matches) {
            assertEquals("Joe's Garage", business.getName());
        }
    }

    @Test
    void constructSpecificationFromBusinessSearch_NoQuotesPartialMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("Joe", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(2, matches.size());
        for (Business business : matches) {
            assertTrue(business.getName().contains("Joe"));
        }
    }


    @Test
    void constructSpecificationFromBusinessSearch_NoQuotesNoMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("kfsdjkdf", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_NoQuotesDifferentCaseMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("JOE", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(2, matches.size());
        for (Business business : matches) {
            assertTrue(business.getName().toLowerCase().contains("joe"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with just the word 'and' as its argument,
     * a SearchFormatException is thrown.
     */
    @Test
    void constructSpecificationFromBusinessSearch_JustAndTest() {
        assertThrows(SearchFormatException.class, () -> {
            SearchHelper.constructSpecificationFromBusinessSearch("and", null);
        });
    }


    @Test
    void constructSpecificationFromBusinessSearch_LowerAndBothMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("Joe's and Garage", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
    }


    @Test
    void constructSpecificationFromBusinessSearch_UpperAndBothMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("joe's AND Garage", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_AndOneMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("joe and \"Potato\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_AndNoMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("tomato and \"Potato\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_JustOrTest() {
        assertThrows(SearchFormatException.class, () -> {
            SearchHelper.constructSpecificationFromBusinessSearch("OR", null);
        });
    }

    @Test
    void constructSpecificationFromBusinessSearch_BothMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("farm or Workshop", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(2, matches.size());
    }


    @Test
    void constructSpecificationFromBusinessSearch_LowerOrOneMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("workshop or \"Potato\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_OrNoMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("tomato or \"Potato\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(0, matches.size());
    }


    @Test
    void constructSpecificationFromBusinessSearch_NoPredicateOneMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("Joe's \"Potato\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_OrExtraWhitespaceTest() {
        createBusinesses();
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("workshop         or      \"Potato\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_AndExtraWhitespaceTest() {
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch("joe   and        \"Potato\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_OpeningQuoteTest() {
        assertThrows(SearchFormatException.class, () -> {
            SearchHelper.constructSpecificationFromBusinessSearch("\"hello", null);
        });
    }

    @ParameterizedTest
    @CsvSource({"Accommodation and Food Services,1", "Retail Trade,2"})
    void constructSpecificationFromBusinessSearch_onlyBusinessTypeProvided_specificationMatchesGivenType(String type, int numMatches) {
        createBusinesses();
        var specification= SearchHelper.constructSpecificationFromBusinessSearch(null, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(numMatches, businesses.size());

    }

    @ParameterizedTest
    @ValueSource(strings = {"Charitable organisation","Non-profit organisation"})
    void constructSpecificationFromBusinessSearch_onlyBusinessTypeProvided_specificationDoesNotMatchOtherType(String type) {
        createBusinesses();
        var specification= SearchHelper.constructSpecificationFromBusinessSearch(null, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(0, businesses.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_invalidBusinessTypeProvided_exceptionThrown() {
        assertThrows(SearchFormatException.class, () -> {
            SearchHelper.constructSpecificationFromBusinessSearch(null, "Foo");
        });
    }

    @Test
    void constructSpecificationFromBusinessSearch_queryAndTypeNotProvided_exceptionThrown() {
        assertThrows(SearchFormatException.class, () -> {
            SearchHelper.constructSpecificationFromBusinessSearch(null, null);
        });
    }

    @ParameterizedTest
    @CsvSource({"Accommodation and Food Services,joe,1", "Retail Trade,Steve,1"})
    void constructSpecificationFromBusinessSearch_queryAndTypeProvided_specificationMatchesIfBothMatch(String type, String query, int numMatches) {
        createBusinesses();
        var specification= SearchHelper.constructSpecificationFromBusinessSearch(query, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(numMatches, businesses.size());
    }

    @ParameterizedTest
    @CsvSource({"Charitable organisation,joe", "Non-profit organisation,Steve"})
    void constructSpecificationFromBusinessSearch_queryAndTypeProvided_specificationDoesNotMatchIfOnlyQueryMatches(String type, String query) {
        createBusinesses();
        var specification= SearchHelper.constructSpecificationFromBusinessSearch(query, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(0, businesses.size());
    }

    @ParameterizedTest
    @CsvSource({"Accommodation and Food Services,hamburgers", "Retail Trade,My shop"})
    void constructSpecificationFromBusinessSearch_queryAndTypeProvided_specificationDoesNotMatchIfOnlyTypeMatches(String type, String query) {
        createBusinesses();
        var specification= SearchHelper.constructSpecificationFromBusinessSearch(query, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(0, businesses.size());
    }

    @ParameterizedTest
    @CsvSource({"Charitable organisation,hamburgers", "Non-profit organisation,My shop"})
    void constructSpecificationFromBusinessSearch_queryAndTypeProvided_specificationDoesNotMatchIfNeitherMatches(String type, String query) {
        createBusinesses();
        var specification= SearchHelper.constructSpecificationFromBusinessSearch(query, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(0, businesses.size());
    }
}
