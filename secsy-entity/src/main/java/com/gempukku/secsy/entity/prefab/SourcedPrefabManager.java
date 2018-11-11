package com.gempukku.secsy.entity.prefab;

import com.gempukku.secsy.context.annotation.Inject;
import com.gempukku.secsy.context.annotation.RegisterSystem;
import com.gempukku.secsy.context.system.AbstractLifeCycleSystem;
import com.gempukku.secsy.entity.Component;
import com.gempukku.secsy.entity.component.ComponentFieldConverter;
import com.gempukku.secsy.entity.component.InternalComponentManager;
import com.gempukku.secsy.entity.io.ComponentData;
import com.gempukku.secsy.entity.io.EntityData;
import com.gempukku.secsy.entity.serialization.JSONEntitySerializer;
import com.gempukku.secsy.entity.serialization.NameComponentManager;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@RegisterSystem(
        profiles = "prefabManager",
        shared = PrefabManager.class)
public class SourcedPrefabManager extends AbstractLifeCycleSystem implements PrefabManager {
    @Inject
    private NameComponentManager nameComponentManager;
    @Inject
    private InternalComponentManager internalComponentManager;
    @Inject
    private ComponentFieldConverter componentFieldConverter;
    @Inject
    private PrefabSource prefabSource;

    private Map<String, NamedEntityData> prefabsByName;
    private JSONEntitySerializer jsonEntitySerializer = new JSONEntitySerializer();

    @Override
    public float getPriority() {
        return 100;
    }

    @Override
    public void initialize() {
        prefabsByName = new HashMap<String, NamedEntityData>();

        try {
            prefabSource.processAllPrefabs(
                    new PrefabSource.PrefabSink() {
                        @Override
                        public void processPrefab(String prefabName, InputStream prefabData) throws IOException {
                            try {
                                NamedEntityData namedEntityData = readPrefabData(prefabName, prefabData);
                                prefabsByName.put(prefabName, namedEntityData);
                            } catch (ParseException exp) {
                                throw new IOException("Unable to read prefab data", exp);
                            }
                        }
                    });
        } catch (IOException exp) {
            throw new RuntimeException("Unable to read prefab data", exp);
        }
    }

    @Override
    public Iterable<? extends NamedEntityData> findPrefabsWithComponents(final Class<? extends Component>... components) {
        return Iterables.filter(
                prefabsByName.values(),
                new Predicate<NamedEntityData>() {
                    @Override
                    public boolean apply(@Nullable NamedEntityData prefabData) {
                        for (Class<? extends Component> component : components) {
                            if (!prefabData.hasComponent(component))
                                return false;
                        }
                        return true;
                    }
                });
    }

    @Override
    public EntityData getPrefabByName(String name) {
        NamedEntityData result = prefabsByName.get(name);
        if (result == null)
            throw new IllegalArgumentException("Prefab with name not found - " + name);
        return result;
    }

    private NamedEntityData readPrefabData(final String prefabName, InputStream prefabInputStream) throws IOException, ParseException {
        final EntityData entityData = jsonEntitySerializer.readEntityData(
                new InputStreamReader(prefabInputStream, Charset.forName("UTF-8")),
                nameComponentManager, internalComponentManager, componentFieldConverter);
        return new NamedEntityData() {
            @Override
            public String getName() {
                return prefabName;
            }

            @Override
            public Iterable<? extends ComponentData> getComponentsData() {
                return entityData.getComponentsData();
            }

            @Override
            public ComponentData getComponentData(Class<? extends Component> componentClass) {
                return entityData.getComponentData(componentClass);
            }

            @Override
            public boolean hasComponent(Class<? extends Component> componentClass) {
                return entityData.hasComponent(componentClass);
            }
        };
    }
}
