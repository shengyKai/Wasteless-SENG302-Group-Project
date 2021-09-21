package org.seng302.leftovers.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.service.searchservice.SearchSpecConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SearchServiceBusinessTest {

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
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("Joe's Garage")
                .withPrimaryOwner(owner)
                .build();
        businessRepository.save(testBusiness1);
        Business testBusiness2 = new Business.Builder()
                .withBusinessType(BusinessType.RETAIL_TRADE)
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("Old Joe's Farm")
                .withPrimaryOwner(owner)
                .build();
        businessRepository.save(testBusiness2);
        Business testBusiness3 = new Business.Builder()
                .withBusinessType(BusinessType.RETAIL_TRADE)
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
        var specification = SearchSpecConstructor.constructSpecificationFromBusinessSearch(query, null);
        var businesses = businessRepository.findAll(specification);
        assertEquals(numMatches, businesses.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_randomSearchTerm_matchesNoBusinesses() {
        createBusinesses();
        var specification = SearchSpecConstructor.constructSpecificationFromBusinessSearch("thereAreNoBusinessesWithThisInTheirName", null);
        var businesses = businessRepository.findAll(specification);
        assertEquals(0, businesses.size());
    }

    @ParameterizedTest
    @CsvSource({"JOE,2", "joe,2"})
    void constructSpecificationFromBusinessSearch_caseIsIgnored(String query, int numMatches) {
        createBusinesses();
        var specification = SearchSpecConstructor.constructSpecificationFromBusinessSearch(query, null);
        var businesses = businessRepository.findAll(specification);
        assertEquals(numMatches, businesses.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_EmptyStringTest() {
        assertThrows(ValidationResponseException.class, () -> SearchSpecConstructor.constructSpecificationFromBusinessSearch("", null));
    }

    @Test
    void constructSpecificationFromBusinessSearch_DoubleQuotesExactMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchSpecConstructor.constructSpecificationFromBusinessSearch("\"Joe's Garage\"", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals("Joe's Garage", matches.get(0).getName());
    }

    @Test
    void constructSpecificationFromBusinessSearch_SingleQuotesExactMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchSpecConstructor.constructSpecificationFromBusinessSearch("'Steves Workshop'", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals("Steves Workshop", matches.get(0).getName());
    }


    @ParameterizedTest
    @CsvSource({
            "\"Joe\",2",
            "'Joe',2",
            "\"zzz\",0",
            "'X',0",
            "\"joe's garage\",1",
            "'steves workshop',1",
            "kfsdjkdf,0",
            "Joe's and Garage,1",
            "joe's AND Garage,1",
            "joe and \"Potato\",0",
            "tomato and \"Potato\",0",
            "farm or Workshop,2",
            "workshop or \"Potato\",1",
            "tomato or \"Potato\",0",
            "Joe's \"Potato\",0",
            "workshop         or      \"Potato\",1",
            "joe   and        \"Potato\",0"
    })
    void constructSpecificationFromBusinessSearch_variousQueries_correctResultNumberReturned(String query, int expectedMatches) {
        createBusinesses();
        Specification<Business> specification = SearchSpecConstructor.constructSpecificationFromBusinessSearch(query, null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(expectedMatches, matches.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_NoQuotesExactMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchSpecConstructor.constructSpecificationFromBusinessSearch("Joe's Garage", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(1, matches.size());
        for (Business business : matches) {
            assertEquals("Joe's Garage", business.getName());
        }
    }

    @Test
    void constructSpecificationFromBusinessSearch_NoQuotesPartialMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchSpecConstructor.constructSpecificationFromBusinessSearch("Joe", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(2, matches.size());
        for (Business business : matches) {
            assertTrue(business.getName().contains("Joe"));
        }
    }

    @Test
    void constructSpecificationFromBusinessSearch_NoQuotesDifferentCaseMatchTest() {
        createBusinesses();
        Specification<Business> specification = SearchSpecConstructor.constructSpecificationFromBusinessSearch("JOE", null);
        List<Business> matches = businessRepository.findAll(specification);
        assertEquals(2, matches.size());
        for (Business business : matches) {
            assertTrue(business.getName().toLowerCase().contains("joe"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with just the word 'and' as its argument,
     * a ValidationResponseException is thrown.
     */
    @Test
    void constructSpecificationFromBusinessSearch_JustAndTest() {
        assertThrows(ValidationResponseException.class, () -> SearchSpecConstructor.constructSpecificationFromBusinessSearch("and", null));
    }

    @Test
    void constructSpecificationFromBusinessSearch_JustOrTest() {
        assertThrows(ValidationResponseException.class, () -> SearchSpecConstructor.constructSpecificationFromBusinessSearch("OR", null));
    }

    @Test
    void constructSpecificationFromBusinessSearch_OpeningQuoteTest() {
        assertThrows(ValidationResponseException.class, () -> SearchSpecConstructor.constructSpecificationFromBusinessSearch("\"hello", null));
    }

    @ParameterizedTest
    @CsvSource({
            "ACCOMMODATION_AND_FOOD_SERVICES,1",
            "RETAIL_TRADE,2"
    })
    void constructSpecificationFromBusinessSearch_onlyBusinessTypeProvided_specificationMatchesGivenType(BusinessType type, int numMatches) {
        createBusinesses();
        var specification= SearchSpecConstructor.constructSpecificationFromBusinessSearch(null, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(numMatches, businesses.size());

    }

    @ParameterizedTest
    @ValueSource(strings = {"CHARITABLE","NON_PROFIT"})
    void constructSpecificationFromBusinessSearch_onlyBusinessTypeProvided_specificationDoesNotMatchOtherType(BusinessType type) {
        createBusinesses();
        var specification= SearchSpecConstructor.constructSpecificationFromBusinessSearch(null, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(0, businesses.size());
    }

    @Test
    void constructSpecificationFromBusinessSearch_queryAndTypeNotProvided_exceptionThrown() {
        assertThrows(ValidationResponseException.class, () -> SearchSpecConstructor.constructSpecificationFromBusinessSearch(null, null));
    }

    @ParameterizedTest
    @CsvSource({
            "ACCOMMODATION_AND_FOOD_SERVICES,joe,1",
            "RETAIL_TRADE,Steve,1"
    })
    void constructSpecificationFromBusinessSearch_queryAndTypeProvided_specificationMatchesIfBothMatch(BusinessType type, String query, int numMatches) {
        createBusinesses();
        var specification= SearchSpecConstructor.constructSpecificationFromBusinessSearch(query, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(numMatches, businesses.size());
    }

    @ParameterizedTest
    @CsvSource({
            "CHARITABLE,joe",
            "NON_PROFIT,Steve"
    })
    void constructSpecificationFromBusinessSearch_queryAndTypeProvided_specificationDoesNotMatchIfOnlyQueryMatches(BusinessType type, String query) {
        createBusinesses();
        var specification= SearchSpecConstructor.constructSpecificationFromBusinessSearch(query, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(0, businesses.size());
    }

    @ParameterizedTest
    @CsvSource({
            "ACCOMMODATION_AND_FOOD_SERVICES,hamburgers",
            "RETAIL_TRADE,My shop"
    })
    void constructSpecificationFromBusinessSearch_queryAndTypeProvided_specificationDoesNotMatchIfOnlyTypeMatches(BusinessType type, String query) {
        createBusinesses();
        var specification= SearchSpecConstructor.constructSpecificationFromBusinessSearch(query, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(0, businesses.size());
    }

    @ParameterizedTest
    @CsvSource({
            "CHARITABLE,hamburgers",
            "NON_PROFIT,My shop"
    })
    void constructSpecificationFromBusinessSearch_queryAndTypeProvided_specificationDoesNotMatchIfNeitherMatches(BusinessType type, String query) {
        createBusinesses();
        var specification= SearchSpecConstructor.constructSpecificationFromBusinessSearch(query, type);
        var businesses = businessRepository.findAll(specification);
        assertEquals(0, businesses.size());
    }
}
