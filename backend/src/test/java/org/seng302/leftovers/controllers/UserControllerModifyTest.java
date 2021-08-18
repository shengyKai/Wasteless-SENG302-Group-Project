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
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
    private User testUser;
    @Mock
    private Location mockLocation;


    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(userRepository.getUser(mockUserId)).thenReturn(testUser);

        when(testUser.getUserID()).thenReturn(mockUserId);
        when(testUser.getBio()).thenReturn("Ella@gmail.com");
        when(testUser.getFirstName()).thenReturn("Ella");
        when(testUser.getLastName()).thenReturn("Ella");
        when(testUser.getMiddleName()).thenReturn("bananas");
        when(testUser.getNickname()).thenReturn("cool gal");
        when(testUser.getDob()).thenReturn(LocalDate.parse("1999-06-26"));
        //when(testUser.getAuthenticationCode()).thenReturn("cool gal");
        when(testUser.getAddress()).thenReturn(mockLocation);

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
        verify(testUser, times(1)).setFirstName(newFirstName);
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
        verify(testUser, times(1)).setLastName(newLastName);
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
        verify(testUser, times(1)).setMiddleName(newMiddleName);
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
        verify(testUser, times(1)).setNickname(newNickname);
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
        verify(testUser, times(1)).setBio(newBio);
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
        verify(testUser, times(1)).setDob(LocalDate.parse(newDateOfBirth));
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
        verify(testUser, times(1)).setPhNum(newPhoneNumber);
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
        verify(testUser, times(1)).setAddress(any());
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
        verify(testUser, times(1)).setAddress(any());
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
        verify(testUser, times(1)).setAddress(any());
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
        verify(testUser, times(1)).setAddress(any());    }

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
        verify(testUser, times(1)).setAddress(any());    }

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
        verify(testUser, times(1)).setAddress(any());    }

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
        verify(testUser, times(1)).setAddress(any());    }

    @Test
    void modifyUser_modifyWithValidEmailAndPasswords_modifiedUser200() throws Exception {

    }

    @Test
    void modifyUser_modifyWithOnlyValidEmail_notModifiedUser403() throws Exception {

    }

    @Test
    void modifyUser_modifyWithOnlyValidCurrentPassword_notModifiedUser403() throws Exception {

    }

    @Test
    void modifyUser_modifyWithOnlyValidNewPassword_notModifiedUser403() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullMiddleName_userModified200() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullNickname_userModified200() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullBio_userModified200() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullPhoneNumber_userModified200() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullDistrict_userModified200() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullEmailAndPassword_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullFirstName_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullLastName_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullDateOfBirth_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullStreetAddress_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullCity_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullRegion_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullCountry_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyWithNullPostcode_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyEmailTooLong_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongPassword_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongFirstName_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongMiddleName_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongLastName_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongNickname_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongBio_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongDateOfBirth_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooShortDateOfBirth_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongPhoneNumber_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongStreetAddress_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongDistrict_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongCity_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongRegion_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongCountry_userNotModified400() throws Exception {

    }

    @Test
    void modifyUser_modifyTooLongPostcode_userNotModified400() throws Exception {

    }
}











