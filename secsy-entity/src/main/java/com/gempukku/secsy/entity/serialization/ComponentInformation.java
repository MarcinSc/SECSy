package com.gempukku.secsy.entity.serialization;

import com.gempukku.secsy.entity.Component;
import com.gempukku.secsy.entity.io.ComponentData;

import java.util.HashMap;
import java.util.Map;

public class ComponentInformation implements ComponentData {
    private Class<? extends Component> clazz;
    private Map<String, Object> fields = new HashMap<String, Object>();

    public ComponentInformation(Class<? extends Component> clazz) {
        this.clazz = clazz;
    }

    public ComponentInformation(ComponentData toCopy) {
        this(toCopy.getComponentClass());

        toCopy.outputFields(
                new ComponentDataOutput() {
                    @Override
                    public void addField(String field, Object value) {
                        fields.put(field, value);
                    }
                }
        );
    }

    @Override
    public Class<? extends Component> getComponentClass() {
        return clazz;
    }

    public void addField(String name, Object value) {
        fields.put(name, value);
    }

    @Override
    public void outputFields(ComponentDataOutput output) {
        for (Map.Entry<String, Object> field : fields.entrySet()) {
            output.addField(field.getKey(), field.getValue());
        }
    }
}
