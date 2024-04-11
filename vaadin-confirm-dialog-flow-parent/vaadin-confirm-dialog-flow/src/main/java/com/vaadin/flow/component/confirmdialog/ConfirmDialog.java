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
 */
package com.vaadin.flow.component.confirmdialog;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.router.NavigationTrigger;
import com.vaadin.flow.shared.Registration;

import java.util.Collection;
import java.util.Optional;

/**
 * Confirm Dialog is a modal Dialog used to confirm user actions.
 * <p>
 * Confirm Dialog consists of:<br>
 * <ul>
 * <li>Title</li>
 * <li>Message</li>
 * <li>Footer</li>
 * <ul>
 * <li>“Cancel” button</li>
 * <li>“Reject” button</li>
 * <li>“Confirm” button</li>
 * </ul>
 * </ul>
 *
 * Each Confirm Dialog should have a title and/or message. The “Confirm” button
 * is shown by default, while the two other buttons are not (they must be
 * explicitly enabled to be displayed).
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-confirm-dialog")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.4.0-alpha20")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/confirm-dialog", version = "24.4.0-alpha20")
@JsModule("@vaadin/confirm-dialog/src/vaadin-confirm-dialog.js")
public class ConfirmDialog extends Component
        implements HasSize, HasStyle, HasOrderedComponents {

    /**
     * `confirm` is sent when the user clicks Confirm button
     */
    @DomEvent("confirm")
    public static class ConfirmEvent extends ComponentEvent<ConfirmDialog> {
        public ConfirmEvent(ConfirmDialog source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * `reject` is sent when the user clicks Reject button
     */
    @DomEvent("reject")
    public static class RejectEvent extends ComponentEvent<ConfirmDialog> {
        public RejectEvent(ConfirmDialog source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * `cancel` is sent when the user clicks Cancel button or presses Escape
     * key. `cancel` is not sent if Cancel button is hidden
     */
    @DomEvent("cancel")
    public static class CancelEvent extends ComponentEvent<ConfirmDialog> {
        public CancelEvent(ConfirmDialog source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    private String height;
    private String width;

    private Registration afterProgrammaticNavigationListenerRegistration;

    /**
     * Sets the width of the component content area.
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px"
     * or "2.5em" (Using relative unit, such as percentage, will lead to
     * unexpected results).
     * <p>
     * If the provided {@code width} value is {@literal null} then width is
     * removed.
     *
     * @param width
     *            the width to set, may be {@code null}
     */
    @Override
    public void setWidth(String width) {
        this.width = width;
        updateWidth();
    }

    private void updateWidth() {
        this.getElement().executeJs("this._contentWidth = $0", this.width);
    }

    /**
     * Sets the height of the component content area.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em" (Using relative unit, such as percentage, will lead to
     * unexpected results).
     * <p>
     * If the provided {@code height} value is {@literal null} then height is
     * removed.
     *
     * @param height
     *            the height to set, may be {@code null}
     */
    @Override
    public void setHeight(String height) {
        this.height = height;
        updateHeight();
    }

    public void updateHeight() {
        this.getElement().executeJs("this._contentHeight = $0", this.height);
    }

    /**
     * Sets the CSS class names of the dialog overlay element. This method
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
     *             ConfirmDialog does not support adding styles to overlay
     */
    @Override
    public Style getStyle() {
        throw new UnsupportedOperationException(
                "ConfirmDialog does not support adding styles to overlay");
    }

    /**
     * Sets the `aria-describedby` attribute of the dialog overlay.
     * <p>
     * By default, all elements inside the message area are linked through the
     * `aria-describedby` attribute. However, there are cases where this can
     * confuse screen reader users (e.g. the dialog may present a password
     * confirmation form). For these cases, it's better to associate only the
     * elements that will help describe the confirmation dialog through this
     * API.
     * <p>
     * To restore the generated value, pass `null` as argument
     *
     * @param describedBy
     *            the attribute value
     */
    public void setAriaDescribedBy(String describedBy) {
        getElement().setProperty("accessibleDescriptionRef", describedBy);
    }

    /**
     * Gets the `aria-describedby` attribute of the dialog overlay.
     * <p>
     * Note that this will only return a value if
     * {@link #setAriaDescribedBy(String)} was called before.
     *
     * @return an optional aria-describedby of the dialog overlay, or an empty
     *         optional if no aria-describedby has been set
     */
    public Optional<String> getAriaDescribedBy() {
        return Optional.ofNullable(
                getElement().getProperty("accessibleDescriptionRef"));
    }

    private boolean autoAddedToTheUi;

    /**
     * Creates an empty dialog with a Confirm button
     */
    public ConfirmDialog() {
        getElement().addEventListener("opened-changed", event -> {
            if (!isOpened()) {
                setModality(false);
            }
            if (autoAddedToTheUi && !isOpened()) {
                getElement().removeFromParent();
                autoAddedToTheUi = false;
            }
        });
    }

    /**
     * Creates a dialog with a Confirm button with its click listener and a
     * given texts
     *
     * @param header
     *            the header text
     * @param text
     *            the confirmation message text
     * @param confirmText
     *            the text inside Confirm button
     * @param confirmListener
     *            the event listener for `confirm` event
     * @see #setHeader(String)
     * @see #setText(String)
     * @see #setConfirmButton(String, ComponentEventListener)
     */
    public ConfirmDialog(String header, String text, String confirmText,
            ComponentEventListener<ConfirmEvent> confirmListener) {
        this();
        setHeader(header);
        setText(text);
        setConfirmButton(confirmText, confirmListener);
    }

    /**
     * Creates a two button dialog with Confirm and Cancel buttons
     *
     * @param header
     *            the header text
     * @param text
     *            the confirmation message text
     * @param confirmText
     *            the text inside Confirm button
     * @param confirmListener
     *            the event listener for `confirm` event
     * @param cancelText
     *            the text inside Cancel button
     * @param cancelListener
     *            the event listener for `cancel` event
     * @see #setHeader(String)
     * @see #setText(String)
     * @see #setConfirmButton(String, ComponentEventListener)
     * @see #setCancelButton(String, ComponentEventListener)
     */
    public ConfirmDialog(String header, String text, String confirmText,
            ComponentEventListener<ConfirmEvent> confirmListener,
            String cancelText,
            ComponentEventListener<CancelEvent> cancelListener) {
        this(header, text, confirmText, confirmListener);
        setCancelButton(cancelText, cancelListener);
    }

    /**
     * Creates a three button dialog with Confirm, Reject and Cancel buttons
     *
     * @param header
     *            the header text
     * @param text
     *            the confirmation message text
     * @param confirmText
     *            the text inside Confirm button
     * @param confirmListener
     *            the event listener for `confirm` event
     * @param rejectText
     *            the text inside Reject button
     * @param rejectListener
     *            the event listener for `reject` event
     * @param cancelText
     *            the text inside Cancel button
     * @param cancelListener
     *            the event listener for `cancel` event
     * @see #setHeader(String)
     * @see #setText(String)
     * @see #setConfirmButton(String, ComponentEventListener)
     * @see #setCancelButton(String, ComponentEventListener)
     * @see #setRejectButton(String, ComponentEventListener)
     */
    public ConfirmDialog(String header, String text, String confirmText,
            ComponentEventListener<ConfirmEvent> confirmListener,
            String rejectText,
            ComponentEventListener<RejectEvent> rejectListener,
            String cancelText,
            ComponentEventListener<CancelEvent> cancelListener) {
        this(header, text, confirmText, confirmListener, cancelText,
                cancelListener);
        setRejectButton(rejectText, rejectListener);
    }

    /**
     * Whether to show or hide Cancel button.
     */
    public void setCancelable(boolean cancelable) {
        getElement().setProperty("cancelButtonVisible", cancelable);
    }

    /**
     * Whether to show or hide Reject button.
     */
    public void setRejectable(boolean rejectable) {
        getElement().setProperty("rejectButtonVisible", rejectable);
    }

    /**
     * Sets Reject button text and `reject` event listener. Makes Reject button
     * visible
     *
     * @param buttonText
     *            the text inside Reject button
     * @param rejectListener
     *            the event listener for `reject` event
     */
    public void setRejectButton(String buttonText,
            ComponentEventListener<RejectEvent> rejectListener) {
        setRejectable(true);
        setRejectText(buttonText);
        addRejectListener(rejectListener);
    }

    /**
     * Sets Reject button text, `reject` event listener, Reject button theme.
     * Makes Reject button visible
     *
     * @param buttonText
     *            the text inside Reject button
     * @param rejectListener
     *            the event listener for `reject` event
     * @param theme
     *            the theme applied for a Reject button
     */
    public void setRejectButton(String buttonText,
            ComponentEventListener<RejectEvent> rejectListener, String theme) {
        setRejectButton(buttonText, rejectListener);
        setRejectButtonTheme(theme);
    }

    /**
     * Sets custom Reject button
     *
     * @param component
     *            the component to display instead of default Reject button
     */
    public void setRejectButton(Component component) {
        SlotUtils.setSlot(this, "reject-button", component);
    }

    /**
     * Sets custom Reject button
     *
     * @param element
     *            the element to display instead of default Reject button
     * @deprecated Usage of Element API at Component level should be avoided
     */
    @Deprecated(forRemoval = true)
    public void setRejectButton(Element element) {
        SlotUtils.setSlot(this, "reject-button", element);
    }

    /**
     * Sets Cancel button text and `cancel` event listener. Makes Cancel button
     * visible
     *
     * @param buttonText
     *            the text inside Cancel button
     * @param cancelListener
     *            the event listener for `cancel` event
     */
    public void setCancelButton(String buttonText,
            ComponentEventListener<CancelEvent> cancelListener) {
        setCancelable(true);
        setCancelText(buttonText);
        addCancelListener(cancelListener);
    }

    /**
     * Sets Cancel button text, `cancel` event listener and Cancel button theme.
     * Makes Cancel button visible
     *
     * @param buttonText
     *            the text inside Cancel button
     * @param cancelListener
     *            the event listener for `cancel` event
     * @param theme
     *            the theme applied for a Cancel button
     */
    public void setCancelButton(String buttonText,
            ComponentEventListener<CancelEvent> cancelListener, String theme) {
        setCancelButton(buttonText, cancelListener);
        setCancelButtonTheme(theme);
    }

    /**
     * Sets custom cancel button
     *
     * @param component
     *            the component to display instead of default Cancel button
     */
    public void setCancelButton(Component component) {
        SlotUtils.setSlot(this, "cancel-button", component);
    }

    /**
     * Sets custom cancel button
     *
     * @param element
     *            the element to display instead of default Cancel button
     * @deprecated Usage of Element API at Component level should be avoided
     */
    @Deprecated(forRemoval = true)
    public void setCancelButton(Element element) {
        SlotUtils.setSlot(this, "cancel-button", element);
    }

    /**
     * Sets Confirm button text and `confirm` event listener
     *
     * @param buttonText
     *            the text inside Confirm button
     * @param confirmListener
     *            the event listener for `confirm` event
     */
    public void setConfirmButton(String buttonText,
            ComponentEventListener<ConfirmEvent> confirmListener) {
        setConfirmText(buttonText);
        addConfirmListener(confirmListener);
    }

    /**
     * Sets Confirm button text, `confirm` event listener and Confirm button
     * theme
     *
     * @param buttonText
     *            the text inside Confirm button
     * @param confirmListener
     *            the event listener for `confirm` event
     * @param theme
     *            the theme applied for a Confirm button
     */
    public void setConfirmButton(String buttonText,
            ComponentEventListener<ConfirmEvent> confirmListener,
            String theme) {
        setConfirmButton(buttonText, confirmListener);
        setConfirmButtonTheme(theme);
    }

    /**
     * Sets custom confirm button
     *
     * @param component
     *            the component to display instead of default Confirm button
     */
    public void setConfirmButton(Component component) {
        SlotUtils.setSlot(this, "confirm-button", component);
    }

    /**
     * Sets custom confirm button
     *
     * @param element
     *            the element to display instead of default Confirm button
     * @deprecated Usage of Element API at Component level should be avoided
     */
    @Deprecated(forRemoval = true)
    public void setConfirmButton(Element element) {
        SlotUtils.setSlot(this, "confirm-button", element);
    }

    /**
     * Sets confirmation message text
     */
    public void setText(String message) {
        getElement().setProperty("message", message);
    }

    /**
     * Sets custom confirmation message
     *
     * @param component
     *            the component to display instead of default confirmation text
     *            node
     */
    public void setText(Component component) {
        getElement().appendChild(component.getElement());
    }

    /**
     * Sets custom confirmation message element
     *
     * @param element
     *            the element to display instead of default confirmation text
     *            node
     * @deprecated Usage of Element API at Component level should be avoided
     */
    @Deprecated(forRemoval = true)
    public void setText(Element element) {
        getElement().appendChild(element);
    }

    /**
     * Sets Confirm button text
     */
    public void setConfirmText(String confirmText) {
        getElement().setProperty("confirmText", confirmText);
    }

    /**
     * Sets Confirm button theme
     */
    public void setConfirmButtonTheme(String confirmTheme) {
        getElement().setProperty("confirmTheme", confirmTheme);
    }

    /**
     * Adds `confirm` event listener
     */
    public Registration addConfirmListener(
            ComponentEventListener<ConfirmEvent> listener) {
        return ComponentUtil.addListener(this, ConfirmEvent.class, listener);
    }

    /**
     * Sets Cancel button text
     */
    public void setCancelText(String cancelText) {
        getElement().setProperty("cancelText", cancelText);

    }

    /**
     * Sets Cancel button theme
     */
    public void setCancelButtonTheme(String cancelTheme) {
        getElement().setProperty("cancelTheme", cancelTheme);
    }

    /**
     * Adds `cancel` event listener
     */
    public Registration addCancelListener(
            ComponentEventListener<CancelEvent> listener) {
        return ComponentUtil.addListener(this, CancelEvent.class, listener);
    }

    /**
     * Sets Reject button text
     */
    public void setRejectText(String rejectText) {
        getElement().setProperty("rejectText", rejectText);
    }

    /**
     * Sets Reject button theme
     */
    public void setRejectButtonTheme(String rejectTheme) {
        getElement().setProperty("rejectTheme", rejectTheme);
    }

    /**
     * Adds `reject` event listener
     */
    public Registration addRejectListener(
            ComponentEventListener<RejectEvent> listener) {
        return ComponentUtil.addListener(this, RejectEvent.class, listener);
    }

    /**
     * Sets confirmation dialog header text
     */
    public void setHeader(String header) {
        getElement().setProperty("header", header);
    }

    /**
     * Sets confirmation dialog custom header
     *
     * @param component
     *            the component to display instead of default header text
     */
    public void setHeader(Component component) {
        SlotUtils.setSlot(this, "header", component);
    }

    /**
     * Sets confirmation dialog custom header element
     *
     * @param element
     *            the element to display instead of default header text
     * @deprecated Usage of Element API at Component level should be avoided
     */
    @Deprecated(forRemoval = true)
    public void setHeader(Element element) {
        SlotUtils.setSlot(this, "header", element);
    }

    /**
     * Opens the dialog.
     * <p>
     * Note: You don't need to add the dialog component before opening it, cause
     * opening a dialog will automatically add it to the {@code <body>} if it's
     * not yet attached anywhere.
     */
    public void open() {
        setOpened(true);
    }

    /**
     * Closes the dialog.
     * <p>
     * Note: This method also removes the dialog component from the DOM after
     * closing it, unless you have added the component manually.
     */
    public void close() {
        setOpened(false);
    }

    @Synchronize(property = "opened", value = "opened-changed")
    public boolean isOpened() {
        return getElement().getProperty("opened", false);
    }

    /**
     * Opens or closes the dialog.
     * <p>
     * Note: Confirm-dialog will be attached or detached from the DOM
     * automatically, if it was not added manually.
     *
     * @param opened
     *            {@code true} to open the confirm-dialog, {@code false} to
     *            close it
     */
    public void setOpened(boolean opened) {
        if (opened) {
            ensureAttached();
        }
        setModality(opened);
        getElement().setProperty("opened", opened);
    }

    /**
     * Gets whether this dialog can be closed by hitting the esc-key or not.
     * <p>
     * By default, the dialog is closable with esc.
     *
     * @return {@code true} if this dialog can be closed with the esc-key,
     *         {@code false} otherwise
     */
    public boolean isCloseOnEsc() {
        return !getElement().getProperty("noCloseOnEsc", false);
    }

    /**
     * Sets whether this dialog can be closed by hitting the esc-key or not.
     * <p>
     * By default, the dialog is closable with esc.
     *
     * @param closeOnEsc
     *            {@code true} to enable closing this dialog with the esc-key,
     *            {@code false} to disable it
     */
    public void setCloseOnEsc(boolean closeOnEsc) {
        getElement().setProperty("noCloseOnEsc", !closeOnEsc);
    }

    /**
     * Confirm dialog does not support adding content. Use
     * {@link #setText(Component)} instead to initialize content as a component.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public void add(Component... components) {
        HasOrderedComponents.super.add(components);
    }

    /**
     * Confirm dialog does not support adding content. Use
     * {@link #setText(Component)} instead to initialize content as a component.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public void add(Collection<Component> components) {
        HasOrderedComponents.super.add(components);
    }

    /**
     * Confirm dialog does not support adding content. Use
     * {@link #setText(String)} instead to initialize content as text.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public void add(String text) {
        HasOrderedComponents.super.add(text);
    }

    /**
     * Confirm dialog does not support removing content.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public void remove(Component... components) {
        HasOrderedComponents.super.remove(components);
    }

    /**
     * Confirm dialog does not support removing content.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public void remove(Collection<Component> components) {
        HasOrderedComponents.super.remove(components);
    }

    /**
     * Confirm dialog does not support removing content.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public void removeAll() {
        HasOrderedComponents.super.removeAll();
    }

    /**
     * Confirm dialog does not support adding content. Use
     * {@link #setText(Component)} instead to initialize content as a component.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public void addComponentAtIndex(int index, Component component) {
        HasOrderedComponents.super.addComponentAtIndex(index, component);
    }

    /**
     * Confirm dialog does not support adding content. Use
     * {@link #setText(Component)} instead to initialize content as a component.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public void addComponentAsFirst(Component component) {
        HasOrderedComponents.super.addComponentAsFirst(component);
    }

    /**
     * Confirm dialog does not support replacing content.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public void replace(Component oldComponent, Component newComponent) {
        HasOrderedComponents.super.replace(oldComponent, newComponent);
    }

    /**
     * Confirm dialog does not support multiple components.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public int indexOf(Component component) {
        return HasOrderedComponents.super.indexOf(component);
    }

    /**
     * Confirm dialog does not support multiple components.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public int getComponentCount() {
        return HasOrderedComponents.super.getComponentCount();
    }

    /**
     * Confirm dialog does not support multiple components.
     * <p>
     * This method is inherited from {@link HasOrderedComponents} and has been
     * marked as deprecated to indicate that it is not supported.
     *
     * @deprecated since v24.4, not supported
     */
    @Deprecated
    @Override
    public Component getComponentAt(int index) {
        return HasOrderedComponents.super.getComponentAt(index);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Same as https://github.com/vaadin/flow-components/pull/725
        Shortcuts.setShortcutListenOnElement("this._overlayElement", this);
    }

    private void setModality(boolean modal) {
        if (isAttached()) {
            getUI().ifPresent(ui -> ui.setChildComponentModal(this, modal));
        }
    }

    private UI getCurrentUI() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException("UI instance is not available. "
                    + "It means that you are calling this method "
                    + "out of a normal workflow where it's always implicitly set. "
                    + "That may happen if you call the method from the custom thread without "
                    + "'UI::access' or from tests without proper initialization.");
        }
        return ui;
    }

    private void ensureAttached() {
        UI ui = getCurrentUI();
        StateTree.ExecutionRegistration addToUiRegistration = ui
                .beforeClientResponse(ui, context -> {
                    if (getElement().getNode().getParent() == null) {
                        ui.addToModalComponent(this);
                        autoAddedToTheUi = true;
                        updateWidth();
                        updateHeight();
                        ui.setChildComponentModal(this, true);
                    }
                    if (afterProgrammaticNavigationListenerRegistration != null) {
                        afterProgrammaticNavigationListenerRegistration
                                .remove();
                    }
                });
        if (ui.getSession() != null) {
            afterProgrammaticNavigationListenerRegistration = ui
                    .addAfterNavigationListener(event -> {
                        if (event.getLocationChangeEvent()
                                .getTrigger() == NavigationTrigger.PROGRAMMATIC) {
                            addToUiRegistration.remove();
                            afterProgrammaticNavigationListenerRegistration
                                    .remove();
                        }
                    });
        }
    }
}
