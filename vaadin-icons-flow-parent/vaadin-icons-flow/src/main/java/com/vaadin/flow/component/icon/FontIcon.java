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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.ElementConstants;

/**
 * Component for displaying an icon from a font icon collection.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-icon")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.2.0-alpha15")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/icon", version = "24.2.0-alpha15")
@JsModule("@vaadin/icon/src/vaadin-icon.js")
public class FontIcon extends AbstractIcon<FontIcon> {

    /**
     * Default constructor. Creates an empty font icon.
     */
    public FontIcon() {
    }

    /**
     * Creates a font icon component with the given font class names.
     *
     * @param font
     *            The font class names, not null
     * @see #setFont(String...)
     */
    public FontIcon(String... font) {
        setFont(font);
    }

    /**
     * Sets the font class names defining an icon font and/or a specific glyph
     * inside an icon font.
     *
     * @param font
     *            The font class names, not null
     */
    public void setFont(String... font) {
        getElement().setProperty("font",
                font.length == 0 ? null : String.join(" ", font));
    }

    /**
     * Gets the font class names defining an icon font and/or a specific glyph
     * inside an icon font.
     *
     * @return The font class names
     */
    public String[] getFont() {
        return Optional.ofNullable(getElement().getProperty("font"))
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
