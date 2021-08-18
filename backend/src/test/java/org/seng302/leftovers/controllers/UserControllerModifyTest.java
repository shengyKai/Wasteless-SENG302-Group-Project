package org.seng302.leftovers.controllers;

import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import org.apache.tomcat.jni.Address;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.exceptions.AccessTokenException;
import org.seng302.leftovers.exceptions.InsufficientPermissionException;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.PasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerModifyTest {
    private long mockUserId = 5L;
    private String validCurrentPassword = "HappyBoiGeorge69#";

    @Autowired
    private MockMvc mockMvc;

    private UserController userController;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;

    @MockBean
    private UserRepository userRepository;

    @Mock
    private User mockUser;
    @Mock
    private Location mockLocation;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(mockUserId))).thenReturn(true);

        when(userRepository.getUser(mockUserId)).thenReturn(mockUser);

        when(mockUser.getUserID()).thenReturn(mockUserId);
        when(mockUser.getBio()).thenReturn("Ella@gmail.com");
        when(mockUser.getFirstName()).thenReturn("Ella");
        when(mockUser.getLastName()).thenReturn("Ella");
        when(mockUser.getMiddleName()).thenReturn("bananas");
        when(mockUser.getNickname()).thenReturn("cool gal");
        when(mockUser.getDob()).thenReturn(LocalDate.parse("1999-06-26"));
        when(mockUser.getAuthenticationCode()).thenReturn(
                PasswordAuthenticator.generateAuthenticationCode(validCurrentPassword));
        when(mockUser.getAddress()).thenReturn(mockLocation);

        when(mockLocation.getCountry()).thenReturn("New Zealand");
        when(mockLocation.getCity()).thenReturn("Christchurch");
        when(mockLocation.getDistrict()).thenReturn("District");
        when(mockLocation.getRegion()).thenReturn("Canterbury");
        when(mockLocation.getPostCode()).thenReturn("1234");
        when(mockLocation.getStreetNumber()).thenReturn("68");
        when(mockLocation.getStreetName()).thenReturn("Arthur street");

        userController = new UserController(userRepository);
    }

    @AfterEach
    public void tearDown() { authenticationTokenManager.close(); }

    @SneakyThrows
    private JSONObject createValidRequest() {
        var jsonBody = new JSONObject();
        jsonBody.put("email", "Ella@gmail.com");
        jsonBody.put("firstName", "Ella");
        jsonBody.put("lastName", "Ella");
        jsonBody.put("dateOfBirth", "1999-06-26");
        jsonBody.put("streetAddress", "69 Elizabeth Street");
        jsonBody.put("city", "Christchurch");
        jsonBody.put("region", "Auckland");
        jsonBody.put("postcode", "8069");
        return jsonBody;
    }

    private JSONObject createSessionForUser(Long userId) {
        var json = new JSONObject();
        json.put("accountId", userId);
        return json;
    }

    @Test
    void modifyUser_modifyWithValidFirstName_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newFirstName = "Nathan";
        jsonBody.put("firstName", newFirstName);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/profile/" + mockUserId + "/modify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString())
                .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setFirstName(newFirstName);
    }

    @Test
    void modifyUser_modifyWithValidLastName_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newLastName = "John";
        jsonBody.put("lastName", newLastName);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setLastName(newLastName);
    }

    @Test
    void modifyUser_modifyWithValidMiddleName_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newMiddleName = "Johnson";
        jsonBody.put("middleName", newMiddleName);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setMiddleName(newMiddleName);
    }

    @Test
    void modifyUser_modifyWithValidNickname_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newNickname = "Johnson";
        jsonBody.put("middleName", newNickname);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setNickname(newNickname);
    }

    @Test
    void modifyUser_modifyWithValidBio_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newBio = "Johnny";
        jsonBody.put("nickname", newBio);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setBio(newBio);
    }

    @Test
    void modifyUser_modifyWithValidDateOfBirth_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newDateOfBirth = "1999-07-06";
        jsonBody.put("dateOfBirth", newDateOfBirth);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setDob(LocalDate.parse(newDateOfBirth));
    }

    @Test
    void modifyUser_modifyWithValidPhoneNumber_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newPhoneNumber = "+64 3 555 0129";
        jsonBody.put("phoneNumber", newPhoneNumber);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setPhNum(newPhoneNumber);
    }

    @Test
    void modifyUser_modifyWithValidStreetNumber_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newStreetNumber = "59";
        jsonBody.put("streetNumber", newStreetNumber);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }

    @Test
    void modifyUser_modifyWithValidStreetName_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newStreetName = "Happy Street";
        jsonBody.put("streetName", newStreetName);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }

    @Test
    void modifyUser_modifyWithValidDistrict_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newDistrict = "Sheepery";
        jsonBody.put("district", newDistrict);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }

    @Test
    void modifyUser_modifyWithValidCity_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newCity = "Invercargill";
        jsonBody.put("city", newCity);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }

    @Test
    void modifyUser_modifyWithValidRegion_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newRegion = "Clutha";
        jsonBody.put("region", newRegion);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }

    @Test
    void modifyUser_modifyWithValidCountry_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newCountry = "Australia";
        jsonBody.put("country", newCountry);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }

    @Test
    void modifyUser_modifyWithValidPostcode_modifiedUser200() throws Exception {
        var jsonBody = createValidRequest();

        String newPostcode = "90953";
        jsonBody.put("postcode", newPostcode);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setAddress(any());
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
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(1)).save(any());
        verify(mockUser, times(1)).setEmail(newEmail);
        verify(mockUser, times(1)).setAuthenticationCodeFromPassword(newPassword);
    }

    @Test
    void modifyUser_modifyWithEmailNoPassword_notModifiedUser400() throws Exception {
        var jsonBody = createValidRequest();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void modifyUser_modifyWithEmailAndWrongPassword_notModifiedUser403() throws Exception {
        var jsonBody = createValidRequest();

        String wrongPassword = "GettingASufficientAmountOfSleep#69";
        jsonBody.put("password", wrongPassword);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isForbidden())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void modifyUser_modifyWithEmailWrongPasswordAndNewPassword_notModifiedUser403() throws Exception {
        var jsonBody = createValidRequest();

        String wrongPassword = "GettingASufficientAmountOfSleep#69";
        String newPassword = "NotHavingAGoodTimeCovid$490";
        jsonBody.put("password", wrongPassword);
        jsonBody.put("newPassword", newPassword);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void modifyUser_modifyWithNewPasswordNoCurrentPassword_notModifiedUser400() throws Exception {
        var jsonBody = createValidRequest();

        String newPassword = "NotHavingAGoodTimeCovid$490";
        jsonBody.put("newPassword", newPassword);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(1)).findById(mockUserId).get();
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void modifyUser_modifyEmailInvalid_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();
        String currentPassword = validCurrentPassword;
        jsonBody.put("password", currentPassword);

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setEmail(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setEmail(any());
    }

    @Test
    void modifyUser_modifyInvalidFirstName_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setFirstName(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setFirstName(any());
    }

    @Test
    void modifyUser_modifyInvalidMiddleName_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setMiddleName(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setMiddleName(any());
    }

    @Test
    void modifyUser_modifyInvalidLastName_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setLastName(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setLastName(any());
    }

    @Test
    void modifyUser_modifyInvalidNickname_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setNickname(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setNickname(any());
    }

    @Test
    void modifyUser_modifyInvalidBio_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setBio(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setBio(any());
    }

    @Test
    void modifyUser_modifyInvalidDateOfBirth_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setBio(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setBio(any());
    }

    @Test
    void modifyUser_modifyInvalidPhoneNumber_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setPhNum(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setPhNum(any());
    }

    @Test
    void modifyUser_modifyInvalidStreetAddress_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setAddress(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }

    @Test
    void modifyUser_modifyInvalidDistrict_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setAddress(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }

    @Test
    void modifyUser_modifyInvalidCity_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setAddress(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }

    @Test
    void modifyUser_modifyInvalidRegion_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setAddress(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }

    @Test
    void modifyUser_modifyInvalidCountry_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setAddress(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }

    @Test
    void modifyUser_modifyInvalidPostcode_userNotModified400() throws Exception {
        var jsonBody = createValidRequest();

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(mockUser).setAddress(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/profile/" + mockUserId + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString())
                        .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(userRepository, times(0)).save(any());
        verify(mockUser, times(1)).setAddress(any());
    }


    @Test
    void modifyUser_invalidSession_userNotModified401() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenException());
        var jsonBody = createValidRequest();

        mockMvc.perform(MockMvcRequestBuilders
                .put("/profile/" + mockUserId + "/modify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString())
                .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void modifyUser_invalidPermission_userNotModified401() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any()))
                .thenThrow(new InsufficientPermissionException());
        var jsonBody = createValidRequest();

        mockMvc.perform(MockMvcRequestBuilders
                .put("/profile/" + mockUserId + "/modify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString())
                .sessionAttrs(createSessionForUser(mockUserId)))
                .andExpect(status().isForbidden())
                .andReturn();

        authenticationTokenManager.verify(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(),any()));
        verify(userRepository, times(0)).save(any());
    }
}











