package com.vaadin.flow.component.select.examples;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

@Route(value = "")
public class TestView extends Div implements HasUrlParameter<String> {

    public static final String SELECT_FIRST_ITEM = "Select first item";
    public static final String SELECT_THIRD_ITEM = "Select third item";
    public static final String SELECT_LAST_ITEM = "Select last item";
    public static final String ITEMS_PARAM = "items=";
    public static final String SELECT_PARAM = "select=";
    private int valueChangeCounter = 0;
    private int itemCounter = 0;

    private final Select<Item> select;
    private List<Item> items;
    private Hr hr0;
    private Hr hr2;
    private Hr hrLast;
    private final Div valueChangeContainer;
    private Checkbox enabled;
    private Checkbox readOnly;
    private Checkbox visible;

    private ComponentRenderer<Div, Item> componentRenderer = new ComponentRenderer<>(
            this::createItemComponent);

    public TestView() {
        select = new Select<>();

        createOptions();

        valueChangeContainer = new Div();
        valueChangeContainer.setId("value-change-container");

        add(select, valueChangeContainer);
    }

    private Div createItemComponent(Item item) {
        Div root = new Div();

        Span span = new Span(item.text);
        NativeButton updateButton = new NativeButton("Update-" + item.index,
                event -> {
                    item.text = item.text + "-UPDATED";
                    select.getDataProvider().refreshItem(item);
                });
        NativeButton removeButton = new NativeButton(
                "Remove button " + item.index);
        removeButton.addClickListener(event -> root.remove(removeButton));
        root.add(span, updateButton, removeButton);
        return root;
    }

    private void createOptions() {
        Div options = new Div();
        options.add(new NativeButton(SELECT_FIRST_ITEM,
                event -> select.setValue(items.get(0))));
        options.add(new NativeButton(SELECT_THIRD_ITEM,
                event -> select.setValue(items.get(2))));
        options.add(new NativeButton(SELECT_LAST_ITEM,
                event -> select.setValue(items.get(items.size() - 1))));

        options.add(new Div());

        options.add(new NativeButton("Reset 0 items", event -> setItems(0)));
        options.add(new NativeButton("Reset 1 items", event -> setItems(1)));
        options.add(new NativeButton("Reset 2 items", event -> setItems(2)));
        options.add(new NativeButton("Reset 5 items", event -> setItems(5)));
        options.add(new NativeButton("Reset 10 items", event -> setItems(10)));
        options.add(new NativeButton("Reset 20 items", event -> setItems(20)));
        options.add(new NativeButton("Reset 50 items", event -> setItems(50)));

        options.add(new Div());

        options.add(
                new NativeButton("Refresh item 0", event -> refreshItem(0)));
        options.add(
                new NativeButton("Refresh item 2", event -> refreshItem(2)));
        options.add(
                new NativeButton("Refresh item 10", event -> refreshItem(10)));
        options.add(new NativeButton("Refresh All", event -> refreshAll()));

        options.add(new Div());

        options.add(new Checkbox("ItemLabelGenerator",
                event -> setItemLabelGenerator(event.getValue())));
        options.add(new Checkbox("ItemEnabledProvider",
                event -> setItemEnabledProvider(event.getValue())));

        options.add(new Div());

        Checkbox emptySelectionEnabled = new Checkbox("emptySelectionEnabled",
                event -> setEmptySelectionAllowed(event.getValue()));
        emptySelectionEnabled.setValue(select.isEmptySelectionAllowed());
        emptySelectionEnabled.setId("emptySelectionEnabled");
        options.add(emptySelectionEnabled);

        Input emptySelectionCaption = new Input();
        emptySelectionCaption.setId("emptySelectionCaption");
        emptySelectionCaption.setValue(select.getEmptySelectionCaption());
        emptySelectionCaption.addValueChangeListener(
                event -> setEmptySelectionCaption(event.getValue()));
        options.add(new Span("EmptySelectionCaption:"), emptySelectionCaption);

        options.add(new Div());

        options.add(new NativeButton("focus()", event -> select.focus()));

        Checkbox requiredIndicatorVisible = new Checkbox("RequiredIndicator",
                event -> select.setRequiredIndicatorVisible(event.getValue()));
        requiredIndicatorVisible.setValue(select.isRequiredIndicatorVisible());
        requiredIndicatorVisible.setId("requiredIndicatorVisible");
        options.add(requiredIndicatorVisible);

        Input errorMessage = new Input();
        errorMessage.setId("errorMessage");
        errorMessage.addValueChangeListener(
                event -> select.setErrorMessage(event.getValue()));
        options.add(new Span("errorMessage"), errorMessage);

        Input placeholder = new Input();
        placeholder.setId("placeholder");
        placeholder.addValueChangeListener(
                event -> select.setPlaceholder(event.getValue()));
        options.add(new Span("placeholder"), placeholder);

        options.add(new Div());

        options.add(new NativeButton("Set renderer", event -> {
            if (select.getItemRenderer() == null) {
                select.setRenderer(componentRenderer);
            } else {
                select.setRenderer(null);
            }
        }), new NativeButton("Add HR 0", event -> addHrInBeginning()),
                new NativeButton("Remove HR 0", event -> removeHrInBeginning()),
                new NativeButton("Add HR 2", event -> addHrAfterIndexTwo()),
                new NativeButton("Remove HR 2",
                        event -> removeHrAfterIndexTwo()),
                new NativeButton("Add HR LAST", event -> addHrLast()),
                new NativeButton("Remove HR LAST", event -> removeHrLast()));

        options.add(new Div());

        enabled = new Checkbox("Enabled");
        enabled.setValue(select.isEnabled());
        enabled.addValueChangeListener(
                event -> select.setEnabled(event.getValue()));

        readOnly = new Checkbox("ReadOnly");
        readOnly.setValue(select.isReadOnly());
        readOnly.addValueChangeListener(
                event -> select.setReadOnly(event.getValue()));

        visible = new Checkbox("Visible");
        visible.setValue(select.isVisible());
        visible.addValueChangeListener(
                event -> select.setVisible(event.getValue()));

        options.add(enabled, readOnly, visible);

        add(options);
    }

