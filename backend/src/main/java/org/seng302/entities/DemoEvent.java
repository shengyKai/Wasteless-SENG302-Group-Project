package org.seng302.entities;

import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.lang.module.ResolutionException;

@Entity
public class DemoEvent extends Event {
    @Column(nullable = false)
    private String message;

    protected DemoEvent() {}

    public DemoEvent(String message) {
        setMessage(message);
    }

    public void setMessage(String message) {
        if (message == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be null");
        }
        this.message = message;
    }

    public String getMessage() {
        return message;
    }


    @Override
    public JSONObject constructJSONObject() {
        JSONObject json = super.constructJSONObject();
        json.put("type", "demo");
        json.put("message", message);
        return json;
    }
}
