/*
 * Copyright 2000-2025 Vaadin Ltd.
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
@NpmPackage(value = "@vaadin/icons", version = "24.8.0-alpha18")
@JsModule("@vaadin/icons/vaadin-iconset.js")
public class Icon extends AbstractIcon<Icon> {

    private static final String ICON_ATTRIBUTE_NAME = "icon";
    private static final String VAADIN_ICON_COLLECTION_NAME = "vaadin";
    private static final String STYLE_FILL = "fill";

    /**
     * Creates an empty Icon.
     */
    public Icon() {
    }

    /**
     * Creates an Icon component that displays the given icon from
     * {@link VaadinIcon}.
     *
     * @param icon
     *            the icon to display
     */
    public Icon(VaadinIcon icon) {
        setIcon(icon);
    }

    /**
     * Creates an Icon component that displays the given icon from vaadin-icons
     * collection.
     *
     * @param icon
     *            the icon name
     */
    public Icon(String icon) {
        setIcon(icon);
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
        setIcon(collection, icon);
    }

    /**
     * Sets the icon to the given icon.
     * <p>
     * If the icon name contains a ":", the first part is used as the collection
     * and the second part as the icon name. If the icon name does not contain a
     * ":", the current collection is used (vaadin by default).
     *
     * @param icon
     *            the icon name
     */
    public void setIcon(String icon) {
        if (icon.contains(":")) {
            String[] parts = icon.split(":", 2);
            setIcon(parts[0], parts[1]);
        } else {
            String collection = getCollection();
            if (collection == null) {
                collection = VAADIN_ICON_COLLECTION_NAME;
            }
            setIcon(collection, icon);
        }
    }

    /**
     * Sets the icon to the given Vaadin icon.
     *
     * @param icon
     *            the icon name
     */
    public void setIcon(VaadinIcon icon) {
        setIcon(VAADIN_ICON_COLLECTION_NAME,
                icon.name().toLowerCase(Locale.ENGLISH).replace('_', '-'));
    }

    /**
     * Gets the full icon name, including the collection.
     *
     * @return the icon name or {@code null} if no icon is set
     */
    public String getIcon() {
        return getElement().getAttribute(ICON_ATTRIBUTE_NAME);
    }

    /**
     * Sets the icon to the given {@code icon} from the given
     * {@code collection}.
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
    public void setIcon(String collection, String icon) {
        getElement().setAttribute(ICON_ATTRIBUTE_NAME, collection + ':' + icon);
    }

    /**
     * Gets the collection of the icon (the part before {@literal :}).
     *
     * @return the collection of the icon or {@code null} if no collection is
     *         set
     */
    public String getCollection() {
        String icon = getIcon();
        if (icon != null && icon.contains(":")) {
            return icon.substring(0, icon.indexOf(':'));
        }
        return null;
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
