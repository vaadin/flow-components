package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@TestPath("time-picker-localization")
public class TimePickerLocalizationIT extends AbstractComponentIT {

    public static final int MINIMUM_NUMBER_OF_LOCALES_TO_TEST = 159;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-time-picker"));
    }

    @Override
    protected int getDeploymentPort() {
        return super.getDeploymentPort();
    }

    @Test
    public void testAllAvailableLocalesWhenValueChangedFromDropDown_stepOneHourAndFormatHourMinute_pickerValuesMatchesBrowserFormatted() {
        // select locale based on locale string
        // test via drop down 00:00 (0) 6:00 (7) 12:00 (13) 18:00 (19) 23:00(24)
        // time picker stores values as a string that is not the same as the
        // shown value in the input
        // this test validates both, value and visible string format in input
        // (the drop down values are not validated, those are anyway the same as
        // what ends up in the input element)

        // the values to test - the component stores value using 24-clock
        String[] values = new String[] { "00:00", "06:00", "12:00", "18:00",
                "23:00" };
        // index of the value elements in the drop down
        List<Integer> valueIndices = Arrays.asList(0, 6, 12, 18, 23);

        runLocalisationTestPattern(values, valueIndices);
    }

    @Test
    public void testAllAvailableLocalesWhenValueChangedFromDropDown_step30Minutes_pickerValuesMatchesBrowserFormatted() {
        // same stuff as the previous test, but instead use a different step

        selectStep("1800.0"); // could test with eg. 15 minutes, but after
        // scrolling down, the iron-list ditches the first items and the indexes
        // are f'ckd, thus after
        // certain point it is too fragile to test based on indexes, index 0
        // might not be the top most item

        // cannot test further than 13:00, otherwise the item at index 0 will be
        // 00:30 instead of 00:00 ...
        runLocalisationTestPattern(
                new String[] { "00:00", "00:30", "01:00", "06:00", "06:30" },
                Arrays.asList(0, 1, 2, 12, 13));
    }

    @Test
    public void testInitialValue_nonDefaultLocale_initialValueLocalizedCorrectly() {
        runInitialLoadValueTestPattern("en-US", "15:00");
        runInitialLoadValueTestPattern("en-CA", "03:00");
        runInitialLoadValueTestPattern("fi-FI", "15:00");
        runInitialLoadValueTestPattern("no-NO", "00:00");
        runInitialLoadValueTestPattern("ar-SY", "15:00");
        runInitialLoadValueTestPattern("ar-JO", "11:00");
        runInitialLoadValueTestPattern("zh-TW", "15:00");
        runInitialLoadValueTestPattern("ko-KR", "23:00");
        runInitialLoadValueTestPattern("es-PA", "15:00");
    }

    private void runInitialLoadValueTestPattern(String locale, String time) {
        String url = getTestURL() + "/" + locale + "-" + time;
        this.getDriver().get(url);
        waitForElementPresent(By.tagName("vaadin-time-picker"));

        String error = verifyValueProperty(time);
        Assert.assertNull(
                "Wrong value on page load for locale " + locale + ": " + error,
                error);
        error = verifyFormat();
        Assert.assertNull(
                "Wrong format on page load for locale " + locale + ": " + error,
                error);

    }

    // TODO could add another test that verifies formats the time values to
    // labels and compares the values in the drop down to those

    private void runLocalisationTestPattern(String[] values,
            List<Integer> valueIndices) {
        Locale locale = null;

        int numberOfErrors = 0;
        Logger logger = Logger.getLogger(getClass().getName());
        for (Iterator<Locale> localeIterator = TimePicker
                .getSupportedAvailableLocales()
                .iterator(); ((Iterator) localeIterator).hasNext();) {
            List<String> errors = new ArrayList<>();
            Locale oldLocale = locale;
            locale = localeIterator.next();
            selectLocale(locale);
            String error;

            // verify that any previously selected value was updated correctly
            // in the input element
            // (the value property object stays the same even when locale
            // changed)
            if (oldLocale != null) {
                error = verifyFormat();
                if (error != null) {
                    errors.add(prettyPrint(locale)
                            + " after locale change, input format error: "
                            + error);
                    error = null;
                }
            }
            for (Integer value : valueIndices) {
                selectItem(value);
                error = verifyValueProperty(
                        values[valueIndices.indexOf(value)]);
                if (error != null) {
                    errors.add(prettyPrint(locale) + " value property error: "
                            + error);
                    error = null;
                }
                error = verifyFormat();
                if (error != null) {
                    errors.add(prettyPrint(locale) + " input format error: "
                            + error);
                    error = null;
                }
            }
            if (!errors.isEmpty()) {
                // log errors early so test run can be interrupted early
                logger.severe(
                        errors.stream().collect(Collectors.joining("\n")));
                numberOfErrors++;
            } else {
                logger.info(
                        "Locale " + locale.getDisplayName() + " validated.");
            }
        }
        long numberOfTestedLocales = TimePicker.getSupportedAvailableLocales()
                .count();
        logger.info("Tested time picker localization with "
                + numberOfTestedLocales + " locales");
        if (numberOfErrors > 0) {
            org.junit.Assert.fail("Localization Errors: " + numberOfErrors);
        }
        Assert.assertTrue("Not enough locales tested",
                MINIMUM_NUMBER_OF_LOCALES_TO_TEST <= numberOfTestedLocales);
    }

    private String verifyValueProperty(String value) {
        String timePickerValue = getTimePickerValue();
        if (value.equals(timePickerValue)) {
            return null;
        } else {
            return "expected: " + value + " actual: " + timePickerValue;
        }
    }

    private String verifyFormat() {
        String timePickerInputValue = getTimePickerInputValue();
        String formattedTextValue = getLabelValue();
        if (formattedTextValue.equals(timePickerInputValue)) {
            return null;
        } else {
            return "expected: " + formattedTextValue + " actual: "
                    + timePickerInputValue;
        }
    }

    private void selectItem(Integer index) {
        selectComboBoxItemByIndex(getTimePickerComboBox(), index);
    }

    private void selectLocale(Locale locale) {
        TestBenchElement comboBox = $("vaadin-combo-box").id("locale-picker");
        executeScript("arguments[0]['$'].clearButton.click()", comboBox);
        comboBox.sendKeys(TimePickerLocalizationView.getLocaleString(locale)
                + Keys.RETURN);
    }

    private void selectStep(String step) {
        TestBenchElement comboBox = $("vaadin-combo-box").id("step-picker");
        executeScript("arguments[0]['$'].clearButton.click()", comboBox);
        comboBox.sendKeys(step + Keys.RETURN);
    }

    private void selectComboBoxItemByIndex(TestBenchElement comboBox,
            int index) {
        executeScript("arguments[0].open()", comboBox);

        scrollToItem(comboBox, index);

        TestBenchElement item = $("vaadin-combo-box-overlay").first()
                .$(TestBenchElement.class).id("content")
                .$(TestBenchElement.class).id("selector")
                .$("vaadin-combo-box-item").get(index);
        item.click();
    }

    private void scrollToItem(TestBenchElement comboBox, int index) {
        executeScript("arguments[0].$.overlay._scrollIntoView(arguments[1])",
                comboBox, index);
    }

    private TestBenchElement getTimePickerComboBox() {
        TestBenchElement picker = getTimePickerElement();
        return picker.$("vaadin-combo-box-light").get(0);
    }

    private String getLabelValue() {
        return $("div").id("formatted-time").getText();
    }

    private String getTimePickerValue() {
        return getTimePickerElement().getPropertyString("value");
    }

    private String getTimePickerInputValue() {
        return getTimePickerElement().$("vaadin-combo-box-light").first()
                .$("vaadin-time-picker-text-field").first()
                .getPropertyString("value");
    }

    private TestBenchElement getTimePickerElement() {
        return $("vaadin-time-picker").first();
    }

    private static String prettyPrint(Locale locale) {
        return locale.getDisplayName() + "["
                + TimePickerLocalizationView.getLocaleString(locale) + "]";
    }
}
