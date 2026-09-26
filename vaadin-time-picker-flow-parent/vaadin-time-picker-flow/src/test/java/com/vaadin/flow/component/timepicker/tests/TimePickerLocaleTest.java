/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.tests.JsFunctionCallUtil;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class TimePickerLocaleTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void newTimePicker_returnsUiLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        ui.setLocale(finnishLocale);
        TimePicker timePicker = new TimePicker();
        Assertions.assertEquals(finnishLocale, timePicker.getLocale());
    }

    @Test
    void setCustomLocale_returnsCustomLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        Locale usLocale = new Locale("en-US");
        ui.setLocale(finnishLocale);
        TimePicker timePicker = new TimePicker();
        timePicker.setLocale(usLocale);
        Assertions.assertEquals(usLocale, timePicker.getLocale());
    }

    @Test
    void setLocale_sendsLanguageAndCountryTag() {
        assertLocaleTag(Locale.UK, "en-GB");
    }

    @Test
    void setLocaleWithoutCountry_sendsLanguageTag() {
        assertLocaleTag(Locale.of("fi"), "fi");
    }

    @Test
    void setLocaleWithScriptAndVariant_sendsLanguageAndCountryTag() {
        assertLocaleTag(Locale.forLanguageTag("sr-Latn-RS-1994"), "sr-RS");
    }

    @Test
    void setLocaleWithLanguageTagAsLanguage_sendsLanguageAndCountryTag() {
        assertLocaleTag(new Locale("en-CA"), "en-CA");
    }

    @Test
    void setLocaleWithIllFormedLanguage_sendsUndeterminedTag() {
        assertLocaleTag(Locale.of("en_GB"), "und");
    }

    @Test
    void setLocaleWithIllFormedCountry_sendsLanguageTag() {
        assertLocaleTag(Locale.of("en", "GB_X"), "en");
    }

    @Test
    void noI18n_sendsNullI18n() {
        TimePicker timePicker = new TimePicker();
        timePicker.setLocale(Locale.US);
        ui.add(timePicker);

        List<List<Object>> calls = getUpdateI18nCalls();
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals("en-US", calls.get(0).get(0));
        Assertions.assertNull(calls.get(0).get(1));
    }

    @Test
    void setI18nWithTimeFormats_sendsTimeFormats() {
        TimePicker timePicker = new TimePicker();
        timePicker.setLocale(Locale.US);
        ui.add(timePicker);
        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();

        timePicker.setI18n(
                new TimePicker.TimePickerI18n().setTimeFormats("HH.mm", "Hmm"));

        List<List<Object>> calls = getUpdateI18nCalls();
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals("en-US", calls.get(0).get(0));
        Assertions.assertEquals("{\"timeFormats\":[\"HH.mm\",\"Hmm\"]}",
                calls.get(0).get(1).toString());
    }

    @Test
    void setI18nWithErrorMessagesOnly_sendsEmptyI18n() {
        TimePicker timePicker = new TimePicker();
        ui.add(timePicker);
        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();

        timePicker.setI18n(new TimePicker.TimePickerI18n()
                .setBadInputErrorMessage("bad input")
                .setRequiredErrorMessage("required").setMinErrorMessage("min")
                .setMaxErrorMessage("max"));

        List<List<Object>> calls = getUpdateI18nCalls();
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals("{}", calls.get(0).get(1).toString());
    }

    @Test
    void setI18nAndSetLocaleInOneRoundtrip_sendsOneUpdate() {
        TimePicker timePicker = new TimePicker();
        ui.add(timePicker);
        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();

        timePicker.setI18n(
                new TimePicker.TimePickerI18n().setTimeFormat("HH:mm"));
        timePicker.setLocale(Locale.UK);

        List<List<Object>> calls = getUpdateI18nCalls();
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals("en-GB", calls.get(0).get(0));
        Assertions.assertEquals("{\"timeFormats\":[\"HH:mm\"]}",
                calls.get(0).get(1).toString());
    }

    @Test
    void detachAndReattach_resendsTimeFormats() {
        TimePicker timePicker = new TimePicker();
        timePicker.setI18n(
                new TimePicker.TimePickerI18n().setTimeFormat("HH:mm"));
        ui.add(timePicker);
        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();

        ui.remove(timePicker);
        ui.add(timePicker);

        List<List<Object>> calls = getUpdateI18nCalls();
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals("{\"timeFormats\":[\"HH:mm\"]}",
                calls.get(0).get(1).toString());
    }

    @Test
    void resetTimeFormatAndSetI18n_sendsEmptyI18n() {
        TimePicker timePicker = new TimePicker();
        TimePicker.TimePickerI18n i18n = new TimePicker.TimePickerI18n()
                .setTimeFormat("HH:mm");
        timePicker.setI18n(i18n);
        ui.add(timePicker);
        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();

        timePicker.setI18n(i18n.setTimeFormat(null));

        List<List<Object>> calls = getUpdateI18nCalls();
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals("{}", calls.get(0).get(1).toString());
    }

    private List<List<Object>> getUpdateI18nCalls() {
        ui.fakeClientCommunication();
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> "$connector.updateI18n"
                        .equals(JsFunctionCallUtil.getFunctionName(invocation)))
                .map(JsFunctionCallUtil::getArguments).toList();
    }

    private void assertLocaleTag(Locale locale, String expectedTag) {
        TimePicker timePicker = new TimePicker();
        ui.add(timePicker);
        timePicker.setLocale(locale);

        List<List<Object>> calls = getUpdateI18nCalls();
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals(Arrays.asList(expectedTag, null), calls.get(0));
    }
}
