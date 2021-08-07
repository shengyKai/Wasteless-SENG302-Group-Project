package cucumber.context;

import io.cucumber.java.Before;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.entities.Event;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.persistence.EventRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
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
     * Parses a list of events from a http response
     * @param response Response to decode
     * @param channel Channel to filter by
     * @return List of events for the given channel
     */
    @SneakyThrows
    public List<JSONObject> parseEvents(MockHttpServletResponse response, String channel) {
        String content = response.getContentAsString();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

        List<JSONObject> events = new ArrayList<>();

        // Iterable of lines that are not comments
        Iterator<String> lineIterator = content.lines().filter(line -> !line.startsWith(":")).iterator();

        while (lineIterator.hasNext()) {
            // Every iteration parses a single event
            // Expects the format:
            //  field:$(channel_name)
            //  data:$(data)
            //  --empty-line--

            String fieldLine = lineIterator.next();
            Assertions.assertTrue(fieldLine.startsWith("event:"));
            String foundChannel = fieldLine.substring("event:".length());

            Assertions.assertTrue(lineIterator.hasNext());
            String dataLine = lineIterator.next();
            Assertions.assertTrue(dataLine.startsWith("data:"));
            String data = dataLine.substring("data:".length());

            Assertions.assertTrue(lineIterator.hasNext());
            Assertions.assertEquals("", lineIterator.next());

            if (foundChannel.equals(channel)) {
                events.add(parser.parse(data, JSONObject.class));
            }
        }

        return events;
    }

    /**
     * Helper method for fetching events from the most recent response
     * @param channel Channel to filter by
     * @return List of most recently received events
     */
    public List<JSONObject> lastReceivedEvents(String channel) {
        return parseEvents(requestContext.getLastResult().getResponse(), channel);
    }
}
