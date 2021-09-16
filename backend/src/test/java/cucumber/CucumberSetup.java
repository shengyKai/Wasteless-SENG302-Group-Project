package cucumber;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.seng302.leftovers.persistence.*;
import org.seng302.leftovers.persistence.event.EventRepository;
import org.seng302.leftovers.persistence.event.ExpiryEventRepository;
import org.seng302.leftovers.persistence.event.InterestEventRepository;
import org.seng302.leftovers.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CucumberSetup {
    @Value("${storage-directory}")
    private Path root;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SaleItemRepository saleItemRepository;
    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;
    @Autowired
    private KeywordRepository keywordRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ExpiryEventRepository expiryEventRepository;
    @Autowired
    private InterestEventRepository interestEventRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private StorageService storageService;
    @Autowired
    private BoughtSaleItemRepository boughtSaleItemRepository;

    /**
     * Set up the mockMvc object for mocking API requests, and remove everything from the repositories.
     */
    @Before(order = 1)
    public void Setup() throws IOException {
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        clearDatabase();
        Files.createDirectory(root);
    }
    
    @After
    public void tearDown() {
        clearDatabase();
    }

    /**
     * Delete all entities from all repositories in the database.
     */
    private void clearDatabase() {
        interestEventRepository.deleteAll();
        saleItemRepository.deleteAll();
        boughtSaleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        eventRepository.deleteAll();
        expiryEventRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        keywordRepository.deleteAll();
        userRepository.deleteAll();
        accountRepository.deleteAll();
        imageRepository.deleteAll();
        storageService.deleteAll();
    }

}
