package com.vaadin.flow.component.confirmdialog;

/*
 * #%L
 * Vaadin Confirm Dialog for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

@Tag("vaadin-confirm-dialog")
@HtmlImport("frontend://bower_components/vaadin-confirm-dialog/src/vaadin-confirm-dialog.html")
public class ConfirmDialog extends Component
        implements HasSize, HasStyle, HasOrderedComponents<ConfirmDialog> {

    @DomEvent("confirm")
    public static class ConfirmEvent extends ComponentEvent<ConfirmDialog> {
        public ConfirmEvent(ConfirmDialog source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("reject")
    public static class RejectEvent extends ComponentEvent<ConfirmDialog> {
        public RejectEvent(ConfirmDialog source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("cancel")
    public static class CancelEvent extends ComponentEvent<ConfirmDialog> {
        public CancelEvent(ConfirmDialog source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    private boolean autoAddedToTheUi;

    public ConfirmDialog() {
        getElement().addEventListener("opened-changed", event -> {
            if (autoAddedToTheUi && !isOpened()) {
                getElement().removeFromParent();
                autoAddedToTheUi = false;
            }
        });
    }

    public ConfirmDialog(String header, String text, String confirmText,
            ComponentEventListener<ConfirmEvent> confirmListener) {
        this();
        setHeader(header);
        setText(text);
        setConfirmButton(confirmText, confirmListener);
    }

    public ConfirmDialog(String header, String text, String confirmText,
            ComponentEventListener<ConfirmEvent> confirmListener,
            String cancelText,
            ComponentEventListener<CancelEvent> cancelListener) {
        this(header, text, confirmText, confirmListener);
        setCancelButton(cancelText, cancelListener);
    }

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

    public void setCancelable(boolean cancelable) {
        getElement().setProperty("cancel", cancelable);
    }

    public void setRejectable(boolean rejectable) {
        getElement().setProperty("reject", rejectable);
    }

    public void setRejectButton(String buttonText,
            ComponentEventListener<RejectEvent> rejectListener) {
        setRejectable(true);
        setRejectText(buttonText);
        addRejectListener(rejectListener);
    }

    public void setRejectButton(String buttonText,
            ComponentEventListener<RejectEvent> rejectListener,
            String theme) {
        setRejectButton(buttonText, rejectListener);
        setRejectButtonTheme(theme);
    }

    public void setRejectButton(Element element) {
        addToSlot("reject-button", element);
    }

    public void setCancelButton(String buttonText,
            ComponentEventListener<CancelEvent> cancelListener) {
        setCancelable(true);
        setCancelText(buttonText);
        addCancelListener(cancelListener);
    }

    public void setCancelButton(String buttonText,
            ComponentEventListener<CancelEvent> cancelListener,
            String theme) {
        setCancelButton(buttonText, cancelListener);
        setCancelButtonTheme(theme);
    }

    public void setCancelButton(Element element) {
        addToSlot("cancel-button", element);
    }

    public void setConfirmButton(String buttonText,
            ComponentEventListener<ConfirmEvent> confirmListener) {
        setConfirmText(buttonText);
        addConfirmListener(confirmListener);
    }

    public void setConfirmButton(String buttonText,
            ComponentEventListener<ConfirmEvent> confirmListener,
            String theme) {
        setConfirmButton(buttonText, confirmListener);
        setConfirmButtonTheme(theme);
    }

    public void setConfirmButton(Element element) {
        addToSlot("confirm-button", element);
    }

    private void addToSlot(String slotName, Element element) {
        element.setAttribute("slot", slotName);
        getElement().appendChild(element);
    }

    public void setText(String message) {
        getElement().setProperty("message", message);
    }

    public void setText(Element element) {
        getElement().appendChild(element);
    }

    public void setConfirmText(String confirmText) {
        getElement().setProperty("confirmText", confirmText);

    }

    public void setConfirmButtonTheme(String confirmTheme) {
        getElement().setProperty("confirmTheme", confirmTheme);
    }

    public Registration addConfirmListener(
            ComponentEventListener<ConfirmEvent> listener) {
        return ComponentUtil.addListener(this, ConfirmEvent.class, listener);
    }

    public void setCancelText(String cancelText) {
        getElement().setProperty("cancelText", cancelText);

    }

    public void setCancelButtonTheme(String cancelTheme) {
        getElement().setProperty("cancelTheme", cancelTheme);
    }

    public Registration addCancelListener(
            ComponentEventListener<CancelEvent> listener) {
        return ComponentUtil.addListener(this, CancelEvent.class, listener);
    }

    public void setRejectText(String rejectText) {
        getElement().setProperty("rejectText", rejectText);

    }

    public void setRejectButtonTheme(String rejectTheme) {
        getElement().setProperty("rejectTheme", rejectTheme);
    }

    public Registration addRejectListener(
            ComponentEventListener<RejectEvent> listener) {
        return ComponentUtil.addListener(this, RejectEvent.class, listener);
    }

    public void setHeader(String header) {
        getElement().setProperty("header", header);
    }

    public void setHeader(Element element) {
        addToSlot("header", element);
    }

    public void open() {
        setOpened(true);
    }

    public void close() {
        setOpened(false);
    }

    @Synchronize(property = "opened", value = "opened-changed")
    public boolean isOpened() {
        return getElement().getProperty("opened", false);
    }

    public void setOpened(boolean opened) {
        if (opened) {
            ensureAttached();
        }
        getElement().setProperty("opened", opened);
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
        if (getElement().getNode().getParent() == null) {
            UI ui = getCurrentUI();
            ui.beforeClientResponse(ui, context -> {
                ui.add(this);
                autoAddedToTheUi = true;
            });
        }
    }
}
