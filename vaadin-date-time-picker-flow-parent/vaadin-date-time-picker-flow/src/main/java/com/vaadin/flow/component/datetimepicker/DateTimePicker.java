/*
 * Copyright 2000-2019 Vaadin Ltd.
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
package com.vaadin.flow.component.datetimepicker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Objects;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

@Tag("vaadin-date-time-picker-date-picker")
class DateTimePickerDatePicker
        extends com.vaadin.flow.component.datepicker.DatePicker {
}

@Tag("vaadin-date-time-picker-time-picker")
class DateTimePickerTimePicker
        extends com.vaadin.flow.component.timepicker.TimePicker {
}

/**
 * Server-side component that encapsulates the functionality of the
 * {@code vaadin-date-time-picker} web component.
 *
 */
@Tag("vaadin-date-time-picker")
@HtmlImport("frontend://bower_components/vaadin-date-time-picker/src/vaadin-date-time-picker.html")
@NpmPackage(value = "@vaadin/vaadin-date-time-picker", version = "1.0.0-alpha5")
@JsModule("@vaadin/vaadin-date-time-picker/src/vaadin-date-time-picker.js")
public class DateTimePicker extends AbstractField<DateTimePicker, LocalDateTime>
        implements HasStyle, HasSize, HasTheme, Focusable<DateTimePicker> {

    private final DateTimePickerDatePicker datePicker = new DateTimePickerDatePicker();
    private final DateTimePickerTimePicker timePicker = new DateTimePickerTimePicker();
    private Locale locale;

    /**
     * Default constructor.
     */
    public DateTimePicker() {
        this((LocalDateTime) null);
    }

    /**
     * Convenience constructor to create a date time picker with a pre-selected
     * date time in current UI locale format.
     *
     * @param initialDateTime
     *            the pre-selected date time in the picker
     */
    public DateTimePicker(LocalDateTime initialDateTime) {
        super(null);
        if (initialDateTime != null) {
            setPresentationValue(initialDateTime);
            updateValue();
        }

        getElement().addEventListener("value-changed", e -> this.updateValue());

        addToSlot(datePicker, "date-picker");
        addToSlot(timePicker, "time-picker");

        setLocale(UI.getCurrent().getLocale());
    }

    /**
     * Convenience constructor to create a date time picker with a
     * {@link ValueChangeListener}.
     *
     * @param listener
     *            the listener to receive value change events
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DateTimePicker(
            ValueChangeListener<ComponentValueChangeEvent<DateTimePicker, LocalDateTime>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date time picker with a pre-selected
     * date time in current UI locale format and a {@link ValueChangeListener}.
     *
     * @param initialDateTime
     *            the pre-selected date time in the picker
     * @param listener
     *            the listener to receive value change events
     * @see #setValue(LocalDateTime)
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DateTimePicker(LocalDateTime initialDateTime,
            ValueChangeListener<ComponentValueChangeEvent<DateTimePicker, LocalDateTime>> listener) {
        this(initialDateTime);
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date time picker with pre-selected
     * date time and locale setup.
     *
     * @param initialDateTime
     *            the pre-selected date time in the picker
     * @param locale
     *            the locale for the date time picker
     */
    public DateTimePicker(LocalDateTime initialDateTime, Locale locale) {
        this(initialDateTime);
        setLocale(locale);
    }

    @Override
    protected void setPresentationValue(LocalDateTime newPresentationValue) {
        datePicker.setValue(newPresentationValue != null
                ? newPresentationValue.toLocalDate()
                : null);
        timePicker.setValue(newPresentationValue != null
                ? newPresentationValue.toLocalTime()
                : null);
        // Make custom field detect and propagate the value change by triggering
        // "change" event on one of the fields.
        datePicker.getElement().executeJs(
                "this.dispatchEvent(new CustomEvent('change', { bubbles: true }));");
    }

    private void updateValue() {
        final LocalDate date = datePicker.getValue();
        final LocalTime time = timePicker.getValue();
        final LocalDateTime newValue = date != null && time != null
                ? LocalDateTime.of(date, time)
                : null;

        setModelValue(newValue, true);
    }

    /**
     * Adds the given Component to the specified slot of this component.
     */
    private void addToSlot(Component component, String slot) {
        Objects.requireNonNull(component, "Component to add cannot be null");
        component.getElement().setAttribute("slot", slot);
        getElement().appendChild(component.getElement());
    }

    /**
     * Set the Locale for the DateTimePicker. The displayed date and time will
     * be matched to the format used in that locale.
     *
     * @param locale
     *            the locale to set to the DateTimePicker, cannot be null
     */
    public void setLocale(Locale locale) {
        Objects.requireNonNull(locale, "Locale must not be null.");
        this.locale = locale;
        datePicker.setLocale(locale);
        timePicker.setLocale(locale);
    }

    /**
     * Gets the Locale for this DateTimePicker
     *
     * @return the locale used for this DateTimePicker
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Synchronizes the theme attribute to the internal DatePicker and
     * TimePicker components.
     */
    private void synchronizeTheme() {
        String theme = getThemeName();
        theme = theme != null ? theme : "";
        datePicker.getElement().setAttribute("theme", theme);
        timePicker.getElement().setAttribute("theme", theme);
    }

    /**
     * Adds a theme name to this component.
     *
     * @param themeName
     *            the theme name to add, not <code>null</code>
     */
    @Override
    public void addThemeName(String themeName) {
        HasTheme.super.addThemeName(themeName);
        synchronizeTheme();
    }

    /**
     * Removes a theme name from this component.
     *
     * @param themeName
     *            the theme name to remove, not <code>null</code>
     * @return <code>true</code> if the theme name was removed,
     *         <code>false</code> if the theme list didn't contain the theme
     *         name
     */
    @Override
    public boolean removeThemeName(String themeName) {
        boolean result = HasTheme.super.removeThemeName(themeName);
        synchronizeTheme();
        return result;
    }

    /**
     * Sets the theme names of this component. This method overwrites any
     * previous set theme names.
     *
     * @param themeName
     *            a space-separated string of theme names to set, or empty
     *            string to remove all theme names
     */
    @Override
    public void setThemeName(String themeName) {
        HasTheme.super.setThemeName(themeName);
        synchronizeTheme();
    }

    /**
     * Sets or removes the given theme name for this component.
     *
     * @param themeName
     *            the theme name to set or remove, not <code>null</code>
     * @param set
     *            <code>true</code> to set the theme name, <code>false</code> to
     *            remove it
     */
    @Override
    public void setThemeName(String themeName, boolean set) {
        HasTheme.super.setThemeName(themeName, set);
        synchronizeTheme();
    }

    /**
     * Adds one or more theme names to this component. Multiple theme names can
     * be specified by using multiple parameters.
     *
     * @param themeNames
     *            the theme name or theme names to be added to the component
     */
    @Override
    public void addThemeNames(String... themeNames) {
        HasTheme.super.addThemeNames(themeNames);
        synchronizeTheme();
    }

    /**
     * Removes one or more theme names from component. Multiple theme names can
     * be specified by using multiple parameters.
     *
     * @param themeNames
     *            the theme name or theme names to be removed from the component
     */
    @Override
    public void removeThemeNames(String... themeNames) {
        HasTheme.super.removeThemeNames(themeNames);
        synchronizeTheme();
    }
}
