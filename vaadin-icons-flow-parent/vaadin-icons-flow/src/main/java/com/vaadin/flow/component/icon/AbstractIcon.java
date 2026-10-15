/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.icon;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.dom.ElementConstants;

/**
 * Abstract base class for icon components
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-icon")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.4.20")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/icon", version = "24.4.20")
@JsModule("@vaadin/icon/src/vaadin-icon.js")
public abstract class AbstractIcon<T extends AbstractIcon<T>> extends Component
        implements ClickNotifier<T>, HasTooltip {

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
     * Sets the color of the icon.
     * <p>
     * The color should be in a format understood by the browser, e.g. "orange",
     * "#FF9E2C" or "rgb(255, 158, 44)".
     *
     * @param color
     *            the color to set, may be <code>null</code> to clear the value
     */
    public abstract void setColor(String color);

    /**
     * Gets the color of this icon as a String.
     *
     * @return the color of the icon, or <code>null</code> if the color has not
     *         been set
     */
    public abstract String getColor();
}
