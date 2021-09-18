package cucumber.context;

import io.cucumber.java.Before;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.seng302.leftovers.entities.event.Event;
import org.seng302.leftovers.persistence.event.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class EventContext {
    private Event lastEvent = null;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RequestContext requestContext;

    @Before
    public void setup() {
        lastEvent = null;
    }

    /**
     * Returns the last modified Event
     * @return last modified Event
     */
    public Event getLast() {return lastEvent;}

    /**
     * Saves an event using the event repository
     * Also sets the last event
     * @param event Event to save
     * @return Saved card
     */
    public <T extends Event> T save(T event) {
        T savedEvent = eventRepository.save(event);
        lastEvent = savedEvent;
        return savedEvent;
    }

    /**
     * Helper method for converting a MvcResult into a list of JSON objects.
     * @param result An MvcResult which should contain the an array of json objects.
     * @return List of json objects from the MvcResult
     */
    public List<JSONObject> mvcResultToJsonObjectList(MvcResult result) throws UnsupportedEncodingException, ParseException {
        List<JSONObject> list = new ArrayList<>();
        String body = result.getResponse().getContentAsString();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray jsonArray = parser.parse(body, JSONArray.class);
        for (Object json : jsonArray) {
            list.add((JSONObject) json);
        }
        return list;
    }
}
