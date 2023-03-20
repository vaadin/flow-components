
package com.vaadin.flow.component.datepicker.demo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.datepicker.demo.entity.Appointment;
import com.vaadin.flow.component.datepicker.demo.entity.Person;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link DatePicker} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-date-picker")
public class DatePickerView extends DemoView {

    @Override
    public void initView() {
        basicDemo(); // Basic usage
        disabledAndReadonly();
        clearButton();
        valueChangeEvent();
        helperText();
        autoOpenDisabled();
        configurationForRequired(); // Validation
        minAndMaXDateValidation();
        customValidator();
        startAndEndDatePickers(); // Presentation
        datePickerWithWeekNumbers();
        finnishDatePicker(); // Localizing
        customDateParser();
        themeVariantsTextAlign(); // Theme variants
        themeVariantsSmallSize();
        helperTextVariants();
        styling(); // Styling
    }

    private void basicDemo() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Basic usage
        DatePicker labelDatePicker = new DatePicker();
        labelDatePicker.setLabel("Label");

        DatePicker placeholderDatePicker = new DatePicker();
        placeholderDatePicker.setPlaceholder("Placeholder");

        DatePicker valueDatePicker = new DatePicker();
        LocalDate now = LocalDate.now();
        valueDatePicker.setValue(now);
        // end-source-example

