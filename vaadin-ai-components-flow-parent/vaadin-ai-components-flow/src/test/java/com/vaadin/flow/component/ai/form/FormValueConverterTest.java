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
import java.util.function.Function;

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
import com.vaadin.flow.component.ai.form.FormTestFields.Project;
import com.vaadin.flow.component.ai.form.FormTestFields.SingleSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.ai.form.FormTestFields.TimeField;
import com.vaadin.flow.component.ai.form.FormValueConverter.RejectedValueException;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * Tests for {@link FormValueConverter#convert(FormFieldDescriptor, JsonNode)} —
 * the write-path conversion used by the {@code fill_form} tool. The read-path
 * methods ({@code isEmpty}, {@code renderItem}, {@code listDataProviderItems})
 * are covered by the state-tool tests via {@code FormStateToolTest}.
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

        var json = json("42");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
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

        var json = json("\"58.4\"");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
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

        var json = json("3.5");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
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

        var json = json("\"not a number\"");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
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

        var json = json("\"yes\"");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
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

        var json = json("\"05/19/2026\"");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
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
    void convert_singleSelectWithoutFieldValueOptions_rejectedWithRegistrationHint() {
        // Without a fieldValueOptions(...) registration, the LLM has no labels
        // to pick and the converter has no toValue function to resolve them
        // — fail loudly and point the developer at the right API.
        var field = wrap(new SingleSelectField<String>(),
                FormFieldType.SINGLE_SELECT);

        var json = json("\"any\"");
        var ex = Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
        Assertions.assertTrue(ex.getMessage().contains("fieldValueOptions"),
                "Rejection reason must point at the missing fieldValueOptions "
                        + "registration; got: " + ex.getMessage());
    }

    @Test
    void convert_multiSelectWithoutFieldValueOptions_rejectedWithRegistrationHint() {
        var field = wrap(new MultiSelectField<String>(),
                FormFieldType.MULTI_SELECT);

        var json = json("[\"any\"]");
        var ex = Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
        Assertions.assertTrue(ex.getMessage().contains("fieldValueOptions"),
                "Rejection reason must point at the missing fieldValueOptions "
                        + "registration; got: " + ex.getMessage());
    }

    @Test
    void convert_singleSelectFromItems_nonStringJsonRejected() {
        // Eager-items SINGLE_SELECT (no fieldValueOptions registered, items
        // populated via setItems) must reject a non-string JSON shape
        // before reaching renderItem-based matching — otherwise a JSON
        // boolean true coincidentally renders to "true" and could match
        // an item whose label happens to be "true", silently writing a
        // wrong-shape value.
        var field = new SingleSelectField<String>();
        field.setItems("true", "false");

        var json = json("true");
        var ex = Assertions
                .assertThrows(RejectedValueException.class,
                        () -> FormValueConverter.convert(
                                wrap(field, FormFieldType.SINGLE_SELECT),
                                json));
        Assertions.assertTrue(ex.getMessage().contains("string label"),
                "Rejection reason must name the type mismatch, not the "
                        + "label match path; got: " + ex.getMessage());
    }

    @Test
    void convert_multiSelectFromItems_nonStringArrayElementRejected() {
        // Symmetric to convert_singleSelectFromItems_nonStringJsonRejected:
        // each element of a multi-select array must be a JSON string
        // before reaching renderItem-based matching against the field's
        // eager items.
        var field = new MultiSelectField<String>();
        field.setItems("true", "false");

        var json = json("[true]");
        var ex = Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(
                        wrap(field, FormFieldType.MULTI_SELECT), json));
        Assertions.assertTrue(ex.getMessage().contains("string label"),
                "Rejection reason must name the type mismatch, not the "
                        + "label match path; got: " + ex.getMessage());
    }

    @Test
    void convert_singleSelectWithValueOptionsToValue_resolvesLabel() {
        // The LLM sends a label string; the registered toValue resolves it
        // to the field's actual value type. Verifies the converter routes
        // SINGLE_SELECT through valueOptionsToValue instead of doing
        // type-driven parsing that would hand setValue a raw String.
        var field = wrap(new SingleSelectField<Project>(),
                FormFieldType.SINGLE_SELECT,
                hintsWithToValue(label -> new Project("P-1", label)));

        var result = FormValueConverter.convert(field, json("\"Apollo\""));

        Assertions.assertEquals(new Project("P-1", "Apollo"), result);
    }

    @Test
    void convert_singleSelectNonStringJsonRejected() {
        var field = wrap(new SingleSelectField<Project>(),
                FormFieldType.SINGLE_SELECT,
                hintsWithToValue(label -> new Project("P-1", label)));

        var json = json("42");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
    }

    @Test
    void convert_singleSelectToValueReturnsNullRejectedAsUnknownLabel() {
        // Convention: returning null from toValue signals "I don't
        // recognise this label". The converter must reject rather than
        // pass null to setValue (which would silently clear the field).
        var field = wrap(new SingleSelectField<Project>(),
                FormFieldType.SINGLE_SELECT, hintsWithToValue(label -> null));

        var json = json("\"NotAProject\"");
        var ex = Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
        Assertions.assertTrue(ex.getMessage().contains("NotAProject"),
                "Rejection reason must name the unmatched label; got: "
                        + ex.getMessage());
    }

    @Test
    void convert_singleSelectToValueThrowsRejectedWithCuratedReason() {
        // The application's toValue can throw arbitrary RuntimeException
        // with arbitrary text; the converter must reject with a curated
        // reason that does NOT echo the third-party exception text.
        var field = wrap(new SingleSelectField<Project>(),
                FormFieldType.SINGLE_SELECT, hintsWithToValue(label -> {
                    throw new IllegalStateException(
                            "internal-detail-from-toValue");
                }));

        var json = json("\"Apollo\"");
        var ex = Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
        Assertions.assertFalse(
                ex.getMessage().contains("internal-detail-from-toValue"),
                "Curated rejection must not echo the third-party "
                        + "exception text; got: " + ex.getMessage());
        Assertions.assertTrue(ex.getMessage().contains("Apollo"),
                "Curated rejection should still name the offending label "
                        + "so the LLM can correlate; got: " + ex.getMessage());
    }

    @Test
    void convert_multiSelectBuildsSetFromLabels() {
        // toValue maps one label to one Project; the converter aggregates
        // them into a LinkedHashSet that matches the field's Set<Project>
        // value type via MultiSelect-erasure.
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithToValue(label -> new Project(label, label)));

        var result = FormValueConverter.convert(field,
                json("[\"Apollo\", \"Vega\"]"));

        Assertions.assertEquals(Set.of(new Project("Apollo", "Apollo"),
                new Project("Vega", "Vega")), result);
    }

    @Test
    void convert_multiSelectDeduplicatesRepeatedLabels() {
        // The converter aggregates into a LinkedHashSet so duplicate labels
        // collapse to one item — matches the MultiSelect contract.
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithToValue(label -> new Project(label, label)));

        var result = FormValueConverter.convert(field,
                json("[\"Apollo\", \"Apollo\", \"Vega\"]"));

        Assertions.assertEquals(
                Set.of(new Project("Apollo", "Apollo"),
                        new Project("Vega", "Vega")),
                result,
                "Repeated labels must collapse into a single set entry");
    }

    @Test
    void convert_multiSelectNonArrayJsonRejected() {
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithToValue(label -> new Project("c", label)));

        var json = json("\"Apollo\"");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
    }

    @Test
    void convert_multiSelectJsonNullReturnsFieldEmptyValue() {
        // JSON null on a multi-select clears the selection — the null
        // short-circuit at the top of convert() must win over the
        // multi-select array-shape enforcement.
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithToValue(label -> new Project("c", label)));

        var result = FormValueConverter.convert(field, json("null"));

        Assertions.assertEquals(field.field().getEmptyValue(), result);
    }

    @Test
    void convert_multiSelectEmptyArrayBuildsEmptySet() {
        // Empty array is the LLM clearing the multi-select; the converter
        // produces an empty LinkedHashSet which the field accepts as the
        // cleared selection.
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithToValue(label -> new Project("c", label)));

        var result = FormValueConverter.convert(field, json("[]"));

        Assertions.assertEquals(Set.of(), result);
    }

    @Test
    void convert_multiSelectArrayElementNotStringRejected() {
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithToValue(label -> new Project("c", label)));

        var json = json("[42]");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
    }

    @Test
    void convert_multiSelectElementUnknownLabelRejected() {
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT, hintsWithToValue(label -> null));

        var json = json("[\"Apollo\", \"Unknown\"]");
        var ex = Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
        Assertions.assertTrue(ex.getMessage().contains("Apollo"),
                "Rejection reason must name the first unmatched label so "
                        + "the LLM knows which entry to fix; got: "
                        + ex.getMessage());
    }

    @Test
    void convert_multiSelectToValueThrowsRejectedWithCuratedReason() {
        // The application's toValue can throw arbitrary text on any label
        // inside the array. The rejection reason must not echo that text
        // back to the LLM, but it should still name the offending label
        // so the LLM can correlate.
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT, hintsWithToValue(label -> {
                    throw new IllegalStateException(
                            "internal-detail-from-toValue");
                }));

        var json = json("[\"Apollo\"]");
        var ex = Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
        Assertions.assertFalse(
                ex.getMessage().contains("internal-detail-from-toValue"),
                "Curated rejection must not echo the third-party "
                        + "exception text; got: " + ex.getMessage());
        Assertions.assertTrue(ex.getMessage().contains("Apollo"),
                "Curated rejection should still name the offending label; "
                        + "got: " + ex.getMessage());
    }

    @Test
    void convert_fieldValueOptionsOnPrimitiveTypeRoutesThroughToValue() {
        // Even when a field's underlying type is not SINGLE_SELECT (e.g.
        // an Integer-typed text field), registering fieldValueOptions(...)
        // makes the LLM speak in labels. The converter must apply toValue
        // instead of trying to parse the label as an integer.
        var field = wrap(new IntField(), FormFieldType.INTEGER,
                hintsWithToValue(label -> "low".equals(label) ? 1 : 10));

        Assertions.assertEquals(1,
                FormValueConverter.convert(field, json("\"low\"")));
        Assertions.assertEquals(10,
                FormValueConverter.convert(field, json("\"high\"")));
    }

    @Test
    void convert_malformedIsoDateTimeRejected() {
        var field = wrap(new DateTimeField(), FormFieldType.DATE_TIME);

        var json = json("\"not a date-time\"");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
    }

    @Test
    void convert_malformedIsoTimeRejected() {
        var field = wrap(new TimeField(), FormFieldType.TIME);

        var json = json("\"25:99:99\"");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
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

        var json = json("42");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
    }

    // --- helpers ---

    private static FormFieldDescriptor wrap(HasValue<?, ?> field,
            FormFieldType type) {
        return new FormFieldDescriptor("test-id", field, type, null, false,
                false, false);
    }

    private static FormFieldDescriptor wrap(HasValue<?, ?> field,
            FormFieldType type, FormFieldHints hints) {
        return new FormFieldDescriptor("test-id", field, type, hints, false,
                false, false);
    }

    private static FormFieldHints hintsWithToValue(
            Function<String, ?> toValue) {
        var hints = new FormFieldHints();
        hints.valueOptionsToValue = toValue;
        return hints;
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
