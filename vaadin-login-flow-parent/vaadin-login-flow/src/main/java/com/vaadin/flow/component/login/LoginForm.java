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

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Login is a component that contains a login form. You can use it for
 * authenticating the user with a username and password. It is compatible with
 * password managers, supports internationalization and works on all device
 * sizes.
 * <p>
 * The basic Login component consists of a title (Log in), two input fields
 * (Username and Password), and two buttons (Log in and Forgot password). You
 * can customize the form’s title, descriptions, labels, and messages using
 * internationalization.
 * <p>
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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/login", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-login", version = "23.1.0-beta1")
@JsModule("@vaadin/login/src/vaadin-login-form.js")
public class LoginForm extends AbstractLogin implements HasStyle {

    public LoginForm() {
    }

    public LoginForm(LoginI18n i18n) {
        super(i18n);
    }

}
