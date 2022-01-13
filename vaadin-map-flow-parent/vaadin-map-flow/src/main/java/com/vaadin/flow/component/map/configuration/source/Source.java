package com.vaadin.flow.component.map.configuration.source;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Constants;

public abstract class Source extends AbstractConfigurationObject {
    @Override
    public String getType() {
        return Constants.OL_SOURCE_SOURCE;
    }
}
