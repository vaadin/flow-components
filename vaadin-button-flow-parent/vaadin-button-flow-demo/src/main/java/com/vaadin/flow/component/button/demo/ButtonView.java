/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.button.demo;

import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.button.GeneratedVaadinButton;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Button} demo.
 */
@Route("vaadin-button")
public class ButtonView extends DemoView {
    private Div message;

    @Override
    public void initView() {
        createDefaultButton();
        createButtonsWithIcons();
        createImageButtonWithAutofocus();
        createImageButtonWithAccessibleLabel();
        createButtonsWithTabIndex();
        createDisabledButton();
        createButtonWithDisableOnClick();
        addVariantsFeature();
        createButtonsWithShortcuts();

        message = new Div();
        message.setId("buttonMessage");
        getElement().appendChild(message.getElement());
    }

    private void createDefaultButton() {
        // begin-source-example
        // source-example-heading: Default button
        Button button = new Button("Vaadin button");

        button.addClickListener(this::showButtonClickedMessage);
        add(button);
        // end-source-example

        addCard("Default button", button);
        button.setId("default-button");
    }

    private void createButtonsWithIcons() {
        // begin-source-example
        // source-example-heading: Buttons with icons
        Button leftButton = new Button("Left", new Icon(VaadinIcon.ARROW_LEFT));

        Button rightButton = new Button("Right",
                new Icon(VaadinIcon.ARROW_RIGHT));
        rightButton.setIconAfterText(true);

        Button thumbsUpButton = new Button(new Icon(VaadinIcon.THUMBS_UP));
        add(leftButton, rightButton, thumbsUpButton);
        // end-source-example

        leftButton.addClickListener(this::showButtonClickedMessage);
        rightButton.addClickListener(this::showButtonClickedMessage);
        thumbsUpButton.addClickListener(this::showButtonClickedMessage);

        addCard("Buttons with icons", leftButton, rightButton, thumbsUpButton);
        leftButton.setId("left-icon-button");
        rightButton.setId("right-icon-button");
        thumbsUpButton.setId("thumb-icon-button");
    }

    private void createImageButtonWithAutofocus() {
        // begin-source-example
        // source-example-heading: Button with image and autofocus
        Button button = new Button(
                new Image("img/vaadin-logo.svg", "Vaadin logo"));
        button.setAutofocus(true);

        button.addClickListener(this::showButtonClickedMessage);
        add(button);
        // end-source-example

        addCard("Button with image and autofocus", button);
        button.setId("image-button");
    }

    private void createImageButtonWithAccessibleLabel() {
        // begin-source-example
        // source-example-heading: Button with ARIA label
        Button button = new Button("Accessible");
        button.getElement().setAttribute("aria-label", "Click me");

        button.addClickListener(this::showButtonClickedMessage);
        add(button);
        // end-source-example

        addCard("Button with ARIA label", button);
        button.setId("accessible-button");
    }

    private void createButtonsWithTabIndex() {
        // begin-source-example
        // source-example-heading: Buttons with custom tabindex
        Button button1 = new Button("1");
        button1.setTabIndex(1);
        button1.addClickListener(this::showButtonClickedMessage);

        Button button2 = new Button("2");
        button2.setTabIndex(2);
        button2.addClickListener(this::showButtonClickedMessage);

        Button button3 = new Button("3");
        button3.setTabIndex(3);
        button3.addClickListener(this::showButtonClickedMessage);
        add(button3, button2, button1);
        // end-source-example

        addCard("Buttons with custom tabindex", button3, button2, button1);
        button1.setId("button-tabindex-1");
        button2.setId("button-tabindex-2");
        button3.setId("button-tabindex-3");
    }

    private void createDisabledButton() {
        // begin-source-example
        // source-example-heading: Disabled button
        Button button = new Button("Disabled");
        button.setEnabled(false);
        add(button);
        // end-source-example

        addCard("Disabled button", button);
        button.addClickListener(evt -> message.setText("Button "
                + evt.getSource().getText()
                + " was clicked, but the button is disabled and this shouldn't happen!"));
        button.setId("disabled-button");
    }

