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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

@NotThreadSafe
class DatePickerDateMetadataTest {

    private static final String SET_CONFIG = "setDateMetadataConfig";

    private static final String CLEAR_CACHE = "clearCache";

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private DatePicker picker;

    @BeforeEach
    void setup() {
        picker = new DatePicker();
    }

    @Test
    void setDateMetadataProvider_configHasProviderFlag() {
        DateMetadataProvider provider = range -> List.of();
        picker.setDateMetadataProvider(provider);

        Assertions.assertSame(provider, picker.getDateMetadataProvider());
        Assertions.assertTrue(picker.createDateMetadataConfig()
                .get("hasProvider").booleanValue());
    }

    @Test
    void setDateMetadataProvider_null_configHasNoProviderFlag() {
        picker.setDateMetadataProvider(range -> List.of());
        picker.setDateMetadataProvider(null);

        Assertions.assertNull(picker.getDateMetadataProvider());
        Assertions.assertFalse(picker.createDateMetadataConfig()
                .get("hasProvider").booleanValue());
    }

    @Test
    void setDateMetadataProvider_configPushedBeforeClearCache() {
        ui.add(picker);
        // Discard the invocations that the attach itself produces
        ui.dumpPendingJavaScriptInvocations();

        picker.setDateMetadataProvider(range -> List.of());

        // The queue is drained on dump, so capture it once and compare the
        // positions within that single list
        List<String> expressions = dumpInvocationExpressions();
        int configIndex = indexOfExpression(expressions, SET_CONFIG);
        int clearCacheIndex = indexOfExpression(expressions, CLEAR_CACHE);

        Assertions.assertNotEquals(-1, configIndex);
        Assertions.assertNotEquals(-1, clearCacheIndex);
        Assertions.assertTrue(configIndex < clearCacheIndex,
                "The config must be pushed before the cache is cleared");
    }

    @Test
    void refreshDateMetadata_clearCacheInvoked() {
        picker.setDateMetadataProvider(range -> List.of());
        ui.add(picker);
        ui.dumpPendingJavaScriptInvocations();

        picker.refreshDateMetadata();

        List<String> expressions = dumpInvocationExpressions();
        Assertions.assertNotEquals(-1,
                indexOfExpression(expressions, CLEAR_CACHE));
    }

    @Test
    void refreshDateMetadata_noProvider_noClientCalls() {
        ui.add(picker);
        ui.dumpPendingJavaScriptInvocations();

        picker.refreshDateMetadata();

        List<String> expressions = dumpInvocationExpressions();
        Assertions.assertEquals(-1, indexOfExpression(expressions, SET_CONFIG));
        Assertions.assertEquals(-1,
                indexOfExpression(expressions, CLEAR_CACHE));
    }

