package org.seng302.datagenerator;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.springframework.data.repository.query.Param;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class PersonNameGeneratorTest {

    private PersonNameGenerator personNameGenerator;
    private PersonNameGenerator.FullName fullName;
    private User.Builder builder;
    @Mock
    private Random mockRandom;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        personNameGenerator = PersonNameGenerator.getInstance();

        // Set the random object in personNameGenerator to the mock
        Field randomField = personNameGenerator.getClass().getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(personNameGenerator, mockRandom);

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
     * Mock the random integers generated by the personNameGenerator's random object to return
     * a set list of integers. This ensures that the tests are deterministic. An integer from
     * the list will be returned each time mockRandom.nextInt() is called, in sequential order.
     * @param values The integers to be returned by mockRandom.nextInt() each time it is called.
     */
    private void setNextIntReturnValues(int[] values) {
        when(mockRandom.nextInt(any(Integer.class))).thenAnswer(new Answer<Integer>() {
            private int count = 0;
            public Integer answer(InvocationOnMock invocation) {
                count++;
                int upperLimit = invocation.getArgument(0);
                return values[count-1] % upperLimit;
            }
        });
    }

    /**
     * Mock the random booleans generated by the personNameGenerator's random object to return
     * a set list of booleans. This ensures that the tests are deterministic. A boolean from
     * the list will be returned each time mockRandom.nextBoolean() is called, in sequential order.
     * @param values The booleans to be returned by mockRandom.nextBoolean() each time it is called.
     */
    private void setNextBooleanReturnValues(boolean[] values) {
        when(mockRandom.nextBoolean()).thenAnswer(new Answer<Boolean>() {
            private int count = 0;
            public Boolean answer(InvocationOnMock invocation) {
                count++;
                return values[count-1];
            }
        });
    }

    /**
     * Return arrays of integer and boolean values to be used for setting the return values for mockRandom
     * to make the tests deterministic. The first argument is an array of two booleans, because nextBoolean()
     * will be called at most twice in the generateName() method. The second argument is an array of six
     * integers, because nextInt() will be called at most six times in the generateName() method.
     * @return Stream of arguments, where each set of arguments consists of an array of two boolean and an
     * array of 6 numbers between 0 and 1000.
     */
    private static Stream<Arguments> mockRandomReturnValues() {
        return Stream.of(
                Arguments.of(new boolean[] {true, true}, new int[] {571, 459, 234, 376, 152, 850}),
                Arguments.of(new boolean[] {false, true}, new int[] {832, 351, 88, 344, 13, 78}),
                Arguments.of(new boolean[] {true, false}, new int[] {192, 640, 171, 86, 716, 890}),
                Arguments.of(new boolean[] {false, false}, new int[] {161, 639, 776, 844, 145, 655}));
    }

    @ParameterizedTest
    @MethodSource("mockRandomReturnValues")
    void generateName_firstNameIsValid(boolean[] booleanReturnValues, int[] intReturnValues) {
        setNextBooleanReturnValues(booleanReturnValues);
        setNextIntReturnValues(intReturnValues);

        fullName = personNameGenerator.generateName();
        builder.withFirstName(fullName.getFirstName());
        assertDoesNotThrow(() -> {
            User user = builder.build();
            assertEquals(fullName.getFirstName(), user.getFirstName());
        });
    }

    @ParameterizedTest
    @MethodSource("mockRandomReturnValues")
    void generateName_middleNameIsValid(boolean[] booleanReturnValues, int[] intReturnValues) {
        setNextBooleanReturnValues(booleanReturnValues);
        setNextIntReturnValues(intReturnValues);

        fullName = personNameGenerator.generateName();
        builder.withMiddleName(fullName.getMiddleName());
        assertDoesNotThrow(() -> {
            User user = builder.build();
            assertEquals(fullName.getMiddleName(), user.getMiddleName());
        });
    }

    @ParameterizedTest
    @MethodSource("mockRandomReturnValues")
    void generateName_lastNameIsValid(boolean[] booleanReturnValues, int[] intReturnValues) {
        setNextBooleanReturnValues(booleanReturnValues);
        setNextIntReturnValues(intReturnValues);

        fullName = personNameGenerator.generateName();
        builder.withLastName(fullName.getLastName());
        assertDoesNotThrow(() -> {
            User user = builder.build();
            assertEquals(fullName.getLastName(), user.getLastName());
        });
    }

    @ParameterizedTest
    @MethodSource("mockRandomReturnValues")
    void generateName_nicknameIsValid(boolean[] booleanReturnValues, int[] intReturnValues) {
        setNextBooleanReturnValues(booleanReturnValues);
        setNextIntReturnValues(intReturnValues);

        fullName = personNameGenerator.generateName();
        builder.withNickName(fullName.getNickname());
        assertDoesNotThrow(() -> {
            User user = builder.build();
            assertEquals(fullName.getNickname(), user.getNickname());
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {571, 459, 234, 376, 152, 850})
    void randomLastName_lastNameIsValid(int returnValue) {
        when(mockRandom.nextInt(any(Integer.class))).thenReturn(returnValue);

        String lastName = personNameGenerator.randomLastName();
        System.out.println(lastName);
        builder.withLastName(lastName);
        assertDoesNotThrow(() -> {
            User user = builder.build();
            assertEquals(lastName, user.getLastName());
        });
    }
}