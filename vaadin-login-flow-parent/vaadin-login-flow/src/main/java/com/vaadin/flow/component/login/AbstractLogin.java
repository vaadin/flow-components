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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.PropertyChangeListener;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

/**
 * Abstract component for the {@code <vaadin-login-overlay>} and
 * {@code <vaadin-login-form>} components. On {@link LoginForm.LoginEvent}
 * component becomes disabled. Disabled component stops to process login events,
 * however the {@link LoginForm.ForgotPasswordEvent} event is processed anyway.
 * To enable use the
 * {@link com.vaadin.flow.component.HasEnabled#setEnabled(boolean)} method.
 * Setting error {@link #setError(boolean)} true makes component automatically
 * enabled for the next login attempt.
 *
 * @author Vaadin Ltd
 */
public abstract class AbstractLogin extends Component implements HasEnabled {

    private static final String LOGIN_EVENT = "login";

    private static final String PROP_ACTION = "action";
    private static final String PROP_DISABLED = "disabled";
    private static final String PROP_ERROR = "error";
    private static final String PROP_NO_FORGOT_PASSWORD = "noForgotPassword";

    private static final PropertyChangeListener NO_OP = event -> {
    };

    /**
     * Initializes a new AbstractLogin with a default localization.
     */
    public AbstractLogin() {
        this(LoginI18n.createDefault());
        getElement().addPropertyChangeListener(PROP_DISABLED, LOGIN_EVENT,
                NO_OP);
        getElement().setProperty("_preventAutoEnable", true);
        addLoginListener(e -> {
            setEnabled(false);
            setError(false);
        });
    }

    /**
     * Initializes a new AbstractLogin.
     *
     * @param i18n
     *            internationalized messages to be used by this instance.
     */
    public AbstractLogin(LoginI18n i18n) {
        setI18n(i18n);
    }

    /**
     * Sets the path where to send the form-data when a form is submitted. Once
     * action is defined a {@link AbstractLogin.LoginEvent} is not fired
     * anymore.
     *
     * @see #getAction()
     */
    public void setAction(String action) {
        getElement().setProperty(PROP_ACTION, action);
    }

    /**
     * Returns the action defined for a login form.
     *
     * @return the value of action property
     */
    @Synchronize(property = PROP_ACTION, value = "action-changed")
    public String getAction() {
        return getElement().getProperty(PROP_ACTION);
    }

    /**
     * Sets whether to show or hide the error message. The message can be set
     * via {@link #setI18n(LoginI18n)}
     *
     * Calling this method with {@code true} will also enable the component.
     *
     * @see #isError()
     *
     * @param error
     *            {@code true} to show the error message and enable component
     *            for next login attempt, {@code false} to hide an error
     */
    public void setError(boolean error) {
        if (error) {
            setEnabled(true);
        }
        getElement().setProperty(PROP_ERROR, error);
    }

    /**
     * Returns whether the error message is displayed or not
     *
     * @return the value of error property
     */
    @Synchronize(property = PROP_ERROR, value = "error-changed")
    public boolean isError() {
        return getElement().getProperty(PROP_ERROR, false);
    }

    /**
     * Sets whether to show or hide the forgot password button. The button is
     * visible by default
     *
     * @see #isForgotPasswordButtonVisible()
     *
     * @param forgotPasswordButtonVisible
     *            whether to display or hide the button
     */
    public void setForgotPasswordButtonVisible(
            boolean forgotPasswordButtonVisible) {
        getElement().setProperty(PROP_NO_FORGOT_PASSWORD,
                !forgotPasswordButtonVisible);
    }

    /**
     * Returns whether the forgot password button is visible or not
     *
     * @return {@code true} if the forgot password button is visible
     *         {@code false} otherwise
     */
    public boolean isForgotPasswordButtonVisible() {
        return !getElement().getProperty(PROP_NO_FORGOT_PASSWORD, false);
    }

    /**
     * Sets the internationalized messages to be used by this instance.
     *
     * @param i18n
     *            the internationalized messages
     * @see LoginI18n#createDefault()
     */
    public void setI18n(LoginI18n i18n) {
        getElement().setPropertyJson("i18n", JsonSerializer.toJson(i18n));
    }

    /**
     * Adds `login` event listener
     */
    public Registration addLoginListener(
            ComponentEventListener<LoginEvent> listener) {
        return ComponentUtil.addListener(this, LoginEvent.class, listener);
    }

    /**
     * Adds `forgotPassword` event listener. Event continues being process even
     * if the component is not {@link #isEnabled()}.
     */
    public Registration addForgotPasswordListener(
            ComponentEventListener<ForgotPasswordEvent> listener) {
        return ComponentUtil.addListener(this, ForgotPasswordEvent.class,
                listener, domReg -> domReg
                        .setDisabledUpdateMode(DisabledUpdateMode.ALWAYS));
    }

    /**
     * `login` is fired when the user either clicks Submit button or presses an
     * Enter key. Event is fired only if client-side validation passed.
     */
    @DomEvent(LOGIN_EVENT)
    public static class LoginEvent extends ComponentEvent<AbstractLogin> {

        private String username;
        private String password;

        public LoginEvent(AbstractLogin source, boolean fromClient,
                @EventData("event.detail.username") String username,
                @EventData("event.detail.password") String password) {
            super(source, fromClient);
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    /**
     * `forgot-password` is fired when the user clicks Forgot password button
     */
    @DomEvent("forgot-password")
    public static class ForgotPasswordEvent
            extends ComponentEvent<AbstractLogin> {
        public ForgotPasswordEvent(AbstractLogin source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        getElement().setProperty(PROP_DISABLED, !enabled);
    }
}
