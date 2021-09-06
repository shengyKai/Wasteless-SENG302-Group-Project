package org.seng302.leftovers.tools;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.seng302.leftovers.controllers.DGAAController;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SearchHelperSaleListingTest {
    /**
     * List of users to be used in testing of getPageInResults.
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
    @Autowired
    private DGAAController dgaaController;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private SaleItemRepository saleItemRepository;

    @BeforeAll
    void init() {
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
    }

    @BeforeEach
    void setUp() throws IOException {
        dgaaController.checkDGAA();
        userRepository.deleteAll();
        for (User user : savedUserList) {
            userRepository.save(user);
        }
    }

    @AfterEach
    void tearDown() {
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @ParameterizedTest
    @CsvSource({
            "The Nathan Apple",
            "Orange Apple",
            "Sweet Lime",
            "Tangerine",
            "Juicy Fruit",
            "\"Juicy Apple\""
    })
    void constructSaleItemSpecificationOnlyIncludingProductName_matchesFullProductName_saleItemReturned() {
        //TODO
    }

    @ParameterizedTest
    @CsvSource({
            "Natha",
            "athan",
            "Apple",
            "The",
            "Oran",
            "A"
    })
    void constructSaleItemSpecificationOnlyIncludingProductName_matchesPartialProductName_saleItemReturned() {
        //TODO
    }

    @ParameterizedTest
    @CsvSource({
            "wow,much,pog",
            "\"Juicy Apple\", apple",
            "yes.yes"
    })
    void constructSaleItemSpecificationOnlyIncludingProductName_doesNotMatchProductName_saleItemNotReturned() {
        //TODO
    }

    @ParameterizedTest
    @CsvSource({
            "Nathan Dugle",
            "Ella Wonson",
            "Ella",
            "Nathan",
            "Dugle",
            "Wonson",
    })
    void constructSaleItemSpecificationOnlyIncludingSellersName_matchesSellersName_saleItemReturned() {
        //TODO
    }

    @ParameterizedTest
    @CsvSource({
            "Nat",
            "han",
            "Du",
            "gle",
            "E",
            "Won"
    })
    void constructSaleItemSpecificationOnlyIncludingSellersName_matchesPartialSellersName_saleItemReturned() {
        //TODO
    }

    @ParameterizedTest
    @CsvSource({
            "\"Ella Wonson\"",
            "Ella, Wonson",
            "Nathan,Dugle",
            "Got.Him",
            "$lla"
    })
    void constructSaleItemSpecificationOnlyIncludingSellersName_doesNotMatchSellersName_saleItemNotReturned() {
        //TODO
    }

    @ParameterizedTest
    @CsvSource({
            "Christchurch",
            "Dunedin",
            "Wellington",
            "Auckland",
            "Gore",
            "Melbourne",
            "London",
            "Canterbury",
            "Otago",
            "Tasman",
            "Marlborough",
            "New Zealand",
            "Australia",
            "Luxembourg",
            "England",
            "Great Britain",
    })
    void constructSaleItemSpecificationOnlyIncludingSellersLocation_matchesLocation_saleItemReturned() {
        //TODO
    }

    @ParameterizedTest
    @CsvSource({
            "Christ",
            "church",
            "land",
            "ore",
            "don",
            "bury",
            "man",
            "e",
            "bourg",
            "tain"
    })
    void constructSaleItemSpecificationOnlyIncludingSellersLocation_matchesPartialLocation_saleItemReturned() {
        //TODO
    }

    @ParameterizedTest
    @CsvSource({
            "Christ,church",
            "Dun,edin",
            "A....uckland",
            "Gor$",
            "Ot@go",
            "T@sman",
            "N*ls0n",
            "Gr#at Br!ti@n",
            "Austr,lia"
    })
    void constructSaleItemSpecificationOnlyIncludingSellersLocation_doesNotMatchLocation_saleItemNotReturned() {
        //TODO
    }

}
