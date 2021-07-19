package org.seng302.leftovers.controllers;

import net.minidev.json.JSONObject;
import org.hibernate.Session;
import org.seng302.datagenerator.BusinessGenerator;
import org.seng302.datagenerator.ProductGenerator;
import org.seng302.datagenerator.UserGenerator;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.JsonTools;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class GeneratorController {
    private final EntityManager entityManager;

    public GeneratorController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Generates a set of demo product
     * @param options Contains the quantity field which determines the number of products to generates
     * @return JSON including generated Users, Businesses and Products IDs
     */
    @PostMapping("/demo/generateProducts")
    public JSONObject generateProducts(HttpServletRequest request, HttpServletResponse response, @RequestBody JSONObject options) {
        AuthenticationTokenManager.checkAuthenticationTokenDGAA(request);

        JSONObject json = new JSONObject();

        int productCount = (int)JsonTools.parseLongFromJsonField(options, "productCount");
        int userCount = (int)JsonTools.parseLongFromJsonField(options, "userCount");
        int businessCount = (int)JsonTools.parseLongFromJsonField(options, "businessCount");

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            var userGenerator = new UserGenerator(connection);
            var businessGenerator = new BusinessGenerator(connection);
            var productGenerator = new ProductGenerator(connection);

            List<Long> userIds = userGenerator.generateUsers(userCount);
            List<Long> businessIds = businessGenerator.generateBusinesses(userIds, businessCount);
            List<Long> productIds = productGenerator.generateProducts(businessIds, productCount);

            json.appendField("generatedUsers", userIds);
            json.appendField("generatedBusinesses", businessIds);
            json.appendField("generatedProducts", productIds);
        });
        return json;
    }

}
