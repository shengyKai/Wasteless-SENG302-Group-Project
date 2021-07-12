package org.seng302.datagenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.entities.User;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CommerceNameGeneratorTest {

    private CommerceNameGenerator commerceNameGenerator;
    @Mock
    private PersonNameGenerator mockPersonNameGenerator;
    @Mock
    private LocationGenerator mockLocationGenerator;
    @Mock
    private Random mockRandom;
    @Mock
    private User mockUser;
    @Mock
    private Location mockLocation;
    @Mock
    private Business mockBusiness;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        commerceNameGenerator = CommerceNameGenerator.getInstance();

        // Set the personNameGenerator object in businessNameGenerator to the mock
        Field personNameGeneratorField = commerceNameGenerator.getClass().getDeclaredField("personNameGenerator");
        personNameGeneratorField.setAccessible(true);
        personNameGeneratorField.set(commerceNameGenerator, mockPersonNameGenerator);

        // Set the locationGenerator object in businessNameGenerator to the mock
        Field locationGeneratorField = commerceNameGenerator.getClass().getDeclaredField("locationGenerator");
        locationGeneratorField.setAccessible(true);
        locationGeneratorField.set(commerceNameGenerator, mockLocationGenerator);

        // Set return values for mocks
        when(mockUser.getDob()).thenReturn(LocalDate.of(2000, 1, 1));
        when(mockBusiness.getAddress()).thenReturn(mockLocation);
        when(mockLocation.getCountry()).thenReturn("Country");
        when(mockPersonNameGenerator.randomLastName()).thenReturn("Lastname");
        when(mockLocationGenerator.randomStreetName()).thenReturn("Street Name");
    }

    void setRandomWithSeed(long seed) {
        try {
            Random random = new Random(seed);
            Field randomField = commerceNameGenerator.getClass().getDeclaredField("random");
            randomField.setAccessible(true);
            randomField.set(commerceNameGenerator, random);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {571, 459, 234, 376, 152, 850})
    void randomBusinessName_validBusinessName(int randomSeed) {
        setRandomWithSeed(randomSeed);

        Business.Builder builder = new Business.Builder()
                .withPrimaryOwner(mockUser)
                .withBusinessType("Retail Trade")
                .withAddress(mockLocation);

        String businessName = commerceNameGenerator.randomBusinessName();
        builder.withName(businessName);

        assertDoesNotThrow(() -> {
            Business business = builder.build();
            assertEquals(businessName, business.getName());
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {571, 459, 234, 376, 152, 850})
    void randomProductName_validProductName(int randomSeed) {
        setRandomWithSeed(randomSeed);

        Product.Builder builder = new Product.Builder()
                .withBusiness(mockBusiness)
                .withProductCode("PROD");

        String productName = commerceNameGenerator.randomProductName();
        builder.withName(productName);

        assertDoesNotThrow(() -> {
            Product product = builder.build();
            assertEquals(productName, product.getName());
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {571, 459, 234, 376, 152, 850})
    void randomManufacturerName_validManufacturerName(int randomSeed) {
        setRandomWithSeed(randomSeed);

        Product.Builder builder = new Product.Builder()
                .withBusiness(mockBusiness)
                .withName("Product")
                .withProductCode("PROD");

        String manufacturerName = commerceNameGenerator.randomManufacturerName();
        builder.withManufacturer(manufacturerName);

        assertDoesNotThrow(() -> {
            Product product = builder.build();
            assertEquals(manufacturerName, product.getManufacturer());
        });
    }


}