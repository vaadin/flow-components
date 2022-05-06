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
package com.vaadin.flow.component.icon;

import java.util.Locale;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.ElementConstants;

/**
 * Component for displaying an icon from the
 * <a href="https://vaadin.com/icons">Vaadin Icons</a> collection.
 *
 * @author Vaadin Ltd
 * @see VaadinIcon
 */
@Tag("vaadin-icon")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/icons", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-icons", version = "23.1.0-beta1")
@JsModule("@vaadin/icons/vaadin-iconset.js")
@NpmPackage(value = "@vaadin/icon", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-icon", version = "23.1.0-beta1")
@JsModule("@vaadin/icon/vaadin-icon.js")
public class Icon extends Component implements HasStyle, ClickNotifier<Icon> {

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

    /**
     * Sets the width and the height of the icon.
     * <p>
     * The size should be in a format understood by the browser, e.g. "100px" or
     * "2.5em".
     *
     * @param size
     *            the size to set, may be <code>null</code> to clear the value
     */
    public void setSize(String size) {
        if (size == null) {
            getStyle().remove(ElementConstants.STYLE_WIDTH);
            getStyle().remove(ElementConstants.STYLE_HEIGHT);
        } else {
            getStyle().set(ElementConstants.STYLE_WIDTH, size);
            getStyle().set(ElementConstants.STYLE_HEIGHT, size);
        }
    }

    /**
     * Sets the fill color of the icon.
     * <p>
     * The color should be in a format understood by the browser, e.g. "orange",
     * "#FF9E2C" or "rgb(255, 158, 44)".
     *
     * @param color
     *            the fill color to set, may be <code>null</code> to clear the
     *            value
     */
    public void setColor(String color) {
        if (color == null) {
            getStyle().remove(STYLE_FILL);
        } else {
            getStyle().set(STYLE_FILL, color);
        }
    }

    /**
     * Gets the fill color of this icon as a String.
     *
     * @return the fill color of the icon, or <code>null</code> if the color has
     *         not been set
     */
    public String getColor() {
        return getStyle().get(STYLE_FILL);
    }
}
