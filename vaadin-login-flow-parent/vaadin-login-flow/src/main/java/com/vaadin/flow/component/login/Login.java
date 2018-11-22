package com.vaadin.flow.component.login;

/*
 * #%L
 * Vaadin Login for Vaadin 10
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
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

/**
 * Server-side component for the {@code <vaadin-login>} component.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-login")
@HtmlImport("frontend://bower_components/vaadin-login/src/vaadin-login.html")
public class Login extends Component {

    /**
     * Initializes a new Login.
     */
    public Login() {
        this(LoginI18n.createDefault());
    }

    /**
     * Initializes a new Login.
     *
     * @param i18n internationalized messages to be used by this instance.
     */
    public Login(LoginI18n i18n) {
        setI18n(i18n);
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
     * Adds `forgotPassword` event listener
     */
    public Registration addForgotPasswordListener(
        ComponentEventListener<ForgotPasswordEvent> listener) {
        return ComponentUtil
            .addListener(this, ForgotPasswordEvent.class, listener);
    }

    /**
     * `forgot-password` is fired when the user clicks Forgot password button
     */
    @DomEvent("forgot-password")
    public static class ForgotPasswordEvent extends ComponentEvent<Login> {
        public ForgotPasswordEvent(Login source, boolean fromClient) {
            super(source, fromClient);
        }
    }

}
