package com.gempukku.secsy;

import java.util.Collection;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityRef<E> {
    public <T extends Component> T addComponent(Class<T> clazz);
    public <T extends Component> T getComponent(Class<T> clazz);
    public void saveComponents(Component ... component);
    public <T extends Component> void removeComponents(Class<T> ... clazz);
    public Collection<Class<? extends Component>> listComponents();
    public boolean exists();
    public void send(E event);
}
