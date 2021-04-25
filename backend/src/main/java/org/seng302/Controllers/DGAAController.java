package org.seng302.Controllers;

import org.seng302.Entities.DefaultGlobalApplicationAdmin;
import org.seng302.Persistence.DGAARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@Component
public class DGAAController {

    private DGAARepository dgaaRepository;

    @Autowired
    public DGAAController(DGAARepository dgaaRepository) {
        this.dgaaRepository = dgaaRepository;
    }

    /**
     * Checks if the DGAA is in the database in intervals and creates one if it hasn't. The checking interval is set for 
     * every 30 minutes (1800 milliseconds)
     */
    @Scheduled(fixedRate = 1800000)
    public void checkDGAA() {
        DefaultGlobalApplicationAdmin dgaa = this.dgaaRepository.findByEmail("wasteless@seng302.com");
        // Default Global Application Admin doesn't exist or has been tampered with
        if (dgaa == null || !dgaa.isIsDGAA()) {
            DefaultGlobalApplicationAdmin admin = new DefaultGlobalApplicationAdmin();
            dgaaRepository.save(admin);
        }
    }
}
