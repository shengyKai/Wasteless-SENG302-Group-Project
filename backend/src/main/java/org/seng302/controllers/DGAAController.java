package org.seng302.controllers;

import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Location;
import org.seng302.entities.User;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class DGAAController {

    private UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger(DGAAController.class.getName());

    @Autowired
    public DGAAController(UserRepository userRepository) {
        this.userRepository = userRepository;
        checkDGAA();
    }

    /**
     * Checks if the DGAA is in the database in intervals and creates one if it hasn't. The checking interval is set for 
     * every 30 minutes (1800 milliseconds)
     */
    @Scheduled(fixedRate = 1800000)
    public void checkDGAA() {
        User dgaa = this.userRepository.findByEmail("wasteless@seng302.com");
        
        if (dgaa != null && !dgaa.isIsDGAA()) {
            userRepository.delete(dgaa);
            dgaa = null;
        }

        // Default Global Application Admin doesn't exist or has been tampered with
        if (dgaa == null) {
            Location dgaaAddress;
            try {
                dgaaAddress = new Location.Builder()
                            .atStreetNumber("1")
                            .onStreet("wasteless")
                            .inCity("wasteless")
                            .inRegion("wasteless")
                            .inCountry("wasteless")
                            .withPostCode("1111")
                            .build();
                dgaa = new User.Builder()
                    .withEmail("wasteless@seng302.com")
                    .withFirstName("DGAA")
                    .withLastName("DGAA")
                    .withPassword("T3amThr33IsTh3")
                    .withDob("2000-03-11")
                    .withAddress(dgaaAddress)
                    .build();
                dgaa.setRole("defaultGlobalApplicationAdmin");

                userRepository.save(dgaa);
            } catch (ParseException e) {
                logger.error(e);
            }
        }
    }
}