    private void addVariantsFeature() {
        // begin-source-example
        // source-example-heading: Theme variants usage
        Button button = new Button();
        button.addThemeVariants(ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_PRIMARY);
        add(button);
        // end-source-example

        addVariantsDemo(() -> button, GeneratedVaadinButton::addThemeVariants,
                GeneratedVaadinButton::removeThemeVariants,
                ButtonVariant::getVariantName, ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_PRIMARY);
    }

    private void createButtonsWithShortcuts() {
        // begin-source-example
        // source-example-heading: Button shortcuts
        Button button = new Button("Has global Enter-shortcut",
                this::showButtonClickedMessage);
        button.addClickShortcut(Key.ENTER);

        TextField firstName = new TextField("First name");
        firstName.setValueChangeMode(ValueChangeMode.EAGER);
        TextField lastName = new TextField("Last name");
        lastName.setValueChangeMode(ValueChangeMode.EAGER);
        Button clearButton = new Button("Clear fields", event -> {
            firstName.clear();
            lastName.clear();
        });
        VerticalLayout container = new VerticalLayout(firstName, lastName,
                clearButton);
        clearButton.addClickShortcut(Key.KEY_L, KeyModifier.ALT)
                .listenOn(container);

        Paragraph paragraph = new Paragraph("Button \"Clean fields\"'s "
              + "shortcut ALT+L works only within the text fields.");
        container.add(paragraph);

        add(button, container);
        // end-source-example
        addCard("Button shortcuts", button, container);
        button.setId("shortcuts-enter-button");
        clearButton.setId("shortcuts-clear-button");
        firstName.setId("shortcuts-firstname");
        lastName.setId("shortcuts-lastname");
    }

    private void showButtonClickedMessage(ClickEvent<Button> evt) {
        Button source = evt.getSource();
        source.getParent()
                .ifPresent(parent -> parent.getElement().insertChild(
                        parent.getElement().getChildCount() - 2,
                        message.getElement()));

        String text = source.getText();
        if (text.isEmpty() && containsChild(source, "img")) {
            text = "with image";
        } else if (text.isEmpty() && containsChild(source, "iron-icon")) {
            text = "thumbs up";
        }

        message.setText("Button " + text + " was clicked.");
    }

    private boolean containsChild(Component parent, String tagName) {
        return parent.getElement().getChildren()
                .anyMatch(element -> element.getTag().equals(tagName));
    }

    private void createButtonWithDisableOnClick() {
        // begin-source-example
        // source-example-heading: Button disabled on click
        Button disableOnClickButton = new Button("Disabled on click", event -> {
            // Triggering an action that can be started only once
        });
        disableOnClickButton.setDisableOnClick(true);

        Button temporarilyDisabledButton = new Button(
                "Temporarily disabled button", event -> {
                    try {
                        // Blocking the user from clicking the button
                        // multiple times, due to a long running request that
                        // is not running asynchronously.
                        Thread.sleep(1500);
                        event.getSource().setEnabled(true);
                    } catch (InterruptedException e) {
                    }
                });
        temporarilyDisabledButton.setDisableOnClick(true);

        final Div disabledMessage = new Div();
        disabledMessage.setId("disabled-message");

        AtomicInteger runCount = new AtomicInteger(0);
        Button enable = new Button("Enable disabled button", click -> {
            disabledMessage.setText("Re-enabled button from server.");
            disableOnClickButton.setEnabled(true);
            runCount.set(0);
        });

        Button toggle = new Button("Disable on click true", event -> {
            disableOnClickButton.setDisableOnClick(
                    !disableOnClickButton.isDisableOnClick());
            event.getSource().setText("Disable on click "
                    + disableOnClickButton.isDisableOnClick());
        });

        add(disableOnClickButton, enable, toggle, disabledMessage,
              temporarilyDisabledButton);
        // end-source-example
        toggle.setId("toggle-button");

        addCard("Button disabled on click", disableOnClickButton, enable,
                toggle, disabledMessage, new Div(temporarilyDisabledButton));

        disableOnClickButton.addClickListener(evt -> disabledMessage
                .setText("Button " + evt.getSource().getText()
                        + " was clicked and enabled state was changed to "
                        + evt.getSource().isEnabled() + " receiving "
                        + runCount.incrementAndGet() + " clicks"));

        disableOnClickButton.setId("disable-on-click-button");
        temporarilyDisabledButton.setId("temporarily-disabled-button");
        enable.setId("enable-button");
    }
}
