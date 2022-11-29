package com.vaadin.flow.component.map.configuration.style;

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Constants;

public class Fill extends AbstractConfigurationObject {

    private String color;

    @Override
    public String getType() {
        return Constants.OL_STYLE_FILL;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        markAsDirty();
    }
}
