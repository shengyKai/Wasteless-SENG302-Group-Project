package org.seng302.leftovers.controllers;

import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DGAAControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private DGAAController dgaaController;

    @BeforeEach
    public void clean() {
        //because business repo has a foreign key in user repo, it needs to be cleared too
        inventoryItemRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        inventoryItemRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * DGAA repo is empty, ensure a new DGAA is generated
     */
    @Test
    void dgaaNotPresent() {
        dgaaController.checkDGAA();
        assertNotNull(userRepository.findByEmail("wasteless@seng302.com"));
    }
}
