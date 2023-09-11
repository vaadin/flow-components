/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamResource;

/**
 * Component for displaying an icon from a SVG file.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-icon")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.2.0-alpha15")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/icon", version = "24.2.0-alpha15")
@JsModule("@vaadin/icon/src/vaadin-icon.js")
public class SvgIcon extends AbstractIcon<SvgIcon> {
    private static final String STYLE_FILL = "fill";

    /**
     * Default constructor. Creates an empty SVG icon.
     */
    public SvgIcon() {
    }

    /**
     * Creates an SVG icon with the given source
     *
     * @param src
     *            the SVG file path
     * @see #setSrc(String)
     */
    public SvgIcon(String src) {
        setSrc(src);
    }

    /**
     * Creates an SVG icon with the given resource
     *
     * @param src
     *            the resource value
     * @see #setSrc(AbstractStreamResource)
     */
    public SvgIcon(AbstractStreamResource src) {
        setSrc(src);
    }

    /**
     * Sets the URL of the SVG file to be used as the icon. The value can be:
     * <ul>
     *
     * <li>A path to a standalone SVG file</li>
     * <li>
     * <p>
     * A path in the format `"path/to/file.svg#symbol-id"` to an SVG file, where
     * "symbol-id" refers to an id of an element (generally a
     * `<symbol></symbol>` element) to be rendered in the icon component.
     * </p>
     * <p>
     * Note that the sprite file needs to follow the same-origin policy
     * </p>
     * </li>
     * <li>Alternatively, the source can be defined as a string in the format
     * `"data:image/svg+xml,<svg>...</svg>`</li>
     * </ul>
     *
     * @param src
     *            the source file of the icon
     */
    public void setSrc(String src) {
        getElement().setAttribute("src", src);
    }

    /**
     * Defines the source of the icon from the given {@link StreamResource} The
     * resource must contain a valid SVG element.
     *
     * @param src
     *            the source value, not null
     */
    public void setSrc(AbstractStreamResource src) {
        getElement().setAttribute("src", src);
    }

    /**
     * Gets the source defined in the icon
     *
     * @return the source defined or {@code null}
     */
    public String getSrc() {
        return getElement().getAttribute("src");
    }

    @Override
    public void setColor(String color) {
        getStyle().set(STYLE_FILL, color);
    }

    @Override
    public String getColor() {
        return getStyle().get(STYLE_FILL);
    }
}
