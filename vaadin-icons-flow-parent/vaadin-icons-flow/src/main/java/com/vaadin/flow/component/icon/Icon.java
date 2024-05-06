/*
 * Copyright 2000-2024 Vaadin Ltd.
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
@NpmPackage(value = "@vaadin/icons", version = "24.4.0-beta1")
@JsModule("@vaadin/icons/vaadin-iconset.js")
public class Icon extends AbstractIcon<Icon> {

    private static final String ICON_ATTRIBUTE_NAME = "icon";
    private static final String ICON_COLLECTION_NAME = "vaadin";
    private static final String STYLE_FILL = "fill";

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
        this(ICON_COLLECTION_NAME,
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
        this(ICON_COLLECTION_NAME, icon);
    }

    /**
     * Creates an Icon component that displays the given {@code icon} from the
     * given {@code collection}.
     *
     * If you want to use a custom {@code <vaadin-iconset>} -based icon set, you
     * also need to add a dependency and an import for it, example:
     *
     * <pre>
     * <code>
     * &#64;NpmPackage(value = "custom-icons", version = "1.0.0")
     * &#64;JsModule("custom-icons/iconset.js")
     * public class MyView extends Div {
     * </code>
     * </pre>
     *
     * @param collection
     *            the icon collection
     * @param icon
     *            the icon name
     */
    public Icon(String collection, String icon) {
        getElement().setAttribute(ICON_ATTRIBUTE_NAME, collection + ':' + icon);
    }

    @Override
    public void setColor(String color) {
        if (color == null) {
            getStyle().remove(STYLE_FILL);
        } else {
            getStyle().set(STYLE_FILL, color);
        }
    }

    @Override
    public String getColor() {
        return getStyle().get(STYLE_FILL);
    }
}
