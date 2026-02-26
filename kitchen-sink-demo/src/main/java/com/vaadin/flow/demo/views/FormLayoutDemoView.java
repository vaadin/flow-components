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

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for FormLayout component.
 */
@Route(value = "form-layout", layout = MainLayout.class)
@PageTitle("Form Layout | Vaadin Kitchen Sink")
public class FormLayoutDemoView extends VerticalLayout {

    public FormLayoutDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Form Layout Component"));
        add(new Paragraph("FormLayout arranges form fields with configurable columns and responsive behavior."));

        // Basic form layout
        FormLayout basic = new FormLayout();
        basic.addFormItem(new TextField(), "First Name");
        basic.addFormItem(new TextField(), "Last Name");
        basic.addFormItem(new EmailField(), "Email");
        basic.addFormItem(new TextField(), "Phone");
        addSection("Basic Form Layout", basic);

        // Two-column layout
        FormLayout twoColumn = new FormLayout();
        twoColumn.setResponsiveSteps(
            new ResponsiveStep("0", 1),
            new ResponsiveStep("500px", 2)
        );
        twoColumn.addFormItem(new TextField(), "First Name");
        twoColumn.addFormItem(new TextField(), "Last Name");
        twoColumn.addFormItem(new EmailField(), "Email");
        twoColumn.addFormItem(new TextField(), "Phone");
        twoColumn.addFormItem(new TextField(), "City");
        twoColumn.addFormItem(new TextField(), "Country");
        addSection("Two-Column Responsive Layout", twoColumn);

        // Three-column layout
        FormLayout threeColumn = new FormLayout();
        threeColumn.setResponsiveSteps(
            new ResponsiveStep("0", 1),
            new ResponsiveStep("400px", 2),
            new ResponsiveStep("700px", 3)
        );
        threeColumn.addFormItem(new TextField(), "First Name");
        threeColumn.addFormItem(new TextField(), "Middle Name");
        threeColumn.addFormItem(new TextField(), "Last Name");
        threeColumn.addFormItem(new TextField(), "Street");
        threeColumn.addFormItem(new TextField(), "City");
        threeColumn.addFormItem(new TextField(), "ZIP");
        addSection("Three-Column Responsive Layout", threeColumn);

        // With colspan
        FormLayout withColspan = new FormLayout();
        withColspan.setResponsiveSteps(
            new ResponsiveStep("0", 1),
            new ResponsiveStep("500px", 2)
        );
        withColspan.addFormItem(new TextField(), "First Name");
        withColspan.addFormItem(new TextField(), "Last Name");
        EmailField email = new EmailField();
        FormLayout.FormItem emailItem = withColspan.addFormItem(email, "Email");
        withColspan.setColspan(emailItem, 2);
        TextArea address = new TextArea();
        FormLayout.FormItem addressItem = withColspan.addFormItem(address, "Address");
        withColspan.setColspan(addressItem, 2);
        addSection("With Column Span", withColspan);

        // Registration form example
        FormLayout registration = new FormLayout();
        registration.setResponsiveSteps(
            new ResponsiveStep("0", 1),
            new ResponsiveStep("500px", 2)
        );

        TextField firstName = new TextField();
        firstName.setRequired(true);
        registration.addFormItem(firstName, "First Name *");

        TextField lastName = new TextField();
        lastName.setRequired(true);
        registration.addFormItem(lastName, "Last Name *");

        EmailField regEmail = new EmailField();
        regEmail.setRequired(true);
        FormLayout.FormItem regEmailItem = registration.addFormItem(regEmail, "Email *");
        registration.setColspan(regEmailItem, 2);

        PasswordField password = new PasswordField();
        password.setRequired(true);
        registration.addFormItem(password, "Password *");

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setRequired(true);
        registration.addFormItem(confirmPassword, "Confirm Password *");

        addSection("Registration Form Example", registration);

        // Contact form example
        FormLayout contact = new FormLayout();
        contact.setResponsiveSteps(
            new ResponsiveStep("0", 1),
            new ResponsiveStep("500px", 2)
        );

        contact.addFormItem(new TextField(), "Name");
        contact.addFormItem(new EmailField(), "Email");
        contact.addFormItem(new TextField(), "Subject");

        TextArea message = new TextArea();
        message.setMinHeight("150px");
        FormLayout.FormItem messageItem = contact.addFormItem(message, "Message");
        contact.setColspan(messageItem, 2);

        addSection("Contact Form Example", contact);
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
