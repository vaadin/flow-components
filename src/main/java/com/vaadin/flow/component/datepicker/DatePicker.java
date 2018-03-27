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
package com.vaadin.flow.component.datepicker;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * Server-side component that encapsulates the functionality of the
 * {@code vaadin-date-picker} webcomponent.
 * <p>
 * It allows setting and getting {@link LocalDate} objects, setting minimum and
 * maximum date ranges and has internationalization support by using the
 * {@link DatePickerI18n} object.
 *
 */
@JavaScript("frontend://datepickerConnector.js")
public class DatePicker extends GeneratedVaadinDatePicker<DatePicker>
        implements HasValue<DatePicker, LocalDate>, HasSize, HasValidation {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final String I18N_PROPERTY = "i18n";

    /**
     * Default constructor.
     */
    public DatePicker() {
        this((LocalDate) null);
    }

    /**
     * Convenience constructor to create a date picker with a pre-selected date.
     *
     * @param initialDate
     *            the pre-selected date in the picker
     * @see #setValue(LocalDate)
     */
    public DatePicker(LocalDate initialDate) {
        getElement().synchronizeProperty("value", "value-changed");
        getElement().synchronizeProperty("invalid", "invalid-changed");
        doSetValue(initialDate);
        addAttachListener(event -> initConnector());
        setLocale(UI.getCurrent().getLocale());
    }

    /**
     * Convenience constructor to create a date picker with a label.
     *
     * @param label
     *            the label describing the date picker
     * @see #setLabel(String)
     */
    public DatePicker(String label) {
        this();
        setLabel(label);
    }

    /**
     * Convenience constructor to create a date picker with a pre-selected date
     * and a label.
     *
     * @param label
     *            the label describing the date picker
     * @param initialDate
     *            the pre-selected date in the picker
     * @see #setValue(LocalDate)
     * @see #setLabel(String)
     */
    public DatePicker(String label, LocalDate initialDate) {
        this(initialDate);
        setLabel(label);
    }

    /**
     * Convenience constructor to create a date picker with a
     * {@link ValueChangeListener}.
     *
     * @param listener
     *            the listener to receive value change events
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DatePicker(ValueChangeListener<DatePicker, LocalDate> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date picker with a
     * {@link ValueChangeListener} and a label.
     *
     *
     * @param label
     *            the label describing the date picker
     * @param listener
     *            the listener to receive value change events
     * @see #setLabel(String)
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DatePicker(String label,
            ValueChangeListener<DatePicker, LocalDate> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date picker with a pre-selected date
     * and a {@link ValueChangeListener}.
     *
     * @param initialDate
     *            the pre-selected date in the picker
     * @param listener
     *            the listener to receive value change events
     * @see #setValue(LocalDate)
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DatePicker(LocalDate initialDate,
            ValueChangeListener<DatePicker, LocalDate> listener) {
        this(initialDate);
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date picker with a pre-selected date,
     * a {@link ValueChangeListener} and a label.
     *
     * @param label
     *            the label describing the date picker
     * @param initialDate
     *            the pre-selected date in the picker
     * @param listener
     *            the listener to receive value change events
     * @see #setLabel(String)
     * @see #setValue(LocalDate)
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DatePicker(String label, LocalDate initialDate,
            ValueChangeListener<DatePicker, LocalDate> listener) {
        this(initialDate);
        setValue(initialDate);
        addValueChangeListener(listener);
    }

    /**
     * Convenience Constructor to create a date picker with pre-selected date
     * and locale setup.
     *
     * @param initialDate
     *            the pre-selected date in the picker
     * @param locale
     *            the locale for the date picker
     */
    public DatePicker(LocalDate initialDate, Locale locale) {
        this(initialDate);
        setLocale(locale);
    }

    /**
     * Sets the minimum date in the date picker. Dates before that will be
     * disabled in the popup.
     *
     * @param min
     *            the minimum date that is allowed to be selected, or
     *            <code>null</code> to remove any minimum constraints
     */
    public void setMin(LocalDate min) {
        if (min == null) {
            setMinAsString("");
        } else {
            setMinAsString(FORMATTER.format(min));
        }
    }

    /**
     * Gets the minimum date in the date picker. Dates before that will be
     * disabled in the popup.
     *
     * @return the minimum date that is allowed to be selected, or
     *         <code>null</code> if there's no minimum
     */
    public LocalDate getMin() {
        return convertDateFromString(getMinAsStringString());
    }

    /**
     * Sets the maximum date in the date picker. Dates after that will be
     * disabled in the popup.
     *
     * @param max
     *            the maximum date that is allowed to be selected, or
     *            <code>null</code> to remove any maximum constraints
     */
    public void setMax(LocalDate max) {
        if (max == null) {
            setMaxAsString("");
        } else {
            setMaxAsString(FORMATTER.format(max));
        }
    }

    /**
     * Gets the maximum date in the date picker. Dates after that will be
     * disabled in the popup.
     *
     * @return the maximum date that is allowed to be selected, or
     *         <code>null</code> if there's no maximum
     */
    public LocalDate getMax() {
        return convertDateFromString(getMaxAsStringString());
    }

    /**
     * Set the Locale for the Date Picker. The displayed date will be matched to
     * the format used in that locale.
     * <p>
     * NOTE:Supported formats are MM/DD/YYYY(default), DD/MM/YYYY and
     * YYYY/MM/DD. Browser compatibility can be different based on the browser
     * and mobile devices, you can check here for more details: <a href=
     * "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleDateString">https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleDateString</a>
     * 
     * @param locale
     *            the locale set to the date picker
     */
    public void setLocale(Locale locale) {
        String languageTag = locale.getLanguage() + "-" + locale.getCountry();
        getElement().callFunction("$connector.setLocale", languageTag);
    }

    private void initConnector() {
        getUI().orElseThrow(() -> new IllegalStateException(
                "Connector can only be initialized for an attached DatePicker"))
                        .getPage()
                        .executeJavaScript("window.datepickerConnector.initLazy($0)",
                                getElement());
    }
    /**
     * Gets the internationalization properties previously set for this
     * component.
     *
     * @return the object with the i18n properties, never <code>null</code>. If
     *         the i18n properties weren't set, the internal properties of the
     *         object will be <code>null</code>, but not the object itself.
     */
    public DatePickerI18n getI18n() {
        DatePickerI18n i18n = new DatePickerI18n();
        Element element = getElement();
        i18n.setCalendar(element.getProperty(I18N_PROPERTY + ".calendar"));
        i18n.setCancel(element.getProperty(I18N_PROPERTY + ".cancel"));
        i18n.setClear(element.getProperty(I18N_PROPERTY + ".clear"));
        i18n.setFirstDayOfWeek(
                element.getProperty(I18N_PROPERTY + ".firstDayOfWeek", 0));
        i18n.setMonthNames(
                JsonSerializer.toObjects(String.class, (JsonArray) element
                        .getPropertyRaw(I18N_PROPERTY + ".monthNames")));
        i18n.setToday(element.getProperty(I18N_PROPERTY + ".today"));
        i18n.setWeek(element.getProperty(I18N_PROPERTY + ".week"));
        i18n.setWeekdays(
                JsonSerializer.toObjects(String.class, (JsonArray) element
                        .getPropertyRaw(I18N_PROPERTY + ".weekdays")));
        i18n.setWeekdaysShort(
                JsonSerializer.toObjects(String.class, (JsonArray) element
                        .getPropertyRaw(I18N_PROPERTY + ".weekdaysShort")));

        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *            the internationalized properties, not <code>null</code>
     */
    public void setI18n(DatePickerI18n i18n) {
        Objects.requireNonNull(i18n,
                "The I18N properties object should not be null");
        JsonObject json = (JsonObject) JsonSerializer.toJson(i18n);
        Element element = getElement();
        for (String key : json.keys()) {
            element.setPropertyJson(I18N_PROPERTY + "." + key, json.get(key));
        }
    }

    @Override
    public void setValue(LocalDate value) {
        if (!Objects.equals(value, getValue())) {
            doSetValue(value);
        }
    }

    @Override
    public LocalDate getValue() {
        return convertDateFromString(super.getValueAsStringString());
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<DatePicker, LocalDate> listener) {
        return getElement().addPropertyChangeListener(
                getClientValuePropertyName(),
                event -> listener
                        .onComponentEvent(new ValueChangeEvent<>(this, this,
                                convertDateFromString(
                                        (String) event.getOldValue()),
                                event.isUserOriginated())));
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            super.setErrorMessage("");
        } else {
            super.setErrorMessage(errorMessage);
        }
    }

    /**
     * Gets the current error message from the datepicker.
     *
     * @return the current error message
     */
    @Override
    public String getErrorMessage() {
        return getErrorMessageString();
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
    }

    /**
     * Gets the validity of the datepicker output.
     * <p>
     * return true, if the value is invalid.
     *
     * @return the {@code validity} property from the datepicker
     */
    @Override
    public boolean isInvalid() {
        return isInvalidBoolean();
    }

    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }

    /**
     * Gets the label of the datepicker.
     *
     * @return the {@code label} property of the datePicker
     */
    public String getLabel() {
        return getLabelString();
    }

    /**
     * Enables or disables this datepicker.
     *
     * @param enabled
     *            the boolean value to set
     */
    public void setEnabled(boolean enabled) {
        setDisabled(!enabled);
    }

    /**
     * Determine whether this datepicker is enabled
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return {@code true} if the datepicker is enabled, {@code false}
     *         otherwise
     */
    public boolean isEnabled() {
        return !isDisabledBoolean();
    }

    @Override
    public void setPlaceholder(String placeholder) {
        super.setPlaceholder(placeholder);
    }

    /**
     * Gets the placeholder of the datepicker.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code placeholder} property of the datePicker
     */
    public String getPlaceholder() {
        return getPlaceholderString();
    }

    /**
     * Date which should be visible when there is no value selected.
     * <p>
     * The same date formats as for the {@code value} property are supported.
     * </p>
     *
     * @param initialPosition
     *            the LocalDate value to set
     */
    public void setInitialPosition(LocalDate initialPosition) {
        if (initialPosition == null) {
            setInitialPosition("");
        } else {
            setInitialPosition(FORMATTER.format(initialPosition));
        }
    }

    /**
     * Get the visible date when there is no value selected.
     * <p>
     * The same date formats as for the {@code value} property are supported.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code initialPosition} property from the datepicker
     */
    public LocalDate getInitialPosition() {
        return convertDateFromString(getInitialPositionString());
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);
    }

    /**
     * Determines whether the datepicker is marked as input required.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return {@code true} if the input is required, {@code false} otherwise
     */
    public boolean isRequired() {
        return isRequiredBoolean();
    }

    /**
     * Set the week number visible in the DatePicker.
     * <p>
     * Set true to display ISO-8601 week numbers in the calendar.
     * <p>
     * Notice that displaying week numbers is only supported when
     * i18n.firstDayOfWeek is 1 (Monday).
     *
     * @param showWeekNumbers
     *            the boolean value to set
     */
    public void setWeekNumbersVisible(boolean weekNumbersVisible) {
        super.setShowWeekNumbers(weekNumbersVisible);
    }

    /**
     * Get the state of {@code showWeekNumbers} property of the datepicker
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code showWeekNumbers} property from the datepicker
     */
    public boolean isWeekNumbersVisible() {
        return isShowWeekNumbersBoolean();
    }

    /**
     * Sets the opened property of the datepicker to open or close its overlay.
     * 
     * @param opened
     *            {@code true} to open the datepicker overlay, {@code false} to
     *            close it
     */
    @Override
    public void setOpened(boolean opened) {
        // The open() and close() functions are used because of
        // https://github.com/vaadin/vaadin-date-picker/issues/524
        if (opened) {
            open();
        } else {
            close();
        }
    }

    /**
     * Opens the datepicker overlay.
     */
    @Override
    public void open() {
        super.open();

        // To update the server-side property synchronously
        super.setOpened(true);
    }

    /**
     * Closes the datepicker overlay.
     */
    @Override
    protected void close() {
        super.close();

        // To update the server-side property synchronously
        super.setOpened(false);
    }

    /**
     * Gets the states of the drop-down for the datepicker
     *
     * @return {@code true} if the drop-down is opened, {@code false} otherwise
     */
    public boolean isOpened() {
        return isOpenedBoolean();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    /**
     * Gets the name of the DatePicker.
     *
     * @return the {@code name} property from the DatePicker
     */
    public String getName() {
        return getNameString();
    }

    @Override
    public void setReadonly(boolean readonly) {
        super.setReadonly(readonly);
    }

    /**
     * Gets the {@code readonly} property from the DatePicker.
     *
     * @return the {@code readonly} property from the DatePicker
     */
    public boolean isReadonly() {
        return isReadonlyBoolean();
    }

    @Override
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent<DatePicker>> listener) {
        return super.addOpenedChangeListener(listener);
    }

    @Override
    public Registration addInvalidChangeListener(
            ComponentEventListener<InvalidChangeEvent<DatePicker>> listener) {
        return super.addInvalidChangeListener(listener);
    }

    private LocalDate convertDateFromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return LocalDate.parse(value, FORMATTER);
    }

    private void doSetValue(LocalDate value) {
        if (value == null) {
            setValueAsString("");
        } else {
            setValueAsString(FORMATTER.format(value));
        }
    }

    /**
     * The internationalization properties for {@link DatePicker}.
     */
    public static class DatePickerI18n implements Serializable {
        private List<String> monthNames;
        private List<String> weekdays;
        private List<String> weekdaysShort;
        private int firstDayOfWeek;
        private String week;
        private String calendar;
        private String clear;
        private String today;
        private String cancel;

        /**
         * Gets the name of the months.
         *
         * @return the month names
         */
        public List<String> getMonthNames() {
            return monthNames;
        }

        /**
         * Sets the name of the months, starting from January and ending on
         * December.
         *
         * @param monthNames
         *            the month names
         * @return this instance for method chaining
         */
        public DatePickerI18n setMonthNames(List<String> monthNames) {
            this.monthNames = monthNames;
            return this;
        }

        /**
         * Gets the name of the week days.
         *
         * @return the week days
         */
        public List<String> getWeekdays() {
            return weekdays;
        }

        /**
         * Sets the name of the week days, starting from {@code Sunday} and
         * ending on {@code Saturday}.
         *
         * @param weekdays
         *            the week days names
         * @return this instance for method chaining
         */
        public DatePickerI18n setWeekdays(List<String> weekdays) {
            this.weekdays = weekdays;
            return this;
        }

        /**
         * Gets the short names of the week days.
         *
         * @return the short names of the week days
         */
        public List<String> getWeekdaysShort() {
            return weekdaysShort;
        }

        /**
         * Sets the short names of the week days, starting from {@code sun} and
         * ending on {@code sat}.
         *
         * @param weekdaysShort
         *            the short names of the week days
         * @return this instance for method chaining
         */
        public DatePickerI18n setWeekdaysShort(List<String> weekdaysShort) {
            this.weekdaysShort = weekdaysShort;
            return this;
        }

        /**
         * Gets the first day of the week.
         * <p>
         * 0 for Sunday, 1 for Monday, 2 for Tuesday, 3 for Wednesday, 4 for
         * Thursday, 5 for Friday, 6 for Saturday.
         *
         * @return the index of the first day of the week
         */
        public int getFirstDayOfWeek() {
            return firstDayOfWeek;
        }

        /**
         * Sets the first day of the week.
         * <p>
         * 0 for Sunday, 1 for Monday, 2 for Tuesday, 3 for Wednesday, 4 for
         * Thursday, 5 for Friday, 6 for Saturday.
         *
         * @param firstDayOfWeek
         *            the index of the first day of the week
         * @return this instance for method chaining
         */
        public DatePickerI18n setFirstDayOfWeek(int firstDayOfWeek) {
            this.firstDayOfWeek = firstDayOfWeek;
            return this;
        }

        /**
         * Gets the translated word for {@code week}.
         *
         * @return the translated word for week
         */
        public String getWeek() {
            return week;
        }

        /**
         * Sets the translated word for {@code week}.
         *
         * @param week
         *            the translated word for week
         * @return this instance for method chaining
         */
        public DatePickerI18n setWeek(String week) {
            this.week = week;
            return this;
        }

        /**
         * Gets the translated word for {@code calendar}.
         *
         * @return the translated word for calendar
         */
        public String getCalendar() {
            return calendar;
        }

        /**
         * Sets the translated word for {@code calendar}.
         *
         * @param calendar
         *            the translated word for calendar
         * @return this instance for method chaining
         */
        public DatePickerI18n setCalendar(String calendar) {
            this.calendar = calendar;
            return this;
        }

        /**
         * Gets the translated word for {@code clear}.
         *
         * @return the translated word for clear
         */
        public String getClear() {
            return clear;
        }

        /**
         * Sets the translated word for {@code clear}.
         *
         * @param clear
         *            the translated word for clear
         * @return this instance for method chaining
         */
        public DatePickerI18n setClear(String clear) {
            this.clear = clear;
            return this;
        }

        /**
         * Gets the translated word for {@code today}.
         *
         * @return the translated word for today
         */
        public String getToday() {
            return today;
        }

        /**
         * Sets the translated word for {@code today}.
         *
         * @param today
         *            the translated word for today
         * @return this instance for method chaining
         */
        public DatePickerI18n setToday(String today) {
            this.today = today;
            return this;
        }

        /**
         * Gets the translated word for {@code cancel}.
         *
         * @return the translated word for cancel
         */
        public String getCancel() {
            return cancel;
        }

        /**
         * Sets the translated word for {@code cancel}.
         *
         * @param cancel
         *            the translated word for cancel
         * @return this instance for method chaining
         */
        public DatePickerI18n setCancel(String cancel) {
            this.cancel = cancel;
            return this;
        }
    }
}
