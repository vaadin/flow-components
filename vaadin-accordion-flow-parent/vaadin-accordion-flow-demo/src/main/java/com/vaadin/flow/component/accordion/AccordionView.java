package com.vaadin.flow.component.accordion;

import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@Route("vaadin-accordion")
public class AccordionView extends DemoView {

    @Override
    protected void initView() {
        basicUsage();
        filledPannels();
        smallPannels();
        reversePannels();
        complexForm();
    }

    private void basicUsage() {

        // begin-source-example
        // source-example-heading: Basics
        Accordion accordion = new Accordion();

        VerticalLayout personalInformationLayout = new VerticalLayout();
        personalInformationLayout.add(
            new TextField("Name"),
            new TextField("Phone"),
            new TextField("Email")
        );
        accordion.add("Personal Information", personalInformationLayout);

        VerticalLayout billingAddressLayout = new VerticalLayout();
        billingAddressLayout.add(
            new TextField("Address"),
            new TextField("City"),
            new TextField("State"),
            new TextField("Zip Code")
        );
        accordion.add("Billing Address", billingAddressLayout);

        VerticalLayout paymenLayout = new VerticalLayout();
        paymenLayout.add(
            new Span("Not yet implemented")
        );
        AccordionPanel billingAddressPanel = accordion.add("Payment", paymenLayout);
        billingAddressPanel.setEnabled(false);

        // end-source-example

        addCard("Basics", accordion);
    }

    private void filledPannels() {

        // begin-source-example
        // source-example-heading: Theme variants - Filled Pannels
        Accordion accordion = new Accordion();

        accordion.add("Panel 1", new Span("Panel content"))
            .addThemeVariants(DetailsVariant.FILLED);

        accordion.add("Panel 2", new Span("Panel content"))
            .addThemeVariants(DetailsVariant.FILLED);

        AccordionPanel disabledPannel = accordion.add("Panel 3", new Span("Panel content"));
        disabledPannel.addThemeVariants(DetailsVariant.FILLED);
        disabledPannel.setEnabled(false);

        // end-source-example

        addCard("Theme variants - Filled Pannels", accordion);
    }

    private void smallPannels() {

        // begin-source-example
        // source-example-heading: Theme variants - Small Pannels
        Accordion accordion = new Accordion();

        accordion.add("Panel 1", new Span("Panel content"))
            .addThemeVariants(DetailsVariant.SMALL);

        accordion.add("Panel 2", new Span("Panel content"))
            .addThemeVariants(DetailsVariant.SMALL);

        accordion.add("Panel 3", new Span("Panel content"))
            .addThemeVariants(DetailsVariant.SMALL);

        // end-source-example

        addCard("Theme variants - Small Pannels", accordion);
    }

    private void reversePannels() {

        // begin-source-example
        // source-example-heading: Theme variants - Reverse Pannels
        Accordion accordion = new Accordion();

        accordion.add("Panel 1", new Span("Panel content"))
            .addThemeVariants(DetailsVariant.REVERSE);

        accordion.add("Panel 2", new Span("Panel content"))
            .addThemeVariants(DetailsVariant.REVERSE);

        accordion.add("Panel 3", new Span("Panel content"))
            .addThemeVariants(DetailsVariant.REVERSE);

        // end-source-example

        addCard("Theme variants - Reverse Pannels", accordion);
    }

    private void complexForm() {
        // begin-source-example
        // source-example-heading: Complex form
        // BEGIN ACCORDION
        Accordion accordion = new Accordion();

        // ACCOUNT INFORMATION
        FormLayout accountForm = new FormLayout();
        accountForm.add(new TextField("Email"));
        accountForm.add(new TextField("Handle"));
        accountForm.add(new PasswordField("Password"));
        accountForm.add(new PasswordField("Confirm password"));

        accordion.add("Account information", accountForm);

        // PROFILE INFORMATION
        FormLayout profileInfoForm = new FormLayout();
        profileInfoForm.add(new TextField("First name"));
        profileInfoForm.add(new TextField("Last name"));
        RadioButtonGroup<String> languageGroup = new RadioButtonGroup<>();
        languageGroup.setLabel("Language");
        languageGroup.setItems("English", "Finnish");
        profileInfoForm.add(languageGroup);
        profileInfoForm.add(new DatePicker("Date of birth"));

        accordion.add("Profile information", profileInfoForm);

        // TOPICS OF INTEREST
        FormLayout topicsForm = new FormLayout();
        topicsForm.add(new Checkbox("Culture"));
        topicsForm.add(new Checkbox("Environment"));
        topicsForm.add(new Checkbox("Fashion"));
        topicsForm.add(new Checkbox("Finance"));
        topicsForm.add(new Checkbox("Food", true));
        topicsForm.add(new Checkbox("Politics"));
        topicsForm.add(new Checkbox("Sports"));
        topicsForm.add(new Checkbox("Technology", true));

        accordion.add("Topics of interest", topicsForm);

        // TERMS AND CONDITIONS
        Paragraph paragraph = new Paragraph();
        paragraph.setText("After all has been said and done, I agree that "
                + "my data shall be safely stored for the sole purpose of "
                + "my ultimate enjoyment.");

        Button submit = new Button("Sign up");
        submit.setEnabled(false);
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickListener(e -> Notification.show("Complete! \uD83D\uDC4D",
                4000, Position.BOTTOM_END));
        Checkbox consent = new Checkbox("I agree");
        consent.addValueChangeListener(e -> submit.setEnabled(e.getValue()));

        HorizontalLayout bottomPanel = new HorizontalLayout(consent, submit);
        bottomPanel.setWidthFull();
        bottomPanel.setFlexGrow(1, consent);
        VerticalLayout terms = new VerticalLayout(paragraph, bottomPanel);

        accordion.add("Terms and conditions", terms);
        // end-source-example

        addCard("Complex form", accordion);
    }
}
