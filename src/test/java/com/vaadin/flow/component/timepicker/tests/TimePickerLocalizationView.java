package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.timepicker.demo.TimePickerView;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

@Route("time-picker-localization")
public class TimePickerLocalizationView extends Div
        implements HasUrlParameter<String> {

    private final TimePicker timePicker;
    private final TimePickerView.LocalTimeTextBlock browserFormattedTime;

    public TimePickerLocalizationView() {
        Stream<Locale> supportedAvailableLocales = TimePicker
                .getSupportedAvailableLocales();
        ComboBox<Locale> localesCB = new ComboBox<>();
        localesCB.setItemLabelGenerator(
                TimePickerLocalizationView::getLocaleString);
        localesCB.setWidth("300px");
        localesCB.setItems(supportedAvailableLocales);
        localesCB.setId("locale-picker");

        ComboBox<Double> stepSelector = new ComboBox<>();
        stepSelector.setItems(0.5, 10.0, 60.0, 900.0, 1800.0, 3600.0);
        stepSelector.setId("step-picker");
        stepSelector.setValue(3600.0); // default is null but it is really an
                                       // hour

        timePicker = new TimePicker();

        browserFormattedTime = new TimePickerView.LocalTimeTextBlock();
        browserFormattedTime.setId("formatted-time");

        localesCB.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                return;
            }
            timePicker.setLocale(event.getValue());
            browserFormattedTime.setLocale(event.getValue());
        });

        stepSelector.addValueChangeListener(event -> {
            if (event.getValue() != null)
                timePicker.setStep(event.getValue().doubleValue());
        });

        timePicker.addValueChangeListener(
                event -> browserFormattedTime.setLocalTime(event.getValue()));

        add(localesCB, stepSelector, timePicker, browserFormattedTime);
        localesCB.setValue(UI.getCurrent().getLocale());
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent,
            @OptionalParameter String initialValue) {
        if (initialValue != null) {
            // eg. fi-FI-10-30
            String[] split = initialValue.split("\\W");
            Locale locale = new Locale(split[0], split[1]);

            timePicker.setLocale(locale);
            browserFormattedTime.setLocale(locale);
            timePicker.setValue(LocalTime.of(Integer.parseInt(split[2]),
                    Integer.parseInt(split[3])));
        }
    }

    static String getLocaleString(Locale locale) {
        return locale.getLanguage() + "-" + locale.getCountry();
    }
}
