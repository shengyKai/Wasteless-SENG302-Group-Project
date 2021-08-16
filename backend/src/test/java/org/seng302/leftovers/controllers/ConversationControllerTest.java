package org.seng302.leftovers.controllers;

import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.persistence.ConversationRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.MessageRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.service.MessageService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ConversationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Mock
    private MarketplaceCardRepository marketplaceCardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageService messageService;
    @Mock
    private Message message;
    @Captor
    private ArgumentCaptor<Message> messageArgumentCaptor;
    @Captor
    private ArgumentCaptor<User> buyerArgumentCaptor;
    @Captor
    private ArgumentCaptor<User> ownerArgumentCaptor;
    @Captor
    private ArgumentCaptor<Conversation> conversationArgumentCaptor;
    @Captor
    private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;
    private Message existingMessage1;
    private Message existingMessage2;
    private Message existingMessage3;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    private ConversationController conversationController;

    private User buyer; // ID = 1
    private User owner; // ID = 2
    private User bystander; // ID = 3
    @Mock
    private MarketplaceCard card;
    private Conversation conversation;

    @BeforeEach
    private void setup() {
        var tempUser1 = new User.Builder()
                .withFirstName("Andy")
                .withMiddleName("Percy")
                .withLastName("Cory")
                .withNickName("Ando")
                .withEmail("123andyelliot@gmail.com")
                .withPassword("password123")
                .withDob("1987-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .build();
        var tempUser2 = new User.Builder()
                .withFirstName("Bobby")
                .withMiddleName("Percy")
                .withLastName("David")
                .withNickName("Ando")
                .withEmail("456andyelliot@gmail.com")
                .withPassword("password123")
                .withDob("1987-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .build();
        var tempUser3 = new User.Builder()
                .withFirstName("Stuart")
                .withMiddleName("Derp")
                .withLastName("Alex")
                .withNickName("Derpy")
                .withEmail("stuart@gmail.com")
                .withPassword("password123")
                .withDob("1987-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Waimairi,Auckland,New Zealand,Auckland,8041"))
                .build();
        buyer = spy(tempUser1);
        owner = spy(tempUser2);
        bystander = spy(tempUser3);

        when(buyer.getUserID()).thenReturn(1L); // mock userId
        when(owner.getUserID()).thenReturn(2L); // mock userId
        when(bystander.getUserID()).thenReturn(3L); // mock userId

        when(userRepository.getUser(1L)).thenReturn(buyer); // Mock repo
        when(userRepository.getUser(2L)).thenReturn(owner);
        when(userRepository.getUser(3L)).thenReturn(bystander);


        when(card.getID()).thenReturn(1L); // mock getID
        when(card.getCreator()).thenReturn(owner); // Mock owner

        conversation = spy(new Conversation(card, buyer));
        existingMessage1 = new Message(conversation, buyer, "gobble gobble");
        existingMessage2 = new Message(conversation, buyer, "I am so sleepy");
        existingMessage3 = new Message(conversation, buyer, "I want to go to bed");

        when(conversation.getMessages()).thenReturn(List.of(existingMessage1));
        when(conversationRepository.findByCardAndBuyer(any(), any())).thenReturn(Optional.of(conversation));

        // Set up authentication manager respond as if user has correct permissions to post
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).then(invocation -> null);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);

        when(marketplaceCardRepository.getCard(any(), any())).thenReturn(card);

        conversationController = new ConversationController(marketplaceCardRepository, conversationRepository, userRepository,
                messageRepository, messageService);
        mockMvc = MockMvcBuilders.standaloneSetup(conversationController).build();
    }

    @AfterEach
    public void tearDown() {
        authenticationTokenManager.close();
    }

    private String createMessageBody(Long senderId) {
        return createMessageBody(senderId, "gimme free stuff");
    }

    private String createMessageBody(Long senderId, String message) {
        JSONObject json = new JSONObject();
        json.appendField("senderId", senderId.toString());
        json.appendField("message", message);

        return json.toJSONString();
    }


    @Test
    void postMarketplaceCardMessage_notLoggedIn_CannotPost() throws Exception {
        // call real method -> no token present
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenCallRealMethod();

        mockMvc.perform(post("/cards/1/conversations/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(createMessageBody(1L)))
        .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    void postMarketplaceCardMessage_senderIdNotAuthorized_CannotPost() throws Exception {
        // call real method -> sender ID is not same as session ID
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenCallRealMethod();

        mockMvc.perform(post("/cards/1/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMessageBody(1L)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void postMarketplaceCardMessage_cardNotExist_CannotPost() throws Exception {
        when(marketplaceCardRepository.getCard(any(), any())).thenCallRealMethod();

        mockMvc.perform(post("/cards/1/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMessageBody(1L)))
                .andExpect(status().isNotAcceptable())
                .andReturn();
    }

    @Test
    void postMarketplaceCardMessage_buyerNotExist_CannotPost() throws Exception {
        when(userRepository.getUser(1L)).thenCallRealMethod();

        mockMvc.perform(post("/cards/1/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMessageBody(1L)))
                .andExpect(status().isNotAcceptable())
                .andReturn();
    }

    @Test
    void postMarketplaceCardMessage_senderNotBuyerOrOwner_CannotPost() throws Exception {
        mockMvc.perform(post("/cards/1/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMessageBody(3L)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void postMarketplaceCardMessage_senderNotBuyerOrOwnerButIsAdmin_CanPost() throws Exception {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(true);
        mockMvc.perform(post("/cards/1/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMessageBody(3L)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    void postMarketplaceCardMessage_firstMessageFromOwner_CannotPost() throws Exception {
        when(conversationRepository.findByCardAndBuyer(any(),any())).thenReturn(Optional.empty()); // first message
        mockMvc.perform(post("/cards/1/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMessageBody(2L)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void postMarketplaceCardMessage_firstMessageFromBuyer_CanPost() throws Exception {
        when(conversationRepository.findByCardAndBuyer(any(),any())).thenReturn(Optional.empty()); // first message
        mockMvc.perform(post("/cards/1/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMessageBody(1L)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    void postMarketplaceCardMessage_OwnerCanReply_CanPost() throws Exception {
        mockMvc.perform(post("/cards/1/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMessageBody(1L)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    void postMarketplaceCardMessage_messageTooLong_CannotPost() throws Exception {
        var longMessage = new String(new char[201]).replace('\0', 'A');
        mockMvc.perform(post("/cards/1/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMessageBody(1L, longMessage)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void postMarketplaceCardMessage_messageEmpty_CannotPost() throws Exception {
        mockMvc.perform(post("/cards/1/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMessageBody(1L, "")))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void postMarketplaceCardMessage_canPost_notifyConversationParticipantsCalled() throws Exception {
        when(conversationRepository.findByCardAndBuyer(any(),any())).thenReturn(Optional.empty()); // first message
        when(messageRepository.save(any())).thenReturn(message);

        mockMvc.perform(post("/cards/1/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMessageBody(1L)));

        Mockito.verify(messageService, times(1)).notifyConversationParticipants(
                messageArgumentCaptor.capture(), buyerArgumentCaptor.capture(), ownerArgumentCaptor.capture());
        Assertions.assertEquals(message, messageArgumentCaptor.getValue());
        Assertions.assertEquals(buyer, buyerArgumentCaptor.getValue());
        Assertions.assertEquals(owner, ownerArgumentCaptor.getValue());
    }

    @Test
    @SneakyThrows
    void fetchMessagesInConversation_notLoggedIn_cannotFetchMessages() {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        mockMvc.perform(get("/cards/1/conversations/1"))
                .andExpect(status().isUnauthorized());

        Mockito.verifyNoInteractions(messageRepository);
    }

    @Test
    @SneakyThrows
    void fetchMessagesInConversation_cardDoesNotExist_cannotFetchMessages() {
        when(marketplaceCardRepository.getCard(1L, HttpStatus.NOT_ACCEPTABLE)).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));

        mockMvc.perform(get("/cards/1/conversations/1"))
                .andExpect(status().isNotAcceptable());

        Mockito.verifyNoInteractions(messageRepository);
    }

    @Test
    @SneakyThrows
    void fetchMessagesInConversation_buyerDoesNotExist_cannotFetchMessages() {
        when(userRepository.getUser(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));

        mockMvc.perform(get("/cards/1/conversations/1"))
                .andExpect(status().isNotAcceptable());

        Mockito.verifyNoInteractions(messageRepository);
    }

    @Test
    @SneakyThrows
    void fetchMessagesInConversation_conversationDoesNotExist_cannotFetchMessages() {
        when(conversationRepository.getConversation(card, buyer)).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));

        mockMvc.perform(get("/cards/1/conversations/1"))
                .andExpect(status().isNotAcceptable());

        Mockito.verifyNoInteractions(messageRepository);
    }

    @Test
    @SneakyThrows
    void fetchMessagesInConversation_notConversationParticipantOrAdmin_cannotFetchMessages() {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(HttpServletRequest.class), 1L)).thenReturn(false);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(HttpServletRequest.class), 2L)).thenReturn(false);

        mockMvc.perform(get("/cards/1/conversations/1"))
                .andExpect(status().isForbidden());

        Mockito.verifyNoInteractions(messageRepository);
    }

    @Test
    @SneakyThrows
    void fetchMessagesInConversation_loggedInAsCardCreator_canFetchMessages() {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(HttpServletRequest.class), 1L)).thenReturn(false);
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(HttpServletRequest.class), 2L)).thenReturn(true);

        Mockito.when(messageRepository.findAllByConversation(any(), any())).thenReturn(Page.of(existingMessage1));

        mockMvc.perform(get("/cards/1/conversations/1"))
                .andExpect(status().isAccepted());

    }

    @Test
    void fetchMessagesInConversation_loggedInAsCardResponder_canFetchMessages() {
        fail("Not yet implemented");
    }

    @Test
    void fetchMessagesInConversation_loggedInAsAdmin_canFetchMessages() {
        fail("Not yet implemented");
    }

    @Test
    void fetchMessagesInConversation_canFetchMessages_requestedConversationFetched() {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        Mockito.verify(messageRepository.findAllByConversation(conversationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture()), times(1));
        Mockito.when(messageRepository.findAllByConversation(any(), any())).thenReturn(Page.of(existingMessage1));

        mockMvc.perform(get("/cards/1/conversations/1"))
                .andExpect(status().isAccepted());

        Assertions.assertEquals(conversation, conversationArgumentCaptor.getValue());
    }

    @Test
    void fetchMessagesInConversation_canFetchMessages_messagesSortedFromMostToLeastRecent() {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        Mockito.verify(messageRepository.findAllByConversation(conversationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture()), times(1));
        Mockito.when(messageRepository.findAllByConversation(any(), any())).thenReturn(Page.of(existingMessage1));

        mockMvc.perform(get("/cards/1/conversations/1"))
                .andExpect(status().isAccepted());

        Assertions.assertEquals(Sort.by("created").descending(), pageRequestArgumentCaptor.getValue().getSort());
    }

    @Test
    void fetchMessagesInConversation_canFetchMessages_requestedPageFetched() {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        Mockito.verify(messageRepository.findAllByConversation(conversationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture()), times(1));
        Mockito.when(messageRepository.findAllByConversation(any(), any())).thenReturn(Page.of(existingMessage1));

        mockMvc.perform(get("/cards/1/conversations/1"))
                .andExpect(status().isAccepted());

        Assertions.assertEquals(2, pageRequestArgumentCaptor.getValue().getRequestedPage());
        Assertions.assertEquals(10, pageRequestArgumentCaptor.getValue().getResultsPerPage());
    }

    @Test
    void fetchMessagesInConversation_singleMessageInConversation_responseHasExpectedFormat() {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        Mockito.verify(messageRepository.findAllByConversation(conversationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture()), times(1));
        Mockito.when(messageRepository.findAllByConversation(any(), any())).thenReturn(Page.of(existingMessage1));

        var response = mockMvc.perform(get("/cards/1/conversations/1"))
                .andExpect(status().isAccepted()).andReturn();

        
        fail("Not yet implemented");

    }

    @Test
    void fetchMessagesInConversation_multipleMessagesInConversation_responseHasExpectedFormat() {
        authenticationTokenManager.when(() -> AuthenticationTokenManager.sessionCanSeePrivate(any(), any())).thenReturn(true);
        Mockito.verify(messageRepository.findAllByConversation(conversationArgumentCaptor.capture(), pageRequestArgumentCaptor.capture()), times(1));
        Mockito.when(messageRepository.findAllByConversation(any(), any())).thenReturn(Page.of(existingMessage1, existingMessage2, existingMessage3));

        var response = mockMvc.perform(get("/cards/1/conversations/1"))
                .andExpect(status().isAccepted()).andReturn();

        fail("Not yet implemented");
    }
}
