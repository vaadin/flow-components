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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ai.form.FormTestFields.BigDecField;
import com.vaadin.flow.component.ai.form.FormTestFields.BoolField;
import com.vaadin.flow.component.ai.form.FormTestFields.DateField;
import com.vaadin.flow.component.ai.form.FormTestFields.DateTimeField;
import com.vaadin.flow.component.ai.form.FormTestFields.DoubleField;
import com.vaadin.flow.component.ai.form.FormTestFields.IntField;
import com.vaadin.flow.component.ai.form.FormTestFields.LabeledStringField;
import com.vaadin.flow.component.ai.form.FormTestFields.MultiSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.SingleSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.ai.form.FormTestFields.TimeField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * Tests for {@link FormAIController}'s {@code fill_form} tool. The tool returns
 * a JSON object the LLM reads back —
 * {@code {"success": <bool>, "written": [field-ids], "rejected": [{"id":
 * <field-id>, "reason": "..."}]}}. Each test drives the tool the way the LLM
 * would and asserts on the parsed response (success flag, per-id attribution)
 * plus the field state. Tests that pin error strings outside the JSON happy
 * path (e.g. malformed {@code arguments}) assert against the raw response
 * string directly.
 */
class FillFormToolTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void fillForm_writesStringValueToTextField() {
        var field = new LabeledStringField();
        field.setLabel("Name");
        var result = fillFormResult(controllerFor(field),
                payload(field, "\"Acme Corp\""));

        Assertions.assertEquals("Acme Corp", field.getValue());
        Assertions.assertTrue(success(result), "Result: " + result);
        Assertions.assertEquals(List.of(idOf(field)), writtenIds(result));
        Assertions.assertTrue(rejectedIds(result).isEmpty(),
                "No rejections expected, got: " + result);
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
    void fillForm_unknownFieldIdInPayloadProducesRejectedEntry() {
        // The LLM may send a field id that doesn't match any visible field
        // (stale from a prior get_form_state, mistyped, ignored field
        // forgery). The tool must reject that entry with a curated reason
        // pointing back at get_form_state — silently dropping the entry
        // would leave the LLM thinking the write succeeded.
        var field = new TestField();
        field.setValue("untouched");
        var controller = controllerFor(field);
        var args = JacksonUtils.createObjectNode();
        args.put("not-a-real-id", "garbage");

        var result = fillFormResult(controller, args);

        Assertions.assertEquals("untouched", field.getValue(),
                "Unknown id must not move any field");
        Assertions.assertFalse(success(result),
                "Unknown id must mark the turn unsuccessful, got: " + result);
        Assertions.assertTrue(writtenIds(result).isEmpty());
        Assertions.assertEquals(List.of("not-a-real-id"), rejectedIds(result));
        var reason = rejectionReason(result, "not-a-real-id");
        Assertions.assertTrue(reason.contains("not-a-real-id"),
                "Reason must name the unknown id; got: " + reason);
        Assertions.assertTrue(reason.contains("get_form_state"),
                "Reason must direct the LLM to refresh via "
                        + "get_form_state; got: " + reason);
    }

