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
package com.vaadin.flow.component.splitlayout;

import java.util.Locale;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;

/**
 * Split Layout is a component with two content areas and a draggable split
 * handle between them.
 *
 * @author Vaadin Ltd
 */
@SuppressWarnings("deprecation")
@NpmPackage(value = "@vaadin/split-layout", version = "23.3.26")
@NpmPackage(value = "@vaadin/vaadin-split-layout", version = "23.3.26")
public class SplitLayout extends GeneratedVaadinSplitLayout<SplitLayout>
        implements HasSize {

    private Component primaryComponent;
    private Component secondaryComponent;
    private StateTree.ExecutionRegistration updateStylesRegistration;
    private Double splitterPosition;

    /**
     * numeration of all available orientation for VaadinSplitLayout component
     */
    public enum Orientation {
        VERTICAL, HORIZONTAL;
    }

    /**
     * Constructs an empty SplitLayout.
     */
    public SplitLayout() {
        setOrientation(Orientation.HORIZONTAL);
        addAttachListener(
                e -> this.requestStylesUpdatesForSplitterPosition(e.getUI()));
    }

    /**
     * Constructs a SplitLayout with the given initial components to set to the
     * primary and secondary splits.
     *
     * @param primaryComponent
     *            the component set to the primary split
     * @param secondaryComponent
     *            the component set to the secondary split
     */
    public SplitLayout(Component primaryComponent,
            Component secondaryComponent) {
        this();
        addToPrimary(primaryComponent);
        addToSecondary(secondaryComponent);
    }

    /**
     * Constructs a SplitLayout with the orientation.
     *
     * @param orientation
     *            the orientation set to the layout
     */
    public SplitLayout(Orientation orientation) {
        setOrientation(orientation);
        addAttachListener(
                e -> this.requestStylesUpdatesForSplitterPosition(e.getUI()));
    }

    /**
     * Constructs a SplitLayout with the given initial components to set to the
     * primary and secondary splits and with the orientation.
     *
     * @param primaryComponent
     *            the component set to the primary split
     * @param secondaryComponent
     *            the component set to the secondary split
     * @param orientation
     *            the orientation set to the layout
     */
    public SplitLayout(Component primaryComponent, Component secondaryComponent,
            Orientation orientation) {
        this(primaryComponent, secondaryComponent);
        setOrientation(orientation);
    }

    /**
     * Set the orientation of the SplitLayout.
     * <p>
     * Default value is {@link Orientation#HORIZONTAL}.
     *
     *
     * @param orientation
     *            the orientation of the SplitLayout. Valid enumerate values are
     *            VERTICAL and HORIZONTAL, never {@code null}
     */
    public void setOrientation(Orientation orientation) {
        Objects.requireNonNull(orientation, "Orientation cannot be null");
        this.setOrientation(orientation.toString().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Get the orientation of the SplitLayout.
     * <p>
     * Default value is {@link Orientation#HORIZONTAL}.
     * <p>
     * <em>NOTE:</em> This property is not synchronized automatically from the
     * client side, so the returned value may not be the same as in client side.
     * </p>
     *
     *
     * @return the {@code orientation} property of the SplitLayout.
     */
    public Orientation getOrientation() {
        return Orientation.valueOf(super.getOrientationString().toUpperCase());
    }

    /**
     * Sets the given components to the primary split of this layout, i.e. the
     * left split if in horizontal mode and the top split if in vertical mode.
     * <p>
     * <b>Note:</b> Calling this method with multiple arguments will wrap the
     * components inside a {@code <div>} element.
     * <p>
     * <b>Note:</b> Removing the primary component through the component API
     * will move the secondary component to the primary split, causing this
     * layout to desync with the server. This is a known issue.
     *
     * @see #setOrientation(Orientation)
     */
    @Override
    public void addToPrimary(Component... components) {
        if (components.length == 1) {
            primaryComponent = components[0];
        } else {
            Div container = new Div();
            container.add(components);
            primaryComponent = container;
        }
        setComponents();
    }

    /**
     * Get the component currently set to the primary split.
     *
     * @return the primary component, may be null
     */
    public Component getPrimaryComponent() {
        return primaryComponent;
    }

    /**
     * Sets the given components to the secondary split of this layout, i.e. the
     * right split if in horizontal mode and the bottom split if in vertical
     * mode.
     * <p>
     * <b>Note:</b> Calling this method with multiple arguments will wrap the
     * components inside a {@code <div>} element.
     *
     * @see #setOrientation(Orientation)
     */
    @Override
    public void addToSecondary(Component... components) {
        if (components.length == 1) {
            secondaryComponent = components[0];
        } else {
            Div container = new Div();
            container.add(components);
            secondaryComponent = container;
        }
        setComponents();
    }

    /**
     * Get the component currently set to the secondary split.
     *
     * @return the primary component, may be null
     */
    public Component getSecondaryComponent() {
        return secondaryComponent;
    }

    /**
     * Sets the relative position of the splitter in percentages. The given
     * value is used to set how much space is given to the primary component
     * relative to the secondary component. In horizontal mode this is the width
     * of the component and in vertical mode this is the height. The given value
     * will automatically be clamped to the range [0, 100].
     *
     * Note that when using vertical orientation, this method only works if the
     * split layout has an explicit height, either as an absolute value or as
     * percentage. When using a percentage value, ensure that ancestors have an
     * explicit height as well.
     *
     * @param position
     *            the relative position of the splitter, in percentages
     */
    public void setSplitterPosition(double position) {
        this.splitterPosition = position;
        getUI().ifPresent(this::requestStylesUpdatesForSplitterPosition);
    }

    private void requestStylesUpdatesForSplitterPosition(UI ui) {
        if (this.updateStylesRegistration != null) {
            updateStylesRegistration.remove();
        }
        this.updateStylesRegistration = ui.beforeClientResponse(this,
                context -> {
                    // Update width or height if splitter position is set.
                    updateStylesForSplitterPosition();

                    this.updateStylesRegistration = null;
                });
    }

    private void updateStylesForSplitterPosition() {
        if (this.splitterPosition == null) {
            return;
        }
        double primary = Math.min(Math.max(this.splitterPosition, 0), 100);
        double secondary = 100 - primary;
        setPrimaryStyle("flex", String.format("1 1 %s%%", primary));
        setSecondaryStyle("flex", String.format("1 1 %s%%", secondary));
    }

    /**
     * Set a style to the component in the primary split.
     *
     * @param styleName
     *            name of the style to set
     * @param value
     *            the value to set
     */
    public void setPrimaryStyle(String styleName, String value) {
        setInnerComponentStyle(styleName, value, true);
    }

    /**
     * Set a style to the component in the secondary split.
     *
     * @param styleName
     *            name of the style to set
     * @param value
     *            the value to set
     */
    public void setSecondaryStyle(String styleName, String value) {
        setInnerComponentStyle(styleName, value, false);
    }

    private void setComponents() {
        removeAll();
        if (primaryComponent == null) {
            super.addToPrimary(new Div());
        } else {
            super.addToPrimary(primaryComponent);
        }
        if (secondaryComponent == null) {
            super.addToSecondary(new Div());
        } else {
            super.addToSecondary(secondaryComponent);
        }
    }

    @Override
    public void remove(Component... components) {
        super.remove(components);
    }

    /**
     * Removes the primary and the secondary components.
     */
    @Override
    public void removeAll() {
        super.removeAll();
    }

    /**
     * Adds a listener for the {@code splitter-dragend} event, which is fired
     * when the user has stopped resizing the splitter with drag and drop.
     *
     * @param listener
     *            the listener to add
     * @return a registration for removing the listener
     */
    @Override
    public Registration addSplitterDragendListener(
            ComponentEventListener<SplitterDragendEvent<SplitLayout>> listener) {
        return super.addSplitterDragendListener(listener);
    }

    private void setInnerComponentStyle(String styleName, String value,
            boolean primary) {
        Component innerComponent = primary ? primaryComponent
                : secondaryComponent;
        if (innerComponent != null) {
            innerComponent.getElement().getStyle().set(styleName, value);
        } else {
            getElement().executeJs(
                    "var element = this.children[$0]; if (element) { element.style[$1]=$2; }",
                    primary ? 0 : 1, styleName, value);
        }
    }

    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    @Override
    public void addThemeVariants(SplitLayoutVariant... variants) {
        super.addThemeVariants(variants);
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    @Override
    public void removeThemeVariants(SplitLayoutVariant... variants) {
        super.removeThemeVariants(variants);
    }

    public static class SplitterDragendEvent<T extends GeneratedVaadinSplitLayout<T>>
            extends GeneratedVaadinSplitLayout.SplitterDragendEvent<T> {
        public SplitterDragendEvent(T source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
