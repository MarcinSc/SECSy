package com.gempukku.secsy.component;

import com.gempukku.secsy.Component;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface ComponentFactory<O> {
    public <T extends Component> O createComponentValueObject(Class<T> clazz);

    public <T extends Component> void disposeComponentValueObject(O valueObject);

    public <T extends Component> T createComponent(Class<T> clazz, O valueObject);

    public <T extends Component> T getComponent(Class<T> clazz, O valueObject);

    public <T extends Component> void saveComponent(T component, O valueObject);

    public <T extends Component> boolean isNewComponent(T component);
}
