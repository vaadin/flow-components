package com.vaadin.flow.component.login;

/*
 * #%L
 * Vaadin Login for Vaadin
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
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

import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;

/**
 * Server-side component for the {@code <vaadin-login-overlay>} component.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-login-overlay")
@HtmlImport("frontend://bower_components/vaadin-login/src/vaadin-login-overlay.html")
public class LoginOverlay extends AbstractLogin {

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
     * Opens or closes the login overlay.
     * On open component becomes enabled {@link #setEnabled(boolean)}
     * <p>
     * Note: Overlay will be attached or detached from the DOM automatically,
     * if it was not added manually.
     *
     * @param opened
     *            {@code true} to open the login overlay, {@code false} to close it
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
                ui.add(this);
                autoAddedToTheUi = true;
            });
        }
    }
}
