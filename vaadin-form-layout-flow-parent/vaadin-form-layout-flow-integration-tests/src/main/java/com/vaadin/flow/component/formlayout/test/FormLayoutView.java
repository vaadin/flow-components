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
package com.vaadin.flow.component.formlayout.test;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

/**
 * Demo view for {@link FormLayout}.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-form-layout")
public class FormLayoutView extends Div {

    public FormLayoutView() {
        createResponsiveLayout();
        createFormLayoutWithItems();
        createLayoutHandleColspans();
        createFormLayoutWithBinder();
        createCompositeLayout();
    }

    /**
     * Example Bean for the Form with Binder.
     */
    private static class Contact implements Serializable {

        private String firstName = "";
        private String lastName = "";
        private String phone = "";
        private String email = "";
        private LocalDate birthDate;
        private boolean doNotCall;

        public boolean isDoNotCall() {
            return doNotCall;
        }

        public void setDoNotCall(boolean doNotCall) {
            this.doNotCall = doNotCall;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public LocalDate getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(firstName).append(" ").append(lastName);
            if (birthDate != null) {
                builder.append(", born on ").append(birthDate);
            }
            if (phone != null && !phone.isEmpty()) {
                builder.append(", phone ").append(phone);
                if (doNotCall) {
                    builder.append(" (don't call me!)");
                } else {
                    builder.append(" (you can call me)");
                }
            }
            if (email != null && !email.isEmpty()) {
                builder.append(", e-mail ").append(email);
            }
            return builder.toString();
        }
    }

    // You can create a custom layout that internally uses FormLayout
    public class MyCustomLayout extends Composite<FormLayout> {

        public void addItemWithLabel(String label, Component... items) {
            Div itemWrapper = new Div();
            // Wrap the given items into a single div
            itemWrapper.add(items);
            // getContent() returns a wrapped FormLayout
            getContent().addFormItem(itemWrapper, label);
        }
    }

    private void createResponsiveLayout() {
        FormLayout nameLayout = new FormLayout();

        TextField titleField = new TextField();
        titleField.setLabel("Title");
        titleField.setPlaceholder("Sir");
        TextField firstNameField = new TextField();
        firstNameField.setLabel("First name");
        firstNameField.setPlaceholder("John");
        TextField lastNameField = new TextField();
        lastNameField.setLabel("Last name");
        lastNameField.setPlaceholder("Doe");

        nameLayout.add(titleField, firstNameField, lastNameField);

        // Default number of columns in a FormLayout is 2. By setting the
        // responsive steps we specify different numbers for columns with
        // breakpoints at “40em” “32em” and “25em”. Now by changing the size of
        // the browser horizontally, you can notice that the number of the
        // columns in the FormLayout changes.
        nameLayout.setResponsiveSteps(new ResponsiveStep("1px", 1),
                new ResponsiveStep("600px", 2), new ResponsiveStep("700px", 3));

        addCard("A form layout with custom responsive layouting", nameLayout);
    }

    private void createFormLayoutWithItems() {
        FormLayout layoutWithFormItems = new FormLayout();

        TextField firstName = new TextField();
        firstName.setPlaceholder("John");
        layoutWithFormItems.addFormItem(firstName, "First name");

        TextField lastName = new TextField();
        lastName.setPlaceholder("Doe");
        layoutWithFormItems.addFormItem(lastName, "Last name");

        addCard("A form layout with fields wrapped in items",
                layoutWithFormItems);
    }

    private void createLayoutHandleColspans() {
        FormLayout columnLayout = new FormLayout();
        // Setting the desired responsive steps for the columns in the layout
        columnLayout.setResponsiveSteps(new ResponsiveStep("25em", 1),
                new ResponsiveStep("32em", 2), new ResponsiveStep("40em", 3));
        TextField firstName = new TextField();
        firstName.setPlaceholder("First Name");
        TextField lastName = new TextField();
        lastName.setPlaceholder("Last Name");
        TextField email = new TextField();
        email.setPlaceholder("Email");
        TextField nickname = new TextField();
        nickname.setPlaceholder("Username");
        TextField website = new TextField();
        website.setPlaceholder("Link to personal website");
        TextField description = new TextField();
        description.setPlaceholder("Enter a short description about yourself");
        columnLayout.add(firstName, lastName, nickname, email, website);
        // You can set the desired column span for the components individually.
        columnLayout.setColspan(website, 2);
        // Or just set it as you add them.
        columnLayout.add(description, 3);

        firstName.setId("colspan-first-name");
        lastName.setId("colspan-last-name");
        nickname.setId("colspan-nickname");
        email.setId("colspan-email");
        website.setId("colspan-website");
        description.setId("colspan-description");

        addCard("Handling columns and colspans in a layout", columnLayout);
    }

    private void createFormLayoutWithBinder() {
        FormLayout layoutWithBinder = new FormLayout();
        Binder<Contact> binder = new Binder<>();

        // The object that will be edited
        Contact contactBeingEdited = new Contact();

        // Create the fields
        TextField firstName = new TextField();
        firstName.setValueChangeMode(ValueChangeMode.EAGER);
        TextField lastName = new TextField();
        lastName.setValueChangeMode(ValueChangeMode.EAGER);
        TextField phone = new TextField();
        phone.setValueChangeMode(ValueChangeMode.EAGER);
        TextField email = new TextField();
        email.setValueChangeMode(ValueChangeMode.EAGER);
        DatePicker birthDate = new DatePicker();
        Checkbox doNotCall = new Checkbox("Do not call");
        Label infoLabel = new Label();
        NativeButton save = new NativeButton("Save");
        NativeButton reset = new NativeButton("Reset");

        layoutWithBinder.addFormItem(firstName, "First name");
        layoutWithBinder.addFormItem(lastName, "Last name");
        layoutWithBinder.addFormItem(birthDate, "Birthdate");
        layoutWithBinder.addFormItem(email, "E-mail");
        FormItem phoneItem = layoutWithBinder.addFormItem(phone, "Phone");
        phoneItem.add(doNotCall);

        // Button bar
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(save, reset);
        save.getStyle().set("marginRight", "10px");

        SerializablePredicate<String> phoneOrEmailPredicate = value -> !phone
                .getValue().trim().isEmpty()
                || !email.getValue().trim().isEmpty();

        // E-mail and phone have specific validators
        Binding<Contact, String> emailBinding = binder.forField(email)
                .withValidator(phoneOrEmailPredicate,
                        "Both phone and email cannot be empty")
                .withValidator(new EmailValidator("Incorrect email address"))
                .bind(Contact::getEmail, Contact::setEmail);

        Binding<Contact, String> phoneBinding = binder.forField(phone)
                .withValidator(phoneOrEmailPredicate,
                        "Both phone and email cannot be empty")
                .bind(Contact::getPhone, Contact::setPhone);

        // Trigger cross-field validation when the other field is changed
        email.addValueChangeListener(event -> phoneBinding.validate());
        phone.addValueChangeListener(event -> emailBinding.validate());

        // First name and last name are required fields
        firstName.setRequiredIndicatorVisible(true);
        lastName.setRequiredIndicatorVisible(true);

        binder.forField(firstName)
                .withValidator(new StringLengthValidator(
                        "Please add the first name", 1, null))
                .bind(Contact::getFirstName, Contact::setFirstName);
        binder.forField(lastName)
                .withValidator(new StringLengthValidator(
                        "Please add the last name", 1, null))
                .bind(Contact::getLastName, Contact::setLastName);

        // Birthdate and doNotCall don't need any special validators
        binder.bind(doNotCall, Contact::isDoNotCall, Contact::setDoNotCall);
        binder.bind(birthDate, Contact::getBirthDate, Contact::setBirthDate);

        // Click listeners for the buttons
        save.addClickListener(event -> {
            if (binder.writeBeanIfValid(contactBeingEdited)) {
                infoLabel.setText("Saved bean values: " + contactBeingEdited);
            } else {
                BinderValidationStatus<Contact> validate = binder.validate();
                String errorText = validate.getFieldValidationStatuses()
                        .stream().filter(BindingValidationStatus::isError)
                        .map(BindingValidationStatus::getMessage)
                        .map(Optional::get).distinct()
                        .collect(Collectors.joining(", "));
                infoLabel.setText("There are errors: " + errorText);
            }
        });
        reset.addClickListener(event -> {
            // clear fields by setting null
            binder.readBean(null);
            infoLabel.setText("");
            doNotCall.setValue(false);
        });

        infoLabel.setId("binder-info");
        firstName.setId("binder-first-name");
        lastName.setId("binder-last-name");
        phone.setId("binder-phone");
        email.setId("binder-email");
        birthDate.setId("binder-birth-date");
        doNotCall.setId("binder-do-not-call");
        save.setId("binder-save");
        reset.setId("binder-reset");

        addCard("A form layout with fields using Binder", layoutWithBinder,
                actions, infoLabel);

    }

    private void createCompositeLayout() {
        // And then just use it like a regular component
        MyCustomLayout layout = new MyCustomLayout();
        TextField name = new TextField();
        TextField email = new TextField();
        Checkbox emailUpdates = new Checkbox("E-mail me updates");

        layout.addItemWithLabel("Name", name);
        // Both the email field and the emailUpdates checkbox are wrapped inside
        // the same form item
        layout.addItemWithLabel("E-mail", email, emailUpdates);

        addCard("Using form layout inside a composite", layout);
    }

    private void addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}
