package com.vaadin.flow.component.accordion.examples;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.accordion.AccordionPanelOpenedChangedEvent;
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

@Route
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
        final AccordionPanel disabledPanel = accordion.add("Disabled", new Span("Disabled panel"));
        disabledPanel.setEnabled(false);
        final AccordionPanel bluePanel = accordion.add("Blue", blueDiv);

        final VerticalLayout accordionEvents;accordionEvents = new VerticalLayout();
        accordionEvents.setId(ACCORDION_EVENTS);

        final VerticalLayout panelEvents = new VerticalLayout();
        panelEvents.setId(PANEL_EVENTS);

        accordion.addOpenedChangedListener(e -> {
            final Optional<AccordionPanel> openedPanel = e.getOpenedPanel();
            final String text = openedPanel
                    .map(accordionPanel -> accordionPanel.getSummaryText() + " opened")
                    .orElse("Accordion collapsed");
            accordionEvents.add(new Span(text));
        });

        accordion.getChildren().forEach(e ->
                ComponentUtil.addListener(e, AccordionPanelOpenedChangedEvent.class, event -> {
            final String text = "Panel " + ((AccordionPanel) e).getSummaryText()
                    + " " + (event.isOpened() ? "opened" : "closed");
            panelEvents.add(new Span(text));
        }));

        final Div controls = new Div();

        final Button collapse = new Button("Collapse", e -> accordion.collapse());
        collapse.setId("collapse");

        final Button red = new Button("Red", e -> accordion.expand(redPanel));
        red.setId("red");

        final Button green = new Button("Green", e -> accordion.expand(greenPanel));
        green.setId("green");

        final Button disabled = new Button("Disabled", e -> accordion.expand(disabledPanel));
        disabled.setId("disabled");

        final Button blue = new Button("Blue", e -> accordion.expand(bluePanel));
        blue.setId("blue");

        final Button toggleDisabled = new Button("Toggle disabled", e -> disabledPanel.setEnabled(!disabledPanel.isEnabled()));
        toggleDisabled.setId("toggle-disabled");

        final Map<Button, Integer> indexButtons = IntStream.rangeClosed(0, 3).boxed().collect(toMap(e -> {
            final Button btn = new Button(e.toString());
            btn.setId(e.toString());
            btn.addClickListener(clickEvent -> accordion.expand(e));
            return btn;
        }, Function.identity(), Integer::compareTo, LinkedHashMap::new));

        controls.add(red, green, disabled, blue, new Hr(), collapse, toggleDisabled, new Hr());
        controls.add(indexButtons.keySet().toArray(new Component[0]));

        add(accordion, accordionEvents, panelEvents, controls);
    }
}
