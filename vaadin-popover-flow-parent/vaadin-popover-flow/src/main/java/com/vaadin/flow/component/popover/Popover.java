/*
 * Copyright 2000-2024 Vaadin Ltd.
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
 *
 */
package com.vaadin.flow.component.popover;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementDetachEvent;
import com.vaadin.flow.dom.ElementDetachListener;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.shared.Registration;

/**
 * Popover is a component for creating overlays that are positioned next to
 * specified component (target).
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-popover")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha4")
@NpmPackage(value = "@vaadin/popover", version = "24.5.0-alpha4")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/popover/src/vaadin-popover.js")
public class Popover extends Component implements HasComponents {

    private Component target;
    private Registration targetAttachRegistration;

    /**
     * Constructs an empty popover.
     */
    public Popover() {
        getElement().getNode().addAttachListener(this::attachComponentRenderer);
    }

    /**
     * Sets position of the popover with respect to its target.
     *
     * @param position
     *            the position to set
     */
    public void setPosition(PopoverPosition position) {
        getElement().setProperty("position", position.getPosition());
    }

    /**
     * Gets position of the popover with respect to its target.
     *
     * @return the position
     */
    public PopoverPosition getPosition() {
        String positionString = getElement().getProperty("position");
        return Arrays.stream(PopoverPosition.values())
                .filter(p -> p.getPosition().equals(positionString)).findFirst()
                .orElse(null);
    }

    /**
     * The {@code id} of the element to be used as the popover {@code target}
     * value.
     * <p>
     * The element should be in the DOM by the time when the attribute is set,
     * otherwise a warning in the Javascript console is shown.
     *
     * @param id
     *            the id of component for this popup, can be {@code null} to
     *            remove the target
     */
    public void setFor(String id) {
        getElement().setProperty("for", id);
    }

    /**
     * Gets the {@code id} of target component of this popup, or {@code null} if
     * the {@code id} was not set.
     *
     * @return the id of target component of this popup
     * @see #setFor(String)
     */
    public String getFor() {
        return getElement().getProperty("for");
    }

    /**
     * The delay in milliseconds before the popover is opened on target keyboard
     * focus.
     *
     * @param focusDelay
     *            the delay in milliseconds
     */
    public void setFocusDelay(int focusDelay) {
        getElement().setProperty("focusDelay", focusDelay);
    }

    /**
     * The delay in milliseconds before the popover is opened on target keyboard
     * focus.
     *
     * @return the delay in milliseconds
     */
    public int getFocusDelay() {
        return getElement().getProperty("focusDelay", 0);
    }

    /**
     * The delay in milliseconds before the popover is opened on target hover.
     *
     * @param hoverDelay
     *            the delay in milliseconds
     */
    public void setHoverDelay(int hoverDelay) {
        getElement().setProperty("hoverDelay", hoverDelay);
    }

    /**
     * The delay in milliseconds before the popover is opened on target hover.
     *
     * @return the delay in milliseconds
     */
    public int getHoverDelay() {
        return getElement().getProperty("hoverDelay", 0);
    }

    /**
     * The delay in milliseconds before the popover is closed on losing hover.
     * On target blur, the popover is closed immediately.
     *
     * @param hideDelay
     *            the delay in milliseconds
     */
    public void setHideDelay(int hideDelay) {
        getElement().setProperty("hideDelay", hideDelay);
    }

    /**
     * The delay in milliseconds before the popover is closed on losing hover.
     * On target blur, the popover is closed immediately.
     *
     * @return the delay in milliseconds
     */
    public int getHideDelay() {
        return getElement().getProperty("hideDelay", 0);
    }

    /**
     * Sets the target component for this popover.
     * <p>
     * By default, the popover can be opened with a click on the target
     * component.
     *
     * @param target
     *            the target component for this popover, can be {@code null} to
     *            remove the target
     * @throws IllegalArgumentException
     *             if the target is a {@link Text} component.
     */
    public void setTarget(Component target) {
        if (target instanceof Text) {
            throw new IllegalArgumentException(
                    "Text as a target is not supported. Consider wrapping the Text inside a Div.");
        }

        if (this.target != null) {
            targetAttachRegistration.remove();
        }

        this.target = target;

        if (target == null) {
            getElement().executeJs("this.target = null");
            return;
        }

        // Target's JavaScript needs to be executed on each attach,
        // because Flow creates a new client-side element
        target.getUI().ifPresent(this::onTargetAttach);
        targetAttachRegistration = target
                .addAttachListener(e -> onTargetAttach(e.getUI()));
    }

    private void onTargetAttach(UI ui) {
        if (target != null) {
            getElement().executeJs("this.target = $0", target.getElement());
        }
    }

    /**
     * Gets the target component of this popover, or {@code null} if it doesn't
     * have a target.
     *
     * @return the target component of this popover
     * @see #setTarget(Component)
     */
    public Component getTarget() {
        return target;
    }

    /**
     * Adds the given components into this popover.
     * <p>
     * The elements in the DOM will not be children of the
     * {@code <vaadin-popover>} element, but will be inserted into an overlay
     * that is attached into the {@code <body>}.
     *
     * @param components
     *            the components to add
     */
    @Override
    public void add(Collection<Component> components) {
        HasComponents.super.add(components);

        updateVirtualChildNodeIds();
    }

    /**
     * Adds the given component into this popover at the given index.
     * <p>
     * The element in the DOM will not be child of the {@code <vaadin-popover>}
     * element, but will be inserted into an overlay that is attached into the
     * {@code <body>}.
     *
     * @param index
     *            the index, where the component will be added.
     *
     * @param component
     *            the component to add
     */
    @Override
    public void addComponentAtIndex(int index, Component component) {
        HasComponents.super.addComponentAtIndex(index, component);

        updateVirtualChildNodeIds();
    }

    private void attachComponentRenderer() {
        getElement().executeJs(
                "Vaadin.FlowComponentHost.patchVirtualContainer(this)");

        String appId = UI.getCurrent().getInternals().getAppId();

        getElement().executeJs(
                "this.renderer = (root) => Vaadin.FlowComponentHost.setChildNodes($0, this.virtualChildNodeIds, root)",
                appId);
    }

    private Map<Element, Registration> childDetachListenerMap = new HashMap<>();

    // Must not use lambda here as that would break serialization. See
    // https://github.com/vaadin/flow-components/issues/5597
    private ElementDetachListener childDetachListener = new ElementDetachListener() {
        @Override
        public void onDetach(ElementDetachEvent e) {
            var child = e.getSource();
            var childDetachedFromContainer = !getElement().getChildren()
                    .anyMatch(containerChild -> Objects.equals(child,
                            containerChild));

            if (childDetachedFromContainer) {
                // The child was removed from the popover

                // Remove the registration for the child detach listener
                childDetachListenerMap.get(child).remove();
                childDetachListenerMap.remove(child);

                updateVirtualChildNodeIds();
            }
        }
    };

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        updateVirtualChildNodeIds();
    }

    /**
     * Updates the virtualChildNodeIds property of the popover element.
     * <p>
     * This method is called whenever the popover's child components change.
     * <p>
     * Also calls {@code requestContentUpdate} on the popover element to trigger
     * the content update.
     */
    private void updateVirtualChildNodeIds() {
        // Add detach listeners (child may be removed with removeFromParent())
        getElement().getChildren().forEach(child -> {
            if (!childDetachListenerMap.containsKey(child)) {
                childDetachListenerMap.put(child,
                        child.addDetachListener(childDetachListener));
            }
        });

        getElement().setPropertyList("virtualChildNodeIds",
                getElement().getChildren()
                        .map(element -> element.getNode().getId())
                        .collect(Collectors.toList()));

        getElement().callJsFunction("requestContentUpdate");
    }

    /**
     * Sets the CSS class names of the popover overlay element. This method
     * overwrites any previous set class names.
     *
     * @param className
     *            a space-separated string of class names to set, or
     *            <code>null</code> to remove all class names
     */
    @Override
    public void setClassName(String className) {
        getClassNames().clear();
        if (className != null) {
            addClassNames(className.split(" "));
        }
    }

    @Override
    public ClassList getClassNames() {
        return new OverlayClassListProxy(this);
    }

    /**
     * @throws UnsupportedOperationException
     *             Popover does not support adding styles to overlay
     */
    @Override
    public Style getStyle() {
        throw new UnsupportedOperationException(
                "Popover does not support adding styles to overlay");
    }
}
