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
package com.vaadin.flow.component.contextmenu;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;

/**
 * @deprecated since v23.3, will be removed in v24.
 */
@Deprecated
@Tag("vaadin-context-menu")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.3.0-alpha6")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/context-menu", version = "23.3.0-alpha6")
@NpmPackage(value = "@vaadin/vaadin-context-menu", version = "23.3.0-alpha6")
@JsModule("@vaadin/context-menu/src/vaadin-context-menu.js")
@JsModule("@vaadin/polymer-legacy-adapter/template-renderer.js")
public abstract class GeneratedVaadinContextMenu<R extends GeneratedVaadinContextMenu<R>>
        extends Component implements HasStyle, ClickNotifier<R> {

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * CSS selector that can be used to target any child element of the context
     * menu to listen for {@code openOn} events.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code selector} property from the webcomponent
     *
     * @deprecated since v23.3, will be removed in v24.
     */
    @Deprecated
    protected String getSelectorString() {
        return getElement().getProperty("selector");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * CSS selector that can be used to target any child element of the context
     * menu to listen for {@code openOn} events.
     * </p>
     *
     * @param selector
     *            the String value to set
     *
     * @deprecated since v23.3, will be removed in v24.
     */
    @Deprecated
    protected void setSelector(String selector) {
        getElement().setProperty("selector", selector == null ? "" : selector);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * True if the overlay is currently displayed.
     * <p>
     * This property is synchronized automatically from client side when a
     * 'opened-changed' event happens.
     * </p>
     *
     * @return the {@code opened} property from the webcomponent
     *
     * @deprecated since v23.3, will be removed in v24.
     */
    @Deprecated
    @Synchronize(property = "opened", value = "opened-changed")
    protected boolean isOpenedBoolean() {
        return getElement().getProperty("opened", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Event name to listen for opening the context menu.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code openOn} property from the webcomponent
     *
     * @deprecated since v23.3, will be removed in v24.
     */
    @Deprecated
    protected String getOpenOnString() {
        return getElement().getProperty("openOn");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Event name to listen for opening the context menu.
     * </p>
     *
     * @param openOn
     *            the String value to set
     *
     * @deprecated since v23.3, will be removed in v24.
     */
    @Deprecated
    protected void setOpenOn(String openOn) {
        getElement().setProperty("openOn", openOn == null ? "" : openOn);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The target element that's listened to for context menu opening events. By
     * default the vaadin-context-menu listens to the target's
     * {@code vaadin-contextmenu} events.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code listenOn} property from the webcomponent
     *
     * @deprecated since v23.3, will be removed in v24.
     */
    @Deprecated
    protected JsonObject getListenOnJsonObject() {
        return (JsonObject) getElement().getPropertyRaw("listenOn");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * The target element that's listened to for context menu opening events. By
     * default the vaadin-context-menu listens to the target's
     * {@code vaadin-contextmenu} events.
     * </p>
     *
     * @param listenOn
     *            the JsonObject value to set
     *
     * @deprecated since v23.3, will be removed in v24.
     */
    @Deprecated
    protected void setListenOn(JsonObject listenOn) {
        getElement().setPropertyJson("listenOn", listenOn);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Event name to listen for closing the context menu.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code closeOn} property from the webcomponent
     *
     * @deprecated since v23.3, will be removed in v24.
     */
    @Deprecated
    protected String getCloseOnString() {
        return getElement().getProperty("closeOn");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Event name to listen for closing the context menu.
     * </p>
     *
     * @param closeOn
     *            the String value to set
     *
     * @deprecated since v23.3, will be removed in v24.
     */
    @Deprecated
    protected void setCloseOn(String closeOn) {
        getElement().setProperty("closeOn", closeOn == null ? "" : closeOn);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Closes the overlay.
     * </p>
     */
    protected void close() {
        getElement().callFunction("close");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Opens the overlay.
     * </p>
     *
     * @deprecated since v23.3, will be removed in v24.
     */
    @Deprecated
    protected void open() {
        getElement().callFunction("open");
    }

    /**
     * @deprecated since v23.3, will be removed in v24.
     */
    @Deprecated
    public static class OpenedChangeEvent<R extends GeneratedVaadinContextMenu<R>>
            extends ComponentEvent<R> {
        private final boolean opened;

        public OpenedChangeEvent(R source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpenedBoolean();
        }

        public boolean isOpened() {
            return opened;
        }
    }

    /**
     * Adds a listener for {@code opened-changed} events fired by the
     * webcomponent.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    protected Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent<R>> listener) {
        return getElement()
                .addPropertyChangeListener("opened",
                        event -> listener.onComponentEvent(
                                new OpenedChangeEvent<R>((R) this,
                                        event.isUserOriginated())));
    }

    /**
     * Has no effect for ContextMenu because the element for ContextMenu is not
     * clickable. Therefore, this method should not be used.
     *
     * @see #addClickListener(ComponentEventListener)
     *
     * @deprecated since 23.3
     */
    @Deprecated
    @Override
    public Registration addClickListener(
            ComponentEventListener<ClickEvent<R>> listener) {
        return ClickNotifier.super.addClickListener(listener);
    }

    /**
     * Has no effect for ContextMenu because the element for ContextMenu is not
     * clickable. Therefore, this method should not be used.
     *
     * @see #addClickShortcut(Key, KeyModifier...)
     *
     * @deprecated since 23.3
     */
    @Deprecated
    @Override
    public ShortcutRegistration addClickShortcut(Key key,
            KeyModifier... keyModifiers) {
        return ClickNotifier.super.addClickShortcut(key, keyModifiers);
    }
}
