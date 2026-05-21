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

import static com.vaadin.flow.component.ai.form.FormTestSupport.findTool;
import static com.vaadin.flow.component.ai.form.FormTestSupport.idOf;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ai.form.FormTestFields.BigDecField;
import com.vaadin.flow.component.ai.form.FormTestFields.BoolField;
import com.vaadin.flow.component.ai.form.FormTestFields.DateField;
import com.vaadin.flow.component.ai.form.FormTestFields.DateTimeField;
import com.vaadin.flow.component.ai.form.FormTestFields.DoubleField;
import com.vaadin.flow.component.ai.form.FormTestFields.IntField;
import com.vaadin.flow.component.ai.form.FormTestFields.LabeledStringField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.ai.form.FormTestFields.TimeField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * Tests for {@link FormAIController}'s {@code fill_form} tool. Each test drives
 * the tool the way the LLM would — finds the tool, executes a JSON payload
 * keyed by each field's id, and asserts the field's {@link HasValue#getValue()}
 * reflects the conversion. The tool's response is a plain-text write-summary
 * with {@code Written:} and {@code Rejected:} blocks; tests assert against it
 * where it matters.
 */
class FillFormToolTest {

    @Test
    void fillForm_writesStringValueToTextField() {
        var field = new LabeledStringField();
        field.setLabel("Name");
        var result = fillFormPayload(controllerFor(field),
                payload(field, "\"Acme Corp\""));

        Assertions.assertEquals("Acme Corp", field.getValue());
        Assertions.assertTrue(result.startsWith("Written:"),
                "Result must start with the Written: block, got: " + result);
        Assertions.assertTrue(result.contains("Name: Acme Corp"),
                "Result must reflect the written value, got: " + result);
        Assertions.assertFalse(result.contains("Rejected:"),
                "Successful write must not produce a Rejected: block, got: "
                        + result);
    }

    @Test
    void fillForm_writesNumberToDoubleField() {
        var field = new DoubleField();
        fillFormPayload(controllerFor(field), payload(field, "58.4"));

        Assertions.assertEquals(58.4, field.getValue());
    }

    @Test
    void fillForm_writesIntegerToIntField() {
        var field = new IntField();
        fillFormPayload(controllerFor(field), payload(field, "3"));

        Assertions.assertEquals(3, field.getValue());
    }

    @Test
    void fillForm_acceptsWholeNumberFloatForIntegerField() {
        // LLMs sometimes emit "3.0" for integer fields; the converter
        // recognises whole-number floats and casts them down. Fractional
        // values are rejected (see fillForm_rejectsFractionalValueForInteger).
        var field = new IntField();
        fillFormPayload(controllerFor(field), payload(field, "3.0"));

        Assertions.assertEquals(3, field.getValue());
    }

    @Test
    void fillForm_rejectsFractionalValueForInteger() {
        var field = new IntField();
        field.setValue(42);
        fillFormPayload(controllerFor(field), payload(field, "3.5"));

        Assertions.assertEquals(42, field.getValue(),
                "Fractional JSON for an integer field must be rejected; "
                        + "the field's prior value must remain unchanged");
    }

    @Test
    void fillForm_writesBigDecimalFromStringToBigDecField() {
        // Schema declares BigDecimal as type=string with the BIG_DECIMAL
        // pattern; the LLM emits the value as a JSON string and the
        // converter parses it.
        var field = new BigDecField();
        fillFormPayload(controllerFor(field), payload(field, "\"1234.50\""));

        Assertions.assertEquals(new BigDecimal("1234.50"), field.getValue());
    }

    @Test
    void fillForm_writesBooleanToBoolField() {
        var field = new BoolField();
        fillFormPayload(controllerFor(field), payload(field, "true"));

        Assertions.assertTrue(field.getValue());
    }

    @Test
    void fillForm_writesIsoDateToDateField() {
        var field = new DateField();
        fillFormPayload(controllerFor(field), payload(field, "\"2026-05-19\""));

        Assertions.assertEquals(LocalDate.of(2026, 5, 19), field.getValue());
    }

    @Test
    void fillForm_acceptsZonedDateTimeForDateTimeField() {
        // DateTimePicker is zone-naive; the converter accepts a zoned/offset
        // form via OffsetDateTime.parse and drops the offset. "Z" is only
        // parseable by OffsetDateTime, so this case pins the offset branch
        // end-to-end through the tool.
        var field = new DateTimeField();
        fillFormPayload(controllerFor(field),
                payload(field, "\"2026-05-19T10:30:00Z\""));

        Assertions.assertEquals(LocalDateTime.of(2026, 5, 19, 10, 30, 0),
                field.getValue(),
                "Zoned ISO date-time must round-trip into a naive local "
                        + "date-time");
    }

    @Test
    void fillForm_acceptsNaiveDateTimeForDateTimeField() {
        // Naive ISO date-time without offset trips OffsetDateTime.parse,
        // is caught, and falls through to LocalDateTime.parse. This pins
        // the LocalDateTime fallback branch end-to-end through the tool,
        // complementing the zoned test above.
        var field = new DateTimeField();
        fillFormPayload(controllerFor(field),
                payload(field, "\"2026-05-19T10:30:00\""));

        Assertions.assertEquals(LocalDateTime.of(2026, 5, 19, 10, 30, 0),
                field.getValue(),
                "Naive ISO date-time must parse via the LocalDateTime "
                        + "fallback branch");
    }

    @Test
    void fillForm_writesIsoTimeToTimeField() {
        var field = new TimeField();
        fillFormPayload(controllerFor(field), payload(field, "\"09:15:00\""));

        Assertions.assertEquals(LocalTime.of(9, 15), field.getValue());
    }

    @Test
    void fillForm_jsonNullClearsStringFieldToEmptyValue() {
        // null clears a field. For a TestField the empty value is "", not
        // null — the converter routes through getEmptyValue().
        var field = new TestField();
        field.setValue("previous");
        fillFormPayload(controllerFor(field), payload(field, "null"));

        Assertions.assertEquals("", field.getValue());
    }

    @Test
    void fillForm_typeMismatchLeavesFieldOnPreviousValue() {
        // Payload sends a JSON string to a NUMBER field. The converter
        // throws RejectedValueException; the controller catches it, logs at
        // DEBUG, and leaves the field on its prior value rather than
        // aborting the fill.
        var field = new DoubleField();
        field.setValue(42.0);
        fillFormPayload(controllerFor(field),
                payload(field, "\"not a number\""));

        Assertions.assertEquals(42.0, field.getValue(),
                "Type-mismatched payload must leave the field unchanged");
    }

    @Test
    void fillForm_unknownFieldIdInPayloadIsIgnored() {
        // The LLM may hallucinate a field id (stale, mistyped, or fabricated).
        // The tool must not throw — it iterates over visibleFields() and
        // skips ids that aren't in the payload, and ignores keys in the
        // payload that don't match any visible field.
        var field = new TestField();
        field.setValue("untouched");
        var controller = controllerFor(field);
        var args = JacksonUtils.createObjectNode();
        args.put("not-a-real-id", "garbage");

        Assertions.assertDoesNotThrow(() -> fillFormPayload(controller, args));
        Assertions.assertEquals("untouched", field.getValue());
    }

    @Test
    void fillForm_partialSuccessWritesValidFieldAndSkipsBadOne() {
        // Mixed payload: name converts cleanly, amount fails. The valid
        // field must land; the failed field must stay at its prior value.
        var name = new TestField();
        var amount = new DoubleField();
        amount.setValue(99.0);
        var controller = controllerFor(name, amount);

        var args = JacksonUtils.createObjectNode();
        args.put(idOf(name), "Acme");
        args.put(idOf(amount), "not a number");
        fillFormPayload(controller, args);

        Assertions.assertEquals("Acme", name.getValue());
        Assertions.assertEquals(99.0, amount.getValue(),
                "Bad field must stay on its prior value while the good "
                        + "field still gets written");
    }

    @Test
    void fillForm_ignoredFieldIsNotWrittenAndAbsentFromSummary() {
        // Ignored fields don't appear in get_form_state, so the LLM never
        // learns the id. Even if the LLM forges the id into the payload,
        // visibleFields() filters it out before any write attempt — and
        // the field is absent from the write-summary either way.
        var visible = new LabeledStringField();
        visible.setLabel("Visible");
        var secret = new LabeledStringField();
        secret.setLabel("Secret");
        secret.setValue("original");
        var controller = controllerFor(visible, secret);
        controller.ignore(secret);

        var args = JacksonUtils.createObjectNode();
        args.put(idOf(visible), "set");
        args.put(idOf(secret), "leaked");
        var result = fillFormPayload(controller, args);

        Assertions.assertEquals("set", visible.getValue());
        Assertions.assertEquals("original", secret.getValue(),
                "Ignored field must never be written, even when its id "
                        + "appears in the payload");
        // The forged id must not surface in either summary block: not in
        // Written: (it wasn't written) and not in Rejected: (we don't even
        // acknowledge that the LLM tried to address it). Pin both the id
        // and the display label.
        Assertions.assertFalse(result.contains(idOf(secret)),
                "Ignored field's id must not appear in the write-summary, "
                        + "got: " + result);
        Assertions.assertFalse(result.contains("Secret"),
                "Ignored field's display label must not appear in the "
                        + "write-summary, got: " + result);
        Assertions.assertFalse(result.contains("leaked"),
                "The forged payload value targeting the ignored field "
                        + "must not be echoed back, got: " + result);
    }

    @Test
    void fillForm_passwordFieldIsNeverWrittenEvenWithPayload() {
        // PasswordField classifies as UNSUPPORTED so visibleFields() never
        // includes it. Same protection as ignore(): get_form_state doesn't
        // surface the id, and even if the LLM forges it into the payload,
        // visibleFields() filters it out before any write attempt.
        var password = new PasswordField("Password");
        password.setValue("untouched");
        var controller = controllerFor(password);

        // onRequestStart already stamped an id (attachIds walks every
        // discovered field, UNSUPPORTED included). Forge it into the
        // payload to verify the controller still skips it.
        var args = JacksonUtils.createObjectNode();
        args.put(idOf(password), "leaked");
        var result = fillFormPayload(controller, args);

        Assertions.assertEquals("untouched", password.getValue());
        // The PasswordField's id, label, and the forged value must not
        // appear in the write-summary — the LLM must not see the field's
        // existence acknowledged via either block.
        Assertions.assertFalse(result.contains(idOf(password)),
                "PasswordField's id must not appear in the write-summary, "
                        + "got: " + result);
        Assertions.assertFalse(result.contains("Password"),
                "PasswordField's label must not appear in the "
                        + "write-summary, got: " + result);
        Assertions.assertFalse(result.contains("leaked"),
                "The forged payload value targeting the PasswordField "
                        + "must not be echoed back, got: " + result);
    }

    @Test
    void fillForm_writtenBlockListsAllWrittenFieldsWithValues() {
        var name = new LabeledStringField();
        name.setLabel("Name");
        var amount = new DoubleField();
        var controller = controllerFor(name, amount);

        var args = JacksonUtils.createObjectNode();
        args.put(idOf(name), "Acme");
        args.put(idOf(amount), 58.4);
        var result = fillFormPayload(controller, args);

        Assertions.assertTrue(result.contains("Name: Acme"),
                "Written: block must list the name field, got: " + result);
        Assertions.assertTrue(result.contains("58.4"),
                "Written: block must list the amount value, got: " + result);
    }

    @Test
    void fillForm_writeSummaryDoesNotLeakUntouchedFields() {
        // The write-summary lists only the fields the LLM attempted to
        // write. Untouched fields — including ones that already have a
        // user-typed value — must not appear so the tool result doesn't
        // leak data the LLM never asked about.
        var written = new LabeledStringField();
        written.setLabel("Name");
        var untouched = new LabeledStringField();
        untouched.setLabel("Notes");
        untouched.setValue("user-typed secret");
        var controller = controllerFor(written, untouched);

        var args = JacksonUtils.createObjectNode();
        args.put(idOf(written), "Acme");
        var result = fillFormPayload(controller, args);

        Assertions.assertTrue(result.contains("Name: Acme"),
                "Attempted field must appear in Written:, got: " + result);
        Assertions.assertFalse(result.contains("Notes"),
                "Untouched field must not appear in the write-summary, got: "
                        + result);
        Assertions.assertFalse(result.contains("user-typed secret"),
                "Untouched field's value must never appear in the "
                        + "write-summary, got: " + result);
    }

    @Test
    void fillForm_emptyPayloadReportsNoChanges() {
        // When the LLM sends an empty payload, the tool didn't attempt any
        // write. The response signals that explicitly so the LLM doesn't
        // confuse a no-op with a missing response.
        var name = new LabeledStringField();
        name.setLabel("Name");
        var controller = controllerFor(name);

        var result = fillFormPayload(controller,
                JacksonUtils.createObjectNode());

        Assertions.assertEquals("No changes.", result.trim(),
                "Empty payload must produce the No changes. sentinel, got: "
                        + result);
    }

    @Test
    void fillForm_emptiedFieldStillAppearsInWrittenBlock() {
        // JSON null clears a field — that's still a write the LLM made,
        // and the write-summary must report it (as <empty>) so the LLM
        // can confirm the clear landed.
        var name = new LabeledStringField();
        name.setLabel("Name");
        name.setValue("previous");
        var controller = controllerFor(name);

        var result = fillFormPayload(controller, payload(name, "null"));

        Assertions.assertEquals("", name.getValue());
        Assertions.assertTrue(result.contains("Name: <empty>"),
                "A cleared field must appear in Written: as <empty>, got: "
                        + result);
    }

    @Test
    void fillForm_rejectedBlockListsConversionFailureWithReason() {
        // A type-mismatch lands the field in Rejected: with the converter's
        // reason text verbatim — the LLM can use the message to fix its
        // next attempt.
        var amount = new DoubleField();
        amount.setValue(42.0);
        var controller = controllerFor(amount);

        var result = fillFormPayload(controller,
                payload(amount, "\"not a number\""));

        Assertions.assertEquals(42.0, amount.getValue(),
                "Rejected payload must leave the field unchanged");
        Assertions.assertTrue(result.contains("Rejected:"),
                "Rejected: block must appear, got: " + result);
        Assertions.assertTrue(result.contains("Expected number"),
                "Rejected: line must surface the converter's reason, got: "
                        + result);
    }

    @Test
    void fillForm_partialSuccessProducesBothBlocks() {
        // Mixed payload: one field writes cleanly, another is rejected.
        // The summary contains both Written: and Rejected: blocks, in
        // that order.
        var name = new LabeledStringField();
        name.setLabel("Name");
        var amount = new LabeledStringField();
        amount.setLabel("Amount");
        // Treat amount as a DoubleField for typing — quickest: use a real
        // DoubleField with a separately-set label is not supported by the
        // stub, so use DoubleField directly.
        var doubleAmount = new DoubleField();
        var controller = controllerFor(name, doubleAmount);

        var args = JacksonUtils.createObjectNode();
        args.put(idOf(name), "Acme");
        args.put(idOf(doubleAmount), "not a number");
        var result = fillFormPayload(controller, args);

        Assertions.assertTrue(result.startsWith("Written:"),
                "Written: block must come before Rejected:, got: " + result);
        Assertions.assertTrue(result.contains("Name: Acme"),
                "Successful write must be in Written:, got: " + result);
        Assertions.assertTrue(result.contains("Rejected:"),
                "Rejected: block must follow, got: " + result);
        Assertions.assertTrue(result.contains("Expected number"),
                "Rejected: must include the failure reason, got: " + result);
    }

    @Test
    void fillForm_setValueThrowAppearsInRejectedBlockWithoutLeakingMessage() {
        // A user-supplied HasValue can throw any RuntimeException from
        // setValue with arbitrary message text. The summary must report
        // the rejection but must not echo the exception message
        // verbatim — that's where a leaky third-party message would land.
        var throwing = new ThrowingSetValueField();
        var controller = controllerFor(throwing);

        var result = fillFormPayload(controller,
                payload(throwing, "\"anything\""));

        Assertions.assertTrue(result.contains("Rejected:"),
                "setValue failure must land in Rejected:, got: " + result);
        Assertions.assertFalse(
                result.contains("setValue rejected the " + "converted value: "),
                "Generic line must not concatenate the raw exception "
                        + "message");
        Assertions.assertFalse(result.contains("internal-detail-from-setvalue"),
                "Raw exception message must not leak, got: " + result);
    }

    @Test
    void fillForm_schemaIsStaticAndOpenKeyed() {
        // The schema does NOT enumerate per-field properties. It's open-
        // keyed so the tool definition stays byte-identical across the
        // session — LLM providers that cache prompt prefixes (system
        // prompt + tool defs) hit the cache on every subsequent prompt.
        // Per-field shape comes from get_form_state on each turn. The
        // field map lives under a single "values" property so future
        // top-level params (e.g. dryRun) can be added without breaking
        // the field-map shape.
        var name = new TestField();
        var controller = controllerFor(name);
        var tool = findTool(controller.getTools(), "fill_form");

        var before = tool.getParametersSchema();
        name.setValue("Acme");
        var after = tool.getParametersSchema();

        Assertions.assertEquals(before, after,
                "Schema must be byte-identical across calls so providers "
                        + "can cache the tool definition. Before: " + before
                        + " / after: " + after);
        Assertions.assertFalse(before.contains(idOf(name)),
                "Schema must not enumerate field ids — the LLM discovers "
                        + "them via get_form_state. Got: " + before);
        Assertions.assertFalse(before.contains("Acme"),
                "Schema must not embed current field values, which would "
                        + "force cache misses every turn. Got: " + before);
        Assertions.assertTrue(before.contains("\"values\""),
                "Schema must declare a top-level 'values' wrapper so "
                        + "future top-level params can be added without "
                        + "breaking the field-map shape. Got: " + before);
        // additionalProperties on the values object tells the LLM that any
        // keys it sends inside values are accepted; the orchestrator routes
        // them per id at execute() time.
        Assertions.assertTrue(before.contains("\"additionalProperties\""),
                "Schema must declare additionalProperties on the values "
                        + "wrapper so the LLM knows arbitrary field ids "
                        + "are accepted. Got: " + before);
    }

    @Test
    void fillForm_returnsErrorForNullArguments() {
        var controller = controllerFor(new TestField());
        var result = findTool(controller.getTools(), "fill_form").execute(null);

        Assertions.assertTrue(result.startsWith("Error"),
                "Null arguments must produce an error result, got: " + result);
    }

    @Test
    void fillForm_returnsErrorWhenValuesKeyMissing() {
        // The static schema wraps field-id → value pairs under a "values"
        // key. If the LLM emits a top-level object without that wrapper,
        // execute() must surface a clear error so the LLM can correct on
        // the next turn rather than silently no-oping.
        var controller = controllerFor(new TestField());
        var args = JacksonUtils.createObjectNode();
        args.put("not-values", "x");
        var result = findTool(controller.getTools(), "fill_form").execute(args);

        Assertions.assertTrue(result.startsWith("Error"),
                "Missing 'values' key must produce an error result, got: "
                        + result);
        Assertions.assertTrue(result.contains("values"),
                "Error must name the 'values' key so the LLM can correct, "
                        + "got: " + result);
    }

    @Test
    void fillForm_returnsErrorWhenValuesIsNotObject() {
        // Pins the second branch of the values-shape guard: 'values' is
        // present but not a JSON object (e.g. an array or scalar).
        var controller = controllerFor(new TestField());
        var args = JacksonUtils.createObjectNode();
        args.put("values", "not-an-object");
        var result = findTool(controller.getTools(), "fill_form").execute(args);

        Assertions.assertTrue(result.startsWith("Error"),
                "'values' that isn't an object must produce an error result, "
                        + "got: " + result);
    }

    @Test
    void fillForm_returnsErrorForNonObjectArguments() {
        var controller = controllerFor(new TestField());
        JsonNode notAnObject;
        try {
            notAnObject = JacksonUtils.getMapper()
                    .readTree("\"not an object\"");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        var result = findTool(controller.getTools(), "fill_form")
                .execute(notAnObject);

        Assertions.assertTrue(result.startsWith("Error"),
                "Non-object arguments must produce an error result, got: "
                        + result);
    }

    @Test
    void fillForm_toolIsExposedByCreateAll() {
        // The fill_form tool must be discoverable by name in the
        // controller's tool list — the rest of these tests rely on
        // findTool(...) finding it, but pin the contract directly here so
        // a regression that drops fillForm from createAll is reported as a
        // clear failure rather than a NoSuchElementException downstream.
        var controller = controllerFor(new TestField());

        Assertions.assertTrue(
                controller.getTools().stream()
                        .anyMatch(t -> "fill_form".equals(t.getName())),
                "fill_form must be exposed via FormAIController.getTools()");
    }

    @Test
    void fillForm_setValueThrowingLeavesOtherFieldsIntact() {
        // ThrowingField's setValue throws RuntimeException. The controller
        // catches the throw, logs at DEBUG, and continues with the next
        // field. Pins the catch(RuntimeException) branch in applyValue —
        // without it, one broken field would abort the entire fill.
        var throwing = new ThrowingSetValueField();
        var ok = new TestField();
        var controller = controllerFor(throwing, ok);

        var args = JacksonUtils.createObjectNode();
        args.put(idOf(throwing), "doesn't matter");
        args.put(idOf(ok), "landed");
        fillFormPayload(controller, args);

        Assertions.assertEquals("landed", ok.getValue(),
                "A throw on one field's setValue must not prevent the "
                        + "next field's write");
    }

    @Test
    void fillForm_writeSummaryUsesFieldLabelWhenAvailable() {
        // Line-label precedence: HasLabel#getLabel() > hints.description >
        // opaque id. With a label set, the label wins.
        var labeled = new LabeledStringField();
        labeled.setLabel("Customer Name");
        var controller = controllerFor(labeled);

        var result = fillFormPayload(controller, payload(labeled, "\"Acme\""));

        Assertions.assertTrue(result.contains("Customer Name: Acme"),
                "When the field has a label, the Written: line must use "
                        + "it, got: " + result);
    }

    @Test
    void fillForm_writeSummaryFallsBackToDescriptionWhenNoLabel() {
        // No label → hints.description (set via describe()) wins.
        var unlabeled = new TestField();
        var controller = controllerFor(unlabeled);
        controller.describe(unlabeled, "Customer ref");

        var result = fillFormPayload(controller,
                payload(unlabeled, "\"Acme\""));

        Assertions.assertTrue(result.contains("Customer ref: Acme"),
                "Without a label, the description hint must be used as "
                        + "the line label, got: " + result);
    }

    @Test
    void fillForm_writeSummaryFallsBackToIdWhenNoLabelAndNoDescription() {
        // No label, no describe() → last-resort fallback is the opaque
        // UUID. Pins the final branch of displayName.
        var bare = new TestField();
        var controller = controllerFor(bare);

        var result = fillFormPayload(controller, payload(bare, "\"Acme\""));

        Assertions.assertTrue(result.contains(idOf(bare) + ": Acme"),
                "Without a label or description, the UUID must surface "
                        + "as the line label so the LLM can still "
                        + "correlate, got: " + result);
    }

    /**
     * Field whose {@link #setValue} always throws — used to pin the
     * controller's catch(RuntimeException) branch around setValue.
     */
    @com.vaadin.flow.component.Tag("throwing-setvalue-field")
    private static class ThrowingSetValueField extends
            com.vaadin.flow.component.AbstractField<ThrowingSetValueField, String> {
        ThrowingSetValueField() {
            super("");
        }

        @Override
        public void setValue(String value) {
            throw new RuntimeException("internal-detail-from-setvalue");
        }

        @Override
        protected void setPresentationValue(String value) {
            // not exercised
        }
    }

    // --- helpers ---

    private static FormAIController controllerFor(Component... fields) {
        var controller = new FormAIController(new Div(fields));
        // Drive onRequestStart() so each discovered field has its UUID id
        // stamped — payload helpers use idOf() to look the id up.
        controller.onRequestStart();
        return controller;
    }

    private static JsonNode payload(HasValue<?, ?> field, String jsonValue) {
        return JacksonUtils
                .readTree("{\"" + idOf(field) + "\":" + jsonValue + "}");
    }

    /**
     * Executes the {@code fill_form} tool with a field-id → value map. Wraps
     * the map in the {@code values} key the tool's parameter schema requires
     * so individual tests can stay focused on the field map.
     */
    private static String fillFormPayload(FormAIController controller,
            JsonNode fieldMap) {
        var wrapped = JacksonUtils.createObjectNode();
        wrapped.set("values", fieldMap);
        return findTool(controller.getTools(), "fill_form").execute(wrapped);
    }
}
