package org.seng302;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.controllers.DGAAController;
import org.seng302.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * NOTE: Use this class to setup application
 * Avoid using Main.class
 */
@Component
public class MainApplicationRunner implements ApplicationRunner {

    @Resource
    private StorageService storageService;

    private DGAAController dgaaController;
    private static final Logger logger = LogManager.getLogger(MainApplicationRunner.class.getName());

    @Autowired
    public MainApplicationRunner(DGAAController dgaaController) {
        this.dgaaController = dgaaController;
    }


    /**
     * By overriding the run method, we tell Spring to run this code at startup. See
     * https://dzone.com/articles/spring-boot-applicationrunner-and-commandlinerunne
     */
    @Override
    public void run(ApplicationArguments args) {
        logger.info("Startup application with {}", args);
        storageService.init();
    }

}