    @Test
    void setDisabledDates_noClearCacheInvoked() {
        ui.add(picker);
        ui.dumpPendingJavaScriptInvocations();

        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));
        picker.setDisabledWeekdays(Set.of(DayOfWeek.SUNDAY));

        List<String> expressions = dumpInvocationExpressions();
        Assertions.assertNotEquals(-1,
                indexOfExpression(expressions, SET_CONFIG));
        Assertions.assertEquals(-1,
                indexOfExpression(expressions, CLEAR_CACHE));
    }

    @Test
    void setProviderWhileDetached_attach_noClearCache() {
        picker.setDateMetadataProvider(range -> List.of());
        ui.add(picker);

        // A freshly created client element has an empty cache already
        List<String> expressions = dumpInvocationExpressions();
        Assertions.assertNotEquals(-1,
                indexOfExpression(expressions, SET_CONFIG));
        Assertions.assertEquals(-1,
                indexOfExpression(expressions, CLEAR_CACHE));
    }

    @Test
    void requestDateMetadata_acceptsZeroBasedMonths() {
        AtomicReference<DateRange> capturedRange = new AtomicReference<>();
        picker.setDateMetadataProvider(range -> {
            capturedRange.set(range);
            return List.of();
        });

        // Month 0 is January on the wire
        picker.requestDateMetadata(2023, 0, 1, 2023, 0, 31);

        Assertions.assertEquals(LocalDate.of(2023, 1, 1),
                capturedRange.get().start());
        Assertions.assertEquals(LocalDate.of(2023, 1, 31),
                capturedRange.get().end());
    }

    @Test
    void requestDateMetadata_returnsZeroBasedMonths() {
        picker.setDateMetadataProvider(range -> List
                .of(new DateMetadata(LocalDate.of(2023, 1, 10), true)));

        ArrayNode entries = picker.requestDateMetadata(2023, 0, 1, 2023, 0, 31);

        Assertions.assertEquals(1, entries.size());
        JsonNode entry = entries.get(0);
        Assertions.assertEquals(2023, entry.get("year").intValue());
        // January is month 0 on the wire
        Assertions.assertEquals(0, entry.get("month").intValue());
        Assertions.assertEquals(10, entry.get("day").intValue());
        Assertions.assertTrue(entry.get("disabled").booleanValue());
    }

    @Test
    void requestDateMetadata_skipsEntriesWithoutMetadata() {
        picker.setDateMetadataProvider(range -> List.of(
                new DateMetadata(LocalDate.of(2023, 1, 10), false),
                new DateMetadata(LocalDate.of(2023, 1, 11), true)));

        ArrayNode entries = picker.requestDateMetadata(2023, 0, 1, 2023, 0, 31);

        Assertions.assertEquals(1, entries.size());
        Assertions.assertEquals(11, entries.get(0).get("day").intValue());
    }

    @Test
    void requestDateMetadata_providerReturnsNull_returnsEmptyArray() {
        picker.setDateMetadataProvider(range -> null);

        ArrayNode entries = picker.requestDateMetadata(2023, 0, 1, 2023, 0, 31);

        Assertions.assertEquals(0, entries.size());
    }

    @Test
    void requestDateMetadata_noProvider_returnsEmptyArray() {
        ArrayNode entries = picker.requestDateMetadata(2023, 0, 1, 2023, 0, 31);

        Assertions.assertEquals(0, entries.size());
    }

    @Test
    void requestDateMetadata_calledOncePerRange() {
        AtomicInteger calls = new AtomicInteger();
        picker.setDateMetadataProvider(range -> {
            calls.incrementAndGet();
            return List.of();
        });

        // A whole year, so a per-date callback would show up as many calls
        picker.requestDateMetadata(2023, 0, 1, 2023, 11, 31);

        Assertions.assertEquals(1, calls.get());
    }

    @Test
    void dateRange_startAfterEnd_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new DateRange(LocalDate.of(2023, 1, 31),
                        LocalDate.of(2023, 1, 1)));
    }

    @Test
    void dateRange_nullBound_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new DateRange(null, LocalDate.of(2023, 1, 31)));
        Assertions.assertThrows(NullPointerException.class,
                () -> new DateRange(LocalDate.of(2023, 1, 1), null));
    }

    @Test
    void dateMetadata_nullDate_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new DateMetadata(null, true));
    }

    @Test
    void providerDisabledDate_validationFails() {
        picker.setI18n(new DatePickerI18n()
                .setDisabledDateErrorMessage("Date is disabled"));
        picker.setDateMetadataProvider(range -> List
                .of(new DateMetadata(LocalDate.of(2023, 1, 10), true)));

        picker.setValue(LocalDate.of(2023, 1, 10));

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals("Date is disabled", picker.getErrorMessage());
    }

    @Test
    void providerEnabledDate_validationPasses() {
        picker.setDateMetadataProvider(range -> List.of(
                new DateMetadata(LocalDate.of(2023, 1, 10), false),
                new DateMetadata(LocalDate.of(2023, 1, 11), true)));

        picker.setValue(LocalDate.of(2023, 1, 10));

        Assertions.assertFalse(picker.isInvalid());
    }

    @Test
    void isDateDisabled_providerCalledWithWholeMonthRange() {
        AtomicReference<DateRange> capturedRange = new AtomicReference<>();
        picker.setDateMetadataProvider(range -> {
            capturedRange.set(range);
            return List.of();
        });

        picker.isDateDisabled(LocalDate.of(2023, 2, 15));

        Assertions.assertEquals(LocalDate.of(2023, 2, 1),
                capturedRange.get().start());
        Assertions.assertEquals(LocalDate.of(2023, 2, 28),
                capturedRange.get().end());
    }

    @Test
    void refreshDateMetadata_valueSet_revalidates() {
        AtomicBoolean restrictive = new AtomicBoolean(false);
        picker.setDateMetadataProvider(range -> restrictive.get()
                ? List.of(new DateMetadata(LocalDate.of(2023, 1, 10), true))
                : List.of());

        picker.setValue(LocalDate.of(2023, 1, 10));
        Assertions.assertFalse(picker.isInvalid());

        restrictive.set(true);
        picker.refreshDateMetadata();

        Assertions.assertTrue(picker.isInvalid());
    }

    @Test
    void refreshDateMetadata_valueSet_firesValidationStatusChangeEvent() {
        picker.setDateMetadataProvider(range -> List.of());
        picker.setValue(LocalDate.of(2023, 1, 10));

        AtomicInteger events = new AtomicInteger();
        picker.addValidationStatusChangeListener(
                event -> events.incrementAndGet());

        picker.refreshDateMetadata();

        Assertions.assertEquals(1, events.get());
    }

    @Test
    void refreshDateMetadata_noValue_doesNotValidate() {
        picker.setRequiredIndicatorVisible(true);
        picker.setDateMetadataProvider(range -> List.of());

        picker.refreshDateMetadata();

        Assertions.assertFalse(picker.isInvalid());
    }

    private List<String> dumpInvocationExpressions() {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .map(JavaScriptInvocation::getExpression).toList();
    }

    private int indexOfExpression(List<String> expressions, String function) {
        for (int i = 0; i < expressions.size(); i++) {
            if (expressions.get(i).contains(function)) {
                return i;
            }
        }
        return -1;
    }
}
