package org.seng302.datagenerator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.seng302.leftovers.Main;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
public class ProductImageGeneratorTest {

    private static final String PRODUCT_NOUNS_FILE = "product-nouns.txt";
    private static final String PRODUCT_ADJECTIVES_FILE = "product-adjectives.txt";

    private Connection conn;
    // LocationGenerator is called by UserGenerator or BusinessGenerator, so it has to be called with either of them
    private UserGenerator userGenerator;
    private BusinessGenerator businessGenerator;
    private ProductGenerator productGenerator;
    private ProductImageGenerator productImageGenerator;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ImageRepository imageRepository;

    private List<String> productNouns = ExampleDataFileReader.readExampleDataFile(PRODUCT_NOUNS_FILE);
    private List<String> productAdjectives = ExampleDataFileReader.readExampleDataFile(PRODUCT_ADJECTIVES_FILE);


    @BeforeEach
    public void setup() throws SQLException {
        Map<String, String> properties = ExampleDataFileReader.readPropertiesFile("/application.properties");
        if (properties.get("spring.datasource.url") == null || properties.get("spring.datasource.username") == null || properties.get("spring.datasource.password") == null) {
            fail("The url/username/password is not found");
        }
        this.conn =  DriverManager.getConnection(properties.get("spring.datasource.url"), properties.get("spring.datasource.username"), properties.get("spring.datasource.password"));

        //Creates generators
        this.userGenerator = new UserGenerator(conn);

    }

    @AfterEach
    public void teardown() throws SQLException {
        userRepository.deleteAll();
        conn.close();
    }


}
