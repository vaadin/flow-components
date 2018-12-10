package com.vaadin.flow.component.login;

/*
 * #%L
 * Vaadin AbstractLogin for Vaadin
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
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

/**
 * Abstract component for the {@code <vaadin-login>} component.
 * On {@link Login.LoginEvent} component becomes disabled.
 * Disabled component stops to process login events, however
 * the {@link Login.ForgotPasswordEvent} event is processed anyway.
 * To enable use the {@link com.vaadin.flow.component.HasEnabled#setEnabled(boolean)} method.
 *
 * @author Vaadin Ltd
 */
public abstract class AbstractLogin extends Component implements HasEnabled {

    private static final String LOGIN_EVENT = "login";

    /**
     * Initializes a new AbstractLogin with a default localization.
     */
    public AbstractLogin() {
        this(LoginI18n.createDefault());
        getElement().synchronizeProperty("disabled", LOGIN_EVENT);
        addLoginListener(e -> {
            setEnabled(false);
            setError(false);
        });
    }

    /**
     * Initializes a new AbstractLogin.
     *
     * @param i18n internationalized messages to be used by this instance.
     */
    public AbstractLogin(LoginI18n i18n) {
        setI18n(i18n);
    }

    /**
     * Sets the path where to send the form-data when a form is submitted.
     * Once action is defined a {@link AbstractLogin.LoginEvent} is not fired anymore.
     *
     * @see #getAction()
     */
    public void setAction(String action) {
        getElement().setProperty("action", action);
    }

    /**
     * Returns the action defined for a login form.
     *
     * @return the value of action property
     */
    @Synchronize(property = "action", value = "action-changed")
    public String getAction() {
        return getElement().getProperty("action");
    }

    /**
     * Sets whether to show or hide the error message.
     * The message can be set via {@link #setI18n(LoginI18n)}
     *
     * @see #isError()
     */
    public void setError(boolean error) {
        getElement().setProperty("error", error);
    }

    /**
     * Returns whether the error message is displayed or not
     *
     * @return the value of error property
     */
    @Synchronize(property = "error", value = "error-changed")
    public boolean isError() {
        return getElement().getProperty("error", false);
    }


    /**
     * Sets the internationalized messages to be used by this instance.
     *
     * @param i18n the internationalized messages
     * @see LoginI18n#createDefault()
     */
    public void setI18n(LoginI18n i18n) {
        getElement().setPropertyJson("i18n", JsonSerializer.toJson(i18n));
    }

    /**
     * Adds `login` event listener
     * Event is fired only if no action is defined
     */
    public Registration addLoginListener(ComponentEventListener<LoginEvent> listener) {
        return ComponentUtil.addListener(this, LoginEvent.class, listener);
    }

    /**
     * Adds `forgotPassword` event listener. Event continues being process even if
     * the component is not {@link #isEnabled()}.
     */
    public Registration addForgotPasswordListener(
            ComponentEventListener<ForgotPasswordEvent> listener) {
        return ComponentUtil.addListener(this, ForgotPasswordEvent.class, listener,
                domReg -> domReg.setDisabledUpdateMode(DisabledUpdateMode.ALWAYS));
    }

    /**
     * `login` is fired when the user either clicks Submit button or presses an Enter key.
     * Event is fired only if no action is set for login form and client-side validation passed.
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
    public static class ForgotPasswordEvent extends ComponentEvent<AbstractLogin> {
        public ForgotPasswordEvent(AbstractLogin source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        getElement().setProperty("disabled", !enabled);
    }
}
