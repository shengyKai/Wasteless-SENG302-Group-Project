package org.seng302.Controllers;

import java.text.ParseException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.Entities.Location;
import org.seng302.Entities.User;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@Component
public class DGAAController {

    private UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger(DGAAController.class.getName());

    @Autowired
    public DGAAController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                    .withPassword("T3amThr33IsTh3B3st")
                    .withDob("2021-03-11")
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
