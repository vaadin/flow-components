/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;

/**
 * Split Layout is a component with two content areas and a draggable split
 * handle between them.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-split-layout")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/split-layout", version = "24.8.0-alpha18")
@JsModule("@vaadin/split-layout/src/vaadin-split-layout.js")
public class SplitLayout extends Component
        implements ClickNotifier<SplitLayout>, HasSize, HasStyle,
        HasThemeVariant<SplitLayoutVariant> {

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
        this(Orientation.HORIZONTAL);
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
        addSplitterDragendListener(e -> {
            splitterPosition = calcNewSplitterPosition(
                    e.primaryComponentFlexBasis, e.secondaryComponentFlexBasis);

            setPrimaryStyle("flex",
                    String.format("1 1 %s", e.primaryComponentFlexBasis));
            setSecondaryStyle("flex",
                    String.format("1 1 %s", e.secondaryComponentFlexBasis));
        });
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
        this(primaryComponent, secondaryComponent, Orientation.HORIZONTAL);
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
        this(orientation);
        addToPrimary(primaryComponent);
        addToSecondary(secondaryComponent);
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
        getElement().setProperty("orientation",
                orientation.toString().toLowerCase(Locale.ENGLISH));
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
        String orientation = getElement().getProperty("orientation");
        return Orientation.valueOf(orientation.toUpperCase());
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
    public void addToPrimary(Component... components) {
        primaryComponent = getComponentOrWrap(components);
        setComponent(primaryComponent, "primary");
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
    public void addToSecondary(Component... components) {
        secondaryComponent = getComponentOrWrap(components);
        setComponent(secondaryComponent, "secondary");
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
     * Gets the relative position of the splitter as a percentage value between
     * 0 and 100. The value will be null unless the splitter position has been
     * explicitly set on the server-side, or the splitter has been moved on the
     * client side. The splitter position is automatically updated when as part
     * of the {@link SplitterDragendEvent}.
     *
     * @return the splitter position, may be null
     */
    public Double getSplitterPosition() {
        return splitterPosition;
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

    /**
     * Returns the component if the given components array contains only one or
     * a wrapper div with the given components if the array contains more.
     *
     * @param components
     *            the components to wrap
     * @return the component or a wrapper div
     */
    private Component getComponentOrWrap(Component... components) {
        return components.length == 1 ? components[0] : new Div(components);
    }

    private void setComponent(Component component, String slot) {
        Component child = component == null ? new Div() : component;
        SlotUtils.setSlot(this, slot, child);
    }

    /**
     * Removes the given child components from this component.
     *
     * @param components
     *            The components to remove.
     * @throws IllegalArgumentException
     *             if any of the components is not a child of this component.
     */
    public void remove(Component... components) {
        for (Component component : components) {
            if (getElement().equals(component.getElement().getParent())) {
                if (component.equals(primaryComponent)) {
                    primaryComponent = null;
                } else if (component.equals(secondaryComponent)) {
                    secondaryComponent = null;
                }
                component.getElement().removeAttribute("slot");
                getElement().removeChild(component.getElement());
            } else {
                throw new IllegalArgumentException("The given component ("
                        + component + ") is not a child of this component");
            }
        }
    }

    /**
     * Removes the primary and the secondary components.
     */
    public void removeAll() {
        getElement().getChildren()
                .forEach(child -> child.removeAttribute("slot"));
        getElement().removeAllChildren();
        primaryComponent = null;
        secondaryComponent = null;
    }

    @DomEvent("splitter-dragend")
    public static class SplitterDragendEvent
            extends ComponentEvent<SplitLayout> {

        private static final String PRIMARY_FLEX_BASIS = "element.querySelector(':scope > [slot=\"primary\"]').style.flexBasis";
        private static final String SECONDARY_FLEX_BASIS = "element.querySelector(':scope > [slot=\"secondary\"]').style.flexBasis";

        String primaryComponentFlexBasis;
        String secondaryComponentFlexBasis;

        public SplitterDragendEvent(SplitLayout source, boolean fromClient,
                @EventData(PRIMARY_FLEX_BASIS) String primaryComponentFlexBasis,
                @EventData(SECONDARY_FLEX_BASIS) String secondaryComponentFlexBasis) {
            super(source, fromClient);
            this.primaryComponentFlexBasis = primaryComponentFlexBasis;
            this.secondaryComponentFlexBasis = secondaryComponentFlexBasis;
        }

    }

    /**
     * Adds a listener for the {@code splitter-dragend} event, which is fired
     * when the user has stopped resizing the splitter with drag and drop.
     *
     * @param listener
     *            the listener to add
     * @return a registration for removing the listener
     */
    public Registration addSplitterDragendListener(
            ComponentEventListener<SplitterDragendEvent> listener) {
        return addListener(SplitterDragendEvent.class,
                (ComponentEventListener) listener);
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

    private Double calcNewSplitterPosition(String primaryFlexBasis,
            String secondaryFlexBasis) {
        // set current splitter position value
        Double splitterPositionValue = this.splitterPosition;

        if (primaryFlexBasis == null || secondaryFlexBasis == null) {
            return splitterPositionValue;
        }

        if (primaryFlexBasis.endsWith("px")) {
            // When moving the splitter, the client side sets pixel values.
            double pFlexBasis = Double
                    .parseDouble(primaryFlexBasis.replace("px", ""));
            double sFlexBasis = Double
                    .parseDouble(secondaryFlexBasis.replace("px", ""));

            splitterPositionValue = (pFlexBasis * 100)
                    / (pFlexBasis + sFlexBasis);
            splitterPositionValue = round(splitterPositionValue);
        } else if (primaryFlexBasis.endsWith("%")) {
            splitterPositionValue = Double
                    .parseDouble(primaryFlexBasis.replace("%", ""));
            splitterPositionValue = round(splitterPositionValue);
        } else {
            throw new IllegalArgumentException(
                    "Given flex basis values are not supported: "
                            + primaryFlexBasis + " / " + secondaryFlexBasis);
        }

        return splitterPositionValue;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
