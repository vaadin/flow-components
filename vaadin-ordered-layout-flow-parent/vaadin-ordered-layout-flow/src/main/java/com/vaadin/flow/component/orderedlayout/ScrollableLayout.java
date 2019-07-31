/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;

/**
 * A layout which can be set as scrollable when its content is overflowing.
 * 
 * @author Vaadin Ltd.
 */
public interface ScrollableLayout extends HasComponents, HasSize {

    /**
     * Sets whether scrollbars should be displayed when the content of this
     * component is overflowing.
     * <p>
     * Often the desired result is achieved by using this together with
     * {@link #setWidth(String)} or {@link #setHeight(String)}. In some cases,
     * when not setting absolute width or height, the size of the parent layout
     * needs to be adjusted as well.
     * <p>
     * Note that if the content doesn't overflow, scrollbars are not displayed
     * even when this is set to {@code true}.
     * 
     * @param scrollable
     *            {@code true} to enable scrolling when the content overflows,
     *            {@code false} to disable it
     */
    default void setScrollable(boolean scrollable) {
        getElement().getStyle().set("overflow", scrollable ? "auto" : null);
    }

    /**
     * Gets whether this component displays scrollbars when its content is
     * overflowing.
     * <p>
     * Note that if the content doesn't overflow, scrollbars are not displayed
     * even if this method returns {@code true}.
     * 
     * @return {@code true} if scrollbars are displayed when content overflows,
     *         {@code false} otherwise
     * @see #setScrollable(boolean)
     */
    default boolean isScrollable() {
        return "auto".equals(getElement().getStyle().get("overflow"));
    }

}
