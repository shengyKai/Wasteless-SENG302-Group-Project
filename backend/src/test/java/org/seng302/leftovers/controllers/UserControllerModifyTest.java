package org.seng302.leftovers.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.dto.LocationDTO;
import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.exceptions.AccessTokenResponseException;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.seng302.leftovers.exceptions.InsufficientPermissionResponseException;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.PasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerModifyTest {
    private long mockUserId = 5L;
    private String validCurrentPassword = "HappyBoiGeorge69#";
    private String takenNewEmail = "This@gmail.com";
    
    private MockMvc mockMvc;

    private UserController userController;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ImageRepository imageRepository;

    @Mock
    private User mockUser;
    @Mock
    private Location mockLocation;

    @Autowired
    private ObjectMapper objectMapper;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(mockUserId))).thenReturn(true);

        when(userRepository.getUser(mockUserId)).thenReturn(mockUser);
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

        when(mockUser.getUserID()).thenReturn(mockUserId);
        when(mockUser.getBio()).thenReturn("Some bio");
        when(mockUser.getEmail()).thenReturn("Ella@gmail.com");
        when(mockUser.getFirstName()).thenReturn("Ella");
        when(mockUser.getLastName()).thenReturn("Ella");
        when(mockUser.getMiddleName()).thenReturn("bananas");
        when(mockUser.getNickname()).thenReturn("cool gal");
        when(mockUser.getDob()).thenReturn(LocalDate.parse("1999-06-26"));
        when(mockUser.getAuthenticationCode()).thenReturn(
                PasswordAuthenticator.generateAuthenticationCode(validCurrentPassword));
        when(mockUser.getAddress()).thenReturn(mockLocation);

        userController = new UserController(userRepository, imageRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @AfterEach
    public void tearDown() { authenticationTokenManager.close(); }

    @SneakyThrows
    private JSONObject createValidRequest() {
        var jsonBody = new JSONObject();
        Location address = new Location.Builder()
                .inCountry("Spain")
                .inCity("Christchurch")
                .inRegion("Region")
                .atStreetNumber("24")
                .onStreet("Cool street")
                .withPostCode("1238")
                .atDistrict("DistrictArea")
                .build();
        jsonBody.put("email", "Ella@gmail.com");
        jsonBody.put("firstName", "Ella");
        jsonBody.put("lastName", "Ella");
        jsonBody.put("dateOfBirth", "1999-06-26");
        jsonBody.put("homeAddress", new LocationDTO(address, true));
        jsonBody.put("imageIds", new JSONArray());
        return jsonBody;
    }

    @Test
    void modifyUser_modifyWithValidFields_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();
        Location address = new Location.Builder()
                .inCountry("Australia")
                .inCity("Brisbane")
                .inRegion("Ozzy")
                .atStreetNumber("62")
                .onStreet("Walnut street")
                .withPostCode("9876")
                .atDistrict("Outback")
                .build();
        LocationDTO jsonAddress = new LocationDTO(address, true);
        jsonBody.put("homeAddress", jsonAddress);
        String newFirstName = "Nathan";
        jsonBody.put("firstName", newFirstName);
        String newLastName = "John";
        jsonBody.put("lastName", newLastName);
        String newMiddleName = "Johnson";
        jsonBody.put("middleName", newMiddleName);
        String newNickname = "Johnson";
        jsonBody.put("nickname", newNickname);
        String newBio = "hello --  welcome to my page";
        jsonBody.put("bio", newBio);
        String newDateOfBirth = "1999-07-06";
        jsonBody.put("dateOfBirth", newDateOfBirth);
        String newPhoneNumber = "+64 3 555 0129";
        jsonBody.put("phoneNumber", newPhoneNumber);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).getUser(mockUserId);
        verify(userRepository, times(1)).save(mockUser);
        verify(mockUser, times(1)).setAddress(any());
        verify(mockUser, times(1)).setFirstName(newFirstName);
        verify(mockUser, times(1)).setLastName(newLastName);
        verify(mockUser, times(1)).setMiddleName(newMiddleName);
        verify(mockUser, times(1)).setNickname(newNickname);
        verify(mockUser, times(1)).setBio(newBio);
        verify(mockUser, times(1)).setDob(LocalDate.parse(newDateOfBirth));
        verify(mockUser, times(1)).setPhNum(newPhoneNumber);
    }

    @Test
    void modifyUser_modifyWithValidEmailAndPasswords_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newEmail = "Nathan@gmail.com";
        String currentPassword = validCurrentPassword;
        String newPassword = "HmmmmmmmmBoiGeorge6`9#";
        jsonBody.put("email", newEmail);
        jsonBody.put("password", currentPassword);
        jsonBody.put("newPassword", newPassword);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).getUser(mockUserId);
        verify(userRepository, times(1)).save(mockUser);
        verify(mockUser, times(1)).setEmail(newEmail);
        verify(mockUser, times(1)).setAuthenticationCodeFromPassword(newPassword);
    }

    @Test
    void modifyUser_newEmailAlreadyInUse_notModifiedUser400() throws Exception {
        when(userRepository.findByEmail(takenNewEmail)).thenReturn(mockUser);

        var jsonBody = createValidRequest();
        jsonBody.put("email", takenNewEmail);
        jsonBody.put("password", validCurrentPassword);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/users/" + mockUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString()))
                .andExpect(status().isConflict())
                .andReturn();

        verify(userRepository, times(1)).getUser(mockUserId);
        verify(userRepository, times(0)).save(mockUser);
    }

    @Test
    void modifyUser_modifyWithEmailNoPassword_notModifiedUser400() throws Exception {
        var jsonBody = createValidRequest();
        jsonBody.put("email", "HappyDog2@gmail.com");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(1)).getUser(mockUserId);
        verify(userRepository, times(0)).save(mockUser);
    }

    @Test
    void modifyUser_modifyWithEmailAndWrongPassword_notModifiedUser400() throws Exception {
        var jsonBody = createValidRequest();
        jsonBody.put("email", "FrostyCookie123@gmail.com");
        String wrongPassword = "GettingASufficientAmountOfSleep#69";
        jsonBody.put("password", wrongPassword);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(1)).getUser(mockUserId);
        verify(userRepository, times(0)).save(mockUser);
    }

    @Test
    void modifyUser_modifyWithEmailWrongPasswordAndNewPassword_notModifiedUser400() throws Exception {
        var jsonBody = createValidRequest();

        String wrongPassword = "GettingASufficientAmountOfSleep#69";
        String newPassword = "NotHavingAGoodTimeCovid$490";
        jsonBody.put("password", wrongPassword);
        jsonBody.put("newPassword", newPassword);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(1)).getUser(mockUserId);
        verify(userRepository, times(0)).save(mockUser);
    }

    @Test
    void modifyUser_modifyWithNewPasswordNoCurrentPassword_notModifiedUser400() throws Exception {
        var jsonBody = createValidRequest();

        String newPassword = "NotHavingAGoodTimeCovid$490";
        jsonBody.put("newPassword", newPassword);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(1)).getUser(mockUserId);
        verify(userRepository, times(0)).save(mockUser);
    }

    @Test
    void modifyUser_modifyEmailInvalid_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();
        String currentPassword = validCurrentPassword;
        jsonBody.put("email", "oops!");
        jsonBody.put("password", currentPassword);

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setEmail(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(mockUser);
        verify(mockUser, times(1)).setEmail(any());
    }

    @Test
    void modifyUser_modifyInvalidFirstName_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setFirstName(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(mockUser);
        verify(mockUser, times(1)).setFirstName(any());
    }

    @Test
    void modifyUser_modifyInvalidMiddleName_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setMiddleName(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(mockUser);
        verify(mockUser, times(1)).setMiddleName(any());
    }

    @Test
    void modifyUser_modifyInvalidLastName_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setLastName(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(mockUser);
        verify(mockUser, times(1)).setLastName(any());
    }

    @Test
    void modifyUser_modifyInvalidNickname_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setNickname(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        
        verify(userRepository, times(0)).save(mockUser);
        verify(mockUser, times(1)).setNickname(any());
    }

    @Test
    void modifyUser_modifyInvalidBio_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setBio(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(mockUser);
        verify(mockUser, times(1)).setBio(any());
    }

    @Test
    void modifyUser_modifyInvalidDateOfBirth_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setBio(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(mockUser);
        verify(mockUser, times(1)).setBio(any());
    }

    @Test
    void modifyUser_modifyInvalidPhoneNumber_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setPhNum(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(mockUser);
        verify(mockUser, times(1)).setPhNum(any());
    }

    @ParameterizedTest
    @ValueSource(strings={
            "streetNumber",
            "streetName",
            "district",
            "city",
            "region",
            "country",
            "postcode"
    })
    void modifyUser_modifyInvalidDistrict_userNotModified400(String field) throws Exception {
        var jsonBody = createValidRequest();

        Location address = new Location.Builder()
                .inCountry("Spain")
                .inCity("Christchurch")
                .inRegion("Region")
                .atStreetNumber("24")
                .onStreet("Cool street")
                .withPostCode("1238")
                .atDistrict("DistrictArea")
                .build();
        var jsonAddress = objectMapper.convertValue(new LocationDTO(address, true), JSONObject.class);
        jsonAddress.put(field, "ÉÄ○b");
        jsonBody.put("homeAddress", jsonAddress);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/" + mockUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(mockUser);
        verify(mockUser, times(0)).setAddress(any());
    }


    @Test
    void modifyUser_invalidSession_userNotModified401() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());
        var jsonBody = createValidRequest();

        mockMvc.perform(MockMvcRequestBuilders
                .put("/users/" + mockUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString()))
                .andExpect(status().isUnauthorized())
                .andReturn();

        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
        verify(userRepository, times(0)).save(mockUser);
    }

    @Test
    void modifyUser_invalidPermission_userNotModified403() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any()))
                .thenThrow(new InsufficientPermissionResponseException());
        var jsonBody = createValidRequest();

        mockMvc.perform(MockMvcRequestBuilders
                .put("/users/" + mockUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString()))
                .andExpect(status().isForbidden())
                .andReturn();

        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(),any()));
        verify(userRepository, times(0)).save(mockUser);
    }

    @Test
    void modifyUser_modifyImages_imagesUpdated() throws Exception {
        var jsonBody = createValidRequest();
        var ids = List.of(1L,2L,3L);
        jsonBody.put("imageIds", ids);
        Image image1 = Mockito.mock(Image.class);
        Image image2 = Mockito.mock(Image.class);
        Image image3 = Mockito.mock(Image.class);
        when(imageRepository.getImagesByIds(any())).thenReturn(List.of(image1,image2,image3));
        mockMvc.perform(MockMvcRequestBuilders
                .put("/users/" + mockUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString()))
                .andExpect(status().isOk());

        var expected = List.of(image1, image2, image3);
        verify(imageRepository, times(1)).getImagesByIds(ids);
        verify(mockUser, times(1)).setImages(expected);
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void modifyUser_modifyImagesInvalidId_imagesNotUpdated() throws Exception {
        var jsonBody = createValidRequest();
        var ids = List.of(1L);
        jsonBody.put("imageIds", ids);
        Image image1 = Mockito.mock(Image.class);
        when(imageRepository.getImagesByIds(any())).thenCallRealMethod();
        when(imageRepository.getImageById(1L)).thenThrow(new DoesNotExistResponseException(Image.class));
        mockMvc.perform(MockMvcRequestBuilders
                .put("/users/" + mockUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString()))
                .andExpect(status().isNotAcceptable());

        verify(imageRepository, times(1)).getImagesByIds(ids);
        verify(mockUser, times(0)).setImages(any());
        verify(userRepository, times(0)).save(mockUser);
    }
}











