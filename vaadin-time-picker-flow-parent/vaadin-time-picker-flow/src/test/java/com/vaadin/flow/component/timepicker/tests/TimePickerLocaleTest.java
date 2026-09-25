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

    private void assertLocaleTag(Locale locale, String expectedTag) {
        TimePicker timePicker = new TimePicker();
        ui.add(timePicker);
        timePicker.setLocale(locale);
        ui.fakeClientCommunication();

        List<Object> arguments = ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> "$connector.setLocale"
                        .equals(JsFunctionCallUtil.getFunctionName(invocation)))
                .map(JsFunctionCallUtil::getArguments).reduce((a, b) -> b)
                .orElseThrow();
        Assertions.assertEquals(List.of(expectedTag), arguments);
    }
}
