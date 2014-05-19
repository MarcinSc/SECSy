package com.gempukku.secsy.system.indexing;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;

import java.util.Iterator;

public interface ComponentIndexingManager<E> {
    public ComponentIndex<E> createIndex(Class<? extends Component>... components);
    public void destroyIndex(ComponentIndex<E> index);

    public interface ComponentIndex<E> {
        public Iterator<EntityRef<E>> getMatchingEntities();
    }
}
