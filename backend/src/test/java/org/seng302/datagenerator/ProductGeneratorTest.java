package org.seng302.datagenerator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.seng302.leftovers.Main;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
class ProductGeneratorTest {
    private Connection conn;
    private UserGenerator userGenerator;
    private BusinessGenerator businessGenerator;
    private ProductGenerator productGenerator;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setup() throws SQLException {
        Map<String, String> properties = ExampleDataFileReader.readPropertiesFile("/application.properties");
        if (properties.get("spring.datasource.url") == null || properties.get("spring.datasource.username") == null || properties.get("spring.datasource.password") == null) {
            fail("The url/username/password is not found");
        }
        this.conn =  DriverManager.getConnection(properties.get("spring.datasource.url"), properties.get("spring.datasource.username"), properties.get("spring.datasource.password"));

        //Creates Generators
        this.userGenerator = new UserGenerator(conn);
        this.businessGenerator = new BusinessGenerator(conn);
        this.productGenerator = new ProductGenerator(conn);
    }

    @AfterEach
    public void teardown() throws SQLException {
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
        conn.close();
    }

    /**
     * Checks that the required fields within the product table are not null by querying the database
     * @param productId the id of the product
     */
    public void checkRequiredFieldsNotNull(long productId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM product WHERE id = ? AND " +
                        "country_of_sale IS NOT NULL AND created IS NOT NULL AND description IS NOT NULL AND " +
                        "manufacturer IS NOT NULL AND name IS NOT NULL AND product_code IS NOT NULL AND " +
                        "recommended_retail_price IS NOT NULL AND business_id IS NOT NULL"
        );
        stmt.setObject(1, productId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        assertEquals(1, results.getLong(1));
    }

    /**
     * Finds out the number of products within the database
     * @return the number of products within the database
     */
    public long getNumProductsInDB() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM product");
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        return results.getLong(1);
    }

    /**
     * Generates a specified number of users and businesses using the generators and returns the ids of the
     * generated businesses.
     * @param userCount the number of users to be generated
     * @param businessCount the number of businesses to be generated
     * @return the ids of the generated businesses
     */
    public List<Long> generateUserAndBusiness(int userCount, int businessCount) {
        List<Long> userIds = userGenerator.generateUsers(userCount);
        return businessGenerator.generateBusinesses(userIds, businessCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 10, 100})
    void generateProducts_generateSomeProducts_correctNumberOfValidProductsGenerated(int count) throws SQLException {
        List<Long> businessIds = generateUserAndBusiness(1, 1);
        List<Long> productIds = productGenerator.generateProducts(businessIds, count);
        assertEquals(count, productIds.size());
        for (Long productId : productIds) {
            checkRequiredFieldsNotNull(productId);
        }
    }

    @Test
    void generateProducts_generateZeroProducts_noProductGenerated() throws SQLException {
        long productsInDB = getNumProductsInDB();
        List<Long> businessIds = generateUserAndBusiness(1, 1);
        productGenerator.generateProducts(businessIds, 0);
        long productsInDBAfter = getNumProductsInDB();
        assertEquals(productsInDB, productsInDBAfter);
    }

    @Test
    void generateProducts_generateNegativeOneProducts_noProductGenerated() throws SQLException {
        long productsInDB = getNumProductsInDB();
        List<Long> businessIds = generateUserAndBusiness(1, 1);
        productGenerator.generateProducts(businessIds, -1);
        long productsInDBAfter = getNumProductsInDB();
        assertEquals(productsInDB, productsInDBAfter);
    }

    @Test
    void generateProducts_generateNegativeTenProducts_noProductGenerated() throws SQLException {
        long productsInDB = getNumProductsInDB();
        List<Long> businessIds = generateUserAndBusiness(1, 1);
        productGenerator.generateProducts(businessIds, -10);
        long productsInDBAfter = getNumProductsInDB();
        assertEquals(productsInDB, productsInDBAfter);
    }

    // Checking the generated product code is valid
    @Test
    void generateProducts_generateFiveHundredProductsCheckProductCodes_productCodesValid() throws SQLException {
        List<Long> businessIds = generateUserAndBusiness(1, 1);
        Business business = mock(Business.class);
        Location location = mock(Location.class);
        when(business.getAddress()).thenReturn(location);
        when(location.getCountry()).thenReturn("EllaLand");
        List<Long> productIds = productGenerator.generateProducts(businessIds, 500);
        for (Long productId: productIds) {
            Product product = productRepository.findById(productId).get();
            var newProduct = new Product.Builder()
                    .withProductCode(product.getProductCode())
                    .withName("Fresh Orange")
                    .withDescription("This is a fresh orange")
                    .withManufacturer("Apple")
                    .withRecommendedRetailPrice("2.01")
                    .withBusiness(business);
            assertDoesNotThrow(newProduct::build, product.getProductCode());
        }
    }
}
