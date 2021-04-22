package org.seng302;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.Controllers.DGAAController;
import org.seng302.Entities.Business;
import org.seng302.Entities.Location;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NOTE: Use this class to setup application
 * Avoid using Main.class
 */
@Component
public class MainApplicationRunner implements ApplicationRunner {

    private UserRepository userRepository;
    private DGAARepository dgaaRepository;
    private BusinessRepository businessRepository;
    private static final Logger logger = LogManager.getLogger(MainApplicationRunner.class.getName());

    @Autowired
    public MainApplicationRunner(UserRepository userRepository, DGAARepository dgaaRepository, BusinessRepository businessRepository) {
        this.userRepository = userRepository;
        this.dgaaRepository = dgaaRepository;
        this.businessRepository = businessRepository;
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
            if (userRepository.findByEmail(user.getEmail()) == null) {
                userRepository.save(user);
            }
        }

        for (User user : userRepository.findAll() ) {
            logger.info(user.toString());
        }

        // Checks if DGAA present in DB and generates one if not

        DGAAController.checkDGAA(dgaaRepository);

        User user = userRepository.findByEmail("123andyelliot@gmail.com");
        Business business = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withDescription("DESCRIPTION")
                .withName("BUSINESS_NAME")
                .withAddress(new Location())
                .withPrimaryOwner(user)
                .build();

        Business testBusiness = businessRepository.save(business);
        //testBusiness.addAdmin(user);
        businessRepository.save(testBusiness);
        user = userRepository.findByEmail("123andyelliot@gmail.com");
    }

    /**
     * Reads users from a csv to create a list of user objects
     * @param filepath the directory of the user data csv
     * @return A list of user objects
     * @throws IOException
     */
    private List<User> readUserFile(String filepath) throws IOException, Exception {
        List<User> userList = new ArrayList<>();
        String row;
        BufferedReader csvReader = new BufferedReader(new FileReader(filepath));
        while ((row = csvReader.readLine()) != null) {
            logger.info(row);
            String[] userData = row.split("\\|");
            logger.info(java.util.Arrays.toString(userData));
            User user = new User.Builder().withFirstName(userData[0]).withMiddleName(userData[1]).withLastName(userData[2]).withNickName(userData[3])
                    .withEmail(userData[4]).withPassword(userData[5]).withAddress(Location.covertAddressStringToLocation(userData[6])).withDob(userData[7]).build();
            userList.add(user);
            logger.info("Successfully read user from file");
            logger.info(user);
        }
        csvReader.close();
        return userList;
    }

}
