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
package com.vaadin.flow.component.confirmdialog;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.shared.internal.OverlayAutoAddController;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.shared.Registration;

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
 * <p>
 * Each Confirm Dialog should have a title and/or message. The “Confirm” button
 * is shown by default, while the two other buttons are not (they must be
 * explicitly enabled to be displayed).
 * <p>
 * Confirm Dialog is modal in {@link ModalityMode#STRICT} mode.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-confirm-dialog")
@NpmPackage(value = "@vaadin/confirm-dialog", version = "25.0.0-alpha20")
@JsModule("@vaadin/confirm-dialog/src/vaadin-confirm-dialog.js")
public class ConfirmDialog extends Component
        implements HasComponents, HasSize, HasStyle {

    /**
     * Event that is fired when the user clicks the Confirm button
     * <p>
     * Note that the event is fired before the dialog's closing animation has
     * finished. When manually adding / removing the dialog to / from the UI,
     * use the {@link ClosedEvent} to wait with the removal until the animation
     * has finished. When relying on the auto-add behavior by just calling
     * {@link #open()} or {@link #setOpened(boolean)}, this is not necessary.
     */
    @DomEvent("confirm")
    public static class ConfirmEvent extends ComponentEvent<ConfirmDialog> {
        public ConfirmEvent(ConfirmDialog source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * Event that is fired when the user clicks the Reject button
     * <p>
     * Note that the event is fired before the dialog's closing animation has
     * finished. When manually adding / removing the dialog to / from the UI,
     * use the {@link ClosedEvent} to wait with the removal until the animation
     * has finished. When relying on the auto-add behavior by just calling
     * {@link #open()} or {@link #setOpened(boolean)}, this is not necessary.
     */
    @DomEvent("reject")
    public static class RejectEvent extends ComponentEvent<ConfirmDialog> {
        public RejectEvent(ConfirmDialog source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * Event that is fired when the user clicks the Cancel button or presses
     * Escape. The event is not sent if the Cancel button is hidden.
     * <p>
     * Note that the event is fired before the dialog's closing animation has
     * finished. When manually adding / removing the dialog to / from the UI,
     * use the {@link ClosedEvent} to wait with the removal until the animation
     * has finished. When relying on the auto-add behavior by just calling
     * {@link #open()} or {@link #setOpened(boolean)}, this is not necessary.
     */
    @DomEvent("cancel")
    public static class CancelEvent extends ComponentEvent<ConfirmDialog> {
        public CancelEvent(ConfirmDialog source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * Event that is fired after the dialog's closing animation has finished.
     * Can be used to remove a dialog from the UI afterward.
     */
    @DomEvent("closed")
    public static class ClosedEvent extends ComponentEvent<ConfirmDialog> {
        public ClosedEvent(ConfirmDialog source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * Event that is fired when the confirm dialog's opened state changes.
     * <p>
     * Note that this event fires immediately when the opened property changes,
     * which, when closing the confirm dialog, is before the closing animation
     * has finished. To wait for the animation to finish, listen for the
     * {@link ClosedEvent} event.
     */
    public static class OpenedChangeEvent
            extends ComponentEvent<ConfirmDialog> {
        private final boolean opened;

        public OpenedChangeEvent(ConfirmDialog source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpened();
        }

        public boolean isOpened() {
            return opened;
        }
    }

    @Override
    public String getWidth() {
        return getElement().getProperty("width");
    }

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
        getElement().setProperty("width", width);
    }

    @Override
    public String getHeight() {
        return getElement().getProperty("height");
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
        getElement().setProperty("height", height);
    }

    /**
     * @throws UnsupportedOperationException
     *             ConfirmDialog does not support adding styles
     */
    @Override
    public Style getStyle() {
        throw new UnsupportedOperationException(
                "ConfirmDialog does not support adding styles");
    }

    /**
     * Sets the `aria-describedby` attribute of the dialog.
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
     * Gets the `aria-describedby` attribute of the dialog.
     * <p>
     * Note that this will only return a value if
     * {@link #setAriaDescribedBy(String)} was called before.
     *
     * @return an optional aria-describedby of the dialog, or an empty optional
     *         if no aria-describedby has been set
     */
    public Optional<String> getAriaDescribedBy() {
        return Optional.ofNullable(
                getElement().getProperty("accessibleDescriptionRef"));
    }

    /**
     * Creates an empty dialog with a Confirm button
     */
    public ConfirmDialog() {
        // Initialize auto-add behavior
        new OverlayAutoAddController<>(this, () -> ModalityMode.STRICT);

        setOpened(false);

        getElement().addPropertyChangeListener("opened", event -> {
            if (isAttached()) {
                getUI().ifPresent(
                        ui -> ui.setChildComponentModal(this, isOpened()));
            }
            fireEvent(new OpenedChangeEvent(this, event.isUserOriginated()));
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
     * Adds a listener for when the user clicks the Confirm button.
     * <p>
     * Note: The event is fired before the dialog's closing animation has
     * finished. When manually adding or removing the dialog to or from the UI,
     * use the {@link ClosedEvent} to wait with the removal until the animation
     * has finished. When relying on the auto-add behavior by just calling
     * {@link #open()} or {@link #setOpened(boolean)}, this is not necessary.
     *
     * @param listener
     *            the listener to add
     * @return a Registration for removing the event listener
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
     * Adds a listener for when the user clicks the Cancel button or presses
     * Escape.
     * <p>
     * Note: The event is fired before the dialog's closing animation has
     * finished. When manually adding or removing the dialog to or from the UI,
     * use the {@link ClosedEvent} to wait with the removal until the animation
     * has finished. When relying on the auto-add behavior by just calling
     * {@link #open()} or {@link #setOpened(boolean)}, this is not necessary.
     *
     * @param listener
     *            the listener to add
     * @return a Registration for removing the event listener
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
     * Adds a listener for when the user clicks the Reject button.
     * <p>
     * Note: The event is fired before the dialog's closing animation has
     * finished. When manually adding or removing the dialog to or from the UI,
     * use the {@link ClosedEvent} to wait with the removal until the animation
     * has finished. When relying on the auto-add behavior by just calling
     * {@link #open()} or {@link #setOpened(boolean)}, this is not necessary.
     *
     * @param listener
     *            the listener to add
     * @return a Registration for removing the event listener
     */
    public Registration addRejectListener(
            ComponentEventListener<RejectEvent> listener) {
        return ComponentUtil.addListener(this, RejectEvent.class, listener);
    }

    /**
     * Add a lister for when the dialog's closing animation has finished. Can be
     * used to remove the dialog from the UI afterward.
     *
     * @param listener
     *            the listener to add
     * @return a Registration for removing the event listener
     */
    public Registration addClosedListener(
            ComponentEventListener<ClosedEvent> listener) {
        return ComponentUtil.addListener(this, ClosedEvent.class, listener);
    }

    /**
     * Add a listener for when the confirm dialog's opened state changes.
     * <p>
     * Note that this event fires immediately when the opened property changes,
     * which, when closing the confirm dialog, is before the closing animation
     * has finished. To wait for the animation to finish, use
     * {@link #addClosedListener(ComponentEventListener)}.
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
     * Opens the dialog.
     * <p>
     * If a dialog was not added manually to a parent component, it will be
     * automatically added to the {@link UI} when opened, and automatically
     * removed from the UI when closed. Note that the dialog is then scoped to
     * the UI, and not the current view. As such, when navigating away from a
     * view, the dialog will still be opened or stay open. In order to close the
     * dialog when navigating away from a view, it should either be explicitly
     * added as a child to the view, or it should be explicitly closed when
     * leaving the view.
     */
    public void open() {
        setOpened(true);
    }

    /**
     * Closes the dialog.
     * <p>
     * This automatically removes the dialog from the {@link UI}, unless it was
     * manually added to a parent component.
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
     * If a dialog was not added manually to a parent component, it will be
     * automatically added to the {@link UI} when opened, and automatically
     * removed from the UI when closed. Note that the dialog is then scoped to
     * the UI, and not the current view. As such, when navigating away from a
     * view, the dialog will still be opened or stay open. In order to close the
     * dialog when navigating away from a view, it should either be explicitly
     * added as a child to the view, or it should be explicitly closed when
     * leaving the view.
     *
     * @param opened
     *            {@code true} to open the confirm-dialog, {@code false} to
     *            close it
     */
    public void setOpened(boolean opened) {
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
     * {@inheritDoc}
     * <p>
     * Removes all components from this component except elements that have slot
     * attribute, such as header and buttons.
     */
    @Override
    public void removeAll() {
        getElement().getChildren().forEach(child -> {
            if (!child.hasAttribute("slot")) {
                child.removeFromParent();
            }
        });
    }
}
