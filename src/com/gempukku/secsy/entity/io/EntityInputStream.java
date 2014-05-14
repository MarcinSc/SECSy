package com.gempukku.secsy.entity.io;

import com.gempukku.secsy.EntityRef;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityInputStream<E> {
    public Map.Entry<Integer, EntityRef<E>> readEntity();
}
