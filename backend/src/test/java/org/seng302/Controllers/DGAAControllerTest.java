package org.seng302.Controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.Entities.Location;
import org.seng302.Entities.User;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DGAAControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private DGAAController dgaaController;

    @BeforeEach
    public void clean() {
        //because business repo has a foreign key in user repo, it needs to be cleared too
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * DGAA repo is empty, ensure a new DGAA is generated
     */
    @Test @Ignore
    public void dgaaNotPresent() {
        dgaaController.checkDGAA();
        assert(userRepository.findByEmail("wasteless@seng302.com") != null);
    }

    /**
     * DGAA already exists, no need for new one
     * @throws ParseException
     */
    @Test @Ignore
    public void dgaaPresent() throws ParseException {
        dgaaController.checkDGAA();
        User dgaa;
        Location adminAddress;
        adminAddress = new Location.Builder()
                    .atStreetNumber("1")
                    .onStreet("wasteless")
                    .inCity("wasteless")
                    .inRegion("wasteless")
                    .inCountry("wasteless")
                    .withPostCode("1111")
                    .build();
        dgaa = new User.Builder()
            .withEmail("wasteless2@seng302.com")
            .withFirstName("DGAA")
            .withLastName("DGAA")
            .withPassword("T3amThr33IsTh3B3st")
            .withDob("2021-03-11")
            .withAddress(adminAddress)
            .build();
        try {
            dgaa.setRole("defaultGlobalApplicationAdmin");
            userRepository.save(dgaa);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Tried creating new DGAA");
        }

    }
}
