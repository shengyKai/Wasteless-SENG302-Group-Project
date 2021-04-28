package org.seng302;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.Controllers.DGAAController;
import org.seng302.Entities.*;
import org.seng302.Entities.Location;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.DGAARepository;
import org.seng302.Persistence.ProductRepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * NOTE: Use this class to setup application
 * Avoid using Main.class
 */
@Component
public class MainApplicationRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final DGAARepository dgaaRepository;
    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;
    private static final Logger logger = LogManager.getLogger(MainApplicationRunner.class.getName());

    @Autowired
    public MainApplicationRunner(UserRepository userRepository, DGAARepository dgaaRepository, BusinessRepository businessRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.dgaaRepository = dgaaRepository;
        this.businessRepository = businessRepository;
        this.productRepository = productRepository;
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
        
        business = businessRepository.save(business);

        Product product = new Product.Builder()
                .withProductCode("NathanApple-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(business)
                .build();
        productRepository.save(product);
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
