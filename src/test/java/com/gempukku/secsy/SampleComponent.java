package com.gempukku.secsy;

import com.gempukku.secsy.component.annotation.GetProperty;
import com.gempukku.secsy.component.annotation.SetProperty;

public interface SampleComponent extends Component {
    @GetProperty("value")
    public String getValue();

    @SetProperty("value")
    public void setValue(String value);

    public void undefinedMethod();
}