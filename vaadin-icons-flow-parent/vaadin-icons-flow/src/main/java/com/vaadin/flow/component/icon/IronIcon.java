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

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.ElementConstants;

/**
 * Server side component for
 * <a href="https://github.com/PolymerElements/iron-icon">iron-icon</a> element
 * to display an icon.
 *
 * @author Vaadin Ltd
 *
 * @deprecated since Vaadin 21, {@code IronIcon} is deprecated in favor of
 *             {@link Icon}
 */
@Tag("iron-icon")
@NpmPackage(value = "@polymer/iron-icon", version = "3.0.1")
@JsModule("@polymer/iron-icon/iron-icon.js")
@Deprecated
public class IronIcon extends Component
        implements HasStyle, ClickNotifier<IronIcon> {
    private static final String ICON_ATTRIBUTE_NAME = "icon";

    /**
     * Creates an Icon component that displays the given {@code icon} from the
     * given {@code collection}.
     *
     * @param collection
     *            the icon collection
     * @param icon
     *            the icon name
     */
    public IronIcon(String collection, String icon) {
        // iron-icon's icon-attribute uses the format "collection:name",
        // e.g. icon="vaadin:arrow-down"
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
            getStyle().remove(ElementConstants.STYLE_COLOR);
        } else {
            getStyle().set(ElementConstants.STYLE_COLOR, color);
        }
    }

    /**
     * Gets the fill color of this icon as a String.
     *
     * @return the fill color of the icon, or <code>null</code> if the color has
     *         not been set
     */
    public String getColor() {
        return getStyle().get(ElementConstants.STYLE_COLOR);
    }
}
