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

import com.vaadin.flow.dom.ElementConstants;

/**
 * Component for displaying an icon from a SVG file.
 */
public class SvgIcon extends AbstractIcon {
    private static final String STYLE_FILL = "fill";

    /**
     * Default constructor. Creates an empty SVG icon.
     */
    public SvgIcon() {
    }

    /**
     * Creates an SVG icon with the given source
     *
     * @param source
     *            the SVG file path
     * @see #setSource(String)
     */
    public SvgIcon(String source) {
        setSource(source);
    }

    /**
     * Defines the path of the SVG file to be used as the icon. The value can
     * be:
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
     * @param source
     *            the source file of the icon
     */
    public void setSource(String source) {
        getElement().setProperty("src", source);
    }

    /**
     * Gets the source defined in the icon
     *
     * @return the source defined or {@code null}
     */
    public String getSource() {
        return getElement().getProperty("src");
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
