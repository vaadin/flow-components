package com.vaadin.flow.component.gridpro;

/*
 * #%L
 * Vaadin GridPro
 * %%
 * Copyright (C) 2018 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

/**
 * Set of editor types applicable for editor of the {@code vaadin-grid-edit-column} component.
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
