package com.gempukku.secsy;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface Component {
    public Class<? extends Component> getComponentClass();
}