    private void setItemEnabledProvider(boolean value) {
        if (value) {
            select.setItemEnabledProvider(item -> item.index % 2 == 0);
        } else {
            select.setItemEnabledProvider(null);
        }
    }

    private void setItemLabelGenerator(boolean value) {
        if (value) {
            select.setItemLabelGenerator(item -> item + "-LABEL");
        } else {
            select.setItemLabelGenerator(null);
        }
    }

    private void addHrInBeginning() {
        if (hr0 == null) {
            hr0 = new Hr();
            hr0.setId("hr0");
        }
        select.addComponentAsFirst(hr0);
    }

    private void removeHrInBeginning() {
        select.remove(hr0);
    }

    private void addHrAfterIndexTwo() {
        if (hr2 == null) {
            hr2 = new Hr();
            hr2.setId("hr2");
        }
        select.addComponents(items.get(2), hr2);
    }

    private void removeHrAfterIndexTwo() {
        select.remove(hr2);
    }

    private void addHrLast() {
        if (hrLast == null) {
            hrLast = new Hr();
            hrLast.setId("hrLast");
        }
        select.add(hrLast);
    }

    private void removeHrLast() {
        select.remove(hrLast);
    }

    private void setEmptySelectionCaption(String value) {
        select.setEmptySelectionCaption(value);
    }

    private void setEmptySelectionAllowed(boolean value) {
        select.setEmptySelectionAllowed(value);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent,
            @OptionalParameter String parameter) {
        if (parameter == null) {
            return;
        }

        parameter = parameter.toLowerCase();
        if (parameter.contains(ITEMS_PARAM)) {
            setItems(parseIntParam(ITEMS_PARAM, parameter));
        } else if (parameter.contains("emptyselectioncaption")) {
            setEmptySelectionAllowed(true);
        } else if (parameter.contains("emptyselectioncaption=")) {
            setEmptySelectionCaption(
                    parameter.replace("emptyselectioncaption=", ""));
        } else if (parameter.contains("autofocus")) {
            select.setAutofocus(true);
        }

        if (parameter.contains("disabled")) {
            select.setEnabled(false);
            enabled.setValue(false);
        }

        if (parameter.contains("readonly")) {
            select.setReadOnly(true);
            readOnly.setValue(true);
        }

        if (parameter.contains("invisible")) {
            select.setVisible(false);
            visible.setValue(false);
        }

        if (parameter.contains("itemlabelgenerator")) {
            setItemLabelGenerator(true);
        }

        if (parameter.contains("itemenabledprovider")) {
            setItemEnabledProvider(true);
        }

        if (parameter.contains("renderer")) {
            select.setRenderer(componentRenderer);
        }

        if (parameter.contains(SELECT_PARAM)) {
            select.setValue(items.get(parseIntParam(SELECT_PARAM, parameter)));
        }

        if (parameter.contains("hr0")) {
            addHrInBeginning();
        }
        if (parameter.contains("hr2")) {
            addHrAfterIndexTwo();
        }
        if (parameter.contains("hrlast")) {
            addHrLast();
        }

        // added here so that event from initial server side value is ignored
        select.addValueChangeListener(event -> {
            Div message = new Div();
            int valueId = valueChangeCounter++;
            message.setText(createEventString(event, valueId));
            message.setId("VCE-" + valueId);
            valueChangeContainer.addComponentAsFirst(message);
        });
    }

    private static int parseIntParam(String parameterName, String parameter) {
        int fromIndex = parameter.indexOf(parameterName);
        if (parameter.substring(fromIndex).contains("&")) {
            int endIndex = parameter.substring(fromIndex).contains("&")
                    ? parameter.indexOf('&', fromIndex)
                    : parameter.length();
            return Integer.parseInt(parameter.substring(fromIndex, endIndex)
                    .replace(parameterName, ""));
        } else {
            return Integer.parseInt(parameter.substring(fromIndex)
                    .replace(parameterName, ""));
        }
    }

    private void refreshItem(int index) {
        Item item = items.get(index);
        item.update();
        select.getDataProvider().refreshItem(item);
    }

    private void refreshAll() {
        items.stream().forEach(Item::update);
        select.getDataProvider().refreshAll();
    }

    private void setItems(int seed) {
        items = new ArrayList<>();
        for (int i = 0; i < seed; i++) {
            items.add(new Item(itemCounter++));
        }
        select.setItems(items);
    }

    private class Item implements Serializable {

        private final int index;
        private String text;

        Item(int index) {
            this.text = "Item-" + index;
            this.index = index;
        }

        public void update() {
            text = text + "-UPDATED";
        }

        @Override
        public String toString() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Item item = (Item) o;
            return index == item.index && Objects.equals(text, item.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index);
        }
    }

    public static String createEventString(
            HasValue.ValueChangeEvent<Item> event, int valueChangeCounter) {
        return createEventString(
                event.getValue() == null ? "null" : event.getValue().text,
                event.getOldValue() == null ? "null" : event.getOldValue().text,
                event.isFromClient(), valueChangeCounter);
    }

    public static String createEventString(String value, String oldValue,
            boolean isFromClient, int valueChangeCounter) {
        return "VALUE:" + value + "|OLD:" + oldValue + "|CLIENT:" + isFromClient
                + "|INDEX:" + valueChangeCounter;
    }

}
