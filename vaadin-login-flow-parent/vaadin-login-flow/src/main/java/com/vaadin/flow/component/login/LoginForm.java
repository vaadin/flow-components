/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.login;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Server-side component for the {@code <vaadin-login-form>} component.
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
@Tag("vaadin-login-form")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "22.1.0")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/login", version = "22.1.0")
@NpmPackage(value = "@vaadin/vaadin-login", version = "22.1.0")
@JsModule("@vaadin/login/src/vaadin-login-form.js")
public class LoginForm extends AbstractLogin {

    public LoginForm() {
    }

    public LoginForm(LoginI18n i18n) {
        super(i18n);
    }

}
