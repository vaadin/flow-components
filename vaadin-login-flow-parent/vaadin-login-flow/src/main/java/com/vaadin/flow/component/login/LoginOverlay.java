package com.vaadin.flow.component.login;

/*
 * #%L
 * Login for Vaadin Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Style;

/**
 * Server-side component for the {@code <vaadin-login-overlay>} component.
 *
 * On {@link LoginForm.LoginEvent} component becomes disabled. Disabled
 * component stops to process login events, however the
 * {@link LoginForm.ForgotPasswordEvent} event is processed anyway. To enable
 * use the {@link com.vaadin.flow.component.HasEnabled#setEnabled(boolean)}
 * method. Setting error {@link #setError(boolean)} true makes component
 * automatically enabled for the next login attempt.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-login-overlay")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/login", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-login", version = "23.1.0-beta1")
@JsModule("@vaadin/login/src/vaadin-login-overlay.js")
@JsModule("./loginOverlayConnector.js")
public class LoginOverlay extends AbstractLogin implements HasStyle {

    private Component title;

    private boolean autoAddedToTheUi;

    public LoginOverlay() {
        initEnsureDetachListener();
    }

    public LoginOverlay(LoginI18n i18n) {
        super(i18n);
        initEnsureDetachListener();
    }

    private void initEnsureDetachListener() {
        getElement().addPropertyChangeListener("opened", event -> {
            if (autoAddedToTheUi && !isOpened()) {
                getElement().removeFromParent();
                autoAddedToTheUi = false;
            }
        });
    }

    /**
     * Closes the login overlay.
     * <p>
     * Note: This method also removes the overlay component from the DOM after
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
     * Opens or closes the login overlay. On open component becomes enabled
     * {@link #setEnabled(boolean)}
     * <p>
     * Note: Overlay will be attached or detached from the DOM automatically, if
     * it was not added manually.
     *
     * @param opened
     *            {@code true} to open the login overlay, {@code false} to close
     *            it
     */
    public void setOpened(boolean opened) {
        if (opened) {
            ensureAttached();
            setEnabled(true);
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
                ui.addToModalComponent(this);
                autoAddedToTheUi = true;
            });
        }
    }

    /**
     * Sets the application title. Detaches the component title if it was set
     * earlier. Note: the method calls {@link #setTitle(Component)}, which will
     * reset the custom title, if it was set. Custom title can be reset only
     * when the overlay is closed.
     *
     * Title is a part of the I18n object. See {@link #setI18n(LoginI18n)}.
     *
     * @see #getTitleAsText()
     */
    public void setTitle(String title) {
        setTitle((Component) null);
        getElement().setProperty("title", title);
    }

    /**
     * Returns the value of the title property or a text content of the title if
     * it was set via {@link #setTitle(Component)}
     *
     * @return the string value of title
     */
    @Synchronize(property = "title", value = "title-changed")
    public String getTitleAsText() {
        if (title != null) {
            return title.getElement().getText();
        }
        return getElement().getProperty("title");
    }

    /**
     * Sets the application title, <code>null</code> to remove any previous
     * title and to display title set via {@link #setTitle(String)}. Note: the
     * title component has to be set when the overlay is closed.
     *
     * @see #getTitle()
     * @param title
     *            the title component to set, or <code>null</code> to remove any
     *            previously set title
     */
    public void setTitle(Component title) {
        if (isOpened()) {
            return;
        }
        if (this.title != null) {
            this.title.getElement().removeFromParent();
        }

        this.title = title;
        if (title == null) {
            return;
        }

        title.getElement().setAttribute("slot", "title");
        getElement().appendChild(title.getElement());
    }

    /**
     * Returns custom title component which was set via
     * {@link #setTitle(Component)}
     *
     * @return the title component, <code>null</code> if nothing was set
     */
    public Component getTitle() {
        return title;
    }

    /**
     * Sets the application description.
     *
     * Description is a part of I18n object. See {@link #setI18n(LoginI18n)}.
     *
     * @see #getDescription()
     * @param description
     *            the description string
     */
    public void setDescription(String description) {
        getElement().setProperty("description", description);
    }

    /**
     * @return the value of description property
     */
    @Synchronize(property = "description", value = "description-changed")
    public String getDescription() {
        return getElement().getProperty("description");
    }

    /**
     * @throws UnsupportedOperationException
     *             LoginOverlay does not support adding styles to overlay
     *             wrapper
     */
    @Override
    public Style getStyle() {
        throw new UnsupportedOperationException(
                "LoginOverlay does not support adding styles to overlay wrapper");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initConnector();
    }

    private void initConnector() {
        getElement().executeJs(
                "window.Vaadin.Flow.loginOverlayConnector.initLazy(this)");
    }
}
