package com.gempukku.secsy;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface Component {
    public Class<? extends Component> getComponentClass();
    public Map<String, Class<?>> getComponentFields();
    public <T> T getComponentFieldValue(String fieldName, Class<T> clazz);
}
