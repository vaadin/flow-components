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

import java.util.Arrays;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;

/**
 * A component which implements Flexbox.
 */
public interface FlexComponent extends HasOrderedComponents, HasStyle, HasSize {

    /**
     * Enum with the possible values for the component alignment inside the
     * layout. It correlates to the <code>align-items</code> CSS property.
     */
    public enum Alignment {

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
         * Items are positioned at the baseline of the container.
         */
        BASELINE("baseline"),

        /**
         * The element inherits its parent container's align-items property, or
         * "stretch" if it has no parent container.
         */
        AUTO("auto");

        private final String flexValue;

        Alignment(String flexValue) {
            this.flexValue = flexValue;
        }

        String getFlexValue() {
            return flexValue;
        }

        static Alignment toAlignment(String flexValue, Alignment defaultValue) {
            return Arrays.stream(values()).filter(
                    alignment -> alignment.getFlexValue().equals(flexValue))
                    .findFirst().orElse(defaultValue);
        }
    }

    /**
     * Enum with the possible values for the way the extra space inside the
     * layout is distributed among the components. It correlates to the
     * <code>justify-content</code> CSS property.
     */
    public enum JustifyContentMode {

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
         * Items are positioned with space between the lines.
         */
        BETWEEN("space-between"),

        /**
         * Items are positioned with space before, between, and after the lines.
         */
        AROUND("space-around"),

        /**
         * Items have equal space around them.
         */
        EVENLY("space-evenly");

        private final String flexValue;

        JustifyContentMode(String flexValue) {
            this.flexValue = flexValue;
        }

        String getFlexValue() {
            return flexValue;
        }

        static JustifyContentMode toJustifyContentMode(String flexValue,
                JustifyContentMode defaultValue) {
            return Arrays.stream(values())
                    .filter(justifyContent -> justifyContent.getFlexValue()
                            .equals(flexValue))
                    .findFirst().orElse(defaultValue);
        }

    }

    /**
     * Sets the default alignment to be used by all components without
     * individual alignments inside the layout. Individual components can be
     * aligned by using the {@link #setAlignSelf(Alignment, HasElement...)}
     * method.
     * <p>
     * It effectively sets the {@code "alignItems"} style value.
     * <p>
     * The default alignment is {@link Alignment#STRETCH}.
     *
     * @param alignment
     *            the alignment to apply to the components. Setting
     *            <code>null</code> will reset the alignment to its default
     */
    default public void setAlignItems(Alignment alignment) {
        if (alignment == null) {
            getStyle().remove(FlexConstants.ALIGN_ITEMS_CSS_PROPERTY);
        } else {
            getStyle().set(FlexConstants.ALIGN_ITEMS_CSS_PROPERTY,
                    alignment.getFlexValue());
        }
    }

    /**
     * Gets the default alignment used by all components without individual
     * alignments inside the layout.
     * <p>
     * The default alignment is {@link Alignment#STRETCH}.
     *
     * @return the general alignment used by the layout, never <code>null</code>
     */
    default public Alignment getAlignItems() {
        return Alignment.toAlignment(
                getStyle().get(FlexConstants.ALIGN_ITEMS_CSS_PROPERTY),
                Alignment.STRETCH);
    }

    /**
     * Sets an alignment for individual element container inside the layout.
     * This individual alignment for the element container overrides any
     * alignment set at the {@link #setAlignItems(Alignment)}.
     * <p>
     * It effectively sets the {@code "alignSelf"} style value.
     * <p>
     * The default alignment for individual components is
     * {@link Alignment#AUTO}.
     *
     * @param alignment
     *            the individual alignment for the children components. Setting
     *            <code>null</code> will reset the alignment to its default
     * @param elementContainers
     *            The element containers (components) to which the individual
     *            alignment should be set
     */
    default public void setAlignSelf(Alignment alignment,
            HasElement... elementContainers) {
        if (alignment == null) {
            for (HasElement container : elementContainers) {
                container.getElement().getStyle()
                        .remove(FlexConstants.ALIGN_SELF_CSS_PROPERTY);
            }
        } else {
            for (HasElement container : elementContainers) {
                container.getElement().getStyle().set(
                        FlexConstants.ALIGN_SELF_CSS_PROPERTY,
                        alignment.getFlexValue());
            }
        }
    }

