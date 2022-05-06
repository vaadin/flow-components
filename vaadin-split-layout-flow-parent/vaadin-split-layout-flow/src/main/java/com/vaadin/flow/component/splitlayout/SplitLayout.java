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
 * <h3>Horizontal and Vertical Layouts</h3>
 * <p>
 * By default, the split's orientation is horizontal, meaning that the content
 * elements are positioned side by side in a flex container with a horizontal
 * layout. You can change the split mode to vertical by using the
 * {@link #setOrientation(Orientation)} with {@link Orientation#VERTICAL}.
 * </p>
 * The {@code <vaadin-split-layout>} element itself is a flex container. It does
 * not inherit the parent height by default, but rather sets its height
 * depending on the content.
 * </p>
 * <p>
 * You can use CSS to set the fixed height for the split layout, as usual with
 * any block element. It is possible to define percentage height as well. Note
 * that you have to set the parent height in order to make percentages work
 * correctly.
 * </p>
 * <h3>Initial Splitter Position</h3>
 * <p>
 * The initial splitter position is determined from the sizes of the content
 * elements inside the split layout. Therefore, changing width on the content
 * components affects the initial splitter position for the horizontal layouts,
 * while height affects the vertical ones.
 * </p>
 * <p>
 * Note that when the total size of the content component does not fit the
 * layout, the content elements are scaled proportionally.
 * </p>
 * <p>
 * When setting initial sizes with relative units, such as percentages, it is
 * recommended to assign the size for both content elements:
 * </p>
 * <p>
 * <code>
 * SplitLayout layout = new SplitLayout();
 * <p>
 * Label first = new Label("First is 1/4");<br>
 * first.setWidth("25%");<br>
 * layout.addToPrimary(first);<br>
 * <p>
 * Label second = new Label("Second is 3/4");<br>
 * second.setWidth("75%");<br>
 * layout.addToSecondary(second);
 * </code>
 * </p>
 * <h3>Size Limits</h3>
 * <p>
 * The {@code min-width}/{@code min-height}, and {@code max-width}/
 * {@code max-height} CSS size values for the content elements are respected and
 * used to limit the splitter position when it is dragged.
 * </p>
 * <p>
 * It is preferred to set the limits only for a single content element, in order
 * to avoid size conflicts:
 * </p>
 * <p>
 * <code>
 * SplitLayout layout = new SplitLayout();<br>
 * layout.addToPrimary(new Label("First"));<br>
 * layout.addToPrimary(new Label("Second with min & max size");<br>
 * layout.setSecondaryStyle("min-width", "200px");<br>
 * layout.setSecondaryStyle("max-width", "600px");
 * </code>
 * </p>
 * <h3>Resize Notification</h3>
 * <p>
 * For notification on when the user has resized the split position, use the
 * {@link #addSplitterDragendListener(ComponentEventListener)}.
 * </p>
 * <h3>Styling</h3>
 * <p>
 * The following shadow DOM parts are available for styling:
 * </p>
 * <table>
 * <thead>
 * <tr>
 * <th>Part name</th>
 * <th>Description</th>
 * <th>Theme for Element</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code splitter}</td>
 * <td>Split element</td>
 * <td>vaadin-split-layout</td>
 * </tr>
 * <tr>
 * <td>{@code handle}</td>
 * <td>The handle of the splitter</td>
 * <td>vaadin-split-layout</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * See
 * <a href="https://github.com/vaadin/vaadin-themable-mixin/wiki">ThemableMixin
 * – how to apply styles for shadow parts</a>
 * </p>
 *
 * @author Vaadin Ltd
 */
@NpmPackage(value = "@vaadin/split-layout", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-split-layout", version = "23.1.0-beta1")
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
}
