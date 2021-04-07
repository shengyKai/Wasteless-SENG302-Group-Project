package org.seng302;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.Controllers.DGAAController;
import org.seng302.Entities.Business;
import org.seng302.Entities.Location;
import org.seng302.Entities.User;
import org.seng302.Persistence.BusinessRepository;
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
import java.util.stream.Collectors;

/**
 * NOTE: Use this class to setup application
 * Avoid using Main.class
 */
@Component
public class MainApplicationRunner implements ApplicationRunner {

    private UserRepository _userRepository;
    private DGAARepository _dgaaRepository;
    private BusinessRepository _businessRepository;
    private static final Logger logger = LogManager.getLogger(MainApplicationRunner.class.getName());

    @Autowired
    public MainApplicationRunner(UserRepository userRepository, DGAARepository dgaaRepository, BusinessRepository businessRepository) {
        this._userRepository = userRepository;
        this._dgaaRepository = dgaaRepository;
        this._businessRepository = businessRepository;
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

        User user = _userRepository.findByEmail("123andyelliot@gmail.com");
        Business business = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withDescription("DESCRIPTION")
                .withName("BUSINESS_NAME")
                .withAddress(new Location())
                .withPrimaryOwner(user)
                .build();

        Business testBusiness = _businessRepository.save(business);
        testBusiness.addAdmin(user);
        _businessRepository.save(testBusiness);
        user = _userRepository.findByEmail("123andyelliot@gmail.com");
    }

    private List<User> readUserFile(String filepath) throws IOException {
        List<User> userList = new ArrayList<>();
        String row;
        BufferedReader csvReader = new BufferedReader(new FileReader(filepath));
        while ((row = csvReader.readLine()) != null) {
            try {
                String[] userData = row.split(";");
                User user = new User.Builder().withFirstName(userData[0]).withMiddleName(userData[1]).withLastName(userData[2]).withNickName(userData[3])
                        .withEmail(userData[4]).withPassword(userData[5]).withAddress(userData[6]).withDob(userData[7]).build();
                userList.add(user);
            } catch (Exception e) {

            }
        }


        csvReader.close();
        return userList;
    }

}