    /**
     * Gets the individual alignment of a given element container.
     * <p>
     * The default alignment for individual element containers is
     * {@link Alignment#AUTO}.
     *
     * @param container
     *            The element container (component) which individual layout
     *            should be read
     * @return the alignment of the container, never <code>null</code>
     */
    default public Alignment getAlignSelf(HasElement container) {
        return Alignment.toAlignment(container.getElement().getStyle()
                .get(FlexConstants.ALIGN_SELF_CSS_PROPERTY), Alignment.AUTO);
    }

    /**
     * Sets the flex grow property of the components inside the layout. The flex
     * grow property specifies what amount of the available space inside the
     * layout the component should take up, proportionally to the other
     * components.
     * <p>
     * For example, if all components have a flex grow property value set to 1,
     * the remaining space in the layout will be distributed equally to all
     * components inside the layout. If you set a flex grow property of one
     * component to 2, that component will take twice the available space as the
     * other components, and so on.
     * <p>
     * Setting to flex grow property value 0 disables the expansion of the
     * element container. Negative values are not allowed.
     *
     * @param flexGrow
     *            the proportion of the available space the element container
     *            should take up
     * @param elementContainers
     *            the containers (components) to apply the flex grow property
     */
    default public void setFlexGrow(double flexGrow,
            HasElement... elementContainers) {
        if (flexGrow < 0) {
            throw new IllegalArgumentException(
                    "Flex grow property cannot be negative");
        }
        if (flexGrow == 0) {
            for (HasElement container : elementContainers) {
                container.getElement().getStyle()
                        .remove(FlexConstants.FLEX_GROW_CSS_PROPERTY);
            }
        } else {
            for (HasElement container : elementContainers) {
                container.getElement().getStyle().set(
                        FlexConstants.FLEX_GROW_CSS_PROPERTY,
                        String.valueOf(flexGrow));
            }
        }
    }

    /**
     * Gets the flex grow property of a given element container.
     *
     * @param elementContainer
     *            the element container to read the flex grow property from
     * @return the flex grow property, or 0 if none was set
     */
    default public double getFlexGrow(HasElement elementContainer) {
        String ratio = elementContainer.getElement().getStyle()
                .get(FlexConstants.FLEX_GROW_CSS_PROPERTY);
        if (ratio == null || ratio.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(ratio);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "The flex grow property of the element container is not parseable to double: "
                            + ratio,
                    e);
        }
    }

    /**
     * Sets the {@link JustifyContentMode} used by this layout.
     * <p>
     * The default justify content mode is {@link JustifyContentMode#START}.
     *
     * @param justifyContentMode
     *            the justify content mode of the layout, never
     *            <code>null</code>
     */
    default public void setJustifyContentMode(
            JustifyContentMode justifyContentMode) {
        if (justifyContentMode == null) {
            throw new IllegalArgumentException(
                    "The 'justifyContentMode' argument can not be null");
        }
        getElement().getStyle().set(FlexConstants.JUSTIFY_CONTENT_CSS_PROPERTY,
                justifyContentMode.getFlexValue());
    }

    /**
     * Gets the current justify content mode of the layout.
     * <p>
     * The default justify content mode is {@link JustifyContentMode#START}.
     *
     * @return the justify content mode used by the layout, never
     *         <code>null</code>
     */
    default public JustifyContentMode getJustifyContentMode() {
        return JustifyContentMode.toJustifyContentMode(
                getElement().getStyle()
                        .get(FlexConstants.JUSTIFY_CONTENT_CSS_PROPERTY),
                JustifyContentMode.START);
    }

    /**
     * Expands the given components.
     * <p>
     * It effectively sets {@code 1} as a flex grow property value for each
     * component.
     *
     * @param componentsToExpand
     *            components to expand
     */
    default public void expand(Component... componentsToExpand) {
        setFlexGrow(1.0d, componentsToExpand);
    }

    @Override
    default public void replace(Component oldComponent,
            Component newComponent) {
        Alignment alignSelf = null;
        double flexGrow = 0;
        if (oldComponent != null) {
            alignSelf = getAlignSelf(oldComponent);
            flexGrow = getFlexGrow(oldComponent);
        }
        HasOrderedComponents.super.replace(oldComponent, newComponent);
        if (newComponent != null && oldComponent != null) {
            setAlignSelf(alignSelf, newComponent);
            setFlexGrow(flexGrow, newComponent);
        }
    }

}
