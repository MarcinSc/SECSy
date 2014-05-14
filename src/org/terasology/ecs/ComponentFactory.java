package org.terasology.ecs;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface ComponentFactory {
    public <T extends Component> T createComponent(Class<T> clazz);
    public <T extends Component> T getComponent(Class<T> clazz, Map<String, Object> storedValues);
    public <T extends Component> Map<String, Object> saveComponent(T component);
    public <T extends Component> boolean isNewComponent(T component);
}
