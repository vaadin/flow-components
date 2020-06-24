package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud
 * %%
 * Copyright (C) 2018 Vaadin Ltd
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

import java.util.Arrays;

/**
 * Enum with the possible values for the crud's editor position.
 */
public enum CrudEditorPosition {

    /**
     * Editor form is positioned inside the overlay
     */
    OVERLAY(""),

    /**
     * Editor form is positioned below the grid
     */
    BOTTOM("bottom"),

    /**
     * Editor form is positioned on the grid side
     * <p>
     * - <code>right</code> - if ltr <br>
     * - <code>left</code> - if rtl
     */
    ASIDE("aside");

    private final String editorPosition;

    CrudEditorPosition(String editorPosition) {
        this.editorPosition = editorPosition;
    }

    String getEditorPosition() {
        return editorPosition;
    }

    static CrudEditorPosition toPosition(String editorPosition, CrudEditorPosition defaultValue) {
        return Arrays.stream(values()).filter(
                alignment -> alignment.getEditorPosition().equals(editorPosition))
                .findFirst().orElse(defaultValue);
    }
}