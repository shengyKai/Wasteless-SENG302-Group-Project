package org.seng302.leftovers.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.card.CreateKeywordDTO;
import org.seng302.leftovers.dto.card.KeywordDTO;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.KeywordCreatedEvent;
import org.seng302.leftovers.exceptions.AccessTokenResponseException;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.seng302.leftovers.exceptions.InsufficientPermissionResponseException;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.persistence.event.CreateKeywordEventRepository;
import org.seng302.leftovers.service.KeywordService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.service.search.SearchSpecConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class KeywordController {
    private static final Logger logger = LogManager.getLogger(KeywordController.class);

    private final KeywordRepository keywordRepository;
    private final CreateKeywordEventRepository createKeywordEventRepository;
    private final KeywordService keywordService;
    private final UserRepository userRepository;

    public KeywordController(KeywordRepository keywordRepository, KeywordService keywordService,
                             CreateKeywordEventRepository createKeywordEventRepository, UserRepository userRepository) {
        this.keywordRepository = keywordRepository;
        this.createKeywordEventRepository = createKeywordEventRepository;
        this.keywordService = keywordService;
        this.userRepository = userRepository;
    }



    /**
     * REST GET method to retrieve keywords partially matching a search term by name
     * When the search term is omitted, or left blank, all keywords are returned.
     * @param request the HTTP request
     * @param searchQuery The term to search for
     * @return List of all the keyword entities
     */
    @GetMapping("/keywords/search")
    public List<KeywordDTO> searchKeywords(HttpServletRequest request, @RequestParam(required = false) String searchQuery) {
        AuthenticationTokenManager.checkAuthenticationToken(request);
        logger.info("Searching for keywords with query: {}", searchQuery);
        if (searchQuery==null || searchQuery.isBlank()) {
            return getAllKeywords();
        }

        var specification = SearchSpecConstructor.constructKeywordSpecificationFromSearchQuery(searchQuery);
        var keywords = keywordRepository.findAll(specification);

        return keywords.stream().map(KeywordDTO::new).collect(Collectors.toList());
    }

    /**
     * Returns a Array containing all of the keywords in the system
     * @return Array of all keywords currently in the system as DTOs
     */
    private List<KeywordDTO> getAllKeywords() {
        try {
            return keywordRepository.findByOrderByNameAsc().stream().map(KeywordDTO::new).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * REST DELETE method to delete the given keyword entity
     * This endpoint is only accessible to system admins
     * @param request The HTTP request
     * @param id Keyword ID to delete
     */
    @DeleteMapping("/keywords/{id}")
    public void deleteKeyword(HttpServletRequest request, @PathVariable Long id) {
        try {
            logger.info("Deleting keyword: {}", id);

            AuthenticationTokenManager.checkAuthenticationToken(request);

            if (!AuthenticationTokenManager.sessionIsAdmin(request)) {
                throw new InsufficientPermissionResponseException("Only admin users can delete keywords");
            }

            Keyword keyword = keywordRepository.findById(id)
                    .orElseThrow(() -> new DoesNotExistResponseException(Keyword.class));
            Optional<KeywordCreatedEvent> keywordEvent = createKeywordEventRepository.getByNewKeyword(keyword);
            keywordEvent.ifPresent(createKeywordEventRepository::delete);
            keywordRepository.delete(keyword);

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * DTO representing the response of a create keyword request
     */
    @Getter
    @ToString
    @AllArgsConstructor
    public static class CreateKeywordResponseDTO {
        private Long keywordId;
    }

    /**
     * REST POST method to add a new keyword entry
     * @param request The HTTP request
     * @param keywordInfo Request body to construct keyword from
     * @return CreateKeywordResponseDTO with the created keyword id
     */
    @PostMapping("/keywords")
    public CreateKeywordResponseDTO addKeyword(HttpServletRequest request, HttpServletResponse response, @RequestBody @Valid CreateKeywordDTO keywordInfo) {
        try {
            String name = keywordInfo.getName();
            logger.info("Adding new keyword with name \"{}\"", name);
            AuthenticationTokenManager.checkAuthenticationToken(request);

            // Formats keyword to have capitals at the start of each word
            Keyword keyword = new Keyword(name);
            if (keywordRepository.findByName(keyword.getName()).isPresent()) {
                throw new ValidationResponseException("Keyword with the given name already exists");
            }

            User creator = findUserFromRequest(request);
            keyword = keywordRepository.save(keyword);
            keywordService.sendNewKeywordEvent(keyword, creator);

            response.setStatus(201);
            return new CreateKeywordResponseDTO(keyword.getID());
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Finds the user who send the HTTP request by using the user ID associated with this session.
     * @param request Incoming HTTP request.
     * @return User associated with HTTP request.
     */
    private User findUserFromRequest(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("accountId");
        if (userId == null) {
            throw new AccessTokenResponseException("Could not get user ID from request");
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new AccessTokenResponseException("Invalid user ID");
        }
        return user.get();
    }
}
