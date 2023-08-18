/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.router.Route;

@Route("all-fields-basic-validation")
public class AllFieldsBasicValidationPage extends Div {
    public static class Bean {
        private String string;
        private Integer integer;
        private Number number;
        private BigDecimal bigDecimal;
        private LocalDate date;
        private LocalTime time;
        private LocalDateTime dateTime;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public Integer getInteger() {
            return integer;
        }

        public void setInteger(Integer integer) {
            this.integer = integer;
        }

        public Number getNumber() {
            return number;
        }

        public void setNumber(Number number) {
            this.number = number;
        }

        public BigDecimal getBigDecimal() {
            return bigDecimal;
        }

        public void setBigDecimal(BigDecimal bigDecimal) {
            this.bigDecimal = bigDecimal;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }
    }

    public AllFieldsBasicValidationPage() {
        super();
        addTextField();
        addNumberField();
        addIntegerField();
        addBigDecimalField();
        addDatePicker();
        addTimePicker();
        addDateTimePicker();
    }

    private void addNumberField() {
        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setPadding(true);
        wrapper.setAlignItems(Alignment.CENTER);

        NumberField field = new NumberField("Number Field");
        field.setWidth("300px");
        wrapper.add(field);

        AtomicInteger validationCounter = new AtomicInteger(0);
        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.addStatusChangeListener(event -> {
            field.setHelperText("Validation count: " + validationCounter.incrementAndGet());
        });
        binder.forField(field).bind("number");

        wrapper.add(createCheckbox("Eager mode", event -> {
            if (field.getValueChangeMode() == ValueChangeMode.ON_CHANGE) {
                field.setValueChangeMode(ValueChangeMode.EAGER);
            } else {
                field.setValueChangeMode(ValueChangeMode.ON_CHANGE);
            }
        }));

        wrapper.add(createInput("Set step", event -> {
            double value = Double.parseDouble(event.getValue());
            field.setStep(value);
        }));

        wrapper.add(createInput("Set min", event -> {
            double value = Double.parseDouble(event.getValue());
            field.setMin(value);
        }));

        wrapper.add(createInput("Set max", event -> {
            double value = Double.parseDouble(event.getValue());
            field.setMax(value);
        }));

        wrapper.add(createButton("Clear value", event -> {
            field.clear();
        }));

        add(wrapper);
    }

    private void addIntegerField() {
        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setPadding(true);
        wrapper.setAlignItems(Alignment.CENTER);

        IntegerField field = new IntegerField("Integer Field");
        field.setWidth("300px");
        wrapper.add(field);

        AtomicInteger validationCounter = new AtomicInteger(0);
        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.addStatusChangeListener(event -> {
            field.setHelperText("Validation count: " + validationCounter.incrementAndGet());
        });
        binder.forField(field).bind("integer");

        wrapper.add(createCheckbox("Eager mode", event -> {
            if (field.getValueChangeMode() == ValueChangeMode.ON_CHANGE) {
                field.setValueChangeMode(ValueChangeMode.EAGER);
            } else {
                field.setValueChangeMode(ValueChangeMode.ON_CHANGE);
            }
        }));

        wrapper.add(createInput("Set step", event -> {
            int value = Integer.parseInt(event.getValue());
            field.setStep(value);
        }));

        wrapper.add(createInput("Set min", event -> {
            int value = Integer.parseInt(event.getValue());
            field.setMin(value);
        }));

        wrapper.add(createInput("Set max", event -> {
            int value = Integer.parseInt(event.getValue());
            field.setMax(value);
        }));

        wrapper.add(createButton("Clear value", event -> {
            field.clear();
        }));

        add(wrapper);
    }

    private void addBigDecimalField() {
        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setPadding(true);
        wrapper.setAlignItems(Alignment.CENTER);

        BigDecimalField field = new BigDecimalField("Big Decimal Field");
        field.setWidth("300px");
        wrapper.add(field);

        AtomicInteger validationCounter = new AtomicInteger(0);
        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.addStatusChangeListener(event -> {
            field.setHelperText("Validation count: " + validationCounter.incrementAndGet());
        });
        binder.forField(field).bind("bigDecimal");

        wrapper.add(createCheckbox("Eager mode", event -> {
            if (field.getValueChangeMode() == ValueChangeMode.ON_CHANGE) {
                field.setValueChangeMode(ValueChangeMode.EAGER);
            } else {
                field.setValueChangeMode(ValueChangeMode.ON_CHANGE);
            }
        }));

        wrapper.add(createButton("Clear value", event -> {
            field.clear();
        }));

        add(wrapper);
    }

