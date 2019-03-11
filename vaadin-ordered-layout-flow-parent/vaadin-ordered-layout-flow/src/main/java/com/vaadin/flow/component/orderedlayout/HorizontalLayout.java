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
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

/**
 * HorizontalLayout is a component container, which shows the subcomponents in
 * the order of their addition (horizontally). A horizontal layout is doesn't
 * have a predefined size - its size is defined by the components inside it.
 */
@Tag("vaadin-horizontal-layout")
@HtmlImport("frontend://bower_components/vaadin-ordered-layout/src/vaadin-horizontal-layout.html")
public class HorizontalLayout extends Component
        implements ThemableLayout, FlexComponent<HorizontalLayout>, ClickNotifier<HorizontalLayout> {

    /**
     * Constructs an empty layout with spacing on by default.
     */
    public HorizontalLayout() {
        setSpacing(true);
    }

    /**
     * Convenience constructor to create a layout with the children already
     * inside it.
     *
     * @param children
     *            the items to add to this layout
     * @see #add(Component...)
     */
    public HorizontalLayout(Component... children) {
        this();
        add(children);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Spacing is enabled by default for horizontal layout.
     *
     * @param spacing adds {@code spacing} theme setting if {@code true} or removes it if {@code false}
     */
    @Override
    public void setSpacing(boolean spacing) {
        ThemableLayout.super.setSpacing(spacing);
    }

    /**
     * Sets a vertical alignment for individual components inside the layout.
     * This individual alignment for the component overrides any alignment set
     * at the {@link #setDefaultVerticalComponentAlignment(Alignment)}.
     * <p>
     * It effectively sets the {@code "alignSelf"} style value.
     * <p>
     * The default alignment for individual components is
     * {@link Alignment#AUTO}.
     * <p>
     * It's the same as the {@link #setAlignSelf(Alignment, HasElement...)}
     * method
     *
     * @see #setAlignSelf(Alignment, HasElement...)
     *
     * @param alignment
     *            the individual alignment for the children components. Setting
     *            <code>null</code> will reset the alignment to its default
     * @param componentsToAlign
     *            The components to which the individual alignment should be set
     */
    public void setVerticalComponentAlignment(Alignment alignment,
            Component... componentsToAlign) {
        setAlignSelf(alignment, componentsToAlign);
    }

    /**
     * Gets the individual vertical alignment of a given component.
     * <p>
     * The default alignment for individual components is
     * {@link Alignment#AUTO}.
     * <p>
     * It's the same as the {@link #getAlignSelf(HasElement)} method.
     *
     * @see #getAlignSelf(HasElement)
     *
     * @param component
     *            The component which individual layout should be read
     * @return the alignment of the component, never <code>null</code>
     */
    public Alignment getVerticalComponentAlignment(Component component) {
        return getAlignSelf(component);
    }

    /**
     * Sets the default vertical alignment to be used by all components without
     * individual alignments inside the layout. Individual components can be
     * aligned by using the
     * {@link #setVerticalComponentAlignment(Alignment, Component...)} method.
     * <p>
     * It effectively sets the {@code "alignItems"} style value.
     * <p>
     * The default alignment is {@link Alignment#START}.
     * <p>
     * It's the same as the {@link #setAlignItems(Alignment)} method.
     *
     * @see #setAlignItems(Alignment)
     *
     * @param alignment
     *            the alignment to apply to the components. Setting
     *            <code>null</code> will reset the alignment to its default
     */
    public void setDefaultVerticalComponentAlignment(Alignment alignment) {
        setAlignItems(alignment);
    }

    /**
     * Gets the default vertical alignment used by all components without
     * individual alignments inside the layout.
     * <p>
     * The default alignment is {@link Alignment#STRETCH}.
     * <p>
     * This is the same as the {@link #getAlignItems()} method.
     *
     * @return the general alignment used by the layout, never <code>null</code>
     */
    public Alignment getDefaultVerticalComponentAlignment() {
        return getAlignItems();
    }

    /**
     * This is the same as
     * {@link #setDefaultVerticalComponentAlignment(Alignment)}.
     *
     * @param alignment
     *            the alignment to apply to the components. Setting
     *            <code>null</code> will reset the alignment to its default
     * @see #setDefaultVerticalComponentAlignment(Alignment)
     */
    @Override
    public void setAlignItems(Alignment alignment) {
        // this method is overridden to make javadocs point to the correct
        // method to be used, and since FlexComponent has different default
        // value.
        FlexComponent.super.setAlignItems(alignment);
    }

    /**
     * This is the same as {@link #getDefaultVerticalComponentAlignment()}.
     *
     * @return the general alignment used by the layout, never <code>null</code>
     */
    @Override
    public Alignment getAlignItems() {
        // this method is overridden to make javadocs point to the correct
        // method to be used, and since FlexComponent has different default
        // value.
        return FlexComponent.super.getAlignItems();
    }

    /**
     * This is the same as
     * {@link #setVerticalComponentAlignment(Alignment, Component...)}.
     *
     * @param alignment
     *            the individual alignment for the children components. Setting
     *            <code>null</code> will reset the alignment to its default
     * @param elementContainers
     *            The element containers (components) to which the individual
     *            alignment should be set
     * @see #setVerticalComponentAlignment(Alignment, Component...)
     */
    @Override
    public void setAlignSelf(Alignment alignment,
                             HasElement... elementContainers) {
        // this method is overridden to have javadocs that point to the method
        // that should be used and has better javadocs.
        FlexComponent.super.setAlignSelf(alignment, elementContainers);
    }

    /**
     * This is the same as {@link #getVerticalComponentAlignment(Component)}.
     *
     * @param container
     *            The element container (component) which individual layout
     *            should be read
     * @return the alignment of the container, never <code>null</code>
     * @see #getVerticalComponentAlignment(Component)
     */
    @Override
    public Alignment getAlignSelf(HasElement container) {
        // this method is overridden to have javadocs that point to the method
        // that should be used and has better javadocs.
        return FlexComponent.super.getAlignSelf(container);
    }
}
