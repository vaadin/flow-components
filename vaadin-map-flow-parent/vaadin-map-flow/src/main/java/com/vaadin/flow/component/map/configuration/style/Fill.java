package com.vaadin.flow.component.map.configuration.style;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
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
