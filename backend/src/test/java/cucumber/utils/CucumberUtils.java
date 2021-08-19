package cucumber.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class containing helpful methods for Cucumber step definitions.
 */
public class CucumberUtils {
    /**
     * Sets a value through a multi layered mapping.
     * E.g. Calling with a path of "a","b","c" will do mapping.get("a").get("b").put("c", value) and will create any
     * required intermediate maps
     * @param mapping Multi-layered map
     * @param path List of keys/subkeys
     * @param value Value to place at the end of the path
     */
    public static void setValueAtPath(Map<String, Object> mapping, List<String> path, Object value) {
        String head = path.get(0);
        if (path.size() == 1) {
            mapping.put(head, value);
        } else {
            if (!mapping.containsKey(head)) {
                mapping.put(head, new HashMap<>());
            }
            setValueAtPath((Map<String, Object>)mapping.get(head), path.subList(1, path.size()), value);
        }
    }
}
