package org.seng302.datagenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;

import java.lang.reflect.Field;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class PersonNameGeneratorTest {

    private PersonNameGenerator personNameGenerator;
    private PersonNameGenerator.FullName fullName;
    private User.Builder builder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        personNameGenerator = PersonNameGenerator.getInstance();

        // Set up a user builder with valid values for each mandatory field
        builder = new User.Builder()
                .withFirstName("First name")
                .withLastName("Last name")
                .withDob("2000-01-01")
                .withEmail("test@test")
                .withPassword("abcd1234")
                .withAddress(Location.covertAddressStringToLocation("1,Street,District,City,Country," +
                        "Country,1234"));
    }

    /**
     * Use reflection to set the random object in the personNameGenerator class to a random object with the given seed,
     * so that the tests will be deterministic.
     * @param seed The seed for the random object of personNameGenerator.
     */
    void setRandomWithSeed(long seed) {
        try {
            Random random = new Random(seed);
            Field randomField = personNameGenerator.getClass().getDeclaredField("random");
            randomField.setAccessible(true);
            randomField.set(personNameGenerator, random);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {571, 459, 234, 376, 152, 850})
    void generateName_firstNameIsValid(int randomSeed) {
        setRandomWithSeed(randomSeed);

        fullName = personNameGenerator.generateName();
        builder.withFirstName(fullName.getFirstName());
        User user = assertDoesNotThrow(builder::build);
        assertEquals(fullName.getFirstName(), user.getFirstName());
    }

    @ParameterizedTest
    @ValueSource(ints = {571, 459, 234, 376, 152, 850})
    void generateName_middleNameIsValid(int randomSeed) {
        setRandomWithSeed(randomSeed);

        fullName = personNameGenerator.generateName();
        builder.withMiddleName(fullName.getMiddleName());
        User user = assertDoesNotThrow(builder::build);
        assertEquals(fullName.getMiddleName(), user.getMiddleName());
    }

    @ParameterizedTest
    @ValueSource(ints = {571, 459, 234, 376, 152, 850})
    void generateName_lastNameIsValid(int randomSeed) {
        setRandomWithSeed(randomSeed);

        fullName = personNameGenerator.generateName();
        builder.withLastName(fullName.getLastName());
        User user = assertDoesNotThrow(builder::build);
        assertEquals(fullName.getLastName(), user.getLastName());
    }

    @ParameterizedTest
    @ValueSource(ints = {571, 459, 234, 376, 152, 850})
    void generateName_nicknameIsValid(int randomSeed) {
        setRandomWithSeed(randomSeed);

        fullName = personNameGenerator.generateName();
        builder.withNickName(fullName.getNickname());
        User user = assertDoesNotThrow(builder::build);
        assertEquals(fullName.getNickname(), user.getNickname());
    }

    @ParameterizedTest
    @ValueSource(ints = {571, 459, 234, 376, 152, 850})
    void randomLastName_lastNameIsValid(int randomSeed) {
        setRandomWithSeed(randomSeed);

        String lastName = personNameGenerator.randomLastName();
        builder.withLastName(lastName);
        User user = assertDoesNotThrow(builder::build);
        assertEquals(lastName, user.getLastName());
    }
}