        labelDatePicker.getStyle().set("margin-right", "5px");
        placeholderDatePicker.getStyle().set("margin-right", "5px");
        div.add(labelDatePicker, placeholderDatePicker, valueDatePicker);
        addCard("Basic usage", div);
    }

    private void disabledAndReadonly() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Disabled and read-only
        DatePicker disabledDatePicker = new DatePicker();
        disabledDatePicker.setLabel("Disabled");
        disabledDatePicker.setValue(LocalDate.now());
        disabledDatePicker.setEnabled(false);

        DatePicker readonlyDatePicker = new DatePicker();
        readonlyDatePicker.setLabel("Read-only");
        readonlyDatePicker.setValue(LocalDate.now());
        readonlyDatePicker.setReadOnly(true);
        // end-source-example

        disabledDatePicker.getStyle().set("margin-right", "5px");
        div.add(disabledDatePicker, readonlyDatePicker);
        addCard("Disabled and read-only", div);
    }

    private void clearButton() {
        // begin-source-example
        // source-example-heading: Clear button
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());

        // Display an icon which can be clicked to clear the value:
        datePicker.setClearButtonVisible(true);
        // end-source-example

        addCard("Clear button", datePicker);
    }

    private void valueChangeEvent() {
        // begin-source-example
        // source-example-heading: Value change event
        DatePicker datePicker = new DatePicker();
        datePicker.setLabel("Label");

        Div value = new Div();
        value.setText("Select a value");
        datePicker.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                value.setText("No date selected");
            } else {
                value.setText("Selected date: " + event.getValue());
            }
        });
        // end-source-example

        VerticalLayout verticalLayout = new VerticalLayout(datePicker, value);
        verticalLayout.setPadding(false);
        addCard("Value change event", verticalLayout);
    }

    private void helperText() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Helper text and Helper component
        DatePicker datePickerHelperText = new DatePicker("Delivery");
        datePickerHelperText.setHelperText("Select a delivery date");

        DatePicker datePickerHelperComponent = new DatePicker("Order");
        datePickerHelperComponent
                .setHelperComponent(new Span("Select an order date"));
        // end-source-example

        datePickerHelperText.getStyle().set("margin-right", "15px");

        div.add(datePickerHelperText, datePickerHelperComponent);
        addCard("Helper text and Helper component", div);
    }

    private void autoOpenDisabled() {
        // begin-source-example
        // source-example-heading: Auto open disabled
        DatePicker datePicker = new DatePicker();

        // Dropdown is only opened when clicking the toggle button or pressing
        // Up or Down arrow keys.
        datePicker.setAutoOpen(false);
        // end-source-example

        addCard("Auto open disabled", datePicker);
    }

    private void configurationForRequired() {
        // begin-source-example
        // source-example-heading: Required
        DatePicker datePicker = new DatePicker();
        Binder<Person> binder = new Binder<>();
        datePicker.setLabel("Birth date");
        binder.forField(datePicker).asRequired("Please choose a date")
                .bind(Person::getBirthDate, Person::setBirthDate);

        Button button = new Button("Submit", event -> binder.validate());
        // end-source-example

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setPadding(false);
        verticalLayout.add(datePicker, button);
        addCard("Validation", "Required", verticalLayout);
    }

    private void minAndMaXDateValidation() {
        // begin-source-example
        // source-example-heading: Min and max date validation
        DatePicker datePicker = new DatePicker();
        datePicker.setLabel("Label");
        datePicker.setValue(LocalDate.of(2019, 11, 11));
        datePicker.setMin(LocalDate.of(2019, 11, 10));
        datePicker.setMax(LocalDate.of(2019, 11, 16));
        // end-source-example

        addCard("Validation", "Min and max date validation", datePicker);
    }

    private void customValidator() {
        // begin-source-example
        // source-example-heading: Custom validator
        DatePicker datePicker = new DatePicker();
        Binder<Appointment> binder = new Binder<>();
        datePicker.setLabel("Select a working day");
        binder.forField(datePicker).withValidator(
                value -> !DayOfWeek.SATURDAY.equals(value.getDayOfWeek())
                        && !DayOfWeek.SUNDAY.equals(value.getDayOfWeek()),
                "The selected date must be between Monday to Friday")
                .bind(Appointment::getDate, Appointment::setDate);
        // end-source-example

        addCard("Validation", "Custom validator", datePicker);
    }

    private void startAndEndDatePickers() {
        Div message = new Div();
        message.setText("Selected range: ");

        // begin-source-example
        // source-example-heading: Date range
        DatePicker fromDatePicker = new DatePicker();
        fromDatePicker.setLabel("From date");
        DatePicker toDatePicker = new DatePicker();
        toDatePicker.setLabel("To date");

        fromDatePicker.addValueChangeListener(event -> {
            LocalDate selectedDate = event.getValue();
            LocalDate endDate = toDatePicker.getValue();
            if (selectedDate != null) {
                toDatePicker.setMin(selectedDate.plusDays(1));
                if (endDate == null) {
                    toDatePicker.setOpened(true);
                    message.setText("Select the to date");
                } else {
                    message.setText(
                            "Selected range: From " + selectedDate.toString()
                                    + " to " + endDate.toString());
                }
            } else {
                toDatePicker.setMin(null);
                message.setText("Select the from date");
            }
        });

        toDatePicker.addValueChangeListener(event -> {
            LocalDate selectedDate = event.getValue();
            LocalDate startDate = fromDatePicker.getValue();
            if (selectedDate != null) {
                fromDatePicker.setMax(selectedDate.minusDays(1));
                if (startDate != null) {
                    message.setText(
                            "Selected range: From " + startDate.toString()
                                    + " to " + selectedDate.toString());
                } else {
                    message.setText("Select the from date");
                }
            } else {
                fromDatePicker.setMax(null);
                if (startDate != null) {
                    message.setText("Select the to date");
                } else {
                    message.setText("No date is selected");
                }
            }
        });
        // end-source-example

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(fromDatePicker, toDatePicker);
        addCard("Presentation", "Date range", horizontalLayout, message);
    }

    private void datePickerWithWeekNumbers() {
        // begin-source-example
        // source-example-heading: Date picker with week numbers
        DatePicker datePicker = new DatePicker();
        datePicker.setLabel("Label");
        datePicker.setWeekNumbersVisible(true);
        datePicker.setI18n(new DatePickerI18n().setWeek("Week")
                .setCalendar("Calendar").setClear("Clear").setToday("Today")
                .setCancel("cancel").setFirstDayOfWeek(1)
                .setMonthNames(Arrays.asList("January", "February", "March",
                        "April", "May", "June", "July", "August", "September",
                        "October", "November", "December"))
                .setWeekdays(Arrays.asList("Sunday", "Monday", "Tuesday",
                        "Wednesday", "Thursday", "Friday", "Saturday"))
                .setWeekdaysShort(Arrays.asList("Sun", "Mon", "Tue", "Wed",
                        "Thu", "Fri", "Sat")));
        // end-source-example

        addCard("Presentation", "Date picker with week numbers", datePicker);
    }

    private void finnishDatePicker() {
        Div message = new Div();
        // begin-source-example
        // source-example-heading: Localizing
        DatePicker datePicker = new DatePicker();
        datePicker.setLabel("Finnish date picker");
        datePicker.setPlaceholder("Syntymäpäivä");
        datePicker.setLocale(new Locale("fi"));

        /* Note that a week starts on Sunday ("sunnuntai"). */
        datePicker.setI18n(new DatePickerI18n().setWeek("viikko")
                .setCalendar("kalenteri").setClear("tyhjennä")
                .setToday("tänään").setCancel("peruuta").setFirstDayOfWeek(1)
                .setMonthNames(Arrays.asList("tammiku", "helmikuu", "maaliskuu",
                        "huhtikuu", "toukokuu", "kesäkuu", "heinäkuu", "elokuu",
                        "syyskuu", "lokakuu", "marraskuu", "joulukuu"))
                .setWeekdays(Arrays.asList("sunnuntai", "maanantai", "tiistai",
                        "keskiviikko", "torstai", "perjantai", "lauantai"))
                .setWeekdaysShort(Arrays.asList("su", "ma", "ti", "ke", "to",
                        "pe", "la")));

        datePicker.addValueChangeListener(event -> {
            LocalDate selectedDate = event.getValue();
            if (selectedDate != null) {
                int weekday = selectedDate.getDayOfWeek().getValue() % 7;
                String weekdayName = datePicker.getI18n().getWeekdays()
                        .get(weekday);

                int month = selectedDate.getMonthValue() - 1;
                String monthName = datePicker.getI18n().getMonthNames()
                        .get(month);

                message.setText("Day of week: " + weekdayName + ", Month: "
                        + monthName);
            } else {
                message.setText("No date is selected");
            }
        });
        // end-source-example

        datePicker.setId("finnish-picker");
        addCard("Localization", "Localizing", datePicker, message);
    }

    private void customDateParser() {
        // begin-source-example
        // source-example-heading: Simple date picker
        DatePicker datePicker = new DatePicker();
        datePicker.setLabel("Label");
        datePicker.setI18n(new DatePickerI18n().setWeek("Week")
                .setCalendar("Calendar").setClear("clear").setToday("today")
                .setCancel("cancel").setFirstDayOfWeek(1)
                .setMonthNames(Arrays.asList("January", "February", "March",
                        "April", "May", "June", "July", "August", "September",
                        "October", "November", "December"))
                .setWeekdays(Arrays.asList("Sunday", "Monday", "Tuesday",
                        "Wednesday", "Thursday", "Friday", "Saturday"))
                .setWeekdaysShort(Arrays.asList("Sun", "Mon", "Tue", "Wed",
                        "Thu", "Fri", "Sat")));
        // end-source-example

    }

    private void themeVariantsTextAlign() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Text align
        DatePicker leftDatePicker = new DatePicker();
        leftDatePicker.setValue(LocalDate.now());
        leftDatePicker.getElement().setAttribute("theme", "align-left");

        DatePicker centerDatePicker = new DatePicker();
        centerDatePicker.setValue(LocalDate.now());
        centerDatePicker.getElement().setAttribute("theme", "align-center");

        DatePicker rightDatePicker = new DatePicker();
        rightDatePicker.setValue(LocalDate.now());
        rightDatePicker.getElement().setAttribute("theme", "align-right");

        // end-source-example

        div.add(leftDatePicker, centerDatePicker, rightDatePicker);
        leftDatePicker.getStyle().set("margin-right", "5px");
        centerDatePicker.getStyle().set("margin-right", "5px");
        addCard("Theme Variants", "Text align", div);
    }

    private void themeVariantsSmallSize() {
        // begin-source-example
        // source-example-heading: Text align
        DatePicker datePicker = new DatePicker();
        datePicker.setLabel("Label");
        datePicker.getElement().setAttribute("theme", "small");
        // end-source-example

        addCard("Theme Variants", "Small text field", datePicker);
    }

    private void helperTextVariants() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Helper text and component above the field
        DatePicker datePickerHelperTextAbove = new DatePicker("Label");
        datePickerHelperTextAbove.setHelperText("Helper text above the field");
        datePickerHelperTextAbove.getElement().getThemeList()
                .set("helper-above-field", true);

        DatePicker datePickerHelperComponentAbove = new DatePicker("Label");
        datePickerHelperComponentAbove.setHelperComponent(
                new Span("Helper component above the field"));
        datePickerHelperComponentAbove.getElement().getThemeList()
                .set("helper-above-field", true);
        add(datePickerHelperTextAbove, datePickerHelperComponentAbove);
        // end-source-example

        datePickerHelperTextAbove.getStyle().set("margin-right", "15px");
        div.add(datePickerHelperTextAbove, datePickerHelperComponentAbove);
        addCard("Theme Variants", "Helper text and component above the field",
                div);
    }

    private void styling() {
        Div firstDiv = new Div();
        firstDiv.setText(
                "To read about styling you can read the related tutorial in");
        Anchor firstAnchor = new Anchor(
                "https://vaadin.com/docs/flow/theme/using-component-themes.html",
                "Using Component Themes");

        Div secondDiv = new Div();
        secondDiv.setText("To know about styling in html you can read the ");
        Anchor secondAnchor = new Anchor("https://vaadin.com/components/"
                + "vaadin-date-picker/html-examples/date-picker-styling-demos",
                "HTML Styling Demos");

        HorizontalLayout firstHorizontalLayout = new HorizontalLayout(firstDiv,
                firstAnchor);
        HorizontalLayout secondHorizontalLayout = new HorizontalLayout(
                secondDiv, secondAnchor);
        // begin-source-example
        // source-example-heading: Styling references

        // end-source-example

        addCard("Styling", "Styling references", firstHorizontalLayout,
                secondHorizontalLayout);
    }
}
