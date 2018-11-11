package com.gempukku.secsy.context;

import com.gempukku.secsy.context.system.ShareSystemInitializer;
import com.gempukku.secsy.context.system.SimpleContext;
import com.gempukku.secsy.context.system.SystemProducer;

import java.util.Collections;
import java.util.Map;

public class SECSyContext extends SimpleContext {
    public SECSyContext(SystemContext parentContext, SystemProducer systemProducer) {
        this(parentContext, systemProducer, Collections.<Class<?>, Object>emptyMap());
    }

    public SECSyContext(SystemContext parentContext, SystemProducer systemProducer,
                        Map<Class<?>, Object> additionalSystems) {
        super(parentContext);
        setSystemProducer(systemProducer);
        ShareSystemInitializer initializer = new ShareSystemInitializer(additionalSystems);
        setObjectInitializer(initializer);
        setSystemExtractor(initializer);
    }

    public SECSyContext(SystemProducer systemProducer) {
        this(null, systemProducer);
    }
}
