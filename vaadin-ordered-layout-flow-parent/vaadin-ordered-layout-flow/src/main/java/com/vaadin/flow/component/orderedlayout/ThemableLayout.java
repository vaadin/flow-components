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
package com.vaadin.flow.component.orderedlayout;

import java.util.Set;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.dom.ThemeList;

/**
 * Common logic for {@link VerticalLayout} and {@link HorizontalLayout} related
 * to dynamic theme adjustment.
 * <p>
 * <b>Note:</b> Dynamic adjustment have effect only if the corresponding
 * component theme supports it.
 *
 * @author Vaadin Ltd.
 */
public interface ThemableLayout extends HasElement {
    /**
     * Toggles {@code margin} theme setting for the element. If a theme supports
     * this attribute, it will apply or remove margin to the element.
     *
     * @param margin
     *            adds {@code margin} theme setting if {@code true} or removes
     *            it if {@code false}
     */
    default void setMargin(boolean margin) {
        getThemeList().set("margin", margin);
    }

    /**
     * Shows if {@code margin} theme setting is applied to the component.
     *
     * @return {@code true} if theme setting is applied, {@code false} otherwise
     */
    default boolean isMargin() {
        return getThemeList().contains("margin");
    }

    /**
     * Toggles {@code padding} theme setting for the element. If a theme
     * supports this attribute, it will apply or remove padding to the element.
     *
     * @param padding
     *            adds {@code padding} theme setting if {@code true} or removes
     *            it if {@code false}
     */
    default void setPadding(boolean padding) {
        getThemeList().set("padding", padding);
    }

    /**
     * Shows if {@code padding} theme setting is applied to the component.
     *
     * @return {@code true} if theme setting is applied, {@code false} otherwise
     */
    default boolean isPadding() {
        return getThemeList().contains("padding");
    }

    /**
     * Toggles {@code spacing} theme setting for the element. If a theme
     * supports this attribute, it will apply or remove spacing to the element.
     * <p>
     * This method adds medium spacing to the component theme, to set other
     * options, use {@link ThemableLayout#getThemeList()}. List of options
     * possible:
     * <ul>
     * <li>spacing-xs
     * <li>spacing-s
     * <li>spacing
     * <li>spacing-l
     * <li>spacing-xl
     * </ul>
     *
     * @param spacing
     *            adds {@code spacing} theme setting if {@code true} or removes
     *            it if {@code false}
     */
    default void setSpacing(boolean spacing) {
        getThemeList().set("spacing", spacing);
    }

    /**
     * Shows if {@code spacing} theme setting is applied to the component.
     *
     * @return {@code true} if theme setting is applied, {@code false} otherwise
     */
    default boolean isSpacing() {
        return getThemeList().contains("spacing");
    }

    /**
     * Gets the set of the theme names applied to the corresponding element in
     * {@code theme} attribute. The set returned can be modified to add or
     * remove the theme names, changes to the set will be reflected in the
     * attribute value.
     * <p>
     * Despite the name implying a list being returned, the return type is
     * actually a {@link Set} since the in-browser return value behaves like a
     * {@link Set} in Java.
     *
     * @return a list of theme names, never {@code null}
     */
    default ThemeList getThemeList() {
        return getElement().getThemeList();
    }

    /**
     * Sets the {@code box-sizing} CSS property of the layout.
     *
     * @param boxSizing
     *            the box-sizing of the layout. <code>null</code> is interpreted
     *            as {@link BoxSizing#UNDEFINED}
     * @see BoxSizing
     */
    default void setBoxSizing(BoxSizing boxSizing) {
        Style style = getElement().getStyle();
        if (boxSizing == null || boxSizing == BoxSizing.UNDEFINED) {
            style.remove("boxSizing");
        } else {
            switch (boxSizing) {
            case CONTENT_BOX:
                style.set("boxSizing", "content-box");
                break;
            case BORDER_BOX:
                style.set("boxSizing", "border-box");
                break;
            }
        }
    }

    /**
     * Gets the box-sizing defined for the layout, or
     * {@link BoxSizing#UNDEFINED} if none was defined on the server-side.
     *
     * @return the box-sizing, never <code>null</code>
     * @see BoxSizing
     */
    default BoxSizing getBoxSizing() {
        Style style = getElement().getStyle();
        String boxSizing = style.get("boxSizing");
        if (boxSizing == null) {
            return BoxSizing.UNDEFINED;
        }
        switch (boxSizing) {
        case "content-box":
            return BoxSizing.CONTENT_BOX;
        case "border-box":
            return BoxSizing.BORDER_BOX;
        default:
            return BoxSizing.UNDEFINED;
        }
    }
}
