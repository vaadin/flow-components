/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.form;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        // Without a fieldValueOptions(...) registration and no eager
        // setItems(...), the field has no option source at all — fail loudly
        // and point the developer at the right API.
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
    void convert_singleSelectWithObservedItems_resolvesLabel() {
        // The converter walks the registration's items, applies the labeler
        // per item, and returns the first whose label matches.
        var apollo = new Project("P-1", "Apollo");
        var field = wrap(new SingleSelectField<Project>(),
                FormFieldType.SINGLE_SELECT,
                hintsWithItems(List.of(apollo), Project::name));

        var result = FormValueConverter.convert(field, json("\"Apollo\""));

        Assertions.assertSame(apollo, result);
    }

    @Test
    void convert_singleSelectNonStringJsonRejected() {
        var apollo = new Project("P-1", "Apollo");
        var field = wrap(new SingleSelectField<Project>(),
                FormFieldType.SINGLE_SELECT,
                hintsWithItems(List.of(apollo), Project::name));

        var json = json("42");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
    }

    @Test
    void convert_singleSelectUnknownLabelRejected() {
        // A label matching no registered item must reject rather than pass
        // null to setValue (which would silently clear the field).
        var apollo = new Project("P-1", "Apollo");
        var field = wrap(new SingleSelectField<Project>(),
                FormFieldType.SINGLE_SELECT,
                hintsWithItems(List.of(apollo), Project::name));

        var ex = Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field,
                        json("\"NotAProject\"")));
        Assertions.assertTrue(ex.getMessage().contains("NotAProject"),
                "Rejection reason must name the unmatched label; got: "
                        + ex.getMessage());
    }

    @Test
    void convert_singleSelectEmptyObservedItemsRejectedWithQueryHint() {
        // Empty observed-items list = query-mode registration that was
        // never queried. The rejection must direct the LLM at
        // query_field_options.
        var field = wrap(new SingleSelectField<Project>(),
                FormFieldType.SINGLE_SELECT,
                hintsWithItems(new ArrayList<>(), Project::name));

        var ex = Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json("\"Apollo\"")));
        Assertions.assertTrue(ex.getMessage().contains("query_field_options"),
                "Empty-cache rejection must direct the LLM to call "
                        + "query_field_options first; got: " + ex.getMessage());
    }

    @Test
    void convert_singleSelectFirstDuplicateLabelWins() {
        // Duplicate labels resolve in registration order; pin so a switch
        // to last-wins is caught.
        var first = new Project("P-1", "Apollo");
        var dup = new Project("P-2", "Apollo");
        var field = wrap(new SingleSelectField<Project>(),
                FormFieldType.SINGLE_SELECT,
                hintsWithItems(List.of(first, dup), Project::name));

        var result = FormValueConverter.convert(field, json("\"Apollo\""));

        Assertions.assertSame(first, result);
    }

    @Test
    void convert_multiSelectBuildsSetFromLabels() {
        // Each chosen label resolves to its registered item; the converter
        // aggregates the matches into a LinkedHashSet.
        var apollo = new Project("APL", "Apollo");
        var vega = new Project("VGA", "Vega");
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithItems(List.of(apollo, vega), Project::name));

        var result = FormValueConverter.convert(field,
                json("[\"Apollo\", \"Vega\"]"));

        Assertions.assertEquals(Set.of(apollo, vega), result);
    }

    @Test
    void convert_multiSelectPreservesLabelOrder() {
        // Iteration order in the resolved set must reflect LLM-supplied
        // label order, not item-registration order.
        var apollo = new Project("APL", "Apollo");
        var vega = new Project("VGA", "Vega");
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithItems(List.of(apollo, vega), Project::name));

        @SuppressWarnings("unchecked")
        var result = (Set<Project>) FormValueConverter.convert(field,
                json("[\"Vega\", \"Apollo\"]"));

        Assertions.assertEquals(List.of(vega, apollo), new ArrayList<>(result));
    }

    @Test
    void convert_multiSelectDeduplicatesRepeatedLabels() {
        // Duplicate labels in the LLM's array collapse via the
        // LinkedHashSet — matches the MultiSelect contract.
        var apollo = new Project("APL", "Apollo");
        var vega = new Project("VGA", "Vega");
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithItems(List.of(apollo, vega), Project::name));

        var result = FormValueConverter.convert(field,
                json("[\"Apollo\", \"Apollo\", \"Vega\"]"));

        Assertions.assertEquals(Set.of(apollo, vega), result);
    }

    @Test
    void convert_multiSelectNonArrayJsonRejected() {
        var apollo = new Project("APL", "Apollo");
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithItems(List.of(apollo), Project::name));

        var json = json("\"Apollo\"");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
    }

    @Test
    void convert_multiSelectJsonNullReturnsFieldEmptyValue() {
        // JSON null on a multi-select clears the selection — the null
        // short-circuit at the top of convert() must win over the
        // multi-select array-shape enforcement.
        var apollo = new Project("APL", "Apollo");
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithItems(List.of(apollo), Project::name));

        var result = FormValueConverter.convert(field, json("null"));

        Assertions.assertEquals(field.field().getEmptyValue(), result);
    }

    @Test
    void convert_multiSelectEmptyArrayBuildsEmptySet() {
        var apollo = new Project("APL", "Apollo");
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithItems(List.of(apollo), Project::name));

        var result = FormValueConverter.convert(field, json("[]"));

        Assertions.assertEquals(Set.of(), result);
    }

    @Test
    void convert_multiSelectArrayElementNotStringRejected() {
        var apollo = new Project("APL", "Apollo");
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithItems(List.of(apollo), Project::name));

        var json = json("[42]");
        Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field, json));
    }

    @Test
    void convert_multiSelectElementUnknownLabelRejected() {
        var apollo = new Project("APL", "Apollo");
        var field = wrap(new MultiSelectField<Project>(),
                FormFieldType.MULTI_SELECT,
                hintsWithItems(List.of(apollo), Project::name));

        var ex = Assertions.assertThrows(RejectedValueException.class,
                () -> FormValueConverter.convert(field,
                        json("[\"Apollo\", \"Unknown\"]")));
        Assertions.assertTrue(ex.getMessage().contains("Unknown"),
                "Rejection reason must name the unmatched label so the "
                        + "LLM knows which entry to fix; got: "
                        + ex.getMessage());
    }

    @Test
    void convert_fieldValueOptionsOnPrimitiveTypeRoutesThroughItems() {
        // Even when a field's underlying type is not SINGLE_SELECT (e.g. an
        // Integer-typed text field), registering fieldValueOptions(...)
        // makes the LLM speak in labels. The converter must resolve via
        // the items list instead of parsing the label as an integer.
        var field = wrap(new IntField(), FormFieldType.INTEGER,
                hintsWithItems(List.of(1, 10), v -> v == 1 ? "low" : "high"));

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

    private static <T> FormFieldHints hintsWithItems(List<T> items,
            Function<T, String> labeler) {
        var hints = new FormFieldHints();
        var map = new LinkedHashMap<String, Object>();
        for (var item : items) {
            map.putIfAbsent(labeler.apply(item), item);
        }
        hints.valueOptionsItems = map;
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Function<Object, String> typed = (Function) labeler;
        hints.itemLabelGenerator = typed;
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
