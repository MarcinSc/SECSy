package com.gempukku.secsy.entity;

import com.gempukku.secsy.context.annotation.RegisterSystem;
import com.gempukku.secsy.entity.io.ComponentData;
import com.gempukku.secsy.entity.io.EntityData;
import com.gempukku.secsy.entity.prefab.NamedEntityData;
import com.gempukku.secsy.entity.prefab.PrefabManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RegisterSystem(
        shared = PrefabManager.class)
public class TestPrefabManager implements PrefabManager {
    private Map<String, EntityData> prefabs = new HashMap<String, EntityData>();

    public void addPrefab(String name, EntityData entityData) {
        prefabs.put(name, entityData);
    }

    @Override
    public Iterable<? extends NamedEntityData> findPrefabsWithComponents(Class<? extends Component>... components) {
        List<NamedEntityData> result = new LinkedList<NamedEntityData>();
        for (final Map.Entry<String, EntityData> prefabData : prefabs.entrySet()) {
            if (hasComponents(prefabData.getValue(), components)) {
                final EntityData value = prefabData.getValue();
                result.add(new NamedEntityData() {
                    @Override
                    public String getName() {
                        return prefabData.getKey();
                    }

                    @Override
                    public Iterable<? extends ComponentData> getComponentsData() {
                        return value.getComponentsData();
                    }

                    @Override
                    public ComponentData getComponentData(Class<? extends Component> componentClass) {
                        return value.getComponentData(componentClass);
                    }

                    @Override
                    public boolean hasComponent(Class<? extends Component> componentClass) {
                        return value.hasComponent(componentClass);
                    }
                });
            }
        }

        return result;
    }

    private boolean hasComponents(EntityData value, Class<? extends Component>[] components) {
        for (Class<? extends Component> component : components) {
            if (!value.hasComponent(component))
                return false;
        }
        return true;
    }

    @Override
    public EntityData getPrefabByName(String name) {
        return prefabs.get(name);
    }
}
