package cucumber.stepDefinitions;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.seng302.entities.*;
import org.seng302.persistence.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProductStepDefinition {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductRepository productRepository;

    public ProductStepDefinition() throws ParseException {
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    //Set up for all variables in Product initialised with valid data
    private Product product;
    private String productCode = "ABCD";
    private String name = "Exploding Pineapples";
    private String description = "Monkeys in planes keep dropping them";
    private String manufacturer = "NinjaKiwi";
    private BigDecimal recommendedRetailPrice = BigDecimal.valueOf(400);
    private Date created = dateFormat.parse("2001-03-12");
    private User owner = new User.Builder()
            .withFirstName("Bob")
            .withLastName("Rich")
            .withEmail("Bobsellsthings@gmail.com")
            .withDob("1863-05-05")
            .withAddress(Location.covertAddressStringToLocation("1,Bob Street,Bob,Bob,Bob,Bob,1010"))
            .withPassword("ThisIsVerySecure69")
            .build();
    private Business business = new Business.Builder()
            .withName("Biz")
            .withDescription("Sells stuff")
            .withPrimaryOwner(owner)
            .build();
    private Image productImage;
    private Long prod_id;

    @After
    public void Setup() {
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        productRepository.deleteAll();
    }
}
