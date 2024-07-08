/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.accordion.examples;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

@Route(value = "vaadin-accordion")
@Theme(Lumo.class)
@BodySize(height = "100vh", width = "100vw")
public class MainView extends HorizontalLayout {

    public static final String ACCORDION_EVENTS = "accordion-events";
    public static final String PANEL_EVENTS = "panel-events";

    public MainView() {
        final Accordion accordion = new Accordion();
        accordion.setWidth("400px");
        accordion.getElement().getStyle().set("max-width", "400px");
        accordion.getElement().getStyle().set("min-width", "400px");

        final Div redDiv = new Div();
        redDiv.getStyle().set("background-color", "red");
        redDiv.setHeight("150px");
        redDiv.setWidth("400px");

        final Div greenDiv = new Div();
        greenDiv.getStyle().set("background-color", "green");
        greenDiv.setHeight("150px");
        greenDiv.setWidth("400px");

        final Div blueDiv = new Div();
        blueDiv.getStyle().set("background-color", "blue");
        blueDiv.setHeight("150px");
        blueDiv.setWidth("400px");

        final AccordionPanel redPanel = accordion.add("Red", redDiv);
        final AccordionPanel greenPanel = accordion.add("Green", greenDiv);
        final AccordionPanel disabledPanel = accordion.add("Disabled",
                new Span("Disabled panel"));
        disabledPanel.setEnabled(false);
        final AccordionPanel bluePanel = accordion.add("Blue", blueDiv);

        final VerticalLayout accordionEvents;
        accordionEvents = new VerticalLayout();
        accordionEvents.setId(ACCORDION_EVENTS);

        final VerticalLayout panelEvents = new VerticalLayout();
        panelEvents.setId(PANEL_EVENTS);

        accordion.addOpenedChangeListener(e -> {
            final Optional<AccordionPanel> openedPanel = e.getOpenedPanel();

            final String text = openedPanel
                    .map(accordionPanel -> accordionPanel.getSummaryText()
                            + " opened")
                    .orElse("Accordion closed");
            accordionEvents.add(new Span(text));
        });

        accordion.getChildren().map(AccordionPanel.class::cast)
                .forEach(panel -> panel.addOpenedChangeListener(event -> {
                    final String text = "Panel " + panel.getSummaryText() + " "
                            + (event.isOpened() ? "opened" : "closed");
                    panelEvents.add(new Span(text));
                }));

        final Div controls = new Div();

        final Button close = new Button("Close", e -> accordion.close());
        close.setId("close");

        final Button red = new Button("Red", e -> accordion.open(redPanel));
        red.setId("red");

        final Button green = new Button("Green",
                e -> accordion.open(greenPanel));
        green.setId("green");

        final Button disabled = new Button("Disabled",
                e -> accordion.open(disabledPanel));
        disabled.setId("disabled");

        final Button blue = new Button("Blue", e -> accordion.open(bluePanel));
        blue.setId("blue");

        final Button toggleDisabled = new Button("Toggle disabled",
                e -> disabledPanel.setEnabled(!disabledPanel.isEnabled()));
        toggleDisabled.setId("toggle-disabled");

        final Map<Button, Integer> indexButtons = IntStream.rangeClosed(0, 3)
                .boxed().collect(toMap(e -> {
                    final Button btn = new Button(e.toString());
                    btn.setId(e.toString());
                    btn.addClickListener(clickEvent -> accordion.open(e));
                    return btn;
                }, Function.identity(), Integer::compareTo,
                        LinkedHashMap::new));

        controls.add(red, green, disabled, blue, new Hr(), close,
                toggleDisabled, new Hr());
        controls.add(indexButtons.keySet().toArray(new Component[0]));

        final Button removeRed = new Button("Remove red",
                e -> accordion.remove(redPanel));
        removeRed.setId("removeRed");

        final Button removeBlueByContent = new Button("Remove blue by content",
                e -> accordion.remove(blueDiv));
        removeBlueByContent.setId("removeBlueByContent");

        controls.add(new Hr(), removeRed, removeBlueByContent);

        add(accordion, accordionEvents, panelEvents, controls);
    }
}
