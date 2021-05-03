package org.seng302.controllers;

import org.seng302.Entities.DefaultGlobalApplicationAdmin;
import org.seng302.Persistence.DGAARepository;
import org.springframework.web.bind.annotation.RestController;

public class DGAAController {

    /**
     * Checks if the DGAA is in the database and creates one if it hasn't
     */
    public static void checkDGAA(DGAARepository dgaaRepository) {
        DefaultGlobalApplicationAdmin dgaa = dgaaRepository.findByEmail("wasteless@seng302.com");
        // Default Global Application Admin doesn't exist or has been tampered with
        if (dgaa == null || !dgaa.isIsDGAA()) {
            DefaultGlobalApplicationAdmin admin = new DefaultGlobalApplicationAdmin();
            dgaaRepository.save(admin);
        }
    }
}
