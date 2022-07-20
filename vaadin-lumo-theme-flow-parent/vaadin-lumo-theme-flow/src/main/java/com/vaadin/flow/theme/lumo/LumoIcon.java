/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.theme.lumo;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IconFactory;

import java.util.Locale;

/**
 * Enumeration of all icons in the
 * <a href= "https://vaadin.com/docs/latest/components/icons">Lumo Icons</a>
 * collection.
 * <p>
 * These instances can be used to create {@link Icon} components by using their
 * {@link #create()} method.
 * <p>
 * NOTE: Using this enum will also include the Vaadin icon set in the frontend
 * bundle.
 *
 * @author Vaadin Ltd
 */
public enum LumoIcon implements IconFactory {

    ALIGN_CENTER, ALIGN_LEFT, ALIGN_RIGHT, ANGLE_DOWN, ANGLE_LEFT, ANGLE_RIGHT, ANGLE_UP, ARROW_DOWN, ARROW_LEFT, ARROW_RIGHT, ARROW_UP, BAR_CHART, BELL, CALENDAR, CHECKMARK, CHEVRON_DOWN, CHEVRON_LEFT, CHEVRON_RIGHT, CHEVRON_UP, CLOCK, COG, CROSS, DOWNLOAD, DROPDOWN, EDIT, ERROR, EYE, EYE_DISABLED, MENU, MINUS, ORDERED_LIST, PHONE, PHOTO, PLAY, PLUS, REDO, RELOAD, SEARCH, UNDO, UNORDERED_LIST, UPLOAD, USER;

    /**
     * Creates a new {@link Icon} instance with the icon determined by the name
     * of this instance.
     *
     * @return a new instance of {@link Icon} component
     */
    public Icon create() {
        return new Icon("lumo",
                name().toLowerCase(Locale.ENGLISH).replace('_', '-'));
    }
}
