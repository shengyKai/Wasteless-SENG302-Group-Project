package org.seng302.leftovers.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.KeywordCreatedEvent;
import org.seng302.leftovers.persistence.CreateKeywordEventRepository;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.service.KeywordService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.service.searchservice.SearchSpecConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Optional;

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
    public JSONArray searchKeywords(HttpServletRequest request, @RequestParam(required = false) String searchQuery) {
        AuthenticationTokenManager.checkAuthenticationToken(request);
        logger.info("Searching for keywords with query: {}", searchQuery);
        if (searchQuery==null || searchQuery.isBlank()) {
            return getAllKeywords();
        }

        var specification = SearchSpecConstructor.constructKeywordSpecificationFromSearchQuery(searchQuery);
        var keywords = keywordRepository.findAll(specification);
        JSONArray result = new JSONArray();
        for (var keyword : keywords) {
            result.add(keyword.constructJSONObject());
        }
        return result;
    }

    /**
     * Returns a JSON Array containing all of the keywords in the system
     * @return JSON Array of all keywords currently in the system
     */
    private JSONArray getAllKeywords() {
        try {
            JSONArray result = new JSONArray();
            for (Keyword keyword : keywordRepository.findByOrderByNameAsc()) {
                result.add(keyword.constructJSONObject());
            }
            return result;
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
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin users can delete keywords");
            }

            Keyword keyword = keywordRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Keyword not found"));
            Optional<KeywordCreatedEvent> keywordEvent = createKeywordEventRepository.getByNewKeyword(keyword);
            keywordEvent.ifPresent(createKeywordEventRepository::delete);
            keywordRepository.delete(keyword);

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * REST POST method to add a new keyword entry
     * @param request The HTTP request
     * @param keywordInfo Request body to construct keyword from
     * @return JSONObject with the created keyword id
     */
    @PostMapping("/keywords")
    public JSONObject addKeyword(HttpServletRequest request, HttpServletResponse response, @RequestBody JSONObject keywordInfo) {
        try {
            String name = keywordInfo.getAsString("name");
            logger.info("Adding new keyword with name \"{}\"", name);
            AuthenticationTokenManager.checkAuthenticationToken(request);

            // Formats keyword to have capitals at the start of each word
            Keyword keyword = new Keyword(name);
            if (keywordRepository.findByName(keyword.getName()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keyword with the given name already exists");
            }

            User creator = findUserFromRequest(request);
            keyword = keywordRepository.save(keyword);
            keywordService.sendNewKeywordEvent(keyword, creator);

            JSONObject json = new JSONObject();
            json.put("keywordId", keyword.getID());

            response.setStatus(201);
            return json;
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not get user ID from request");
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user ID");
        }
        return user.get();
    }
}
