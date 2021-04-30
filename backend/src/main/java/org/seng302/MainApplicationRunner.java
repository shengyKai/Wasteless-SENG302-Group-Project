package org.seng302;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.controllers.DGAAController;
import org.seng302.persistence.DGAARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * NOTE: Use this class to setup application
 * Avoid using Main.class
 */
@Component
public class MainApplicationRunner implements ApplicationRunner {

    private final DGAARepository dgaaRepository;
    private static final Logger logger = LogManager.getLogger(MainApplicationRunner.class.getName());

    @Autowired
    public MainApplicationRunner(DGAARepository dgaaRepository) {
        this.dgaaRepository = dgaaRepository;
    }


    /**
     * By overriding the run method, we tell Spring to run this code at startup. See
     * https://dzone.com/articles/spring-boot-applicationrunner-and-commandlinerunne
     */
    @Override
    public void run(ApplicationArguments args) {
        // Checks if DGAA present in DB and generates one if not
        DGAAController.checkDGAA(dgaaRepository);

        logger.info("Startup application with {}", args);
    }

}
