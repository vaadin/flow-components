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
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.tests.JsFunctionCallUtil;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class DatePickerLocaleTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void newDatePicker_returnsUiLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        ui.setLocale(finnishLocale);
        DatePicker datePicker = new DatePicker();
        Assertions.assertEquals(finnishLocale, datePicker.getLocale());
    }

    @Test
    void newDatePickerWithCustomLocale_returnsCustomLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        Locale usLocale = new Locale("en-US");
        ui.setLocale(finnishLocale);
        DatePicker datePicker = new DatePicker(LocalDate.now(), usLocale);
        Assertions.assertEquals(usLocale, datePicker.getLocale());
    }

    @Test
    void setCustomLocale_returnsCustomLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        Locale usLocale = new Locale("en-US");
        ui.setLocale(finnishLocale);
        DatePicker datePicker = new DatePicker();
        datePicker.setLocale(usLocale);
        Assertions.assertEquals(usLocale, datePicker.getLocale());
    }

    @Test
    void setLocaleWhileDetached_attach_i18nUpdatedAfterInitLazy() {
        DatePicker datePicker = new DatePicker();
        datePicker.setLocale(Locale.GERMANY);
        ui.add(datePicker);

        List<JavaScriptInvocation> invocations = ui
                .dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation).toList();
        int initIndex = indexOf(invocations, invocation -> invocation
                .getExpression().contains("datepickerConnector.initLazy"));
        int i18nIndex = indexOf(invocations,
                invocation -> "$connector.updateI18n".equals(
                        JsFunctionCallUtil.getFunctionName(invocation)));
        Assertions.assertTrue(initIndex >= 0, "initLazy was not invoked");
        Assertions.assertTrue(i18nIndex > initIndex,
                "I18n must be updated after the connector is initialized");
    }

    private static int indexOf(List<JavaScriptInvocation> invocations,
            Predicate<JavaScriptInvocation> predicate) {
        return IntStream.range(0, invocations.size())
                .filter(i -> predicate.test(invocations.get(i))).findFirst()
                .orElse(-1);
    }
}
