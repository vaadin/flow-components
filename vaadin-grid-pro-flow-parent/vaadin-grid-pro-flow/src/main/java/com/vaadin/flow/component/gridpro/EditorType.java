/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

/*
 * #%L
 * Vaadin GridPro
 * %%
 * Copyright (C) 2018 - 2020 Vaadin Ltd
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

/**
 * Set of editor types applicable for editor of the
 * {@code vaadin-grid-edit-column} component.
 */
public enum EditorType {
    TEXT("text"), CHECKBOX("checkbox"), SELECT("select"), CUSTOM("custom");

    private final String type;

    EditorType(String type) {
        this.type = type;
    }

    /**
     * Gets the type name.
     *
     * @return type name
     */
    public String getTypeName() {
        return type;
    }
}
