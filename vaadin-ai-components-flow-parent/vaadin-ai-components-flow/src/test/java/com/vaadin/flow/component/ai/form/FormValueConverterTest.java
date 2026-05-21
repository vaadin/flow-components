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
package com.vaadin.flow.component.ai.form;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ai.form.FormAITools.FormFieldDescriptor;
import com.vaadin.flow.component.ai.form.FormTestFields.BigDecField;
import com.vaadin.flow.component.ai.form.FormTestFields.BoolField;
import com.vaadin.flow.component.ai.form.FormTestFields.DateField;
import com.vaadin.flow.component.ai.form.FormTestFields.DateTimeField;
import com.vaadin.flow.component.ai.form.FormTestFields.DoubleField;
import com.vaadin.flow.component.ai.form.FormTestFields.IntField;
import com.vaadin.flow.component.ai.form.FormTestFields.MultiSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.SingleSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.ai.form.FormTestFields.TimeField;
import com.vaadin.flow.component.ai.form.FormValueConverter.RejectedValueException;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * Tests for {@link FormValueConverter#convert(FormFieldDescriptor, JsonNode)}
 * and {@link FormValueConverter#displayValue(FormFieldDescriptor)} — the
 * write-path conversion and rendering used by the {@code fill_form} tool. The
 * read-path methods ({@code isEmpty}, {@code renderItem},
 * {@code listDataProviderItems}) are covered by the state-tool tests via
 * {@code FormStateToolTest}.
 */
class FormValueConverterTest {

    @Test
    void convert_nullJsonReturnsEmptyValue() {
        var field = wrap(new TestField(), FormFieldType.STRING);

        Assertions.assertEquals("", FormValueConverter.convert(field, null));
    }

    @Test
    void convert_jsonNullReturnsEmptyValue() {
        var field = wrap(new TestField(), FormFieldType.STRING);

        Assertions.assertEquals("",
                FormValueConverter.convert(field, json("null")));
    }

    @Test
    void convert_stringJsonReturnsString() {
        var field = wrap(new TestField(), FormFieldType.STRING);

        Assertions.assertEquals("Acme",
                FormValueConverter.convert(field, json("\"Acme\"")));
    }

    @Test
    void convert_nonStringJsonForStringFieldRejects() {
        var field = wrap(new TestField(), FormFieldType.STRING);

        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json("42")));
    }

    @Test
    void convert_numberJsonReturnsDouble() {
        var field = wrap(new DoubleField(), FormFieldType.NUMBER);

        Assertions.assertEquals(58.4,
                FormValueConverter.convert(field, json("58.4")));
    }

    @Test
    void convert_nonNumberForNumberFieldRejects() {
        var field = wrap(new DoubleField(), FormFieldType.NUMBER);

        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json("\"58.4\"")));
    }

    @Test
    void convert_integerJsonReturnsInt() {
        var field = wrap(new IntField(), FormFieldType.INTEGER);

        Assertions.assertEquals(42,
                FormValueConverter.convert(field, json("42")));
    }

    @Test
    void convert_wholeNumberFloatAcceptedForInteger() {
        // LLMs sometimes emit "3.0" for integer fields; accept it.
        var field = wrap(new IntField(), FormFieldType.INTEGER);

        Assertions.assertEquals(3,
                FormValueConverter.convert(field, json("3.0")));
    }

    @Test
    void convert_fractionalFloatRejectedForInteger() {
        var field = wrap(new IntField(), FormFieldType.INTEGER);

        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json("3.5")));
    }

    @Test
    void convert_bigDecimalFromStringJson() {
        // Schema declares BigDecimal as string with the BIG_DECIMAL pattern;
        // the LLM emits the value as a JSON string and convert parses it.
        var field = wrap(new BigDecField(), FormFieldType.BIG_DECIMAL);

        Assertions.assertEquals(new BigDecimal("1234.50"),
                FormValueConverter.convert(field, json("\"1234.50\"")));
    }

    @Test
    void convert_bigDecimalFromNumberJsonAlsoAccepted() {
        // Lenient: also accept a JSON number for BigDecimal. The schema
        // says string but LLMs sometimes emit a bare number, and there's
        // no precision-loss path through Jackson here.
        var field = wrap(new BigDecField(), FormFieldType.BIG_DECIMAL);

        Assertions.assertEquals(new BigDecimal("42"),
                FormValueConverter.convert(field, json("42")));
    }

    @Test
    void convert_unparseableBigDecimalRejected() {
        var field = wrap(new BigDecField(), FormFieldType.BIG_DECIMAL);

        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field,
                        json("\"not a number\"")));
    }

    @Test
    void convert_booleanJsonReturnsBoolean() {
        var field = wrap(new BoolField(), FormFieldType.BOOLEAN);

        Assertions.assertEquals(true,
                FormValueConverter.convert(field, json("true")));
        Assertions.assertEquals(false,
                FormValueConverter.convert(field, json("false")));
    }

    @Test
    void convert_nonBooleanRejected() {
        var field = wrap(new BoolField(), FormFieldType.BOOLEAN);

        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json("\"yes\"")));
    }

    @Test
    void convert_isoDateJsonReturnsLocalDate() {
        var field = wrap(new DateField(), FormFieldType.DATE);

        Assertions.assertEquals(LocalDate.of(2026, 5, 19),
                FormValueConverter.convert(field, json("\"2026-05-19\"")));
    }

    @Test
    void convert_nonIsoDateRejected() {
        var field = wrap(new DateField(), FormFieldType.DATE);

        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field,
                        json("\"05/19/2026\"")));
    }

    @Test
    void convert_naiveIsoDateTimeReturnsLocalDateTime() {
        var field = wrap(new DateTimeField(), FormFieldType.DATE_TIME);

        Assertions.assertEquals(LocalDateTime.of(2026, 5, 19, 10, 30),
                FormValueConverter.convert(field,
                        json("\"2026-05-19T10:30:00\"")));
    }

    @Test
    void convert_zonedIsoDateTimeDropsZone() {
        // DateTimePicker is zone-naive; the converter accepts a zoned or
        // offset form and drops the zone information.
        var field = wrap(new DateTimeField(), FormFieldType.DATE_TIME);

        Assertions.assertEquals(LocalDateTime.of(2026, 5, 19, 10, 30),
                FormValueConverter.convert(field,
                        json("\"2026-05-19T10:30:00Z\"")));
    }

    @Test
    void convert_isoTimeJsonReturnsLocalTime() {
        var field = wrap(new TimeField(), FormFieldType.TIME);

        Assertions.assertEquals(LocalTime.of(9, 15),
                FormValueConverter.convert(field, json("\"09:15:00\"")));
    }

    @Test
    void convert_emailRoutesThroughStringBranch() {
        // EMAIL is just STRING + format=email at the schema layer; convert
        // does the same string handling.
        var field = wrap(new TestField(), FormFieldType.EMAIL);

        Assertions.assertEquals("ops@acme.example", FormValueConverter
                .convert(field, json("\"ops@acme.example\"")));
    }

    @Test
    void convert_singleSelectTypeRejected() {
        // Selection types are not handled by convert in this PR — they
        // require label-to-value lookup via the field's data provider,
        // which the converter doesn't do. The fill path rejects them
        // rather than silently mis-writing.
        var field = wrap(new SingleSelectField<String>(),
                FormFieldType.SINGLE_SELECT);

        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json("\"any\"")));
    }

    @Test
    void convert_multiSelectTypeRejected() {
        // MULTI_SELECT hits the same default → throw branch as
        // SINGLE_SELECT; pin it separately so a regression that handled
        // one but not the other still fails.
        var field = wrap(new MultiSelectField<String>(),
                FormFieldType.MULTI_SELECT);

        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json("[\"any\"]")));
    }

    @Test
    void convert_malformedIsoDateTimeRejected() {
        var field = wrap(new DateTimeField(), FormFieldType.DATE_TIME);

        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field,
                        json("\"not a date-time\"")));
    }

    @Test
    void convert_malformedIsoTimeRejected() {
        var field = wrap(new TimeField(), FormFieldType.TIME);

        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json("\"25:99:99\"")));
    }

    @Test
    void convert_jsonNullReturnsTheFieldsActualEmptyValue() {
        // A DoubleField's empty value is null, not "". Pins that convert
        // routes through field.getEmptyValue() rather than returning a
        // hard-coded "".
        var field = wrap(new DoubleField(), FormFieldType.NUMBER);

        Assertions.assertNull(FormValueConverter.convert(field, json("null")),
                "DoubleField's empty value is null; convert must return "
                        + "that, not a sentinel empty string");
    }

    @Test
    void convert_nonStringForEmailRejected() {
        // EMAIL routes through the same string-handling branch as STRING;
        // a JSON number must be rejected the same way (no implicit
        // toString).
        var field = wrap(new TestField(), FormFieldType.EMAIL);

        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json("42")));
    }

    @Test
    void displayValue_emptyValueRendersAsSentinel() {
        var field = wrap(new TestField(), FormFieldType.STRING);

        Assertions.assertEquals("<empty>",
                FormValueConverter.displayValue(field));
    }

    @Test
    void displayValue_nonEmptyStringRendersVerbatim() {
        var text = new TestField();
        text.setValue("Acme Corp");
        var field = wrap(text, FormFieldType.STRING);

        Assertions.assertEquals("Acme Corp",
                FormValueConverter.displayValue(field));
    }

    @Test
    void displayValue_collectionRendersCommaSeparated() {
        // Multi-select fields hold a Set; displayValue joins with comma+
        // space so the LLM can read all selected values from the Current
        // state: block.
        var multi = new MultiSelectField<String>();
        multi.setItems("alpha", "beta", "gamma");
        multi.setValue(Set.of("alpha", "gamma"));
        var field = wrap(multi, FormFieldType.MULTI_SELECT);

        var rendered = FormValueConverter.displayValue(field);
        // Set ordering isn't guaranteed; assert both members appear with
        // the ", " separator.
        Assertions.assertTrue(
                rendered.contains("alpha") && rendered.contains("gamma"),
                "Both selected values must appear, got: " + rendered);
        Assertions.assertTrue(rendered.contains(", "),
                "Collection members must be comma-separated, got: " + rendered);
    }

    @Test
    void displayValue_emptyCollectionRendersAsSentinel() {
        var multi = new MultiSelectField<String>();
        // value is the default empty Set
        var field = wrap(multi, FormFieldType.MULTI_SELECT);

        Assertions.assertEquals("<empty>",
                FormValueConverter.displayValue(field));
    }

    // --- helpers ---

    private static FormFieldDescriptor wrap(HasValue<?, ?> field,
            FormFieldType type) {
        return new FormFieldDescriptor("test-id", field, type, null);
    }

    private static JsonNode json(String text) {
        // JacksonUtils.readTree returns ObjectNode; these tests feed scalar
        // JSON (numbers, strings, booleans, null) so route through the raw
        // mapper instead.
        try {
            return JacksonUtils.getMapper().readTree(text);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
