/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import com.vaadin.flow.dom.ThemeList;

/**
 * Common logic for {@link VerticalLayout} and {@link HorizontalLayout} related to dynamic theme adjustment.
 * <p>
 * <b>Note:</b> Dynamic adjustment have effect only if the corresponding component theme supports it.
 *
 * @author Vaadin Ltd.
 */
public interface ThemableLayout extends HasElement {
    /**
     * Appends to or removes from {@code theme} attribute the {@code margin} value.
     * If a theme supports this attribute, it will apply or remove margin to the element.
     *
     * @param margin adds {@code margin} value to {@code theme} attribute if {@code true} or removes it if {@code false}
     */
    default void setMargin(boolean margin) {
        getThemeList().set("margin", margin);
    }

    /**
     * Appends to or removes from {@code theme} attribute the {@code padding} value.
     * If a theme supports this attribute, it will apply or remove padding to the element.
     *
     * @param padding adds {@code padding} value to {@code theme} attribute if {@code true} or removes it if {@code false}
     */
    default void setPadding(boolean padding) {
        getThemeList().set("padding", padding);
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
}
