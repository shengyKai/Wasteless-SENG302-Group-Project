package org.seng302.leftovers.persistence;

import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BoughtSaleItemRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BoughtSaleItemRepository boughtSaleItemRepository;

    private User user;
    private Business business;
    private Product product;

    @Mock
    private SaleItem saleItem;

    private BoughtSaleItem boughtSaleItem;

    @BeforeAll
    void init() {
        boughtSaleItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setup() {
        user = new User.Builder()
                .withFirstName("Fergus")
                .withMiddleName("Connor")
                .withLastName("Hitchcock")
                .withNickName("Ferg")
                .withEmail("fergus.hitchcock@gmail.com")
                .withPassword("IDoLikeBreaks69#H3!p")
                .withBio("Did you know I had a second last name Yarker")
                .withDob("1999-07-17")
                .withPhoneNumber("64 273702682")
                .withAddress(Location.covertAddressStringToLocation("6,Help Street,Place,Dunedin,New Zelaand,Otago,6959"))
                .build();
        user = userRepository.save(user);

        business = new Business.Builder()
                .withPrimaryOwner(user)
                .withName("Help Industries")
                .withAddress(Location.covertAddressStringToLocation("6,Help Street,Place,Dunedin,New Zelaand,Otago,6959"))
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withDescription("Helps industries hopefully")
                .build();
        business = businessRepository.save(business);

        product = new Product.Builder()
                .withBusiness(business)
                .withProductCode("PIECEOFFISH69")
                .withName("A Piece of Fish")
                .withDescription("A fish but only a piece of it remains")
                .withManufacturer("Tokyo Fishing LTD")
                .withRecommendedRetailPrice("3.20")
                .build();
        product = productRepository.save(product);

        when(saleItem.getId()).thenReturn(3L);
        when(saleItem.getProduct()).thenReturn(product);
        when(saleItem.getPrice()).thenReturn(new BigDecimal("100"));
        when(saleItem.getQuantity()).thenReturn(6);
        when(saleItem.getCreated()).thenReturn(Instant.parse("2021-09-14T03:07:30.713Z"));
        when(saleItem.getLikeCount()).thenReturn(5);

        boughtSaleItem = new BoughtSaleItem(saleItem, user);
        boughtSaleItem = boughtSaleItemRepository.save(boughtSaleItem);
    }

    @AfterEach
    void tearDown() {
        boughtSaleItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void findById_withValidId_boughtSaleItemReturned() {
        var found = boughtSaleItemRepository.findById(boughtSaleItem.getId()).orElseThrow();
        assertEquals(boughtSaleItem.getId(), found.getId());
        assertEquals(boughtSaleItem.getInterestCount(), found.getInterestCount());
        assertEquals(boughtSaleItem.getProduct().getID(), found.getProduct().getID());
        assertEquals(boughtSaleItem.getBuyer().getUserID(), found.getBuyer().getUserID());
    }

    @Test
    void findById_withInvalidId_noSaleItemReturned() {
        var found = boughtSaleItemRepository.findById(boughtSaleItem.getId() + 1L);
        assertTrue(found.isEmpty());
    }
}
