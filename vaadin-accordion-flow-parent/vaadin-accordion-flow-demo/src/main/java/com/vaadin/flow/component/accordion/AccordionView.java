package com.vaadin.flow.component.accordion;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.details.Details.OpenedChangeEvent;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

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
        accordion.add("Green", createBox("green"))
                .addThemeVariants(
                        DetailsVariant.FILLED,
                        DetailsVariant.REVERSE,
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
        // PROGRESS INDICATOR
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setMax(100);
        progressBar.setMin(0);
        progressBar.setValue(25);

        BiConsumer<OpenedChangeEvent, Integer> activateProgress
                = (event, progress) -> {

            Style style = event.getSource().getSummary().getElement().getStyle();
            if (event.isOpened()) {
                style.set("color", "var(--lumo-primary-color)");
                progressBar.setValue(progress);
            } else {
                style.remove("color");
            }
        };


        // SUMMARY FACTORY
        BiFunction<Integer, String, Component> summaryFactory = (index, title) -> {
            Div summary = new Div();

            Div indexDiv = new Div();
            indexDiv.setText(String.valueOf(index));
            indexDiv.setHeight("25px");
            indexDiv.setWidth("25px");
            indexDiv.getStyle().set("border-radius", "50%");
            indexDiv.getStyle().set("border-style", "dashed");
            indexDiv.getStyle().set("border-width", "2px");
            indexDiv.getStyle().set("display", "inline-block");
            indexDiv.getStyle().set("font-weight", "bold");
            indexDiv.getStyle().set("line-height", "25px");
            indexDiv.getStyle().set("margin-inline-end", "10px");
            indexDiv.getStyle().set("text-align", "center");
            indexDiv.getStyle().set("vertical-align", "middle");

            summary.add(indexDiv);
            summary.add(new Span(title));

            return summary;
        };


        // BEGIN ACCORDION
        Accordion accordion = new Accordion();


        // ACCOUNT INFORMATION
        AccordionPanel accountInfo = new AccordionPanel();
        accountInfo.setSummary(summaryFactory.apply(1, "Account information"));
        accountInfo.addOpenedChangeListener(
                event -> activateProgress.accept(event, 25));

        FormLayout accountForm = new FormLayout();

        TextField emailField = new TextField();
        emailField.setLabel("Email");
        accountForm.add(emailField);

        TextField handleField = new TextField();
        handleField.setLabel("Handle");
        accountForm.add(handleField);


        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Password");
        accountForm.add(passwordField);


        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setLabel("Confirm password");
        accountForm.add(confirmPasswordField);

        accountInfo.setContent(accountForm);
        accordion.add(accountInfo);


        // PROFILE INFORMATION
        AccordionPanel profileInfo = new AccordionPanel();
        profileInfo.setSummary(summaryFactory.apply(2, "Profile information"));
        profileInfo.addOpenedChangeListener(
                event -> activateProgress.accept(event, 50));

        FormLayout profileInfoForm = new FormLayout();
        profileInfoForm.addFormItem(new TextField(), "First name");
        profileInfoForm.addFormItem(new TextField(), "Last name");
        RadioButtonGroup<String> languageGroup = new RadioButtonGroup<>();
        languageGroup.setItems("English", "Finnish");
        profileInfoForm.addFormItem(languageGroup, "Language");
        profileInfoForm.addFormItem(new DatePicker(), "Date of birth");

        profileInfo.setContent(profileInfoForm);
        accordion.add(profileInfo);


        // TOPICS OF INTEREST
        AccordionPanel topicsOfInterest = new AccordionPanel();
        topicsOfInterest.setSummary(summaryFactory.apply(3, "Topics of interest"));
        topicsOfInterest.addOpenedChangeListener(
                event -> activateProgress.accept(event, 75));

        FormLayout topicsForm = new FormLayout();
        topicsForm.add(new Checkbox("Culture"));
        topicsForm.add(new Checkbox("Environment"));
        topicsForm.add(new Checkbox("Fashion"));
        topicsForm.add(new Checkbox("Finance"));
        topicsForm.add(new Checkbox("Food", true));
        topicsForm.add(new Checkbox("Politics"));
        topicsForm.add(new Checkbox("Sports"));
        topicsForm.add(new Checkbox("Technology", true));

        topicsOfInterest.setContent(topicsForm);
        accordion.add(topicsOfInterest);


        // TERMS AND CONDITIONS
        AccordionPanel conditions = new AccordionPanel();
        conditions.setSummary(summaryFactory.apply(4, "Terms and conditions"));
        conditions.addOpenedChangeListener(
                event -> activateProgress.accept(event, 100));

        Paragraph paragraph = new Paragraph();
        paragraph.setText("After all has been said and done, I agree that " +
                "my data shall be safely stored for the sole purpose of " +
                "my ultimate enjoyment.");

        Button submit = new Button("Sign up");
        submit.setEnabled(false);
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickListener(e -> Notification.show("Complete! \uD83D\uDC4D",
                4000, Position.BOTTOM_END));

        Checkbox consent = new Checkbox("I agree");
        consent.addValueChangeListener(e -> submit.setEnabled(e.getValue()));

        Div termsDetails = new Div();
        termsDetails.getStyle().set("display", "flex");
        termsDetails.getStyle().set("flex-wrap", "wrap");
        termsDetails.getStyle().set("justify-content", "space-between");
        termsDetails.add(paragraph, consent, submit);

        conditions.setContent(termsDetails);
        accordion.add(conditions);

        // end-source-example

        addCard("Complex form", accordion, progressBar);
    }
}
