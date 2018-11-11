package com.gempukku.secsy.entity.prefab;

import com.gempukku.secsy.entity.Component;
import com.gempukku.secsy.entity.io.EntityData;

public interface PrefabManager {
    Iterable<? extends NamedEntityData> findPrefabsWithComponents(Class<? extends Component>... components);

    EntityData getPrefabByName(String name);
}
