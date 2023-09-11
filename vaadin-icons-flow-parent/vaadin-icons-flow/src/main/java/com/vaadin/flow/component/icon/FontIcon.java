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

import java.util.Optional;

import com.vaadin.flow.dom.ElementConstants;

/**
 * Component for displaying an icon from a font icon collection. Note that the
 * icon font must be loaded separately. One way to do this is by including it in
 * the application's theme.
 *
 * @author Vaadin Ltd
 */
public class FontIcon extends AbstractIcon<FontIcon> {

    /**
     * Default constructor. Creates an empty font icon.
     */
    public FontIcon() {
    }

    /**
     * Creates a font icon component with the given icon class names.
     *
     * Example: <code>new FontIcon("fa-solid", "fa-user")</code>.
     *
     * @param iconClassNames
     *            The icon class names, not null
     * @see #setIconClassNames(String...)
     */
    public FontIcon(String... iconClassNames) {
        setIconClassNames(iconClassNames);
    }

    /**
     * Sets the icon class names defining an icon font and/or a specific glyph
     * inside an icon font.
     *
     * Example: <code>setIconClassNames("fa-solid", "fa-user")</code>.
     *
     * @param iconClassNames
     *            The icon class names, not null
     */
    public void setIconClassNames(String... iconClassNames) {
        getElement().setProperty("icon-class", iconClassNames.length == 0 ? null
                : String.join(" ", iconClassNames));
    }

    /**
     * Gets the icon class names defining an icon font and/or a specific glyph
     * inside an icon font.
     *
     * @return The icon class names
     */
    public String[] getIconClassNames() {
        return Optional.ofNullable(getElement().getProperty("icon-class"))
                .map(f -> f.split(" ")).orElse(new String[0]);
    }

    /**
     * Sets the font family to use for the font icon.
     *
     * @param fontFamily
     *            the font family to use
     */
    public void setFontFamily(String fontFamily) {
        getElement().setProperty("fontFamily", fontFamily);
    }

    /**
     * Gets the font family to use for the font icon.
     *
     * @return the font family to use
     */
    public String getFontFamily() {
        return getElement().getProperty("fontFamily");
    }

    /**
     * Sets the specific glyph from a font to use as an icon. Can be a code
     * point or a ligature name.
     *
     * @param charCode
     *            the character code to use
     */
    public void setCharCode(String charCode) {
        getElement().setProperty("char", charCode);
    }

    /**
     * Gets the specific glyph from a font to use as an icon. Can be a code
     * point or a ligature name.
     *
     * @return the character code to use
     */
    public String getCharCode() {
        return getElement().getProperty("char");
    }

    @Override
    public void setColor(String color) {
        getStyle().set(ElementConstants.STYLE_COLOR, color);
    }

    @Override
    public String getColor() {
        return getStyle().get(ElementConstants.STYLE_COLOR);
    }

}
