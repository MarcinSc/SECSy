package com.gempukku.secsy.component.annotation;

import com.gempukku.secsy.Component;

public interface SampleComponent extends Component {
    @GetProperty("value")
    public String getValue();

    @SetProperty("value")
    public void setValue(String value);

    public void undefinedMethod();
}