    @Test
    void fillForm_partialSuccessLeavesBadFieldOnPriorValue() {
        // Mixed payload: name converts cleanly, amount fails. The valid
        // field must land; the failed field must stay on its prior value.
        // The JSON response shape is covered separately by
        // fillForm_partialSuccessHasWrittenAndRejectedSideBySide.
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
    void fillForm_ignoredFieldIsNeverWrittenAndDoesNotLeakLabelOrValue() {
        // Ignored fields don't appear in get_form_state, so the LLM never
        // learns the id. If the LLM somehow forges the id (forged or
        // carried over from a state it should no longer trust), the
        // controller treats it the same as any unknown id: the rejected
        // entry uses the generic "Unknown field id" wording, never
        // anything that would acknowledge the field's existence (e.g.
        // "ignored" or the label).
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
        var raw = fillFormPayload(controller, args);
        var result = parseResult(raw);

        Assertions.assertEquals("set", visible.getValue());
        Assertions.assertEquals("original", secret.getValue(),
                "Ignored field must never be written, even when its id "
                        + "appears in the payload");
        Assertions.assertEquals(List.of(idOf(secret)), rejectedIds(result),
                "Forged id must surface in rejected as if it were unknown");
        var reason = rejectionReason(result, idOf(secret));
        Assertions.assertTrue(reason.contains("Unknown field id"),
                "Reason must use the generic unknown-id wording, not a "
                        + "special 'ignored field' message that would leak "
                        + "the field's existence; got: " + reason);
        Assertions.assertFalse(raw.contains("Secret"),
                "Ignored field's label must not appear in the response, "
                        + "got: " + raw);
        Assertions.assertFalse(raw.contains("leaked"),
                "The forged payload value targeting the ignored field "
                        + "must not be echoed back, got: " + raw);
    }

    @Test
    void fillForm_passwordFieldIsNeverWrittenAndDoesNotLeakLabelOrValue() {
        // PasswordField classifies as UNSUPPORTED so visibleFields() never
        // includes it. Same protection as ignore(): forged ids surface as
        // generic "Unknown field id" rejections — the label, value, and
        // even the UNSUPPORTED classification stay out of the response.
        var password = new PasswordField("Password");
        password.setValue("untouched");
        var controller = controllerFor(password);

        // onRequest already stamped an id (attachIds walks every
        // discovered field, UNSUPPORTED included). Forge it into the
        // payload to verify the controller still skips it.
        var args = JacksonUtils.createObjectNode();
        args.put(idOf(password), "leaked");
        var raw = fillFormPayload(controller, args);
        var result = parseResult(raw);

        Assertions.assertEquals("untouched", password.getValue());
        Assertions.assertEquals(List.of(idOf(password)), rejectedIds(result));
        var reason = rejectionReason(result, idOf(password));
        Assertions.assertTrue(reason.contains("Unknown field id"),
                "Reason must use the generic unknown-id wording, not a "
                        + "special 'unsupported' message; got: " + reason);
        Assertions.assertFalse(raw.contains("Password"),
                "PasswordField's label must not appear in the response, "
                        + "got: " + raw);
        Assertions.assertFalse(raw.contains("leaked"),
                "The forged payload value targeting the PasswordField "
                        + "must not be echoed back, got: " + raw);
    }

    @Test
    void fillForm_allWrittenIdsAppearInWrittenArray() {
        var name = new LabeledStringField();
        name.setLabel("Name");
        var amount = new DoubleField();
        var controller = controllerFor(name, amount);

        var args = JacksonUtils.createObjectNode();
        args.put(idOf(name), "Acme");
        args.put(idOf(amount), 58.4);
        var result = fillFormResult(controller, args);

        Assertions.assertTrue(success(result), "Result: " + result);
        Assertions.assertTrue(writtenIds(result).contains(idOf(name)),
                "Written must include the name id; got: " + result);
        Assertions.assertTrue(writtenIds(result).contains(idOf(amount)),
                "Written must include the amount id; got: " + result);
        Assertions.assertTrue(rejectedIds(result).isEmpty());
    }

    @Test
    void fillForm_writeResultDoesNotLeakUntouchedFields() {
        // The response surfaces only ids the LLM attempted to write.
        // Untouched fields — including ones that hold user-typed values —
        // must not appear so the response doesn't disclose data the LLM
        // never asked about.
        var written = new LabeledStringField();
        written.setLabel("Name");
        var untouched = new LabeledStringField();
        untouched.setLabel("Notes");
        untouched.setValue("user-typed secret");
        var controller = controllerFor(written, untouched);

        var args = JacksonUtils.createObjectNode();
        args.put(idOf(written), "Acme");
        var raw = fillFormPayload(controller, args);
        var result = parseResult(raw);

        Assertions.assertEquals(List.of(idOf(written)), writtenIds(result));
        Assertions.assertTrue(rejectedIds(result).isEmpty());
        Assertions.assertFalse(raw.contains(idOf(untouched)),
                "Untouched field's id must not appear in the response, "
                        + "got: " + raw);
        Assertions.assertFalse(raw.contains("Notes"),
                "Untouched field's label must not appear, got: " + raw);
        Assertions.assertFalse(raw.contains("user-typed secret"),
                "Untouched field's value must not appear, got: " + raw);
    }

    @Test
    void fillForm_emptyPayloadReturnsSuccessWithEmptyArrays() {
        // No payload means no writes attempted — success is true (no
        // failures), both arrays are empty. The LLM uses the JSON shape
        // to confirm it didn't accidentally fire a malformed turn.
        var name = new LabeledStringField();
        name.setLabel("Name");
        var controller = controllerFor(name);

        var result = fillFormResult(controller,
                JacksonUtils.createObjectNode());

        Assertions.assertTrue(success(result),
                "Empty payload is a no-op, not a failure; got: " + result);
        Assertions.assertTrue(writtenIds(result).isEmpty());
        Assertions.assertTrue(rejectedIds(result).isEmpty());
    }

    @Test
    void fillForm_clearingFieldStillCountsAsWrite() {
        // JSON null clears a field — that's still a write the LLM made,
        // and the response must report it so the LLM can confirm the
        // clear landed. The field's value is now empty.
        var name = new LabeledStringField();
        name.setLabel("Name");
        name.setValue("previous");
        var controller = controllerFor(name);

        var result = fillFormResult(controller, payload(name, "null"));

        Assertions.assertEquals("", name.getValue());
        Assertions.assertTrue(success(result));
        Assertions.assertEquals(List.of(idOf(name)), writtenIds(result));
    }

    @Test
    void fillForm_conversionFailureSurfacesIdAndReason() {
        // A type-mismatch lands the field in 'rejected' with the
        // converter's reason text verbatim — the LLM can use the message
        // to fix its next attempt, and the entry is keyed by the field's
        // opaque id so the LLM can match it back to its own payload.
        var amount = new DoubleField();
        amount.setValue(42.0);
        var controller = controllerFor(amount);

        var result = fillFormResult(controller,
                payload(amount, "\"not a number\""));

        Assertions.assertEquals(42.0, amount.getValue(),
                "Rejected payload must leave the field unchanged");
        Assertions.assertFalse(success(result));
        Assertions.assertTrue(writtenIds(result).isEmpty());
        Assertions.assertEquals(List.of(idOf(amount)), rejectedIds(result));
        Assertions.assertTrue(
                rejectionReason(result, idOf(amount))
                        .contains("Expected number"),
                "Reason must surface the converter's text; got: "
                        + rejectionReason(result, idOf(amount)));
    }

    @Test
    void fillForm_partialSuccessHasWrittenAndRejectedSideBySide() {
        // Mixed payload: one writes cleanly, another is rejected.
        // success=false because at least one entry was rejected; written
        // and rejected each carry their own ids.
        var name = new TestField();
        var amount = new DoubleField();
        var controller = controllerFor(name, amount);

        var args = JacksonUtils.createObjectNode();
        args.put(idOf(name), "Acme");
        args.put(idOf(amount), "not a number");
        var result = fillFormResult(controller, args);

        Assertions.assertFalse(success(result), "Result: " + result);
        Assertions.assertEquals(List.of(idOf(name)), writtenIds(result));
        Assertions.assertEquals(List.of(idOf(amount)), rejectedIds(result));
        Assertions.assertTrue(rejectionReason(result, idOf(amount))
                .contains("Expected number"));
    }

    @Test
    void fillForm_setValueThrowSurfacesRejectionWithoutLeakingMessage() {
        // A user-supplied HasValue can throw any RuntimeException from
        // setValue with arbitrary message text. The response must report
        // the rejection but the reason must NOT echo the third-party
        // exception text — that's where leaks would land.
        var throwing = new ThrowingSetValueField();
        var controller = controllerFor(throwing);

        var raw = fillFormPayload(controller,
                payload(throwing, "\"anything\""));
        var result = parseResult(raw);

        Assertions.assertEquals(List.of(idOf(throwing)), rejectedIds(result));
        var reason = rejectionReason(result, idOf(throwing));
        Assertions.assertNotNull(reason);
        Assertions.assertFalse(raw.contains("internal-detail-from-setvalue"),
                "Raw exception message must not leak anywhere in the "
                        + "response; got: " + raw);
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
    void fillForm_callbackThrowingToolException_surfacesMessageVerbatim() {
        // ToolException is the curated, LLM-facing failure channel — its
        // message is allowed to leak into the response (callers must scrub
        // it themselves before throwing). Pins the ToolException catch in
        // FormAITools.fillForm.execute().
        var tool = FormAITools
                .fillForm(throwingCallbacks(new FormAITools.ToolException(
                        "field 'foo' is no longer addressable")));

        var result = tool.execute(wrappedValues());

        Assertions.assertEquals("Error: field 'foo' is no longer addressable",
                result);
    }

    @Test
    void fillForm_callbackThrowingRuntimeException_returnsGenericError() {
        // Anything that isn't a ToolException is an uncontrolled failure —
        // the catch must return a generic message so internal exception
        // text (potentially carrying PII or stack-trace detail) does not
        // leak to the LLM.
        var tool = FormAITools.fillForm(throwingCallbacks(
                new RuntimeException("internal-detail-that-must-not-leak")));

        var result = tool.execute(wrappedValues());

        Assertions.assertEquals("Error: fill failed.", result);
        Assertions.assertFalse(
                result.contains("internal-detail-that-must-not-leak"),
                "Generic catch must not echo the raw exception message; "
                        + "got: " + result);
    }

    @Test
    void fillForm_singleSelect_writesResolvedValueViaValueOptionsToValue() {
        // The LLM speaks in labels for SINGLE_SELECT fields with
        // valueOptions registered. The tool must apply toValue so the
        // field gets the domain instance, not the raw label string.
        var field = new SingleSelectField<Project>();
        var projects = Map.of("Apollo", new Project("P-1", "Apollo"));
        var controller = newController(field);
        controller.valueOptions(field, (filter, limit) -> List.of("Apollo"),
                projects::get);
        controller.onRequest();

        var result = fillFormResult(controller, payload(field, "\"Apollo\""));

        Assertions.assertEquals(new Project("P-1", "Apollo"), field.getValue(),
                "Field must receive the resolved domain object, not the "
                        + "raw label string");
        Assertions.assertTrue(success(result));
        Assertions.assertEquals(List.of(idOf(field)), writtenIds(result));
    }

    @Test
    void fillForm_singleSelect_withoutValueOptionsIsRejectedWithHint() {
        // SINGLE_SELECT without a valueOptions registration means the
        // LLM has no labels to pick and the converter has no toValue to
        // resolve. The fill must fail loudly with a reason that points
        // at the missing registration so the developer can fix it.
        var field = new SingleSelectField<Project>();
        var controller = controllerFor(field);

        var result = fillFormResult(controller, payload(field, "\"Apollo\""));

        Assertions.assertNull(field.getValue());
        Assertions.assertFalse(success(result));
        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        Assertions.assertTrue(
                rejectionReason(result, idOf(field)).contains("valueOptions"),
                "Reason must point at the missing valueOptions "
                        + "registration; got: "
                        + rejectionReason(result, idOf(field)));
    }

    @Test
    void fillForm_singleSelect_unknownLabelIsRejected() {
        // toValue returning null is the agreed signal for "label doesn't
        // match any option". The orchestrator must reject rather than
        // pass null to setValue (which would silently clear the field).
        var field = new SingleSelectField<Project>();
        var controller = newController(field);
        controller.valueOptions(field, (filter, limit) -> List.of("Apollo"),
                label -> null);
        controller.onRequest();

        var result = fillFormResult(controller, payload(field, "\"Unknown\""));

        Assertions.assertNull(field.getValue(),
                "Unknown label must not silently clear the field");
        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        Assertions.assertTrue(
                rejectionReason(result, idOf(field)).contains("Unknown"),
                "Reason must name the unmatched label; got: "
                        + rejectionReason(result, idOf(field)));
    }

    @Test
    void fillForm_multiSelect_writesResolvedSetViaValueOptionsWithSetWrap() {
        // The typed valueOptions signature forces toValue to return the
        // field's value type (Set<Project> for MultiSelectField<Project>).
        // The application wraps each per-label lookup in Set.of(...); the
        // orchestrator flat-unions the per-label sets into the final
        // selection.
        var field = new MultiSelectField<Project>();
        var controller = newController(field);
        controller.valueOptions(field,
                (filter, limit) -> List.of("Apollo", "Vega"),
                label -> Set.of(new Project(label, label)));
        controller.onRequest();

        var result = fillFormResult(controller,
                payload(field, "[\"Apollo\", \"Vega\"]"));

        Assertions.assertEquals(Set.of(new Project("Apollo", "Apollo"),
                new Project("Vega", "Vega")), field.getValue());
        Assertions.assertTrue(success(result));
        Assertions.assertEquals(List.of(idOf(field)), writtenIds(result));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void fillForm_multiSelect_writesResolvedSetViaValueOptionsWithRawCast() {
        // Alternative caller pattern: drop the Set wrap by raw-casting the
        // field argument so toValue is Function<String, Item>. The
        // orchestrator detects the non-Collection return at runtime and
        // adds each resolved item directly to the aggregate set, so both
        // caller patterns produce the same final value on the field.
        var field = new MultiSelectField<Project>();
        var controller = newController(field);
        controller.valueOptions((HasValue) field,
                (filter, limit) -> List.of("Apollo", "Vega"),
                label -> new Project((String) label, (String) label));
        controller.onRequest();

        var result = fillFormResult(controller,
                payload(field, "[\"Apollo\", \"Vega\"]"));

        Assertions.assertEquals(Set.of(new Project("Apollo", "Apollo"),
                new Project("Vega", "Vega")), field.getValue());
        Assertions.assertTrue(success(result));
        Assertions.assertEquals(List.of(idOf(field)), writtenIds(result));
    }

    @Test
    void fillForm_multiSelect_withoutValueOptionsIsRejectedWithHint() {
        var field = new MultiSelectField<Project>();
        var controller = controllerFor(field);

        var result = fillFormResult(controller, payload(field, "[\"Apollo\"]"));

        Assertions.assertEquals(Set.of(), field.getValue());
        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        Assertions.assertTrue(
                rejectionReason(result, idOf(field)).contains("valueOptions"));
    }

    @Test
    void fillForm_multiSelect_emptyArrayClearsField() {
        // Empty array is the LLM clearing the multi-select; the resulting
        // value matches the field's own empty value (Set.of() for Vaadin
        // multi-selects), so other code observing the field sees the
        // shape it expects.
        var field = new MultiSelectField<Project>();
        var existing = new Project("X", "X");
        field.setValue(Set.of(existing));
        var controller = newController(field);
        controller.valueOptions(field, (filter, limit) -> List.of(),
                label -> Set.of(existing));
        controller.onRequest();

        var result = fillFormResult(controller, payload(field, "[]"));

        Assertions.assertEquals(Set.of(), field.getValue());
        Assertions.assertTrue(success(result));
        Assertions.assertEquals(List.of(idOf(field)), writtenIds(result));
    }

    @Test
    void fillForm_valueOptionsOnPrimitiveTypeUsesToValueNotTypeDrivenParsing() {
        // Registering valueOptions(...) on a non-SELECT field still makes
        // the LLM speak in labels (the schema advertises an enum/queryable
        // string). The converter must apply toValue and skip type-driven
        // parsing — otherwise the field would receive a raw String and a
        // typed setValue (e.g. ComboBox<Project>) would reject it.
        var field = new IntField();
        var controller = newController(field);
        controller.valueOptions(field,
                (filter, limit) -> List.of("low", "high"),
                label -> "low".equals(label) ? 1 : 10);
        controller.onRequest();

        var result = fillFormResult(controller, payload(field, "\"high\""));

        Assertions.assertEquals(10, field.getValue());
        Assertions.assertTrue(success(result));
        Assertions.assertEquals(List.of(idOf(field)), writtenIds(result));
    }

    @Test
    void fillForm_mixedKnownAndUnknownIds_producesCombinedJsonResult() {
        // Real-world scenario: the LLM sends one stale id plus one valid
        // id. The valid write lands; the stale entry is rejected with a
        // get_form_state hint. success=false because at least one entry
        // was rejected, even though the other one succeeded.
        var field = new TestField();
        var controller = controllerFor(field);

        var args = JacksonUtils.createObjectNode();
        args.put(idOf(field), "Acme");
        args.put("stale-id-from-prior-turn", "garbage");
        var result = fillFormResult(controller, args);

        Assertions.assertEquals("Acme", field.getValue());
        Assertions.assertFalse(success(result));
        Assertions.assertEquals(List.of(idOf(field)), writtenIds(result));
        Assertions.assertEquals(List.of("stale-id-from-prior-turn"),
                rejectedIds(result));
        Assertions
                .assertTrue(rejectionReason(result, "stale-id-from-prior-turn")
                        .contains("get_form_state"));
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
    void fillForm_detachedFormSurfacesAsGenericError() {
        // executeFill demands an attached UI (its writes must land on the
        // UI thread). When the form isn't attached the controller throws
        // IllegalStateException; the fill_form tool's outer execute()
        // catch maps it to the generic "Error: fill failed." so the LLM
        // sees a curated error rather than the orchestrator's internal
        // state diagnostic. Pin the fail-fast contract here.
        var field = new TestField();
        var detachedForm = new Div(field);
        // No ui.add(detachedForm) — form is intentionally detached.
        var controller = new FormAIController(detachedForm);
        controller.onRequest();

        var raw = fillFormPayload(controller, payload(field, "\"Acme\""));

        Assertions.assertEquals("Error: fill failed.", raw,
                "Detached form must surface as the generic fill_form "
                        + "error; got: " + raw);
        Assertions.assertEquals("", field.getValue(),
                "Detached form must not be written to");
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

    /** Domain-typed item used by the select-field tests. */
    private record Project(String code, String name) {
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

    private FormAIController controllerFor(Component... fields) {
        var controller = newController(fields);
        // Drive onRequest() so each discovered field has its UUID id
        // stamped — payload helpers use idOf() to look the id up.
        controller.onRequest();
        return controller;
    }

    /**
     * Builds a controller around a form attached to {@code MockUIExtension}'s
     * UI but stops short of {@code onRequest()} so callers can register
     * {@code valueOptions} before the first turn. Attaching the form is
     * required: {@code executeFill} throws {@link IllegalStateException} on a
     * detached form, matching the production contract.
     */
    private FormAIController newController(Component... fields) {
        var form = new Div(fields);
        ui.add(form);
        return new FormAIController(form);
    }

    private static JsonNode payload(HasValue<?, ?> field, String jsonValue) {
        return JacksonUtils
                .readTree("{\"" + idOf(field) + "\":" + jsonValue + "}");
    }

    /**
     * Executes the {@code fill_form} tool with a field-id → value map and
     * returns the raw response string. Wraps the map in the {@code values} key
     * the tool's parameter schema requires so individual tests can stay focused
     * on the field map.
     */
    private static String fillFormPayload(FormAIController controller,
            JsonNode fieldMap) {
        var wrapped = JacksonUtils.createObjectNode();
        wrapped.set("values", fieldMap);
        return findTool(controller.getTools(), "fill_form").execute(wrapped);
    }

    /**
     * Same as {@link #fillFormPayload(FormAIController, JsonNode)} but parses
     * the response as JSON. Use for happy-path tests asserting on the
     * {@code success}/{@code written}/{@code rejected} structure.
     */
    private static JsonNode fillFormResult(FormAIController controller,
            JsonNode fieldMap) {
        return parseResult(fillFormPayload(controller, fieldMap));
    }

    private static JsonNode parseResult(String response) {
        try {
            return JacksonUtils.getMapper().readTree(response);
        } catch (Exception ex) {
            throw new AssertionError("Response is not valid JSON: " + response,
                    ex);
        }
    }

    private static boolean success(JsonNode result) {
        return result.path("success").asBoolean();
    }

    private static List<String> writtenIds(JsonNode result) {
        var ids = new ArrayList<String>();
        result.path("written").forEach(node -> ids.add(node.asString()));
        return ids;
    }

    private static List<String> rejectedIds(JsonNode result) {
        var ids = new ArrayList<String>();
        result.path("rejected")
                .forEach(node -> ids.add(node.path("id").asString()));
        return ids;
    }

    /**
     * Returns the rejection reason recorded for the given id, or {@code null}
     * if the id is not in {@code rejected}.
     */
    private static String rejectionReason(JsonNode result, String id) {
        for (var entry : result.path("rejected")) {
            if (id.equals(entry.path("id").asString())) {
                return entry.path("reason").asString();
            }
        }
        return null;
    }

    /**
     * Returns a {@link FormAITools.Callbacks} whose {@code executeFill} throws
     * the given exception — lets a test pin the outer execute() catches in
     * {@link FormAITools#fillForm(FormAITools.Callbacks)} without going through
     * the controller layer.
     */
    private static FormAITools.Callbacks throwingCallbacks(
            RuntimeException toThrow) {
        return new FormAITools.Callbacks() {
            @Override
            public List<FormAITools.FormFieldDescriptor> visibleFields() {
                return List.of();
            }

            @Override
            public List<String> queryFieldOptions(String fieldId, String filter,
                    int limit) {
                throw new AssertionError(
                        "queryFieldOptions must not be called from "
                                + "fill_form execute()");
            }

            @Override
            public String executeFill(JsonNode arguments) {
                throw toThrow;
            }
        };
    }

    /** Minimal valid {@code fill_form} arguments — an empty values object. */
    private static JsonNode wrappedValues() {
        var args = JacksonUtils.createObjectNode();
        args.set("values", JacksonUtils.createObjectNode());
        return args;
    }
}