    private void addTextField() {
        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setPadding(true);
        wrapper.setAlignItems(Alignment.CENTER);

        TextField field = new TextField("Text Field");
        field.setWidth("300px");
        wrapper.add(field);

        AtomicInteger validationCounter = new AtomicInteger(0);
        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.addStatusChangeListener(event -> {
            field.setHelperText("Validation count: " + validationCounter.incrementAndGet());
        });
        binder.forField(field).bind("string");

        wrapper.add(createCheckbox("Eager mode", event -> {
            if (field.getValueChangeMode() == ValueChangeMode.ON_CHANGE) {
                field.setValueChangeMode(ValueChangeMode.EAGER);
            } else {
                field.setValueChangeMode(ValueChangeMode.ON_CHANGE);
            }
        }));

        wrapper.add(createInput("Set minlength", event -> {
            int value = Integer.parseInt(event.getValue());
            field.setMinLength(value);
        }));

        wrapper.add(createInput("Set maxlength", event -> {
            int value = Integer.parseInt(event.getValue());
            field.setMaxLength(value);
        }));

        wrapper.add(createInput("Set pattern", event -> {
            field.setPattern(event.getValue());
        }));

        wrapper.add(createButton("Clear value", event -> {
            field.clear();
        }));

        add(wrapper);
    }

    private void addDatePicker() {
        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setPadding(true);
        wrapper.setAlignItems(Alignment.CENTER);

        DatePicker field = new DatePicker("Date Picker");
        field.setWidth("300px");
        wrapper.add(field);

        AtomicInteger validationCounter = new AtomicInteger(0);
        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.addStatusChangeListener(event -> {
            field.setHelperText("Validation count: " + validationCounter.incrementAndGet());
        });
        binder.forField(field).bind("date");

        wrapper.add(createInput("Set min", event -> {
            LocalDate value = LocalDate.parse(event.getValue());
            field.setMin(value);
        }));

        wrapper.add(createInput("Set max", event -> {
            LocalDate value = LocalDate.parse(event.getValue());
            field.setMax(value);
        }));

        wrapper.add(createButton("Clear value", event -> {
            field.clear();
        }));

        add(wrapper);
    }

    private void addTimePicker() {
        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setPadding(true);
        wrapper.setAlignItems(Alignment.CENTER);

        TimePicker field = new TimePicker("Time Picker");
        field.setWidth("300px");
        wrapper.add(field);

        AtomicInteger validationCounter = new AtomicInteger(0);
        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.addStatusChangeListener(event -> {
            field.setHelperText("Validation count: " + validationCounter.incrementAndGet());
        });
        binder.forField(field).bind("time");

        wrapper.add(createInput("Set min", event -> {
            LocalTime value = LocalTime.parse(event.getValue());
            field.setMin(value);
        }));

        wrapper.add(createInput("Set max", event -> {
            LocalTime value = LocalTime.parse(event.getValue());
            field.setMax(value);
        }));

        wrapper.add(createButton("Clear value", event -> {
            field.clear();
        }));

        add(wrapper);
    }

    private void addDateTimePicker() {
        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setPadding(true);
        wrapper.setAlignItems(Alignment.CENTER);

        DateTimePicker field = new DateTimePicker("Date Time Picker");
        field.setWidth("300px");
        wrapper.add(field);

        AtomicInteger validationCounter = new AtomicInteger(0);
        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.addStatusChangeListener(event -> {
            field.setHelperText("Validation count: " + validationCounter.incrementAndGet());
        });
        binder.forField(field).bind("dateTime");

        wrapper.add(createInput("Set min", event -> {
            LocalDateTime value = LocalDateTime.parse(event.getValue());
            field.setMin(value);
        }));

        wrapper.add(createInput("Set max", event -> {
            LocalDateTime value = LocalDateTime.parse(event.getValue());
            field.setMax(value);
        }));

        wrapper.add(createButton("Clear value", event -> {
            field.clear();
        }));

        add(wrapper);
    }

    /**
     * A helper to create a native button element.
     */
    protected NativeButton createButton(String title,
            ComponentEventListener<ClickEvent<NativeButton>> listener) {
        NativeButton button = new NativeButton(title, listener);
        return button;
    }

    /**
     * A helper to create a native button element.
     */
    protected Select<String> createSelect(String title,
            ValueChangeListener<ComponentValueChangeEvent<Select<String>, String>> listener, String... options) {
        Select<String> select = new Select<>(title, listener, options);
        return select;
    }

    protected NativeLabel createCheckbox(String title, DomEventListener listener) {
        NativeLabel label = new NativeLabel();
        Input input = new Input();
        input.setType("checkbox");
        input.getElement().addEventListener("change", listener);
        label.add(input);
        label.add(title);
        return label;
    }

    /**
     * A helper to create a native input element.
     */
    protected Input createInput(String placeholder,
            ValueChangeListener<? super ComponentValueChangeEvent<Input, String>> listener) {
        Input input = new Input();
        input.setPlaceholder(placeholder);
        input.addValueChangeListener(listener);
        return input;
    }
}
