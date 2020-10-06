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
package com.vaadin.flow.component.icon;

import java.util.Locale;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Component for displaying an icon from the
 * <a href="https://vaadin.com/icons">Vaadin Icons</a> collection.
 *
 * @author Vaadin Ltd
 * @see VaadinIcon
 */
@NpmPackage(value = "@vaadin/vaadin-icons", version = "20.0.0-alpha5")
@JsModule("@vaadin/vaadin-icons/vaadin-icons.js")
public class Icon extends IronIcon {

    private static final String ICON_COLLECTION_NAME = "vaadin";

    /**
     * Creates an Icon component that displays a Vaadin logo.
     */
    public Icon() {
        this(VaadinIcon.VAADIN_H);
    }

    /**
     * Creates an Icon component that displays the given icon from
     * {@link VaadinIcon}.
     *
     * @param icon
     *            the icon to display
     */
    public Icon(VaadinIcon icon) {
        super(ICON_COLLECTION_NAME, 
                icon.name().toLowerCase(Locale.ENGLISH).replace('_', '-'));
    }

    /**
     * Creates an Icon component that displays the given icon from vaadin-icons
     * collection.
     *
     * @param icon
     *            the icon name
     */
    public Icon(String icon) {
        super(ICON_COLLECTION_NAME, icon);
    }

    /**
     * Creates an Icon component that displays the given {@code icon} from the
     * given {@code collection}.
     *
     * @param collection
     *            the icon collection
     * @param icon
     *            the icon name
     * @deprecated Use either {@link #Icon(String)} or
     *             {@link IronIcon#IronIcon(String,String)
     *             IronIcon(String,String)}
     */
    @Deprecated
    public Icon(String collection, String icon) {
        super(collection, icon);
    }

}