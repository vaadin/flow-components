/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.formlayout.demo;

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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

/**
 * Demo view for {@link FormLayout}.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-form-layout")
public class FormLayoutView extends DemoView {

    @Override
    public void initView() {
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
        private String address = "";

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

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if ((firstName != null && !firstName.isEmpty())
                    || (lastName != null && !lastName.isEmpty())) {
                builder.append(firstName).append(" ").append(lastName)
                        .append(", ");
            }
            if (birthDate != null) {
                builder.append("born on ").append(birthDate).append(", ");
            }
            if (phone != null && !phone.isEmpty()) {
                builder.append("phone ").append(phone).append(", ");
            }
            if (email != null && !email.isEmpty()) {
                builder.append("e-mail ").append(email).append(", ");
            }
            if (address != null && !address.isEmpty()) {
                builder.append("address ").append(address);
            }
            return builder.toString();
        }
    }

    // begin-source-example
    // source-example-heading: Using form layout inside a composite
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
    // end-source-example

    private void createResponsiveLayout() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: A form layout with custom responsive layouting
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

        nameLayout.setResponsiveSteps(
                new ResponsiveStep("1px", 1),
                new ResponsiveStep("600px", 2),
                new ResponsiveStep("700px", 3));
        add(nameLayout);
        // end-source-example
        // @formatter:on

        addCard("A form layout with custom responsive layouting", nameLayout);
    }

    private void createFormLayoutWithItems() {
        // begin-source-example
        // source-example-heading: A form layout with fields wrapped in items
        FormLayout layoutWithFormItems = new FormLayout();

        TextField firstName = new TextField();
        firstName.setPlaceholder("John");

        TextField lastName = new TextField();
        lastName.setPlaceholder("Doe");

        TextField phone = new TextField();
        TextField email = new TextField();
        DatePicker birthDate = new DatePicker();
        Checkbox doNotCall = new Checkbox("Do not call");

        layoutWithFormItems.addFormItem(firstName, "First name");
        layoutWithFormItems.addFormItem(lastName, "Last name");

        layoutWithFormItems.addFormItem(birthDate, "Birthdate");
        layoutWithFormItems.addFormItem(email, "E-mail");
        FormItem phoneItem = layoutWithFormItems.addFormItem(phone, "Phone");
        phoneItem.add(doNotCall);

        add(layoutWithFormItems);

        // end-source-example

        addCard("A form layout with fields wrapped in items",
                layoutWithFormItems);
    }

    private void createLayoutHandleColspans() {
        // begin-source-example
        // source-example-heading: Handling columns and colspans in a layout
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
        add(columnLayout);
        // end-source-example

        firstName.setId("colspan-first-name");
        lastName.setId("colspan-last-name");
        nickname.setId("colspan-nickname");
        email.setId("colspan-email");
        website.setId("colspan-website");
        description.setId("colspan-description");

        addCard("Handling columns and colspans in a layout", columnLayout);
    }

    private void createFormLayoutWithBinder() {
        // begin-source-example
        // source-example-heading: A form layout with fields using Binder
        FormLayout layoutWithBinder = new FormLayout();
        Binder<Contact> binder = new Binder<>();

        // The object that will be edited
        Contact contactBeingEdited = new Contact();

        // Create the fields
        TextField address = new TextField();
        address.setValueChangeMode(ValueChangeMode.EAGER);
        TextField phone = new TextField();
        phone.setValueChangeMode(ValueChangeMode.EAGER);
        TextField email = new TextField();
        email.setValueChangeMode(ValueChangeMode.EAGER);
        Label infoLabel = new Label();
        NativeButton save = new NativeButton("Save");
        NativeButton reset = new NativeButton("Reset");

        layoutWithBinder.addFormItem(address, "Address");
        layoutWithBinder.addFormItem(email, "E-mail");
        layoutWithBinder.addFormItem(phone, "Phone");

        // Button bar
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(save, reset);
        save.getStyle().set("marginRight", "10px");

        // Both phone and email cannot be empty
        SerializablePredicate<String> phoneOrEmailPredicate = value -> !phone
                .getValue().trim().isEmpty()
                || !email.getValue().trim().isEmpty();

        // E-mail and phone have specific validators
        Binding<Contact, String> emailBinding = binder.forField(email)
                .withNullRepresentation("")
                .withValidator(phoneOrEmailPredicate,
                        "Please specify your email")
                .withValidator(new EmailValidator("Incorrect email address"))
                .bind(Contact::getEmail, Contact::setEmail);

        Binding<Contact, String> phoneBinding = binder.forField(phone)
                .withValidator(phoneOrEmailPredicate,
                        "Please specify your phone")
                .bind(Contact::getPhone, Contact::setPhone);

        // Trigger cross-field validation when the other field is changed
        email.addValueChangeListener(event -> phoneBinding.validate());
        phone.addValueChangeListener(event -> emailBinding.validate());

        // Address is a required field
        address.setRequiredIndicatorVisible(true);
        binder.forField(address)
                .withValidator(new StringLengthValidator(
                        "Please add the address", 1, null))
                .bind(Contact::getAddress, Contact::setAddress);

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
        });
        add(layoutWithBinder, actions, infoLabel);
        // end-source-example

        infoLabel.setId("binder-info");
        address.setId("binder-address");
        phone.setId("binder-phone");
        email.setId("binder-email");
        save.setId("binder-save");
        reset.setId("binder-reset");

        addCard("A form layout with fields using Binder", layoutWithBinder,
                actions, infoLabel);

    }

    private void createCompositeLayout() {
        // begin-source-example
        // source-example-heading: Using form layout inside a composite
        // And then just use it like a regular component
        MyCustomLayout layout = new MyCustomLayout();
        TextField name = new TextField();
        TextField email = new TextField();
        Checkbox emailUpdates = new Checkbox("E-mail me updates");

        layout.addItemWithLabel("Name", name);
        // Both the email field and the emailUpdates checkbox are wrapped inside
        // the same form item
        layout.addItemWithLabel("E-mail", email, emailUpdates);
        add(layout);
        // end-source-example

        addCard("Using form layout inside a composite", layout);
    }

}
