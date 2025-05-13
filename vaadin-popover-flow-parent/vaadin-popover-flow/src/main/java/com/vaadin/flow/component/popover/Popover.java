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
package com.vaadin.flow.component.popover;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementDetachEvent;
import com.vaadin.flow.dom.ElementDetachListener;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonArray;

/**
 * Popover is a component for creating overlays that are positioned next to
 * specified component (target).
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-popover")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@NpmPackage(value = "@vaadin/popover", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/popover/src/vaadin-popover.js")
@JsModule("./vaadin-popover/popover.ts")
public class Popover extends Component implements HasAriaLabel, HasComponents,
        HasThemeVariant<PopoverVariant> {

    private static Integer defaultHideDelay;
    private static Integer defaultFocusDelay;
    private static Integer defaultHoverDelay;
    final static AtomicBoolean uiInitListenerRegistered = new AtomicBoolean(
            false);

    private Component target;
    private Registration targetAttachRegistration;
    private Registration targetDetachRegistration;
    private boolean autoAddedToTheUi;

    private boolean openOnClick = true;
    private boolean openOnHover = false;
    private boolean openOnFocus = false;

    /**
     * Constructs an empty popover.
     */
    public Popover() {
        getElement().getNode().addAttachListener(this::attachComponentRenderer);

        // Workaround for: https://github.com/vaadin/flow/issues/3496
        getElement().setProperty("opened", false);

        updateTrigger();
        setOverlayRole("dialog");
    }

    /**
     * Creates a popover with given components inside.
     *
     * @param components
     *            the components inside the popover
     * @see #add(Component...)
     */
    public Popover(Component... components) {
        this();
        add(components);
    }

    /**
     * Sets the default focus delay to be used by all popover instances (running
     * in the same JVM), except for those that have focus delay configured using
     * {@link #setFocusDelay(int)}.
     *
     * @param defaultFocusDelay
     *            the default focus delay
     */
    public static void setDefaultFocusDelay(int defaultFocusDelay) {
        Popover.defaultFocusDelay = defaultFocusDelay;
        applyConfiguration();
    }

    /**
     * Sets the default hide delay to be used by all popover instances (running
     * in the same JVM), except for those that have hide delay configured using
     * {@link #setHideDelay(int)}.
     *
     * @param defaultHideDelay
     *            the default hide delay
     */
    public static void setDefaultHideDelay(int defaultHideDelay) {
        Popover.defaultHideDelay = defaultHideDelay;
        applyConfiguration();
    }

    /**
     * Sets the default hover delay to be used by all popover instances (running
     * in the same JVM), except for those that have hover delay configured using
     * {@link #setHoverDelay(int)}.
     *
     * @param defaultHoverDelay
     *            the default hover delay
     */
    public static void setDefaultHoverDelay(int defaultHoverDelay) {
        Popover.defaultHoverDelay = defaultHoverDelay;
        applyConfiguration();
    }

    private static void applyConfiguration() {
        if (UI.getCurrent() != null) {
            // Apply the default popover configuration for the current UI
            applyConfigurationForUI(UI.getCurrent());
        }

        if (uiInitListenerRegistered.compareAndSet(false, true)) {
            // Apply the popover configuration for all new UIs
            VaadinService.getCurrent()
                    .addUIInitListener(e -> applyConfigurationForUI(e.getUI()));
        }
    }

    private static void applyConfigurationForUI(UI ui) {
        ui.getElement().executeJs(
                "((window.Vaadin ||= {}).Flow ||= {}).popover ||= {}");

        if (defaultHideDelay != null) {
            ui.getElement().executeJs(
                    "const popover = window.Vaadin.Flow.popover;"
                            + "popover.defaultHideDelay = $0;"
                            + "popover.setDefaultHideDelay?.($0)",
                    defaultHideDelay);
        }

        if (defaultFocusDelay != null) {
            ui.getElement().executeJs(
                    "const popover = window.Vaadin.Flow.popover;"
                            + "popover.defaultFocusDelay = $0;"
                            + "popover.setDefaultFocusDelay?.($0)",
                    defaultFocusDelay);
        }

        if (defaultHoverDelay != null) {
            ui.getElement().executeJs(
                    "const popover = window.Vaadin.Flow.popover;"
                            + "popover.defaultHoverDelay = $0;"
                            + "popover.setDefaultHoverDelay?.($0)",
                    defaultHoverDelay);
        }
    }

    /**
     * {@code opened-changed} event is sent when the overlay opened state
     * changes.
     */
    @DomEvent("opened-changed")
    public static class OpenedChangeEvent extends ComponentEvent<Popover> {
        private final boolean opened;

        public OpenedChangeEvent(Popover source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpened();
        }

        public boolean isOpened() {
            return opened;
        }
    }

    /**
     * Opens or closes the popover.
     *
     * @param opened
     *            {@code true} to open the popover, {@code false} to close it
     */
    public void setOpened(boolean opened) {
        if (opened != isOpened()) {
            getElement().setProperty("opened", opened);
            fireEvent(new OpenedChangeEvent(this, false));
        }
    }

    /**
     * Opens the popover.
     */
    public void open() {
        setOpened(true);
    }

    /**
     * Closes the popover.
     */
    public void close() {
        setOpened(false);
    }

    /**
     * Gets the open state from the popover.
     *
     * @return the {@code opened} property from the popover
     */
    @Synchronize(property = "opened", value = "opened-changed")
    public boolean isOpened() {
        return getElement().getProperty("opened", false);
    }

    /**
     * Add a listener for event fired by the {@code opened-changed} events.
     *
     * @param listener
     *            the listener to add
     * @return a Registration for removing the event listener
     */
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent> listener) {
        return addListener(OpenedChangeEvent.class, listener);
    }

    /**
     * Sets whether component should open modal or modeless popover. When the
     * popover is modal, interacting with elements behind it will be prevented
     * until the popover is closed.
     * <p>
     * Setting the modal to {@code true} does not enable showing the backdrop
     * (modality curtain) automatically. This should be done separately using
     * {@link #setBackdropVisible(boolean)} or optionally passed as a second
     * parameter using {@link #setModal(boolean, boolean)}.
     * <p>
     * NOTE: this setting does not involve server-side modality, as the modal
     * popover is typically not used to prevent anything else from happening
     * while it's open.
     * <p>
     * By default, the popover is non-modal.
     *
     * @param modal
     *            {@code true} to enable popover to open as modal, {@code false}
     *            otherwise.
     * @see #setBackdropVisible(boolean)
     * @see #setModal(boolean, boolean)
     */
    public void setModal(boolean modal) {
        getElement().setProperty("modal", modal);
    }

    /**
     * Sets whether component should open modal or modeless popover and whether
     * the component should show a backdrop (modality curtain) when opened.
     * <p>
     * NOTE: this setting does not involve server-side modality, as the modal
     * popover is typically not used to prevent anything else from happening
     * while it's open.
     * <p>
     * By default, the popover is non-modal and has no modality curtain.
     *
     * @param modal
     *            {@code true} to enable popover to open as modal, {@code false}
     *            otherwise.
     * @param backdropVisible
     *            {@code true} to show the backdrop, {@code false} otherwise.
     * @see #setBackdropVisible(boolean)
     */
    public void setModal(boolean modal, boolean backdropVisible) {
        setModal(modal);
        setBackdropVisible(backdropVisible);
    }

    /**
     * Gets whether component is set as modal or modeless popover. By default,
     * the popover is non-modal.
     *
     * @return {@code true} if modal popover, {@code false} otherwise.
     */
    public boolean isModal() {
        return getElement().getProperty("modal", false);
    }

    /**
     * Sets whether component should show a backdrop (modality curtain) when
     * opened.
     * <p>
     * By default, the backdrop is not shown.
     *
     * @param backdropVisible
     *            {@code true} to show the backdrop, {@code false} otherwise.
     * @see #setModal(boolean)
     */
    public void setBackdropVisible(boolean backdropVisible) {
        getElement().setProperty("withBackdrop", backdropVisible);
    }

    /**
     * Gets whether component shows a backdrop (modality curtain) when opened.
     *
     * @return {@code true} if backdrop is visible, {@code false} otherwise.
     */
    public boolean isBackdropVisible() {
        return getElement().getProperty("withBackdrop", false);
    }

    @Override
    public void setAriaLabel(String ariaLabel) {
        getElement().setProperty("accessibleName", ariaLabel);
    }

    @Override
    public Optional<String> getAriaLabel() {
        return Optional.ofNullable(getElement().getProperty("accessibleName"));
    }

    @Override
    public void setAriaLabelledBy(String labelledBy) {
        getElement().setProperty("accessibleNameRef", labelledBy);
    }

    @Override
    public Optional<String> getAriaLabelledBy() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameRef"));
    }

    /**
     * Set {@code true} to make the popover content automatically receive focus
     * after it is opened. Modal popovers use this behavior by default.
     *
     * @param autofocus
     *            the boolean value to set
     */
    public void setAutofocus(boolean autofocus) {
        getElement().setProperty("autofocus", autofocus);
    }

    /**
     * Get if the popover content automatically receives focus after it is
     * opened. Modal popovers use this behavior by default.
     *
     * @return {@code true} if the popover content receives focus when opened,
     *         {@code false} otherwise
     */
    public boolean isAutofocus() {
        return getElement().getProperty("autofocus", false);
    }

    /**
     * Sets the ARIA role for the overlay element, used by screen readers.
     *
     * @param role
     *            the role to set
     */
    public void setOverlayRole(String role) {
        Objects.requireNonNull(role, "Role cannot be null");

        getElement().setProperty("overlayRole", role);
    }

    /**
     * Gets the ARIA role for the overlay element, used by screen readers.
     * Defaults to {@code dialog}.
     *
     * @return the role
     */
    public String getOverlayRole() {
        return getElement().getProperty("overlayRole");
    }

    /**
     * Gets whether this popover can be closed by pressing the Esc key or not.
     * <p>
     * By default, the popover is closable with Esc.
     *
     * @return {@code true} if this popover can be closed with the Esc key,
     *         {@code false} otherwise
     */
    public boolean isCloseOnEsc() {
        return !getElement().getProperty("noCloseOnEsc", false);
    }

    /**
     * Sets whether this popover can be closed by pressing the Esc key or not.
     * <p>
     * By default, the popover is closable with Esc.
     * <p>
     * NOTE: when none of the opening actions are specified by setting all of
     * {@link #setOpenOnClick(boolean)}, {@link #setOpenOnHover(boolean)} and
     * {@link #setOpenOnFocus(boolean)} to {@code false} this method does
     * nothing as in this case the popover can be only closed programmatically.
     *
     * @param closeOnEsc
     *            {@code true} to enable closing this popover with the Esc key,
     *            {@code false} to disable it
     */
    public void setCloseOnEsc(boolean closeOnEsc) {
        getElement().setProperty("noCloseOnEsc", !closeOnEsc);
    }

    /**
     * Gets whether this popover can be closed by clicking outside of it or not.
     * <p>
     * By default, the popover is closable with an outside click.
     *
     * @return {@code true} if this popover can be closed by an outside click,
     *         {@code false} otherwise
     */
    public boolean isCloseOnOutsideClick() {
        return !getElement().getProperty("noCloseOnOutsideClick", false);
    }

    /**
     * Sets whether this popover can be closed by clicking outside of it or not.
     * <p>
     * By default, the popover is closable with an outside click.
     * <p>
     * NOTE: when none of the opening actions are specified by setting all of
     * {@link #setOpenOnClick(boolean)}, {@link #setOpenOnHover(boolean)} and
     * {@link #setOpenOnFocus(boolean)} to {@code false} this method does
     * nothing as in this case the popover can be only closed programmatically.
     *
     * @param closeOnOutsideClick
     *            {@code true} to enable closing this popover with an outside
     *            click, {@code false} to disable it
     */
    public void setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        getElement().setProperty("noCloseOnOutsideClick", !closeOnOutsideClick);
    }

    /**
     * Sets position of the popover with respect to its {@code target}.
     * <p>
     * NOTE: when the target component is not set, the position does not take
     * any effect and the popover is rendered in the middle of the screen.
     *
     * @param position
     *            the position to set
     * @see #setFor(String)
     * @see #setTarget(Component)
     */
    public void setPosition(PopoverPosition position) {
        getElement().setProperty("position", position.getPosition());
    }

    /**
     * Gets position of the popover with respect to its {@code target}.
     * <p>
     * NOTE: when the target component is not set, the position does not take
     * any effect and the popover is rendered in the middle of the screen.
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
     *            the id of target component for this popover, can be
     *            {@code null} to remove the target
     */
    public void setFor(String id) {
        getElement().setProperty("for", id);
    }

    /**
     * Gets the {@code id} of target component of the popover, or {@code null}
     * if the {@code id} was not set.
     *
     * @return the id of target component for this popover
     * @see #setFor(String)
     */
    public String getFor() {
        return getElement().getProperty("for");
    }

    /**
     * The delay in milliseconds before the popover is opened on target focus.
     * When not specified, the default {@code 500} milliseconds delay is used.
     *
     * @param focusDelay
     *            the delay in milliseconds
     */
    public void setFocusDelay(int focusDelay) {
        getElement().setProperty("focusDelay", focusDelay);
    }

    /**
     * The delay in milliseconds before the popover is opened on target focus.
     * When not specified, the default {@code 500} milliseconds delay is used.
     *
     * @return the delay in milliseconds
     */
    public int getFocusDelay() {
        return getElement().getProperty("focusDelay", 0);
    }

    /**
     * The delay in milliseconds before the popover is opened on target hover.
     * When not specified, the default {@code 500} milliseconds delay is used.
     *
     * @param hoverDelay
     *            the delay in milliseconds
     */
    public void setHoverDelay(int hoverDelay) {
        getElement().setProperty("hoverDelay", hoverDelay);
    }

    /**
     * The delay in milliseconds before the popover is opened on target hover.
     * When not specified, the default {@code 500} milliseconds delay is used.
     *
     * @return the delay in milliseconds
     */
    public int getHoverDelay() {
        return getElement().getProperty("hoverDelay", 0);
    }

    /**
     * The delay in milliseconds before the popover is closed on losing hover.
     * When not specified, the default {@code 500} milliseconds delay is used.
     * <p>
     * NOTE: on target blur, the popover is closed immediately.
     *
     * @param hideDelay
     *            the delay in milliseconds
     */
    public void setHideDelay(int hideDelay) {
        getElement().setProperty("hideDelay", hideDelay);
    }

    /**
     * The delay in milliseconds before the popover is closed on losing hover.
     * When not specified, the default {@code 500} milliseconds delay is used.
     * <p>
     * NOTE: on target blur, the popover is closed immediately.
     *
     * @return the delay in milliseconds
     */
    public int getHideDelay() {
        return getElement().getProperty("hideDelay", 0);
    }

    /**
     * Sets whether the popover can be opened via target click. Defaults to
     * {@code true}.
     *
     * @param openOnClick
     *            {@code true} to allow opening the popover via target click,
     *            {@code false} to disallow it.
     */
    public void setOpenOnClick(boolean openOnClick) {
        this.openOnClick = openOnClick;
        updateTrigger();
    }

    /**
     * Gets whether the popover can be opened via target click. Defaults to
     * {@code true}.
     *
     * @return {@code true} if the popover can be opened with target click,
     *         {@code false} otherwise.
     */
    public boolean isOpenOnClick() {
        return this.openOnClick;
    }

    /**
     * Sets whether the popover can be opened via target focus. Defaults to
     * {@code false}.
     *
     * @param openOnFocus
     *            {@code true} to allow opening the popover via target focus,
     *            {@code false} to disallow it.
     */
    public void setOpenOnFocus(boolean openOnFocus) {
        this.openOnFocus = openOnFocus;
        updateTrigger();
    }

    /**
     * Gets whether the popover can be opened via target focus. Defaults to
     * {@code false}.
     *
     * @return {@code true} if the popover can be opened with target focus,
     *         {@code false} otherwise.
     */
    public boolean isOpenOnFocus() {
        return this.openOnFocus;
    }

    /**
     * Sets whether the popover can be opened via target hover. Defaults to
     * {@code false}.
     *
     * @param openOnHover
     *            {@code true} to allow opening the popover via target hover,
     *            {@code false} to disallow it.
     */
    public void setOpenOnHover(boolean openOnHover) {
        this.openOnHover = openOnHover;
        updateTrigger();
    }

    /**
     * Gets whether the popover can be opened via target hover. Defaults to
     * {@code false}.
     *
     * @return {@code true} if the popover can be opened with target hover,
     *         {@code false} otherwise.
     */
    public boolean isOpenOnHover() {
        return this.openOnHover;
    }

    private void updateTrigger() {
        JsonArray trigger = Json.createArray();

        if (isOpenOnClick()) {
            trigger.set(trigger.length(), "click");
        }

        if (isOpenOnHover()) {
            trigger.set(trigger.length(), "hover");
        }

        if (isOpenOnFocus()) {
            trigger.set(trigger.length(), "focus");
        }

        getElement().setPropertyJson("trigger", trigger);
    }

    /**
     * Sets the target component for this popover.
     * <p>
     * By default, the popover can be opened with a click on the target
     * component.
     * <p>
     * Note: setting target will also add the popover to the {@code <body>} if
     * it's not yet attached anywhere.
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
            targetDetachRegistration.remove();
        }

        if (autoAddedToTheUi) {
            getElement().removeFromParent();
            autoAddedToTheUi = false;
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
        targetDetachRegistration = target.addDetachListener(e -> {
            if (autoAddedToTheUi) {
                getElement().removeFromParent();
                autoAddedToTheUi = false;
            }
        });
    }

    private void onTargetAttach(UI ui) {
        if (target != null) {
            ui.beforeClientResponse(ui, context -> {
                if (getElement().getNode().getParent() == null) {
                    // Remove the popover from its current state tree
                    getElement().removeFromTree(false);
                    ui.addToModalComponent(this);
                    autoAddedToTheUi = true;
                }
            });
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
     * Sets the width of the popover overlay content area.
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px"
     * or "2.5em" (Using relative unit, such as percentage, will lead to
     * unexpected results).
     * <p>
     * If the provided {@code width} value is {@literal null} then width is
     * removed, and the popover overlay is auto-sized based on the content.
     *
     * @param width
     *            the width to set, may be {@code null}
     */
    public void setWidth(String width) {
        getElement().setProperty("contentWidth", width);
    }

    /**
     * Sets the height of the popover overlay content area.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em" (Using relative unit, such as percentage, will lead to
     * unexpected results).
     * <p>
     * If the provided {@code height} value is {@literal null} then height is
     * removed, and the popover overlay is auto-sized based on the content.
     *
     * @param height
     *            the height to set, may be {@code null}
     */
    public void setHeight(String height) {
        getElement().setProperty("contentHeight", height);
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
