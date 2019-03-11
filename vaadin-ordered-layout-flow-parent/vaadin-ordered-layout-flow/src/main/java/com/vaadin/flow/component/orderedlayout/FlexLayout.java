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

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

import java.util.Arrays;

/**
 * A layout component that implements Flexbox. It uses the default
 * flex-direction and doesn't have any predetermined width or height.
 * <p>
 * This component can be used as a base class for more advanced layouts.
 *
 * @see <a href=
 *      "https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Flexible_Box_Layout/Using_CSS_flexible_boxes">Using
 *      CSS Flexible boxes on MDN</a>
 */
@Tag(Tag.DIV)
public class FlexLayout extends Component
        implements FlexComponent<FlexLayout>, ClickNotifier<FlexLayout> {

    /**
     * Possible values for the {@code flex-wrap} CSS property, which determines how the elements inside the layout
     * should behave when they don't fit inside the layout.
     */
    public enum WrapMode {

        /**
         * If the items use up too much space they will overflow.
         */
        NOWRAP("nowrap"),

        /**
         * If items are not able to fit into a single row they are allowed to wrap into a follow up line.
         *
         * */
        WRAP("wrap"),

        /**
         * If items are not able to fit into a single row they are allowed to wrap into a follow up line.
         *  Additionally the order of the items will be reversed.
         */
        WRAP_REVERSE("wrap-reverse");

        private final String flexValue;

        WrapMode(String flexValue) {
            this.flexValue = flexValue;
        }

        String getFlexValue() {
            return flexValue;
        }

        static WrapMode toWrapMode(String flexValue, WrapMode defaultValue) {
            return Arrays.stream(values())
                    .filter(flexWrap -> flexWrap.getFlexValue()
                            .equals(flexValue))
                    .findFirst().orElse(defaultValue);
        }

    }

    /**
     * Default constructor. Creates an empty layout.
     */
    public FlexLayout() {
        getStyle().set("display", "flex");
    }

    /**
     * Convenience constructor to create a layout with the children already
     * inside it.
     *
     * @param children
     *            the items to add to this layout
     * @see #add(Component...)
     */
    public FlexLayout(Component... children) {
        this();
        add(children);
    }

    /**
     * Gets the {@link WrapMode} used by this layout.
     * <p>
     * The default flex wrap mode is {@link WrapMode#NOWRAP}.
     *
     * @param wrapMode the flex wrap mode of the layout, never
     *                     <code>null</code>
     */
    public void setWrapMode(WrapMode wrapMode) {
        if (wrapMode == null) {
            throw new IllegalArgumentException(
                    "The 'wrapMode' argument can not be null");
        }
        getElement().getStyle().set(FlexConstants.FLEX_WRAP_CSS_PROPERTY,
                wrapMode.getFlexValue());
    }

    /**
     * Gets the current flex wrap mode of the layout.
     * <p>
     * The default flex wrap mode is {@link WrapMode#NOWRAP}.
     *
     * @return the flex wrap mode used by the layout, never
     * <code>null</code>
     */
    public WrapMode getWrapMode() {
        return WrapMode.toWrapMode(
                getElement().getStyle()
                        .get(FlexConstants.FLEX_WRAP_CSS_PROPERTY),
                WrapMode.NOWRAP);
    }

}
