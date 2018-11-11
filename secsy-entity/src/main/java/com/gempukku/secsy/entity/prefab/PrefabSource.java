package com.gempukku.secsy.entity.prefab;

import java.io.IOException;
import java.io.InputStream;

public interface PrefabSource {
    void processAllPrefabs(PrefabSink prefabSink) throws IOException;

    interface PrefabSink {
        void processPrefab(String prefabName, InputStream prefabData) throws IOException;
    }
}
