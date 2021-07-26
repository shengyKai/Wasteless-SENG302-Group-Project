package cucumber.context;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventContext {

    private List<JSONObject> parseEvents(MockHttpServletResponse response, String channel) throws UnsupportedEncodingException, ParseException {
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
}
