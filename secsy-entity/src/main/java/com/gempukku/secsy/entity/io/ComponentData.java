package com.gempukku.secsy.entity.io;

import com.gempukku.secsy.entity.Component;

public interface ComponentData {
    Class<? extends Component> getComponentClass();

    void outputFields(ComponentDataOutput output);

    interface ComponentDataOutput {
        void addField(String field, Object value);
    }
}
