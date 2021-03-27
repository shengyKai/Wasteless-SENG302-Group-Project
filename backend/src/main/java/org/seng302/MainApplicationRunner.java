package org.seng302;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.Controllers.DGAAController;
import org.seng302.Entities.Location;
import org.seng302.Entities.User;
import org.seng302.Persistence.DGAARepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * NOTE: Use this class to setup application
 * Avoid using Main.class
 */
@Component
public class MainApplicationRunner implements ApplicationRunner {

    private UserRepository _userRepository;
    private DGAARepository _dgaaRepository;
    private static final Logger logger = LogManager.getLogger(MainApplicationRunner.class.getName());

    @Autowired
    public MainApplicationRunner(UserRepository userRepository, DGAARepository dgaaRepository) {
        this._userRepository = userRepository;
        this._dgaaRepository = dgaaRepository;
    }


    /**
     * By overriding the run method, we tell Spring to run this code at startup. See
     * https://dzone.com/articles/spring-boot-applicationrunner-and-commandlinerunne
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Startup application with {}", args);
        List<User> demoUsers = readUserFile("src//main//DemoData.txt");
        for (User user : demoUsers) {
            if (_userRepository.findByEmail(user.getEmail()) == null) {
                _userRepository.save(user);
            }
        }

        for (User user : _userRepository.findAll() ) {
            logger.info(user.toString());
        }

        // Checks if DGAA present in DB and generates one if not

        DGAAController.checkDGAA(_dgaaRepository);

    }

    private List<User> readUserFile(String filepath) throws IOException {
        List<User> userList = new ArrayList<>();
        String row;
        BufferedReader csvReader = new BufferedReader(new FileReader(filepath));
        while ((row = csvReader.readLine()) != null) {
            try {
                String[] userData = row.split(";");
                User user = new User.Builder().withFirstName(userData[0]).withMiddleName(userData[1]).withLastName(userData[2]).withNickName(userData[3])
                        .withEmail(userData[4]).withPassword(userData[5]).withAddress(Location.covertAddressStringToLocation(userData[6])).withDob(userData[7]).build();
                userList.add(user);
            } catch (Exception e) {

            }
        }
        csvReader.close();
        return userList;
    }

}
