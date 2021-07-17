package org.seng302.leftovers.controllers;

import net.minidev.json.JSONArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@RestController
public class KeywordController {
    private static final Logger logger = LogManager.getLogger(KeywordController.class);

    private final KeywordRepository keywordRepository;

    public KeywordController(KeywordRepository keywordRepository) {
        this.keywordRepository = keywordRepository;
    }

    /**
     * REST GET method to retrieve all the global keyword entities
     * @param request the HTTP request
     * @return List of all the keyword entities
     */
    @GetMapping("/keywords/search")
    public JSONArray searchKeywords(HttpServletRequest request) {
        try {
            logger.info("Getting all the keywords");
            AuthenticationTokenManager.checkAuthenticationToken(request);

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
            keywordRepository.delete(keyword);

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
}
