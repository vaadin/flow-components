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
package com.vaadin.flow.component.orderedlayout;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementDetachEvent;
import com.vaadin.flow.dom.ElementDetachListener;
import com.vaadin.flow.shared.Registration;

/**
 * Horizontal Layout places components side-by-side in a row. By default, it has
 * undefined width and height, meaning its size is determined by the components
 * it contains.
 */
@Tag("vaadin-horizontal-layout")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/horizontal-layout", version = "24.8.0-alpha18")
@JsModule("@vaadin/horizontal-layout/src/vaadin-horizontal-layout.js")
public class HorizontalLayout extends Component implements ThemableLayout,
        FlexComponent, ClickNotifier<HorizontalLayout> {

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
     * Convenience constructor to create a layout with the children and
     * specified justifyContentMode.
     *
     * @param justifyContentMode
     *            the justifyContentMode
     * @param children
     *            the items to add to this layout
     *
     * @see #add(Component...)
     * @see #setJustifyContentMode(JustifyContentMode)
     */
    public HorizontalLayout(JustifyContentMode justifyContentMode,
            Component... children) {
        this(children);
        setJustifyContentMode(justifyContentMode);
    }

    /**
     * Convenience constructor to create a layout with the children and
     * specified vertical alignment.
     *
     * @param alignment
     *            the vertical alignment
     * @param children
     *            the items to add to this layout
     *
     * @see #add(Component...)
     * @see #setDefaultVerticalComponentAlignment(Alignment)
     */
    public HorizontalLayout(Alignment alignment, Component... children) {
        this(children);
        setDefaultVerticalComponentAlignment(alignment);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Spacing is enabled by default for horizontal layout.
     *
     * @param spacing
     *            adds {@code spacing} theme setting if {@code true} or removes
     *            it if {@code false}
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
     * The default alignment is {@link Alignment#STRETCH}.
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
     * @param components
     *            The components to which the individual alignment should be set
     * @see #setVerticalComponentAlignment(Alignment, Component...)
     */
    @Override
    public void setAlignSelf(Alignment alignment, HasElement... components) {
        // this method is overridden to have javadocs that point to the method
        // that should be used and has better javadocs.
        FlexComponent.super.setAlignSelf(alignment, components);
    }

    /**
     * This is the same as {@link #getVerticalComponentAlignment(Component)}.
     *
     * @param component
     *            The component which individual layout should be read
     * @return the alignment of the component, never <code>null</code>
     * @see #getVerticalComponentAlignment(Component)
     */
    @Override
    public Alignment getAlignSelf(HasElement component) {
        // this method is overridden to have javadocs that point to the method
        // that should be used and has better javadocs.
        return FlexComponent.super.getAlignSelf(component);
    }

    /**
     * Adds the given components to this layout and sets them as expanded. The
     * flex-grow of all added child components are set to 1 so that the
     * expansion will be effective. The width of this layout is also set to
     * 100%.
     *
     * @param components
     *            the components to set, not <code>null</code>
     */
    public void addAndExpand(Component... components) {
        add(components);
        setWidthFull();
        expand(components);
    }

    @Override
    public void replace(Component oldComponent, Component newComponent) {
        String oldSlotName = oldComponent != null
                ? oldComponent.getElement().getAttribute("slot")
                : null;

        String newSlotName = newComponent != null
                ? newComponent.getElement().getAttribute("slot")
                : null;

        FlexComponent.super.replace(oldComponent, newComponent);

        if (newComponent != null && oldComponent != null) {
            if (oldSlotName == null) {
                newComponent.getElement().removeAttribute("slot");
            } else {
                newComponent.getElement().setAttribute("slot", oldSlotName);
            }

            if (newSlotName == null) {
                oldComponent.getElement().removeAttribute("slot");
            } else {
                oldComponent.getElement().setAttribute("slot", newSlotName);
            }
        }

        updateChildDetachListeners();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method only adds components to the start slot.
     */
    @Override
    public void add(Collection<Component> components) {
        addToStart(components);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method only adds components to the start slot.
     */
    @Override
    public void add(Component... components) {
        addToStart(components);
    }

    @Override
    public void addComponentAtIndex(int index, Component component) {
        Component oldComponent = getComponentCount() > index
                ? getComponentAt(index)
                : null;
        String slotName = oldComponent != null
                ? oldComponent.getElement().getAttribute("slot")
                : null;

        FlexComponent.super.addComponentAtIndex(index, component);

        if (slotName == null) {
            component.getElement().removeAttribute("slot");
        } else {
            component.getElement().setAttribute("slot", slotName);
        }

        updateChildDetachListeners();
    }

    /**
     * Adds the components to the <em>start</em> slot of this layout.
     *
     * @param components
     *            Components to add to the start slot.
     * @throws NullPointerException
     *             if any of the components is null or if the components array
     *             is null.
     */
    public void addToStart(Component... components) {
        Objects.requireNonNull(components, "Components should not be null");

        addToStart(Arrays.asList(components));
    }

    /**
     * Adds the components to the <em>start</em> slot of this layout.
     *
     * @param components
     *            Components to add to the start slot.
     * @throws NullPointerException
     *             if any of the components is null or if the components array
     *             is null.
     */
    public void addToStart(Collection<Component> components) {
        var idx = getChildren().filter((child) -> {
            var slotName = child.getElement().getAttribute("slot");
            return slotName == null;
        }).count();

        final AtomicInteger itemCounter = new AtomicInteger((int) idx);

        components.stream()
                .map(component -> Objects.requireNonNull(component,
                        "Component to add cannot be null"))
                .forEach((component) -> {
                    var isChild = getElement()
                            .equals(component.getElement().getParent());
                    getElement().insertChild(
                            isChild ? itemCounter.get()
                                    : itemCounter.getAndIncrement(),
                            component.getElement());
                });
    }

    /**
     * Adds the components to the <em>middle</em> slot of this layout.
     *
     * @param components
     *            Components to add to the middle slot.
     * @throws NullPointerException
     *             if any of the components is null or if the components array
     *             is null.
     */
    public void addToMiddle(Component... components) {
        Objects.requireNonNull(components, "Components should not be null");

        addToMiddle(Arrays.asList(components));
    }

    /**
     * Adds the components to the <em>middle</em> slot of this layout.
     *
     * @param components
     *            Components to add to the middle slot.
     * @throws NullPointerException
     *             if any of the components is null or if the components array
     *             is null.
     */
    public void addToMiddle(Collection<Component> components) {
        Objects.requireNonNull(components, "Components should not be null");

        var idx = getChildren().filter((child) -> {
            var slotName = child.getElement().getAttribute("slot");
            return slotName == null || slotName.equals("middle");
        }).count();

        final AtomicInteger itemCounter = new AtomicInteger((int) idx);

        components.stream()
                .map(component -> Objects.requireNonNull(component,
                        "Component to add cannot be null"))
                .forEach((component) -> {
                    if (component instanceof Text) {
                        throw new IllegalArgumentException("Text as a middle"
                                + " slot content is not supported. "
                                + "Consider wrapping the Text inside a Div.");
                    }

                    component.getElement().setAttribute("slot", "middle");

                    var isChild = getElement()
                            .equals(component.getElement().getParent());
                    getElement().insertChild(
                            isChild ? itemCounter.get()
                                    : itemCounter.getAndIncrement(),
                            component.getElement());
                });

        updateChildDetachListeners();
    }

    /**
     * Adds the components to the <em>middle</em> slot of this layout.
     *
     * @param components
     *            Components to add to the middle slot.
     * @throws NullPointerException
     *             if any of the components is null or if the components array
     *             is null.
     */
    public void addToEnd(Component... components) {
        Objects.requireNonNull(components, "Components should not be null");

        addToEnd(Arrays.asList(components));
    }

    /**
     * Adds the components to the <em>end</em> slot of this layout.
     *
     * @param components
     *            Components to add to the middle slot.
     * @throws NullPointerException
     *             if any of the components is null or if the components array
     *             is null.
     */
    public void addToEnd(Collection<Component> components) {
        Objects.requireNonNull(components, "Components should not be null");

        components.stream()
                .map(component -> Objects.requireNonNull(component,
                        "Component to add cannot be null"))
                .forEach((component) -> {
                    if (component instanceof Text) {
                        throw new IllegalArgumentException("Text as an end"
                                + " slot content is not supported. "
                                + "Consider wrapping the Text inside a Div.");
                    }

                    component.getElement().setAttribute("slot", "end");
                    getElement().appendChild(component.getElement());
                });

        updateChildDetachListeners();
    }

    private Map<Element, Registration> childDetachListenerMap = new HashMap<>();
    // Must not use lambda here as that would break serialization. See
    // https://github.com/vaadin/flow-components/issues/5597
    private ElementDetachListener childDetachListener = new ElementDetachListener() {
        @Override
        public void onDetach(ElementDetachEvent e) {
            var child = e.getSource();
            var childDetachedFromLayout = !getElement().getChildren().anyMatch(
                    layoutChild -> Objects.equals(child, layoutChild));

            if (childDetachedFromLayout) {
                // The child was removed from the layout
                // Clear slot to avoid problems when moving to other layout
                child.removeAttribute("slot");

                // Remove the registration for the child detach listener
                childDetachListenerMap.get(child).remove();
                childDetachListenerMap.remove(child);
            }
        }
    };

    private void updateChildDetachListeners() {
        // Add detach listeners (child may be removed with removeFromParent())
        getElement().getChildren().forEach(child -> {
            if (!childDetachListenerMap.containsKey(child)) {
                childDetachListenerMap.put(child,
                        child.addDetachListener(childDetachListener));
            }
        });
    }
}
