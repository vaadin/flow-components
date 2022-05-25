/*
 * Copyright 2000-2022 Vaadin Ltd.
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

package com.vaadin.flow.component.timepicker.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.component.timepicker.tests.TimePickerLocalizationView.LocalTimeTextBlock;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-time-picker/time-picker-localization")
public class TimePickerLocalizationIT extends AbstractComponentIT {

    // Maximum is `Locale.getAvailableLocales()` about 159 that makes the
    // test to take several minutes, testing a few of them also guarantees
    // that the component is working correctly.
    public static final int MINIMUM_NUMBER_OF_LOCALES_TO_TEST = 4;

    @Before
    public void init() {
        open();
        $(TimePickerElement.class).waitForFirst();
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

        selectStep("30m");
        runLocalisationTestPattern(
                new String[] { "00:00", "00:30", "01:00", "06:00", "06:30" },
                Arrays.asList(0, 1, 2, 12, 13));
    }

    @Test
    public void testMilliseconds_localeWithColonSeparator_inputParsedProperly() {
        // 12h : separator
        runMillisecondLocalizationTest(Locale.US, ":", "AM");

        // 24 h : separator
        runMillisecondLocalizationTest(Locale.FRANCE, ":", "");

        // 24h . separator
        runMillisecondLocalizationTest(new Locale("fi", "FI"), ".", "");

        // 12h : separator, AM/PM before entry
        runMillisecondLocalizationTest(new Locale("zh", "SG"), ":", "上午");
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

    @Test
    public void testChangingStep_reduceStepToHigherScale_valueIsNotTooDetailed() {
        runReduceStepTest(new Locale("en", "US"), "4:00 PM", "PM");
        runReduceStepTest(new Locale("en", "CA"), "16:00", "p.m.");
        runReduceStepTest(new Locale("fi", "FI"), "16:00", "");
        runReduceStepTest(new Locale("no", "NO"), "16:00", "PM");
        runReduceStepTest(new Locale("zh", "TW"), "16:00", "");
        runReduceStepTest(new Locale("ko", "KR"), "16:00", "");
        runReduceStepTest(new Locale("es", "PA"), "16:00", "p. m.");
    }

    @Test
    public void testAMCaseSensitivity() {
        assertTimeIsParsedCorrectly(new Locale("en", "US"), "1 AM", "1:00 AM");
        assertTimeIsParsedCorrectly(new Locale("en", "US"), "1 am", "1:00 AM");
        assertTimeIsParsedCorrectly(new Locale("en", "CA"), "1 A.M.",
                "1:00 a.m.");
        assertTimeIsParsedCorrectly(new Locale("en", "CA"), "1 a.m.",
                "1:00 a.m.");
        assertTimeIsParsedCorrectly(new Locale("es", "PA"), "1 A. M.",
                "1:00 a. m.");
        assertTimeIsParsedCorrectly(new Locale("es", "PA"), "1 a. m.",
                "1:00 a. m.");
        assertTimeIsParsedCorrectly(new Locale("ko", "KR"), "오전 1", "오전 1:00");
    }

    @Test
    public void testAMSpaceSensitivity() {
        assertTimeIsParsedCorrectly(new Locale("en", "US"), "1 A M", "1:00 AM");
        assertTimeIsParsedCorrectly(new Locale("en", "US"), "1 a m", "1:00 AM");
        assertTimeIsParsedCorrectly(new Locale("en", "CA"), "1 A. M.",
                "1:00 a.m.");
        assertTimeIsParsedCorrectly(new Locale("en", "CA"), "1 a. m.",
                "1:00 a.m.");
        assertTimeIsParsedCorrectly(new Locale("es", "PA"), "1 A.M.",
                "1:00 a. m.");
        assertTimeIsParsedCorrectly(new Locale("es", "PA"), "1 a.m.",
                "1:00 a. m.");
        assertTimeIsParsedCorrectly(new Locale("ko", "KR"), "오 전 1", "오전 1:00");
    }

    @Test
    public void testPMCaseSensitivity() {
        assertTimeIsParsedCorrectly(new Locale("en", "US"), "1 PM", "1:00 PM");
        assertTimeIsParsedCorrectly(new Locale("en", "US"), "1 pm", "1:00 PM");
        assertTimeIsParsedCorrectly(new Locale("en", "CA"), "1 P.M.",
                "1:00 p.m.");
        assertTimeIsParsedCorrectly(new Locale("en", "CA"), "1 p.m.",
                "1:00 p.m.");
        assertTimeIsParsedCorrectly(new Locale("es", "PA"), "1 P. M.",
                "1:00 p. m.");
        assertTimeIsParsedCorrectly(new Locale("es", "PA"), "1 p. m.",
                "1:00 p. m.");
        assertTimeIsParsedCorrectly(new Locale("ko", "KR"), "오후 1", "오후 1:00");
    }

    @Test
    public void testPMSpaceSensitivity() {
        assertTimeIsParsedCorrectly(new Locale("en", "US"), "1 P M", "1:00 PM");
        assertTimeIsParsedCorrectly(new Locale("en", "US"), "1 p m", "1:00 PM");
        assertTimeIsParsedCorrectly(new Locale("en", "CA"), "1 P. M.",
                "1:00 p.m.");
        assertTimeIsParsedCorrectly(new Locale("en", "CA"), "1 p. m.",
                "1:00 p.m.");
        assertTimeIsParsedCorrectly(new Locale("es", "PA"), "1 P.M.",
                "1:00 p. m.");
        assertTimeIsParsedCorrectly(new Locale("es", "PA"), "1 p.m.",
                "1:00 p. m.");
        assertTimeIsParsedCorrectly(new Locale("ko", "KR"), "오 후 1", "오후 1:00");
    }

    public void assertTimeIsParsedCorrectly(Locale locale, String value,
            String expected) {
        selectLocale(locale);
        getTimePickerElement().selectByText(value);
        Assert.assertEquals(expected,
                getTimePickerInputValueWithNormalSpaces());
    }

    private void runReduceStepTest(Locale locale, String initialValue4PM,
            String pmString) {
        ArrayList<String> errors = new ArrayList<>(0);

        selectLocale(locale);
        getTimePickerElement().selectByText(initialValue4PM);

        errors.add(verifyFormat());
        errors.add(verifyValueProperty("16:00"));
        errors.add(verifyServerValue("16:0:0.0"));

        selectStep("0.001s");

        errors.add(verifyValueProperty("16:00:00.000"));
        // value on the server side stays the same when scale gets smaller
        errors.add(verifyServerValue("16:0:0.0"));

        getTimePickerElement().selectByText("17:22:33.123");

        errors.add(verifyFormatIncludingMilliseconds(pmString));
        errors.add(verifyValueProperty("17:22:33.123"));
        errors.add(verifyServerValue("17:22:33.123"));

        selectStep("1s");

        errors.add(verifyFormat());
        errors.add(verifyValueProperty("17:22:33"));
        errors.add(verifyServerValue("17:22:33.0"));

        selectStep("1m");

        errors.add(verifyFormat());
        errors.add(verifyValueProperty("17:22"));
        errors.add(verifyServerValue("17:22:0.0"));

        selectStep("1h");

        // this is consistent with the web component, minutes are not cleaned up
        // when changing granularity from 1min -> 60min
        errors.add(verifyFormat());
        errors.add(verifyValueProperty("17:22"));
        errors.add(verifyServerValue("17:22:0.0"));

        errors.removeIf(item -> item == null);

        Assert.assertTrue("Errors with Locale " + locale.getDisplayName()
                + String.join("\n", errors), errors.isEmpty());
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

    private void runLocalisationTestPattern(String[] values,
            List<Integer> valueIndices) {
        Locale locale = null;

        int numberOfErrors = 0;
        int numberOfTestedLocales = MINIMUM_NUMBER_OF_LOCALES_TO_TEST;
        int tested = 0;
        Logger logger = Logger.getLogger(getClass().getName());

        for (Iterator<Locale> localeIterator = TimePicker
                .getSupportedAvailableLocales()
                .iterator(); tested < numberOfTestedLocales
                        && localeIterator.hasNext(); tested++) {

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
                getTimePickerElement().selectItemByIndex(value);
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
            }
        }

        logger.info("Tested time picker localization with "
                + numberOfTestedLocales + " locales");
        if (numberOfErrors > 0) {
            org.junit.Assert.fail("Localization Errors: " + numberOfErrors);
        }
    }

    private void runMillisecondLocalizationTest(Locale locale, String separator,
            String amString) {
        // there is some timing weirdness in team city with the last locale
        // (zh-SG),
        // unable to reproduce it locally -> reload UI
        open();

        ArrayList<String> errors = new ArrayList<>();

        selectLocale(locale);
        selectStep("1h");
        getTimePickerElement().selectItemByIndex(1); // 1:00 AM
        errors.add(verifyFormat());
        errors.add(verifyValueProperty("01:00"));

        selectStep("0.001s");

        // the server side value stays the same when duration gets smaller
        errors.add(verifyValueProperty("01:00:00.000"));
        errors.add(verifyServerValue("1:0:0.0"));

        getTimePickerElement().selectByText("2:03:04.555");

        errors.add(verifyFormatIncludingMilliseconds(amString));
        errors.add(verifyValueProperty("02:03:04.555"));
        errors.add(verifyServerValue("2:3:4.555"));

        getTimePickerElement().selectByText("6:3");

        errors.add(verifyFormatIncludingMilliseconds(amString));
        errors.add(verifyValueProperty("06:03:00.000"));
        errors.add(verifyServerValue("6:3:0.0"));

        getTimePickerElement().selectByText("1:2:3");

        errors.add(verifyFormatIncludingMilliseconds(amString));
        errors.add(verifyValueProperty("01:02:03.000"));
        errors.add(verifyServerValue("1:2:3.0"));

        getTimePickerElement().selectByText("2:3:4.5");

        errors.add(verifyFormatIncludingMilliseconds(amString));
        errors.add(verifyValueProperty("02:03:04.500"));
        errors.add(verifyServerValue("2:3:4.500"));

        getTimePickerElement().selectByText("6:7:8.90");

        errors.add(verifyFormatIncludingMilliseconds(amString));
        errors.add(verifyValueProperty("06:07:08.900"));
        errors.add(verifyServerValue("6:7:8.900"));

        getTimePickerElement().selectByText("2:3:4.05");

        errors.add(verifyFormatIncludingMilliseconds(amString));
        errors.add(verifyValueProperty("02:03:04.050"));
        errors.add(verifyServerValue("2:3:4.50"));

        getTimePickerElement().selectByText("6:7:8.009");

        errors.add(verifyFormatIncludingMilliseconds(amString));
        errors.add(verifyValueProperty("06:07:08.009"));
        errors.add(verifyServerValue("6:7:8.9"));

        getTimePickerElement().selectByText("10:11:12.100");

        errors.add(verifyFormatIncludingMilliseconds(amString));
        errors.add(verifyValueProperty("10:11:12.100"));
        errors.add(verifyServerValue("10:11:12.100"));

        getTimePickerElement().selectByText("1 2 3.111");

        errors.add(verifyFormatIncludingMilliseconds(amString));
        errors.add(verifyValueProperty("01:02:03.111"));
        errors.add(verifyServerValue("1:2:3.111"));

        getTimePickerElement().selectByText("3 0 0.222");

        errors.add(verifyFormatIncludingMilliseconds(amString));
        errors.add(verifyValueProperty("03:00:00.222"));
        errors.add(verifyServerValue("3:0:0.222"));

        getTimePickerElement().selectByText("4 5 6 123");

        errors.add(verifyFormatIncludingMilliseconds(amString));
        errors.add(verifyValueProperty("04:05:06.000"));
        errors.add(verifyServerValue("4:5:6.0"));

        errors.removeIf(item -> item == null);

        Assert.assertTrue(
                "Errors with Locale " + locale.getDisplayName() + "\n"
                        + errors.stream().collect(Collectors.joining("\n")),
                errors.isEmpty());
    }

    private String verifyValueProperty(String value) {
        String timePickerValue = getTimePickerElement().getValue();
        if (value.equals(timePickerValue)) {
            return null;
        } else {
            return "expected: " + value + " actual: " + timePickerValue;
        }
    }

    private String verifyServerValue(String value) {
        String serverValue = findElement(By.id("value-label")).getText();
        if (value.equals(serverValue)) {
            return null;
        } else {
            return "Server value error: expected: " + value + " actual: "
                    + serverValue;
        }
    }

    private String verifyFormat() {
        String timePickerInputValue = getTimePickerInputValueWithNormalSpaces();
        String formattedTextValue = getLabelValue();
        if (formattedTextValue.equals(timePickerInputValue)) {
            return null;
        } else {
            return "expected: " + formattedTextValue + " actual: "
                    + timePickerInputValue;
        }
    }

    private String verifyFormatIncludingMilliseconds(String amPmString) {
        String timePickerInputValue = getTimePickerInputValueWithNormalSpaces();
        String[] splitInputValue = timePickerInputValue.replace(amPmString, "")
                .split("\\.");
        String millisecondsInputValue = amPmString != null
                && !amPmString.isEmpty()
                        ? splitInputValue[splitInputValue.length - 1]
                                .replace(amPmString, "").trim()
                        : splitInputValue[splitInputValue.length - 1];

        timePickerInputValue = timePickerInputValue
                .replace("." + millisecondsInputValue, "");

        String[] splitLabelValue = getLabelValue()
                .split(LocalTimeTextBlock.MILLISECONDS_SPLIT);
        String formattedTextValue = splitLabelValue[0].trim();
        String formattedTextValueMilliseconds = splitLabelValue[1].trim();

        StringBuilder errors = new StringBuilder();
        if (!formattedTextValue.equals(timePickerInputValue)) {
            errors.append("Invalid value formatted, expected: "
                    + formattedTextValue + " actual: " + timePickerInputValue);
        }
        // using integer to match 0 and 000, 14 and 014
        if (Integer.parseInt(formattedTextValueMilliseconds) != Integer
                .parseInt(millisecondsInputValue)) {
            if (errors.length() > 0) {
                errors.append("\n");
            }
            errors.append("Invalid milliseconds formatted, expected: "
                    + formattedTextValueMilliseconds + " actual: "
                    + millisecondsInputValue);
        }
        return errors.length() > 0 ? errors.toString() : null;
    }

    private void selectLocale(Locale locale) {
        NativeSelectElement select = $(NativeSelectElement.class)
                .id("locale-picker");
        select.setValue(locale.toLanguageTag());
    }

    private void selectStep(String step) {
        NativeSelectElement select = $(NativeSelectElement.class)
                .id("step-picker");
        select.setValue(step);
    }

    private String getLabelValue() {
        return $("div").id("formatted-time").getText();
    }

    private TimePickerElement getTimePickerElement() {
        return $(TimePickerElement.class).first();
    }

    private static String prettyPrint(Locale locale) {
        return locale.getDisplayName() + "[" + locale.toLanguageTag() + "]";
    }

    /**
     * Calls {@code getTimePickerInputValue()} for {@code
     * TimePickerElement} and replaces non-breaking space characters (char 160)
     * with normal spaces (char 32) for easier comparison. Small number of
     * locales (such as es-PA) seem to use those for their localized timestamps.
     *
     * @return space-normalized timestamp
     */
    private String getTimePickerInputValueWithNormalSpaces() {
        return getTimePickerElement().getTimePickerInputValue()
                .replace((char) 160, (char) 32);
    }
}
