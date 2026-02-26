/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for Login components.
 */
@Route(value = "login", layout = MainLayout.class)
@PageTitle("Login | Vaadin Kitchen Sink")
public class LoginDemoView extends VerticalLayout {

    public LoginDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Login Components"));
        add(new Paragraph("Login components provide authentication interfaces."));

        // Basic login form
        LoginForm basicForm = new LoginForm();
        basicForm.addLoginListener(e -> {
            if ("admin".equals(e.getUsername()) && "admin".equals(e.getPassword())) {
                Notification.show("Login successful!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                basicForm.setError(true);
            }
        });
        basicForm.addForgotPasswordListener(e ->
            Notification.show("Forgot password clicked"));
        addSection("Basic Login Form", basicForm);

        // Login form without forgot password
        LoginForm noForgot = new LoginForm();
        noForgot.setForgotPasswordButtonVisible(false);
        noForgot.addLoginListener(e ->
            Notification.show("Login attempted: " + e.getUsername()));
        addSection("Without Forgot Password", noForgot);

        // Disabled login form
        LoginForm disabled = new LoginForm();
        disabled.setEnabled(false);
        addSection("Disabled Login Form", disabled);

        // Login overlay button
        Button openOverlayBtn = new Button("Open Login Overlay", e -> {
            LoginOverlay overlay = new LoginOverlay();
            overlay.setTitle("My Application");
            overlay.setDescription("Login to access the system");
            overlay.addLoginListener(event -> {
                if ("admin".equals(event.getUsername()) && "admin".equals(event.getPassword())) {
                    overlay.close();
                    Notification.show("Login successful!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    overlay.setError(true);
                }
            });
            overlay.setOpened(true);
        });
        addSection("Login Overlay", openOverlayBtn);

        // Custom i18n
        LoginForm customI18n = new LoginForm();
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("Sign In");
        i18n.getForm().setUsername("Email Address");
        i18n.getForm().setPassword("Secret Code");
        i18n.getForm().setSubmit("Enter");
        i18n.getForm().setForgotPassword("Need help?");
        i18n.getErrorMessage().setTitle("Authentication Failed");
        i18n.getErrorMessage().setMessage("Invalid credentials. Please try again.");
        customI18n.setI18n(i18n);
        customI18n.addLoginListener(e ->
            Notification.show("Custom login attempted"));
        addSection("Custom Internationalization", customI18n);

        // Additional info text
        LoginForm withInfo = new LoginForm();
        LoginI18n infoI18n = LoginI18n.createDefault();
        infoI18n.setAdditionalInformation("Contact IT support at support@company.com for account issues.");
        withInfo.setI18n(infoI18n);
        withInfo.addLoginListener(e ->
            Notification.show("Login attempted"));
        addSection("With Additional Information", withInfo);

        // Login form in error state
        LoginForm errorState = new LoginForm();
        errorState.setError(true);
        errorState.addLoginListener(e -> {
            if ("admin".equals(e.getUsername()) && "admin".equals(e.getPassword())) {
                errorState.setError(false);
                Notification.show("Login successful!");
            } else {
                errorState.setError(true);
            }
        });
        addSection("Error State (try admin/admin)", errorState);
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
