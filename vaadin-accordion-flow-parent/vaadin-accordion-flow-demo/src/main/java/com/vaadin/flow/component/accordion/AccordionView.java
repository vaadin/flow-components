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
        basicAccordion();
        themeVariants();
        eventHandling();
        complexForm();
    }

    private void basicAccordion() {
        // begin-source-example
        // source-example-heading: Basic Accordion
        Accordion accordion = new Accordion();
        accordion.add("Red", createBox("red"));
        accordion.add("Orange", createBox("orange"));
        accordion.add("Yellow", createBox("yellow"));
        accordion.add("Green", createBox("green"));
        accordion.add("Blue", createBox("blue"));
        accordion.add("Indigo", createBox("indigo"));
        accordion.add("Violet", createBox("violet"));

        // end-source-example

        addCard("Basic Accordion", accordion);
    }

    private void themeVariants() {
        // begin-source-example
        // source-example-heading: Theme variants
        Accordion accordion = new Accordion();

        accordion.add("Red", createBox("red"))
                .addThemeVariants(DetailsVariant.FILLED);

        accordion.add("Orange", createBox("orange"))
                .addThemeVariants(DetailsVariant.REVERSE);

        accordion.add("Yellow", createBox("yellow"))
                .addThemeVariants(DetailsVariant.SMALL);

        // Multiple theme variants can be combined.
        accordion.add("Green", createBox("green")).addThemeVariants(
                DetailsVariant.FILLED, DetailsVariant.REVERSE,
                DetailsVariant.SMALL);

        // end-source-example

        addCard("Theme variants", accordion);
    }

    private void eventHandling() {
        // begin-source-example
        // source-example-heading: Event handling
        Accordion accordion = new Accordion();

        accordion.add("Top start", createBox("red"));
        accordion.add("Top end", createBox("orange"));
        accordion.add("Bottom start", createBox("yellow"));
        accordion.add("Bottom end", createBox("yellow"));

        Checkbox isActive = new Checkbox("Activate notifications");

        Stream<AccordionPanel> panels = accordion.getChildren()
                .map(AccordionPanel.class::cast);

        panels.forEach(panel -> panel.addOpenedChangeListener(event -> {
            if (event.isOpened() && isActive.getValue()) {
                String title = event.getSource().getSummaryText();
                String position = title.replace(' ', '_').toUpperCase();

                Notification.show(title + " opened", 2000,
                        Position.valueOf(position));
            }
        }));

        accordion.addOpenedChangeListener(event -> {
            if (!event.getOpenedPanel().isPresent() && isActive.getValue()) {
                Notification.show("Accordion closed", 2000,
                        Position.BOTTOM_CENTER);
            }
        });

        // end-source-example

        addCard("Event handling", accordion, isActive);
    }

    private Component createBox(String color) {
        final Div box = new Div();
        box.getStyle().set("background-color", color);
        box.getStyle().set("border-radius", "10px");
        box.setHeight("150px");
        box.setWidth("400px");
        return box;
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
