/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.ui.icon;

import com.vaadin.ui.common.HtmlImport;

/**
 * Component for displaying an icon from the
 * <a href="https://vaadin.com/icons">Vaadin Icons</a> collection.
 * 
 * @author Vaadin Ltd
 * @see VaadinIcons
 */
@HtmlImport("frontend://bower_components/vaadin-icons/vaadin-icons.html")
public class Icon extends IronIcon {

    private static final String ICON_COLLECTION_NAME = "vaadin";

    /**
     * Creates an Icon component that displays the given icon from
     * {@link VaadinIcons}.
     *
     * @param icon
     *            the icon to display
     */
    public Icon(VaadinIcons icon) {
        super(ICON_COLLECTION_NAME,
                icon.name().toLowerCase().replace('_', '-'));
    }
}
