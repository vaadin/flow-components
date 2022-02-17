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

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
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
        implements FlexComponent, ClickNotifier<FlexLayout> {

    /**
     * Enum with the possible values for the component alignment inside the
     * layout. It correlates to the <code>align-items</code> CSS property.
     */
    public enum ContentAlignment {

        /**
         * Items are positioned at the beginning of the container.
         */
        START("flex-start"),

        /**
         * Items are positioned at the end of the container.
         */
        END("flex-end"),

        /**
         * Items are positioned at the center of the container.
         */
        CENTER("center"),

        /**
         * Items are stretched to fit the container.
         */
        STRETCH("stretch"),

        /**
         * Items are distributed evenly inside the container. The first item is
         * flush with the start, the last is flush with the end.
         */
        SPACE_BETWEEN("space-between"),

        /**
         * Items are distributed evenly inside the container. Items have a
         * half-size space on either end.
         */
        SPACE_AROUND("space-around");

        private final String flexValue;

        ContentAlignment(String flexValue) {
            this.flexValue = flexValue;
        }

        String getFlexValue() {
            return flexValue;
        }

        static ContentAlignment toAlignment(String flexValue,
                ContentAlignment defaultValue) {
            return Arrays.stream(values()).filter(
                    alignment -> alignment.getFlexValue().equals(flexValue))
                    .findFirst().orElse(defaultValue);
        }
    }

    /**
     * Possible values for the {@code flex-direction} CSS property, which
     * determines how the elements are placed inside the layout.
     */
    public enum FlexDirection {

        /**
         * The items are displayed horizontally, as a row.
         */
        ROW("row"),

        /**
         * The items are displayed horizontally, as a row in reverse order.
         */
        ROW_REVERSE("row-reverse"),

        /**
         * The items are displayed vertically, as a column.
         */
        COLUMN("column"),

        /**
         * The items are displayed vertically, as a column in reverse order.
         */
        COLUMN_REVERSE("column-reverse");

        private final String directionValue;

        FlexDirection(String directionValue) {
            this.directionValue = directionValue;
        }

        String getDirectionValue() {
            return directionValue;
        }

        static FlexDirection toFlexDirection(String flexValue,
                FlexDirection defaultValue) {
            return Arrays.stream(values())
                    .filter(flexDirection -> flexDirection.getDirectionValue()
                            .equals(flexValue))
                    .findFirst().orElse(defaultValue);
        }
    }

    /**
     * Possible values for the {@code flex-wrap} CSS property, which determines
     * how the elements inside the layout should behave when they don't fit
     * inside the layout.
     */
    public enum FlexWrap {

        /**
         * If the items use up too much space they will overflow.
         */
        NOWRAP("nowrap"),

        /**
         * If items are not able to fit into a single row they are allowed to
         * wrap into a follow up line.
         *
         */
        WRAP("wrap"),

        /**
         * If items are not able to fit into a single row they are allowed to
         * wrap into a follow up line. Additionally the order of the items will
         * be reversed.
         */
        WRAP_REVERSE("wrap-reverse");

        private final String flexValue;

        FlexWrap(String flexValue) {
            this.flexValue = flexValue;
        }

        String getFlexValue() {
            return flexValue;
        }

        static FlexWrap toFlexWrap(String flexValue, FlexWrap defaultValue) {
            return Arrays.stream(values()).filter(
                    flexWrap -> flexWrap.getFlexValue().equals(flexValue))
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
     * Gets the {@link FlexWrap} used by this layout.
     * <p>
     * The default flex wrap is {@link FlexWrap#NOWRAP}.
     *
     * @param flexWrap
     *            the flex wrap of the layout, never <code>null</code>
     */
    public void setFlexWrap(FlexWrap flexWrap) {
        if (flexWrap == null) {
            throw new IllegalArgumentException(
                    "The 'flexWrap' argument can not be null");
        }
        getElement().getStyle().set(FlexConstants.FLEX_WRAP_CSS_PROPERTY,
                flexWrap.getFlexValue());
    }

    /**
     * Gets the current flex wrap of the layout.
     * <p>
     * The default flex wrap is {@link FlexWrap#NOWRAP}.
     *
     * @return the flex wrap used by the layout, never <code>null</code>
     */
    public FlexWrap getFlexWrap() {
        return FlexWrap.toFlexWrap(getElement().getStyle()
                .get(FlexConstants.FLEX_WRAP_CSS_PROPERTY), FlexWrap.NOWRAP);
    }

    /**
     * Similar to {@link #setAlignItems(Alignment)}, but instead of aligning
     * components, it aligns flex lines.
     * <p>
     * It effectively sets the {@code "alignContent"} style value.
     * <p>
     * The default alignment is {@link ContentAlignment#STRETCH}.
     *
     * @param alignment
     *            the alignment to apply to the components. Setting
     *            <code>null</code> will reset the alignment to its default
     */
    public void setAlignContent(ContentAlignment alignment) {
        if (alignment == null) {
            getStyle().remove(FlexConstants.ALIGN_CONTENT_CSS_PROPERTY);
        } else {
            getStyle().set(FlexConstants.ALIGN_CONTENT_CSS_PROPERTY,
                    alignment.getFlexValue());
        }
    }

    /**
     * Gets the current align content property of the layout.
     *
     * @return the align content property, or {@link ContentAlignment#STRETCH}
     *         if none was set
     */
    public ContentAlignment getAlignContent() {
        return ContentAlignment.toAlignment(
                getElement().getStyle()
                        .get(FlexConstants.ALIGN_CONTENT_CSS_PROPERTY),
                ContentAlignment.STRETCH);
    }

    /**
     * Sets the flex basis property of the components inside the layout. The
     * flex basis property specifies the initial main size of a component.
     *
     * @param width
     *            the width for the components. Setting <code>null</code> will
     *            remove the flex basis property
     * @param elementContainers
     *            the containers (components) to apply the flex basis property
     */
    public void setFlexBasis(String width, HasElement... elementContainers) {
        if (width == null) {
            for (HasElement element : elementContainers) {
                element.getElement().getStyle()
                        .remove(FlexConstants.FLEX_BASIS_CSS_PROPERTY);
            }
        } else {
            for (HasElement element : elementContainers) {
                element.getElement().getStyle()
                        .set(FlexConstants.FLEX_BASIS_CSS_PROPERTY, width);
            }
        }
    }

    /**
     * Gets the flex basis property of a given element container.
     *
     * @param elementContainer
     *            the element container to read the flex basis property from
     * @return the flex grow property
     */
    public String getFlexBasis(HasElement elementContainer) {
        return elementContainer.getElement().getStyle()
                .get(FlexConstants.FLEX_BASIS_CSS_PROPERTY);
    }

    /**
     * Sets the flex direction property of the layout. The flex direction
     * property specifies how components are placed in the layout defining the
     * main axis and the direction (normal or reversed).
     *
     * The default direction is {@link FlexDirection#ROW}.
     *
     * @param flexDirection
     *            the direction for the components. Setting <code>null</code>
     *            will remove the flex direction property
     */
    public void setFlexDirection(FlexDirection flexDirection) {
        if (flexDirection == null) {
            getElement().getStyle()
                    .remove(FlexConstants.FLEX_DIRECTION_CSS_PROPERTY);
        } else {
            getElement().getStyle().set(
                    FlexConstants.FLEX_DIRECTION_CSS_PROPERTY,
                    flexDirection.getDirectionValue());
        }
    }

    /**
     * Gets the flex direction property of a given element container.
     *
     * @param elementContainer
     *            the element container to read the flex direction property from
     * @return the flex direction property, or {@link FlexDirection#ROW} if none
     *         was set
     */
    public FlexDirection getFlexDirection(HasElement elementContainer) {
        return FlexDirection.toFlexDirection(
                elementContainer.getElement().getStyle()
                        .get(FlexConstants.FLEX_DIRECTION_CSS_PROPERTY),
                FlexDirection.ROW);
    }

    /**
     * Sets the flex shrink property of the components inside the layout. The
     * flex shrink property specifies how the item will shrink relative to the
     * rest of the components inside the same layout.
     *
     * Negative values are not allowed.
     *
     * The default value is 1.
     *
     * @param flexShrink
     *            how much the component will shrink relative to the rest of the
     *            components
     * @param elementContainers
     *            the containers (components) to apply the flex shrink property
     */
    public void setFlexShrink(double flexShrink,
            HasElement... elementContainers) {
        if (flexShrink < 0) {
            throw new IllegalArgumentException(
                    "Flex shrink property cannot be negative");
        }

        for (HasElement container : elementContainers) {
            container.getElement().getStyle().set(
                    FlexConstants.FLEX_SHRINK_CSS_PROPERTY,
                    String.valueOf(flexShrink));
        }
    }

    /**
     * Gets the flex shrink property of a given element container.
     *
     * @param elementContainer
     *            the element container to read the flex shrink property from
     * @return the flex shrink property, or 1 if none was set
     */
    public double getFlexShrink(HasElement elementContainer) {
        String ratio = elementContainer.getElement().getStyle()
                .get(FlexConstants.FLEX_SHRINK_CSS_PROPERTY);
        if (ratio == null || ratio.isEmpty()) {
            return 1;
        }

        try {
            return Double.parseDouble(ratio);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "The flex shrink property of the element container is not parseable to double: "
                            + ratio,
                    e);
        }
    }

    /**
     * Sets the order property of the component inside the layout. The order
     * property specifies the order of a component relative to the rest of the
     * components inside the same layout.
     *
     * The default value is 0, and setting 0 can be used to remove an existing
     * order for a component.
     *
     * @param order
     *            the order for the component
     * @param elementContainer
     *            the container (component) to apply the order property
     */
    public void setOrder(int order, HasElement elementContainer) {
        if (order == 0) {
            elementContainer.getElement().getStyle()
                    .remove(FlexConstants.ORDER_CSS_PROPERTY);
        }
        elementContainer.getElement().getStyle()
                .set(FlexConstants.ORDER_CSS_PROPERTY, String.valueOf(order));
    }

    /**
     * Gets the order property of a given element container.
     *
     * @param elementContainer
     *            the element container to read the order property from
     * @return the order property, or 0 if none was set
     */
    public int getOrder(HasElement elementContainer) {
        String order = elementContainer.getElement().getStyle()
                .get(FlexConstants.ORDER_CSS_PROPERTY);

        if (order == null || order.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(order);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "The order property of the element container is not parseable to integer: "
                            + order,
                    e);
        }
    }
}
