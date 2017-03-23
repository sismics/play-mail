package helpers.mail.freemarker;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author jtremeaux
 */
public class MapResourceBundle extends ResourceBundle {

    private Map<String, Object> map;

    public MapResourceBundle(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    protected Object handleGetObject(String key) {
        return map.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(map.keySet());
    }

}
