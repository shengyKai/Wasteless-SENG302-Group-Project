package org.seng302.datagenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.datagenerator.utils.RandomTestUtils;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.entities.User;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Random;
import java.util.stream.Stream;

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


        // Set the random object in businessNameGenerator to the mock
        Field randomField = commerceNameGenerator.getClass().getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(commerceNameGenerator, mockRandom);

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

    /**
     * Return arrays of integers to be used to set the return values for mockRandom.nextInt() in order to make the tests
     * deterministic.
     * @return A stream of arguments, where each argument is an array of two integers.
     */
    private static Stream<Arguments> mockRandomReturnValues() {
        return Stream.of(
                Arguments.of((Object) new int[] {571,459}),
                Arguments.of((Object) new int[] {234,376}),
                Arguments.of((Object) new int[] {152,850}),
                Arguments.of((Object) new int[] {832,351}),
                Arguments.of((Object) new int[] {661,951}),
                Arguments.of((Object) new int[] {783, 755}));
    }

    @ParameterizedTest
    @MethodSource("mockRandomReturnValues")
    void randomBusinessName_validBusinessName(int[] values) {
        Business.Builder builder = new Business.Builder()
                .withPrimaryOwner(mockUser)
                .withBusinessType("Retail Trade")
                .withAddress(mockLocation);

        RandomTestUtils.setNextIntReturnValues(mockRandom, values);
        String businessName = commerceNameGenerator.randomBusinessName();
        builder.withName(businessName);

        assertDoesNotThrow(() -> {
            Business business = builder.build();
            assertEquals(businessName, business.getName());
        });
    }

    @ParameterizedTest
    @MethodSource("mockRandomReturnValues")
    void randomProductName_validProductName(int[] values) {
        Product.Builder builder = new Product.Builder()
                .withBusiness(mockBusiness)
                .withProductCode("PROD");

        RandomTestUtils.setNextIntReturnValues(mockRandom, values);
        String productName = commerceNameGenerator.randomProductName();
        builder.withName(productName);

        assertDoesNotThrow(() -> {
            Product product = builder.build();
            assertEquals(productName, product.getName());
        });
    }

    @ParameterizedTest
    @MethodSource("mockRandomReturnValues")
    void randomManufacturerName_validManufacturerName(int[] values) {
        Product.Builder builder = new Product.Builder()
                .withBusiness(mockBusiness)
                .withName("Product")
                .withProductCode("PROD");

        RandomTestUtils.setNextIntReturnValues(mockRandom, values);
        String manufacturerName = commerceNameGenerator.randomManufacturerName();
        builder.withManufacturer(manufacturerName);

        assertDoesNotThrow(() -> {
            Product product = builder.build();
            assertEquals(manufacturerName, product.getManufacturer());
        });
    }


}