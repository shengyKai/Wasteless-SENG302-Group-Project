package org.seng302.leftovers.controllers;

import net.minidev.json.JSONObject;
import org.hibernate.Session;
import org.seng302.datagenerator.ProductGenerator;
import org.seng302.leftovers.tools.JsonTools;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class GeneratorController {
    private final EntityManager entityManager;

    public GeneratorController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Generates a set of demo products
     * @param options Contains the quantity field which determines the number of products to generates
     * @return
     */
    @PostMapping("/demo/generateProducts")
    public JSONObject generateProducts(HttpServletRequest request, HttpServletResponse response, @RequestBody JSONObject options) {
        JSONObject json = new JSONObject();
        Session session = entityManager.unwrap(Session.class);

        int productQuantity = (int)JsonTools.parseLongFromJsonField(options, "quantity");

        session.doWork(connection -> {
            try {
                new ProductGenerator(connection).generateProducts(productQuantity);
            } catch (InterruptedException e) {
                json.appendField("response", e.getMessage());
                response.setStatus(500);
            }
        });
        return json;
    }

}
