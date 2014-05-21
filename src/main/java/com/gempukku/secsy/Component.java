package com.gempukku.secsy;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface Component {
    // TODO: These two methods have to be moved somewhere, most likely - EntityFactory interface
    public Map<String, Class<?>> getComponentFields();
    public <T> T getComponentFieldValue(String fieldName, Class<T> clazz);
}
