package org.seng302.leftovers.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.seng302.leftovers.dto.LocationDTO;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.exceptions.AccessTokenResponseException;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.service.ImageService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
class BusinessControllerMockedTest {

    private MockMvc mockMvc;

    private final long mockBusinessId = 6L;
    private final long mockOwnerId    = 7L;
    private final long mockNonOwnerId = 8L;
    private final long mockImageId = 9L;
    private final long mockImageId2 = 23L;
    private final long mockImageId3 = 19L;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private Business mockBusiness;
    @Mock
    private User mockOwner;
    @Mock
    private User mockNonOwner;
    @Mock
    private Product mockProduct1;
    @Mock
    private Product mockProduct2;
    @Mock
    private Product mockProduct3;
    @Mock
    private Image mockImage;
    @Mock
    private Image mockImage2;
    @Mock
    private Image mockImage3;
    @Mock
    private Image mockPrimaryImage;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), eq(mockOwnerId))).thenReturn(true);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), not(eq(mockOwnerId)))).thenReturn(false);

        when(mockOwner.getUserID()).thenReturn(mockOwnerId);
        when(mockNonOwner.getUserID()).thenReturn(mockNonOwnerId);
        when(mockImage.getID()).thenReturn(mockImageId);
        when(mockImage2.getID()).thenReturn(mockImageId2);
        when(mockImage3.getID()).thenReturn(mockImageId3);

        when(userRepository.findById(mockOwnerId)).thenReturn(Optional.of(mockOwner));
        when(userRepository.findById(mockNonOwnerId)).thenReturn(Optional.of(mockNonOwner));
        when(userRepository.findById(not(or(eq(mockOwnerId), eq(mockNonOwnerId))))).thenReturn(Optional.empty());


        when(mockBusiness.getPrimaryOwner()).thenReturn(mockOwner);
        when(mockBusiness.getImages()).thenReturn(Arrays.asList(mockPrimaryImage,mockImage));

        when(businessRepository.findById(mockBusinessId)).thenReturn(Optional.of(mockBusiness));
        when(businessRepository.findById(not(eq(mockBusinessId)))).thenReturn(Optional.empty());
        when(businessRepository.getBusinessById(any())).thenAnswer(CALLS_REAL_METHODS);

        when(imageRepository.findById(mockImageId)).thenReturn(Optional.of(mockImage));
        when(imageRepository.findById(mockImageId2)).thenReturn(Optional.of(mockImage2));
        when(imageRepository.findById(mockImageId3)).thenReturn(Optional.of(mockImage3));

        BusinessController businessController = new BusinessController(businessRepository, userRepository, imageService, imageRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(businessController).build();
    }

    @AfterEach
    void tearDown() {
        authenticationTokenManager.close();
    }

    private JSONObject createSessionForUser(Long userId) {
        var json = new JSONObject();
        json.put("accountId", userId);
        return json;
    }

    @SneakyThrows
    private JSONObject createValidRequest() {
        var json = new JSONObject();
        json.put("primaryAdministratorId", mockOwnerId);
        json.put("name", "New business name");
        json.put("description", "New business description");
        json.put("address", new LocationDTO(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand,Canterbury,8041"), true));
        json.put("businessType", objectMapper.convertValue(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES, String.class));
        json.put("updateProductCountry", true);
        return json;
    }

    @Test
    void modifyBusiness_notLoggedIn_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        var json = createValidRequest();
        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
        verify(businessRepository, times(0)).save(any());
    }

    @Test
    void modifyBusiness_businessDoesNotExist_406Response() throws Exception {
        var json = createValidRequest();
        mockMvc.perform(
                put("/businesses/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockOwnerId)))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        verify(businessRepository, times(1)).findById(9999L);
        verify(businessRepository, times(0)).save(any());
    }


    @Test
    void modifyBusiness_notOwner_403Response() throws Exception {
        var json = createValidRequest();
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(mockBusiness).checkSessionPermissions(any());
        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockNonOwnerId)))
                .andExpect(status().isForbidden())
                .andReturn();

        verify(businessRepository, times(0)).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"primaryAdministratorId", "name", "description", "address", "businessType", "updateProductCountry"})
    void modifyBusiness_fieldNotProvided_400Response(String field) throws Exception {
        var json = createValidRequest();
        json.remove(field);

        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockOwnerId)))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(businessRepository, times(0)).save(any());
    }

    @Test
    void modifyBusiness_invalidOwner_400Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(mockBusiness).setPrimaryOwner(any());

        var json = createValidRequest();
        json.put("primaryAdministratorId", mockNonOwnerId);
        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockOwnerId)))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(mockBusiness, times(1)).setPrimaryOwner(any());
    }

    @Test
    void modifyBusiness_invalidName_400Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(mockBusiness).setName(any());

        var json = createValidRequest();
        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockOwnerId)))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(mockBusiness, times(1)).setName(any());
    }

    @Test
    void modifyBusiness_invalidDescription_400Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(mockBusiness).setDescription(any());

        var json = createValidRequest();
        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockOwnerId)))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(mockBusiness, times(1)).setDescription(any());
    }

    @Test
    void modifyBusiness_invaidBusinessType_400Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(mockBusiness).setBusinessType(any());

        var json = createValidRequest();
        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockOwnerId)))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(mockBusiness, times(1)).setBusinessType(any());
    }

    @Test
    void modifyBusiness_invaidAddress_400Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(mockBusiness).setAddress(any());

        var json = createValidRequest();
        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockOwnerId)))
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(mockBusiness, times(1)).setAddress(any());
    }

    @Test
    void modifyBusiness_isOwner_200ResponseAndModified() throws Exception {
        var json = createValidRequest();
        json.put("primaryAdministratorId", mockNonOwnerId);
        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockOwnerId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(mockBusiness, times(1)).setPrimaryOwner(mockNonOwner);
        verify(mockBusiness, times(1)).setName((String)json.get("name"));
        verify(mockBusiness, times(1)).setDescription((String)json.get("description"));

        var businessType = objectMapper.convertValue(json.getAsString("businessType"), BusinessType.class);
        verify(mockBusiness, times(1)).setBusinessType(businessType);

        var addressCaptor = ArgumentCaptor.forClass(Location.class);
        verify(mockBusiness, times(1)).setAddress(addressCaptor.capture());
        Location location = addressCaptor.getValue();
        assertEquals("4", location.getStreetNumber());
        assertEquals("Rountree Street", location.getStreetName());
        assertEquals("Ashburton", location.getDistrict());
        assertEquals("Christchurch", location.getCity());
        assertEquals("New Zealand", location.getCountry());
        assertEquals("Canterbury", location.getRegion());
        assertEquals("8041", location.getPostCode());

        verify(businessRepository, times(1)).save(mockBusiness);
    }

    @Test
    void modifyBusiness_isAdmin_200Response() throws Exception {
        doNothing().when(mockBusiness).checkSessionPermissions(any());

        var json = createValidRequest();
        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockNonOwnerId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(mockBusiness, times(1)).checkSessionPermissions(any());
        verify(businessRepository, times(1)).save(mockBusiness);
    }

    @Test
    void modifyBusiness_isOwnerAndUpdateCurrencyForOneProduct_200ResponseAndCurrencyUpdated() throws Exception {
        when(mockBusiness.getCatalogue()).thenReturn(List.of(mockProduct1));
        var json = createValidRequest();
        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockOwnerId)))
                .andExpect(status().isOk())
                .andReturn();
        
        var addressCaptor = ArgumentCaptor.forClass(Location.class);
        verify(mockBusiness, times(1)).setAddress(addressCaptor.capture());
        verify(mockBusiness, times(1)).getCatalogue();
        verify(mockProduct1, times(1)).setCountryOfSale("New Zealand");
    }

    @Test
    void modifyBusiness_isOwnerAndUpdateCurrencyForMultipleProducts_200ResponseAndCurrencyUpdated() throws Exception {
        when(mockBusiness.getCatalogue()).thenReturn(List.of(mockProduct1, mockProduct2, mockProduct3));
        var json = createValidRequest();
        mockMvc.perform(
                put("/businesses/" + mockBusinessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString())
                        .sessionAttrs(createSessionForUser(mockOwnerId)))
                .andExpect(status().isOk())
                .andReturn();

        var addressCaptor = ArgumentCaptor.forClass(Location.class);
        verify(mockBusiness, times(1)).setAddress(addressCaptor.capture());
        String country = addressCaptor.getValue().getCountry();

        verify(mockBusiness, times(1)).getCatalogue();
        verify(mockProduct1, times(1)).setCountryOfSale(country);
        verify(mockProduct2, times(1)).setCountryOfSale(country);
        verify(mockProduct3, times(1)).setCountryOfSale(country);
    }

    @Test
    void modifyBusinessSetImage_notLoggedIn_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenResponseException());

        var json = createValidRequest();
        json.put("imageIds", Collections.singletonList(mockImageId));

        mockMvc.perform(put("/businesses/" + mockBusinessId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
        verify(businessRepository, times(0)).save(any());
    }

    @Test
    void modifyBusinessSetImage_businessDoesNotExist_406Response() throws Exception {
        var json = createValidRequest();
        json.put("imageIds", Collections.singletonList(mockImageId));

        mockMvc.perform(put("/businesses/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        verify(businessRepository, times(1)).findById(9999L);
        verify(mockBusiness, times(0)).setImages(any());
        verify(businessRepository, times(0)).save(any());
    }

    @Test
    void modifyBusinessSetImage_ImageDoesNotExist_406Response() throws Exception {
        var json = createValidRequest();
        json.put("imageIds", Collections.singletonList(9999L));

        mockMvc.perform(put("/businesses/" + mockBusinessId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        verify(businessRepository, times(1)).findById(mockBusinessId);
        verify(mockBusiness, times(0)).setImages(any());
        verify(businessRepository, times(0)).save(any());
    }

    @Test
    void modifyBusinessSetImage_notAuthorisedForBusiness_403Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(mockBusiness).checkSessionPermissions(any());

        var json = createValidRequest();
        json.put("imageIds", Collections.singletonList(mockImageId));

        mockMvc.perform(put("/businesses/" + mockBusinessId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isForbidden())
                .andReturn();

        verify(mockBusiness, times(1)).checkSessionPermissions(any());
        verify(mockBusiness, times(0)).setImages(any());
        verify(businessRepository, times(0)).save(any());
    }

    @Test
    void modifyBusinessSetImage_validRequest_200ResponseAndPrimary() throws Exception {
        var json = createValidRequest();
        json.put("imageIds", Collections.singletonList(mockImageId));

        mockMvc.perform(put("/businesses/" + mockBusinessId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isOk())
                .andReturn();

        verify(mockBusiness, times(1)).setImages(any());
        verify(businessRepository, times(1)).save(mockBusiness);
    }

    @Test
    void modifyBusinessSetManyImage_validRequest_200Response() throws Exception {
        var json = createValidRequest();
        List<Long> expected = List.of(mockImageId, mockImageId2, mockImageId3);
        json.put("imageIds", expected);

        mockMvc.perform(put("/businesses/" + mockBusinessId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isOk())
                .andReturn();

        verify(mockBusiness, times(1)).setImages(any());
        verify(businessRepository, times(1)).save(mockBusiness);
    }
}
