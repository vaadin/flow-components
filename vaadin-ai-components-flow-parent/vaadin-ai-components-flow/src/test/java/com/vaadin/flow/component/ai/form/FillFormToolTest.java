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
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

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
import com.vaadin.flow.component.ai.form.FormTestFields.Project;
import com.vaadin.flow.component.ai.form.FormTestFields.SingleSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.ai.form.FormTestFields.TimeField;
import com.vaadin.flow.component.ai.form.FormTestFields.ValidatedField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * Tests for {@link FormAIController}'s {@code fill_form} tool. Per the RFC the
 * tool returns the same shape as {@code get_form_state} — a {@code fields}
 * block listing every visible field's current state — plus a {@code rejected}
 * block carrying {@code {"id", "value", "reason"}} entries for any value that
 * failed to parse, resolve, or validate. Each test drives the tool the way the
 * LLM would and asserts on the parsed response (rejection state, fields block
 * contents) plus the field state. Tests that pin error strings outside the JSON
 * happy path (e.g. malformed {@code arguments}) assert against the raw response
 * string directly.
 */
class FillFormToolTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void fillForm_responseFieldsBlockMirrorsGetFormStateForAllVisibleFields() {
        // Per RFC: fill_form returns the same shape as get_form_state. The
        // LLM sees every visible field's current state in the response, not
        // just the ids it wrote — so structural or cascading changes from
        // value-change listeners surface without an extra get_form_state
        // round-trip.
        var name = new LabeledStringField();
        name.setLabel("Name");
        var notes = new LabeledStringField();
        notes.setLabel("Notes");
        notes.setValue("untouched");
        var controller = controllerFor(name, notes);

        var result = fillFormResult(controller, payload(name, "\"Acme\""));

        var ids = new ArrayList<String>();
        result.path("fields").forEach(f -> ids.add(f.path("id").asString()));
        Assertions.assertTrue(ids.contains(idOf(name)),
                "fields block must include the written field; got: " + result);
        Assertions.assertTrue(ids.contains(idOf(notes)),
                "fields block must include every visible field, even ones "
                        + "the LLM didn't write; got: " + result);
    }

    @Test
    void fillForm_responseSurfacesPostWriteValueInFieldsBlock() {
        var field = new LabeledStringField();
        field.setLabel("Name");
        var controller = controllerFor(field);

        var result = fillFormResult(controller, payload(field, "\"Acme\""));

        var entry = fieldEntry(result, idOf(field));
        Assertions.assertNotNull(entry,
                "Field must appear in fields block, got: " + result);
        Assertions.assertEquals("Acme", entry.path("value").asString(),
                "fields entry must carry the post-write value; got: " + result);
    }

    @Test
    void fillForm_responseSurfacesValueChangeListenerCascadeInFieldsBlock() {
        // The killer case for full-state responses: a value-change listener
        // on the written field updates a SECOND field. The LLM sees that
        // cascade in the same response and can react in the same turn.
        var name = new LabeledStringField();
        name.setLabel("Name");
        var email = new LabeledStringField();
        email.setLabel("Email");
        name.addValueChangeListener(
                e -> email.setValue(e.getValue() + "@example.com"));
        var controller = controllerFor(name, email);

        var result = fillFormResult(controller, payload(name, "\"acme\""));

        Assertions.assertEquals("acme@example.com",
                fieldEntry(result, idOf(email)).path("value").asString(),
                "Cascaded value from listener must surface in fields block, "
                        + "got: " + result);
    }

    @Test
    void fillForm_responseOmitsIgnoredFields() {
        var visible = new LabeledStringField();
        visible.setLabel("Visible");
        var secret = new LabeledStringField();
        secret.setLabel("Secret");
        secret.setValue("classified");
        var controller = controllerFor(visible, secret);
        controller.ignore(secret);

        var raw = fillFormPayload(controller, payload(visible, "\"Acme\""));

        Assertions.assertFalse(raw.contains(idOf(secret)),
                "Ignored field's id must not appear, got: " + raw);
        Assertions.assertFalse(raw.contains("Secret"),
                "Ignored field's label must not appear, got: " + raw);
        Assertions.assertFalse(raw.contains("classified"),
                "Ignored field's value must not appear, got: " + raw);
    }

    @Test
    void fillForm_rejectedEntryIncludesAttemptedValue() {
        var amount = new DoubleField();
        var controller = controllerFor(amount);

        var result = fillFormResult(controller,
                payload(amount, "\"not a number\""));

        Assertions.assertEquals("\"not a number\"",
                rejectionValue(result, idOf(amount)),
                "Rejected entry must echo the LLM's input value, got: "
                        + result);
    }

    @Test
    void fillForm_writesStringValueToTextField() {
        var field = new LabeledStringField();
        field.setLabel("Name");
        var result = fillFormResult(controllerFor(field),
                payload(field, "\"Acme Corp\""));

        Assertions.assertEquals("Acme Corp", field.getValue());
        Assertions.assertTrue(success(result), "Result: " + result);
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
        Assertions.assertEquals(List.of("not-a-real-id"), rejectedIds(result));
        var reason = rejectionReason(result, "not-a-real-id");
        Assertions.assertTrue(reason.contains("not-a-real-id"),
                "Reason must name the unknown id; got: " + reason);
        Assertions.assertTrue(reason.contains("get_form_state"),
                "Reason must direct the LLM to refresh via "
                        + "get_form_state; got: " + reason);
    }

    @Test
    void fillForm_rejectsWriteToDisabledFieldWithoutChangingIt() {
        // A disabled field is shown to the LLM as context (with a "disabled"
        // flag) but is not writable. A write to it must be rejected with a
        // reason that points at enabling it, and the field must keep its value.
        var disabled = new TestField();
        disabled.setValue("orig");
        disabled.setEnabled(false);
        var controller = controllerFor(new TestField(), disabled);

        var result = fillFormResult(controller, payload(disabled, "\"new\""));

        Assertions.assertEquals("orig", disabled.getValue(),
                "Disabled field must keep its value");
        Assertions.assertEquals(List.of(idOf(disabled)), rejectedIds(result));
        var reason = rejectionReason(result, idOf(disabled));
        Assertions.assertTrue(reason.contains("disabled"),
                "Reason must say the field is disabled; got: " + reason);
    }

    @Test
    void fillForm_rejectsWriteToReadOnlyFieldWithoutChangingIt() {
        // An application-read-only field is context only. A write to it must be
        // rejected without changing it. The controller's own turn lock does not
        // count as read-only here — that path is exercised by every happy-path
        // fill test, which writes through the lock after onRequest().
        var readOnly = new TestField();
        readOnly.setValue("orig");
        readOnly.setReadOnly(true);
        var controller = controllerFor(new TestField(), readOnly);

        var result = fillFormResult(controller, payload(readOnly, "\"new\""));

        Assertions.assertEquals("orig", readOnly.getValue(),
                "Read-only field must keep its value");
        Assertions.assertEquals(List.of(idOf(readOnly)), rejectedIds(result));
        var reason = rejectionReason(result, idOf(readOnly));
        Assertions.assertTrue(reason.contains("read-only"),
                "Reason must say the field is read-only; got: " + reason);
    }

    @Test
    void fillForm_rejectsWriteToFieldDisabledByEarlierWriteInSamePayload() {
        // A field's availability can change mid-fill: writing a controlling
        // field earlier in the same payload can disable a field that appears
        // later. The not-writable check must re-evaluate each field's LIVE
        // state at write time, not a snapshot taken before any writes —
        // otherwise the write lands on a field the user can no longer edit.
        var trigger = new TestField();
        var dependent = new TestField();
        dependent.setValue("orig");
        // Writing the trigger disables the dependent field.
        trigger.addValueChangeListener(e -> dependent.setEnabled(false));
        var controller = controllerFor(trigger, dependent);

        // Order matters: the trigger is written first so the dependent is
        // already disabled by the time its entry is processed.
        var args = JacksonUtils.createObjectNode();
        args.put(idOf(trigger), "go");
        args.put(idOf(dependent), "new");
        var result = fillFormResult(controller, args);

        Assertions.assertEquals("orig", dependent.getValue(),
                "A field disabled by an earlier write in the same payload "
                        + "must not be written");
        Assertions.assertEquals(List.of(idOf(dependent)), rejectedIds(result),
                "The write to the now-disabled field must be rejected; got: "
                        + result);
        var reason = rejectionReason(result, idOf(dependent));
        Assertions.assertTrue(reason.contains("disabled"),
                "Reason must say the field is disabled; got: " + reason);
    }

    @Test
    void fillForm_rejectsWriteToNowHiddenFieldAsUnknownId() {
        // The LLM may hold an id from an earlier get_form_state for a field
        // that has since been hidden. A hidden field is off the surface, so
        // the write is rejected as an unknown id (which tells the LLM to
        // refresh via get_form_state), and the field keeps its value.
        var field = new TestField();
        field.setValue("orig");
        var controller = controllerFor(field); // id stamped while visible
        field.setVisible(false); // hidden after the LLM saw it

        var result = fillFormResult(controller, payload(field, "\"new\""));

        Assertions.assertEquals("orig", field.getValue(),
                "Hidden field must keep its value");
        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        var reason = rejectionReason(result, idOf(field));
        Assertions.assertTrue(reason.contains("Unknown field id"),
                "A hidden field is off the surface, so the write must be "
                        + "rejected as an unknown id; got: " + reason);
    }

    @Test
    void fillForm_rejectsWriteToFieldUnderNowHiddenContainerAsUnknownId() {
        // Same as above, but the field is hidden because an ancestor container
        // is hidden rather than the field itself. Effective visibility keeps it
        // off the surface, so the write is rejected as an unknown id.
        var field = new TestField();
        field.setValue("orig");
        var container = new Div(field);
        var controller = controllerFor(container); // id stamped while visible
        container.setVisible(false); // ancestor hidden afterwards

        var result = fillFormResult(controller, payload(field, "\"new\""));

        Assertions.assertEquals("orig", field.getValue(),
                "Field under a hidden container must keep its value");
        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        var reason = rejectionReason(result, idOf(field));
        Assertions.assertTrue(reason.contains("Unknown field id"),
                "A field hidden by an ancestor is off the surface, so the "
                        + "write must be rejected as an unknown id; got: "
                        + reason);
    }

    @Test
    void fillForm_postWriteSnapshotDoesNotFlagTurnLockedFieldAsReadOnly() {
        // The fill_form response re-snapshots the form AFTER the writes, while
        // the controller's turn lock is still on. A locked-but-writable field
        // must appear in that snapshot WITHOUT a readOnly flag — the lock is
        // the controller's, not the application's — and must carry the value
        // just written. Guards the formatResult() path the way
        // get_form_state's own carve-out test guards the read path.
        var field = new TestField();
        var controller = controllerFor(field); // onRequest() locks the field

        var result = fillFormResult(controller, payload(field, "\"filled\""));

        var entry = fieldEntry(result, idOf(field));
        Assertions.assertNotNull(entry,
                "Locked field must appear in the post-write fields block; got: "
                        + result);
        Assertions.assertFalse(entry.has("readOnly"),
                "Turn-locked field must not be flagged readOnly in the "
                        + "fill_form post-write snapshot; got: " + entry);
        Assertions.assertEquals("filled", field.getValue(),
                "Write to a turn-locked (but app-writable) field must land");
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
        Assertions.assertFalse(raw.contains("original"),
                "Ignored field's current value must not appear in the "
                        + "response, got: " + raw);
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
        Assertions.assertFalse(raw.contains("untouched"),
                "PasswordField's current value must not appear in the "
                        + "response, got: " + raw);
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
        Assertions.assertTrue(rejectedIds(result).isEmpty());
    }

    @Test
    void fillForm_emptyPayloadReturnsSuccessWithEmptyRejected() {
        // No payload means no writes attempted — success (no rejected
        // entries) holds. The LLM uses the JSON shape to confirm it
        // didn't accidentally fire a malformed turn.
        var name = new LabeledStringField();
        name.setLabel("Name");
        var controller = controllerFor(name);

        var result = fillFormResult(controller,
                JacksonUtils.createObjectNode());

        Assertions.assertTrue(success(result),
                "Empty payload is a no-op, not a failure; got: " + result);
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
        Assertions.assertEquals(List.of(idOf(amount)), rejectedIds(result));
        Assertions.assertTrue(rejectionReason(result, idOf(amount))
                .contains("Expected number"));
    }

    @Test
    void fillForm_bindingValidatorRejectionSurfacesInRejected() {
        // RFC: validation runs at fill_form time and rejections come back in
        // the same response so the LLM can self-correct in the same turn
        // without an extra get_form_state round-trip.
        var field = new LabeledStringField();
        var binder = new Binder<>(TestBean.class);
        binder.forField(field).withValidator(v -> v != null && v.length() >= 3,
                "Name must be at least 3 characters").bind("name");
        var controller = controllerForBound(binder, field);

        var result = fillFormResult(controller, payload(field, "\"X\""));

        Assertions.assertFalse(success(result),
                "Validator rejection must produce success=false, got: "
                        + result);
        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        Assertions.assertEquals("Name must be at least 3 characters",
                rejectionReason(result, idOf(field)));
    }

    @Test
    void fillForm_bindingValidatorRejectionLeavesValueInField() {
        var field = new LabeledStringField();
        var binder = new Binder<>(TestBean.class);
        binder.forField(field).withValidator(v -> v != null && v.length() >= 3,
                "Name must be at least 3 characters").bind("name");
        var controller = controllerForBound(binder, field);

        fillFormResult(controller, payload(field, "\"X\""));

        Assertions.assertEquals("X", field.getValue(),
                "Invalid values must stay in the field; the binder shows "
                        + "the error and the LLM corrects on the next turn");
    }

    @Test
    void fillForm_bindingValidatorPassDoesNotEmitRejected() {
        var field = new LabeledStringField();
        var binder = new Binder<>(TestBean.class);
        binder.forField(field).withValidator(v -> v != null && v.length() >= 3,
                "Name must be at least 3 characters").bind("name");
        var controller = controllerForBound(binder, field);

        var result = fillFormResult(controller, payload(field, "\"Acme\""));

        Assertions.assertTrue(success(result),
                "Passing validator must yield success=true, got: " + result);
        Assertions.assertTrue(rejectedIds(result).isEmpty(),
                "No rejected expected, got: " + result);
    }

    @Test
    void fillForm_bindingConverterFailureSurfacesReasonInRejected() {
        // A withConverter chain (e.g. String -> Integer) is the typical way
        // applications adapt a TextField to a non-String bean property. Bad
        // input fails inside the binder's chain rather than in the form
        // controller's own FormValueConverter; the rejection block must
        // still carry the converter's error message so the LLM can correct
        // on the next turn.
        var field = new LabeledStringField();
        var binder = new Binder<>(IntegerBean.class);
        binder.forField(field).withConverter(Integer::parseInt,
                i -> i == null ? "" : i.toString(), "must be a whole number")
                .bind("count");
        var controller = controllerForBound(binder, field);

        var result = fillFormResult(controller, payload(field, "\"abc\""));

        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        Assertions.assertEquals("must be a whole number",
                rejectionReason(result, idOf(field)),
                "Converter failure must surface its message in rejected, "
                        + "got: " + result);
    }

    @Test
    void fillForm_binderLevelCrossFieldValidatorSurfacesAsRejection() {
        // A bean-level cross-field validator registered via
        // Binder.withValidator((bean, ctx) -> ...) must surface its rejection
        // in the fill_form response when the LLM writes a combination of
        // values that violates the rule. Per-binding validators alone aren't
        // enough — some rules only make sense across multiple fields (e.g.
        // "if format=Lightning, length<=10"). The rejection is keyed on a
        // sentinel id since the rule isn't bound to one specific field.
        stubVaadinContext();
        var formatField = new LabeledStringField();
        var lengthField = new IntField();
        var binder = new Binder<>(TwoFieldBean.class);
        binder.forField(formatField).bind("format");
        binder.forField(lengthField).bind("length");
        binder.withValidator((bean, ctx) -> {
            if ("Lightning".equals(bean.getFormat()) && bean.getLength() != null
                    && bean.getLength() > 10) {
                return ValidationResult
                        .error("Lightning talks must be 10 minutes or less");
            }
            return ValidationResult.ok();
        });
        binder.setBean(new TwoFieldBean());
        var controller = controllerForBound(binder, formatField, lengthField);

        var result = fillFormResult(controller,
                JacksonUtils.readTree(
                        "{\"" + idOf(formatField) + "\":\"Lightning\",\""
                                + idOf(lengthField) + "\":25}"));

        Assertions.assertFalse(success(result),
                "Cross-field validation failure must surface in rejected; "
                        + "got: " + result);
        var crossReason = rejectionReason(result, "__form__");
        Assertions.assertNotNull(crossReason,
                "Cross-field rejection must be keyed on the '__form__' "
                        + "sentinel id (not on either field id, since the "
                        + "rule isn't tied to one field); got rejected ids: "
                        + rejectedIds(result));
        Assertions.assertTrue(crossReason.contains("Lightning"),
                "Reason must surface the validator's message so the LLM "
                        + "can self-correct; got: " + crossReason);
    }

    @Test
    void fillForm_crossFieldValidationDoesNotMarkUntouchedFieldInvalid() {
        // Regression guard for the core fix: reading the post-write verdict —
        // including the bean-level cross-field rule — must not fire a
        // validation status event for a field this turn did not write.
        // Binder.validate() (fireEvent=true) re-validates every binding and
        // lights up each field's invalid indicator, so an untouched-but-invalid
        // field would wrongly show an error after an unrelated fill. The
        // controller must instead read the verdict without firing UI events
        // (binding.validate(false) per written field, plus a side-effect-free
        // bean-level read). A field's binding-level validation status handler
        // is exactly what the binder calls to light up that field, so a
        // handler that never fires proves the field was not marked.
        stubVaadinContext();
        var formatField = new LabeledStringField();
        var lengthField = new IntField();
        var untouchedField = new LabeledStringField();
        var binder = new Binder<>(TwoFieldBean.class);
        binder.forField(formatField).bind("format");
        binder.forField(lengthField).bind("length");
        var untouchedStatusEvents = new int[] { 0 };
        binder.forField(untouchedField)
                // Always-invalid: Binder.validate() would mark it, so a clean
                // counter can only mean the field was never validated.
                .withValidator(value -> false, "untouched is always invalid")
                .withValidationStatusHandler(
                        status -> untouchedStatusEvents[0]++)
                .bind("notes");
        binder.withValidator((bean, ctx) -> {
            if ("Lightning".equals(bean.getFormat()) && bean.getLength() != null
                    && bean.getLength() > 10) {
                return ValidationResult
                        .error("Lightning talks must be 10 minutes or less");
            }
            return ValidationResult.ok();
        });
        binder.setBean(new TwoFieldBean());
        var controller = controllerForBound(binder, formatField, lengthField,
                untouchedField);
        // Ignore any status events fired while wiring up the binder/bean; only
        // the fill itself is under test.
        untouchedStatusEvents[0] = 0;

        // Write only the cross-field pair (which fails the rule); never touch
        // untouchedField.
        fillFormResult(controller,
                JacksonUtils.readTree(
                        "{\"" + idOf(formatField) + "\":\"Lightning\",\""
                                + idOf(lengthField) + "\":25}"));

        Assertions.assertEquals(0, untouchedStatusEvents[0],
                "Validating the post-write state must not fire a validation "
                        + "status event for a field this turn did not write; "
                        + "the untouched field's indicator must stay clean. "
                        + "Got events: " + untouchedStatusEvents[0]);
    }

    @Test
    void fillForm_binderLevelCrossFieldValidatorPassDoesNotEmitRejection() {
        // Symmetric guard: when the cross-field validator passes, no sentinel
        // rejection appears in the response.
        stubVaadinContext();
        var formatField = new LabeledStringField();
        var lengthField = new IntField();
        var binder = new Binder<>(TwoFieldBean.class);
        binder.forField(formatField).bind("format");
        binder.forField(lengthField).bind("length");
        binder.withValidator((bean, ctx) -> {
            if ("Lightning".equals(bean.getFormat()) && bean.getLength() != null
                    && bean.getLength() > 10) {
                return ValidationResult
                        .error("Lightning talks must be 10 minutes or less");
            }
            return ValidationResult.ok();
        });
        binder.setBean(new TwoFieldBean());
        var controller = controllerForBound(binder, formatField, lengthField);

        var result = fillFormResult(controller,
                JacksonUtils.readTree("{\"" + idOf(formatField)
                        + "\":\"Standard\",\"" + idOf(lengthField) + "\":45}"));

        Assertions.assertTrue(success(result),
                "Passing cross-field validator must produce success=true; "
                        + "got: " + result);
    }

    @Test
    void fillForm_binderLevelCrossFieldValidatorSkippedWithoutBean() {
        // Binder.validate()'s bean-level validators only run when a bean is set
        // on the binder. When the application uses readBean/writeBean instead
        // of setBean the bean-level rule has no target — the controller must
        // not emit a sentinel rejection in that case, since there is nothing
        // to validate against.
        var formatField = new LabeledStringField();
        var lengthField = new IntField();
        var binder = new Binder<>(TwoFieldBean.class);
        binder.forField(formatField).bind("format");
        binder.forField(lengthField).bind("length");
        binder.withValidator((bean, ctx) -> ValidationResult
                .error("never-fires-without-bean"));
        // intentionally no setBean
        var controller = controllerForBound(binder, formatField, lengthField);

        var result = fillFormResult(controller,
                JacksonUtils.readTree(
                        "{\"" + idOf(formatField) + "\":\"Lightning\",\""
                                + idOf(lengthField) + "\":25}"));

        Assertions.assertTrue(success(result),
                "Without setBean the bean-level validator has no target and "
                        + "must not produce a sentinel rejection; got: "
                        + result);
    }

    @Test
    void fillForm_bindingValidatorBlankMessageIsStillSurfacedAsRejection() {
        var field = new LabeledStringField();
        var binder = new Binder<>(TestBean.class);
        binder.forField(field).withValidator(v -> false, "").bind("name");
        var controller = controllerForBound(binder, field);

        var result = fillFormResult(controller, payload(field, "\"X\""));

        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result),
                "Validator that fails with a blank message must still "
                        + "produce a rejected entry; otherwise the LLM "
                        + "thinks the write succeeded while the field is "
                        + "in an invalid state. Got: " + result);
    }

    @Test
    void fillForm_hasValidatorRejectionForUnboundFieldSurfacesInRejected() {
        // Unbound fields fall back to HasValidator's default validator. The
        // fill_form path must hit the same FormFieldValidation hook that
        // get_form_state uses, otherwise unbound rejections only show on
        // the next get_form_state call.
        var field = new ValidatedField();
        field.rejectAllWith("Value not accepted");
        var controller = controllerFor(field);

        var result = fillFormResult(controller, payload(field, "\"X\""));

        Assertions.assertFalse(success(result),
                "HasValidator rejection must surface in fill_form, got: "
                        + result);
        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        Assertions.assertEquals("Value not accepted",
                rejectionReason(result, idOf(field)));
    }

    @Test
    void fillForm_hasValidatorWarningLevelResultEmitsNoRejection() {
        // ValidationResult with ErrorLevel.WARNING isError()=false; the
        // early return in errorFromHasValidator's non-error branch is what
        // keeps the message from being surfaced as a rejection. The
        // outer-catch safety net hides the bug for ValidationResult.ok()
        // because getErrorMessage() throws there, but a non-error result
        // with a real message does not throw — the rejection leaks out.
        var field = new ValidatedField();
        field.setDefaultValidator((value,
                ctx) -> com.vaadin.flow.data.binder.ValidationResult.create(
                        "soft-warning",
                        com.vaadin.flow.data.binder.ErrorLevel.WARNING));
        var controller = controllerFor(field);

        var result = fillFormResult(controller, payload(field, "\"X\""));

        Assertions.assertTrue(rejectedIds(result).isEmpty(),
                "Non-error ValidationResult (INFO/WARNING) must not produce "
                        + "a rejection, got: " + result);
    }

    @Test
    void fillForm_blankValidatorMessageReplacedWithGenericReason() {
        // A validator that fails with a blank (whitespace-only) message must
        // not surface that blank text as the rejection reason — an empty
        // reason gives the LLM nothing to act on. The reason must fall back to
        // the generic message instead. Pins the blank-handling in
        // FormFieldValidation.message(); a non-blank message passes through
        // unchanged (covered by fillForm_hasValidatorRejection...).
        var field = new ValidatedField();
        field.setDefaultValidator(
                (value, ctx) -> ValidationResult.error("   "));
        var controller = controllerFor(field);

        var result = fillFormResult(controller, payload(field, "\"X\""));

        Assertions.assertEquals("Field rejected the value.",
                rejectionReason(result, idOf(field)),
                "A blank validator message must be replaced with the generic "
                        + "reason so the LLM gets actionable text, got: "
                        + result);
    }

    @Test
    void fillForm_hasValidatorReceivesComponentInValueContext() {
        // Pins that FormFieldValidation builds the ValueContext with the
        // field as the Component so locale-aware validators (and anything
        // else reading ctx.getComponent()) sees the source component, not
        // an empty Optional.
        var field = new ValidatedField();
        field.setDefaultValidator((value,
                ctx) -> com.vaadin.flow.data.binder.ValidationResult.error(
                        "component-was-" + ctx.getComponent().isPresent()));
        var controller = controllerFor(field);

        var result = fillFormResult(controller, payload(field, "\"X\""));

        Assertions.assertEquals("component-was-true",
                rejectionReason(result, idOf(field)),
                "Validator must receive a ValueContext carrying the field "
                        + "as its Component; got: " + result);
    }

    @Test
    void firstError_bindingValidateThrows_returnsEmptyNotNull() {
        // A per-field validator that throws (rather than returning an error
        // result) must not crash the post-write validation pass: firstError
        // must catch the throw and return Optional.empty(), not null — the
        // caller chains .ifPresent(...) on the result and null breaks the
        // chain. The fill_form pipeline intercepts validator throws at
        // setValue (the binder triggers validation through the value-change
        // listener), so the catch is only reachable when firstError is called
        // directly with a binding whose validate(false) still throws — pin the
        // contract here.
        var field = new LabeledStringField();
        var binder = new Binder<>(TestBean.class);
        binder.forField(field)
                .withValidator(
                        (com.vaadin.flow.data.binder.Validator<String>) (value,
                                ctx) -> {
                            throw new RuntimeException("validator-boom");
                        })
                .bind("name");
        var binding = BinderReflection.findBinding(binder, field);

        var result = FormFieldValidation.firstError(field, binding);

        Assertions.assertEquals(java.util.Optional.empty(), result,
                "errorFromBinding catch must return Optional.empty(), not "
                        + "null; null breaks the caller's .ifPresent() chain");
    }

    @Test
    void beanErrors_beanValidatorThrows_returnsEmptyNotNull() {
        // A bean-level cross-field validator that throws must not crash the
        // post-write pass: beanErrors must swallow the throw and return an
        // empty (never null) list so doFill can still format a response for
        // the rest of the turn.
        stubVaadinContext();
        var field = new LabeledStringField();
        var binder = new Binder<>(TwoFieldBean.class);
        binder.forField(field).bind("format");
        binder.withValidator((bean, ctx) -> {
            throw new RuntimeException("bean-validator-boom");
        });
        binder.setBean(new TwoFieldBean());

        var result = FormFieldValidation.beanErrors(binder);

        Assertions.assertTrue(result.isEmpty(),
                "beanErrors must swallow a throwing bean-level validator and "
                        + "return an empty, non-null list; got: " + result);
    }

    @Test
    void fillForm_boundHasValidatorFieldNotDoubleValidated() {
        // ValidatedField implements HasValidator. The binder wraps the
        // field's default validator into the binding chain by default, so a
        // failing validator would otherwise be counted both via
        // Binding.validate(false) and via the direct HasValidator branch in
        // FormFieldValidation. The early return after the binding path
        // keeps the rejection from being recorded twice.
        var field = new ValidatedField();
        field.rejectAllWith("X");
        var binder = new Binder<>(TestBean.class);
        binder.forField(field).bind("name");
        var controller = controllerForBound(binder, field);

        var result = fillFormResult(controller, payload(field, "\"anything\""));

        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result),
                "Bound HasValidator field must record exactly one rejection, "
                        + "not two; got: " + result);
    }

    @Test
    void fillForm_conversionFailureDoesNotRunValidators() {
        // A converter rejection short-circuits applyValue before setValue
        // runs — the validator must not run either (running it on the
        // pre-fill value would produce a misleading reason that doesn't
        // describe the LLM's failed input).
        var field = new IntField();
        var binder = new Binder<>(TestBean.class);
        binder.forField(field)
                .withValidator(v -> false, "validator should not have run")
                .bind(b -> 0, (b, v) -> {
                });
        var controller = controllerForBound(binder, field);

        var result = fillFormResult(controller, payload(field, "\"not-int\""));

        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        Assertions.assertNotEquals("validator should not have run",
                rejectionReason(result, idOf(field)),
                "Conversion failure must report the converter's reason, "
                        + "not the validator's; got: " + result);
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
        controller.valueOptions(ValueOptions.forField(field)
                .options((filter, limit) -> List.of("Apollo")), projects::get);
        controller.onRequest();

        var result = fillFormResult(controller, payload(field, "\"Apollo\""));

        Assertions.assertEquals(new Project("P-1", "Apollo"), field.getValue(),
                "Field must receive the resolved domain object, not the "
                        + "raw label string");
        Assertions.assertTrue(success(result));
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
    void fillForm_singleSelect_eagerItemsResolveLabelWithoutValueOptions() {
        // A `ComboBox<String>`/`Select<String>` with eager
        // `setItems(...)` has a non-empty ListDataProvider and the schema
        // already surfaces its items as `enum`. The converter must resolve
        // an LLM-supplied label against those items via
        // FormValueConverter.renderItem so the field is writable without
        // also requiring FormAIController.valueOptions(...).
        var field = new SingleSelectField<String>();
        field.setItems("EUR", "USD", "GBP");
        var controller = controllerFor(field);

        var result = fillFormResult(controller, payload(field, "\"EUR\""));

        Assertions.assertEquals("EUR", field.getValue(),
                "Eager-items SINGLE_SELECT must accept a label that "
                        + "matches one of its items");
        Assertions.assertTrue(success(result));
    }

    @Test
    void fillForm_singleSelect_eagerItemsRejectLabelNotInItems() {
        // Symmetric to the above: when eager items exist, a label that is
        // not in the rendered set must be rejected with a reason that
        // names the offending label so the LLM can self-correct.
        var field = new SingleSelectField<String>();
        field.setItems("EUR", "USD", "GBP");
        var controller = controllerFor(field);

        var result = fillFormResult(controller, payload(field, "\"YEN\""));

        Assertions.assertNull(field.getValue(),
                "Unknown label must not write a value");
        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        Assertions.assertTrue(
                rejectionReason(result, idOf(field)).contains("YEN"),
                "Reason must name the unmatched label; got: "
                        + rejectionReason(result, idOf(field)));
    }

    @Test
    void fillForm_singleSelect_eagerItemsResolveLabelViaItemLabelGenerator() {
        // The symmetry between schema (emits `enum`) and converter
        // (matches by label) rides on FormValueConverter.renderItem.
        // String items collapse to Object#toString, so an item type with
        // a custom ItemLabelGenerator is what actually exercises the
        // generator path on both halves of the protocol.
        var field = new SingleSelectField<Project>();
        var apollo = new Project("APL", "Apollo");
        var vega = new Project("VGA", "Vega");
        field.setItems(apollo, vega);
        field.setItemLabelGenerator(Project::name);
        var controller = controllerFor(field);

        var result = fillFormResult(controller, payload(field, "\"Apollo\""));

        Assertions.assertEquals(apollo, field.getValue(),
                "Eager-items SINGLE_SELECT must match via the custom "
                        + "ItemLabelGenerator, not via toString()");
        Assertions.assertTrue(success(result));
    }

    @Test
    void fillForm_singleSelect_unknownLabelIsRejected() {
        // toValue returning null is the agreed signal for "label doesn't
        // match any option". The orchestrator must reject rather than
        // pass null to setValue (which would silently clear the field).
        var field = new SingleSelectField<Project>();
        var controller = newController(field);
        controller.valueOptions(ValueOptions.forField(field)
                .options((filter, limit) -> List.of("Apollo")), label -> null);
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
    void fillForm_multiSelect_writesResolvedSetViaValueOptions() {
        // valueOptions on a MultiSelectField takes a single-item Function;
        // the converter accumulates per-label items into the Set<Project>
        // value type via an internal LinkedHashSet.
        var field = new MultiSelectField<Project>();
        var controller = newController(field);
        controller.valueOptions(
                ValueOptions.forField(field)
                        .options((filter, limit) -> List.of("Apollo", "Vega")),
                label -> new Project(label, label));
        controller.onRequest();

        var result = fillFormResult(controller,
                payload(field, "[\"Apollo\", \"Vega\"]"));

        Assertions.assertEquals(Set.of(new Project("Apollo", "Apollo"),
                new Project("Vega", "Vega")), field.getValue());
        Assertions.assertTrue(success(result));
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
    void fillForm_multiSelect_eagerItemsResolveLabelsWithoutValueOptions() {
        // Symmetric to the SINGLE_SELECT regression guard: a
        // `MultiSelectComboBox<String>`/`CheckboxGroup<String>` with eager
        // `setItems(...)` has a non-empty ListDataProvider and the schema
        // surfaces the items as the `items.enum` of the array. The
        // converter must resolve each label against those items so the
        // field is writable without also requiring valueOptions(...).
        var field = new MultiSelectField<String>();
        field.setItems("AI", "Cloud", "Security");
        var controller = controllerFor(field);

        var result = fillFormResult(controller,
                payload(field, "[\"AI\", \"Cloud\"]"));

        Assertions.assertEquals(Set.of("AI", "Cloud"), field.getValue(),
                "Eager-items MULTI_SELECT must accept labels that "
                        + "match its items");
        Assertions.assertTrue(success(result));
    }

    @Test
    void fillForm_multiSelect_eagerItemsRejectAnyLabelNotInItems() {
        // Unknown labels in the multi-select array must surface in the
        // rejection so the LLM can drop or re-pick them; the field must
        // not be partially written either.
        var field = new MultiSelectField<String>();
        field.setItems("AI", "Cloud", "Security");
        var controller = controllerFor(field);

        var result = fillFormResult(controller,
                payload(field, "[\"AI\", \"Quantum\"]"));

        Assertions.assertEquals(Set.of(), field.getValue(),
                "A label miss must abort the multi-select write, not "
                        + "leave the field with the partial set");
        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        Assertions.assertTrue(
                rejectionReason(result, idOf(field)).contains("Quantum"),
                "Reason must name the unmatched label; got: "
                        + rejectionReason(result, idOf(field)));
    }

    @Test
    void fillForm_multiSelect_eagerItemsResolveLabelsViaItemLabelGenerator() {
        // Same lock as the SINGLE_SELECT variant: a typed item with a
        // custom ItemLabelGenerator exercises renderItem on both the
        // schema-emit side and the converter-resolve side, so the test
        // catches drift if either side stops honoring the generator.
        var field = new MultiSelectField<Project>();
        var apollo = new Project("APL", "Apollo");
        var vega = new Project("VGA", "Vega");
        field.setItems(apollo, vega);
        field.setItemLabelGenerator(Project::name);
        var controller = controllerFor(field);

        var result = fillFormResult(controller,
                payload(field, "[\"Apollo\", \"Vega\"]"));

        Assertions.assertEquals(Set.of(apollo, vega), field.getValue(),
                "Eager-items MULTI_SELECT must match via the custom "
                        + "ItemLabelGenerator, not via toString()");
        Assertions.assertTrue(success(result));
    }

    @Test
    void fillForm_multiSelect_emptyArrayClearsField() {
        // Empty array is the LLM clearing the multi-select; the converter
        // builds an empty LinkedHashSet so the field's setValue receives
        // an empty Set.
        var field = new MultiSelectField<Project>();
        var existing = new Project("X", "X");
        field.setValue(Set.of(existing));
        var controller = newController(field);
        controller.valueOptions(ValueOptions.forField(field)
                .options((filter, limit) -> List.of()), label -> existing);
        controller.onRequest();

        var result = fillFormResult(controller, payload(field, "[]"));

        Assertions.assertEquals(Set.of(), field.getValue());
        Assertions.assertTrue(success(result));
    }

    @Test
    void fillForm_multiSelect_nonArrayPayloadIsRejected() {
        // The LLM sends a bare string for a multi-select field. The
        // converter enforces the array shape so a stray scalar payload
        // doesn't reach setValue.
        var field = new MultiSelectField<Project>();
        var existing = new Project("X", "X");
        field.setValue(Set.of(existing));
        var controller = newController(field);
        controller.valueOptions(
                ValueOptions.forField(field)
                        .options((filter, limit) -> List.of("Apollo")),
                label -> new Project(label, label));
        controller.onRequest();

        var result = fillFormResult(controller, payload(field, "\"Apollo\""));

        Assertions.assertEquals(Set.of(existing), field.getValue(),
                "Rejected fill must not mutate the prior selection");
        Assertions.assertEquals(List.of(idOf(field)), rejectedIds(result));
        Assertions.assertTrue(
                rejectionReason(result, idOf(field)).contains("array"),
                "Reason must name the missing array shape; got: "
                        + rejectionReason(result, idOf(field)));
    }

    @Test
    void fillForm_multiSelect_reregistrationOverwritesPriorOptions() {
        // valueOptions called twice on the same MultiSelect field — the
        // second call wins, including switching from fixed to queryable
        // and replacing the toValue function.
        var field = new MultiSelectField<Project>();
        var controller = newController(field);
        controller.valueOptions(
                ValueOptions.forField(field).options(List.of("First")),
                label -> new Project("v1", label));
        controller.valueOptions(
                ValueOptions.forField(field)
                        .options((filter, limit) -> List.of("Second")),
                label -> new Project("v2", label));
        controller.onRequest();

        var result = fillFormResult(controller, payload(field, "[\"Second\"]"));

        Assertions.assertTrue(success(result));
        Assertions.assertEquals(Set.of(new Project("v2", "Second")),
                field.getValue(),
                "Re-registration must hand the LLM-supplied label to the "
                        + "second toValue, not the first");
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
        controller.valueOptions(
                ValueOptions.forField(field)
                        .options((filter, limit) -> List.of("low", "high")),
                label -> "low".equals(label) ? 1 : 10);
        controller.onRequest();

        var result = fillFormResult(controller, payload(field, "\"high\""));

        Assertions.assertEquals(10, field.getValue());
        Assertions.assertTrue(success(result));
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
    void fillForm_unexpectedConverterThrowKeepsStructuredResponse() {
        // FormValueConverter.convert delegates to field.getEmptyValue() for
        // JSON null. A field whose getEmptyValue() throws produces an
        // exception that is NOT a RejectedValueException; the controller's
        // applyValue only catches RejectedValueException, so the throw
        // propagates all the way out of doFill. The response must still be
        // a structured JSON document so the LLM can attribute the failure
        // to the offending field and retry the rest — collapsing the entire
        // turn into a generic "Error: fill failed." erases every other
        // field's write and rejection record.
        var bad = new EmptyValueThrowingField();
        var ok = new TestField();
        var controller = controllerFor(bad, ok);

        var args = JacksonUtils.createObjectNode();
        args.putNull(idOf(bad));
        args.put(idOf(ok), "landed");
        var raw = fillFormPayload(controller, args);

        Assertions.assertFalse(raw.startsWith("Error"),
                "Unexpected converter throw must not collapse the entire "
                        + "turn into a raw error string, got: " + raw);
        var result = parseResult(raw);
        Assertions.assertTrue(rejectedIds(result).contains(idOf(bad)),
                "Bad field must surface in rejected, got: " + result);
        Assertions.assertNotNull(fieldEntry(result, idOf(ok)),
                "Other fields must still appear in the fields block, got: "
                        + result);
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

    /** Minimal bean used by the binder-validator tests. */
    private static class TestBean {
        private String name;

        @SuppressWarnings("unused")
        public String getName() {
            return name;
        }

        @SuppressWarnings("unused")
        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Bean driving the binder-level cross-field-validator tests. {@code format}
     * and {@code length} feed the cross-field rule; {@code notes} is an
     * unrelated property for binding a field the cross-field tests leave
     * untouched. Public visibility is required because
     * {@code Binder.setBean(...)} reflects through the property getters via
     * {@code BeanPropertySet}.
     */
    public static class TwoFieldBean {
        private String format;
        private Integer length;
        private String notes;

        @SuppressWarnings("unused")
        public String getFormat() {
            return format;
        }

        @SuppressWarnings("unused")
        public void setFormat(String format) {
            this.format = format;
        }

        @SuppressWarnings("unused")
        public Integer getLength() {
            return length;
        }

        @SuppressWarnings("unused")
        public void setLength(Integer length) {
            this.length = length;
        }

        @SuppressWarnings("unused")
        public String getNotes() {
            return notes;
        }

        @SuppressWarnings("unused")
        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    /**
     * Bean with a non-String property — drives the converter-failure rejection
     * test where the binder's chain (not the controller's own converter) is the
     * source of the error.
     */
    private static class IntegerBean {
        private Integer count;

        @SuppressWarnings("unused")
        public Integer getCount() {
            return count;
        }

        @SuppressWarnings("unused")
        public void setCount(Integer count) {
            this.count = count;
        }
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

    /**
     * Field whose {@link #getEmptyValue} throws — exercises the path where
     * {@code FormValueConverter.convert} raises a non-{@code
     * RejectedValueException} for a JSON {@code null} payload.
     */
    @com.vaadin.flow.component.Tag("empty-value-throwing-field")
    private static class EmptyValueThrowingField extends
            com.vaadin.flow.component.AbstractField<EmptyValueThrowingField, String> {
        EmptyValueThrowingField() {
            super("");
        }

        @Override
        public String getEmptyValue() {
            throw new RuntimeException("internal-detail-from-getemptyvalue");
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

    private FormAIController controllerForBound(Binder<?> binder,
            Component... fields) {
        var form = new Div(fields);
        ui.add(form);
        var controller = new FormAIController(form, binder);
        controller.onRequest();
        return controller;
    }

    /**
     * Stubs {@code service.getContext()} and the
     * {@link ApplicationConfiguration} attribute on the
     * {@link MockUIExtension}'s mocked {@code VaadinService} so
     * {@code Binder.setBean(...)} can resolve its I18N / production-mode
     * lookups without tripping over Mockito's default {@code null} return. Only
     * the cross-field-validator tests that call {@code setBean} need this.
     */
    private void stubVaadinContext() {
        var context = Mockito.mock(VaadinContext.class);
        var appConfig = Mockito.mock(ApplicationConfiguration.class);
        Mockito.when(appConfig.isProductionMode()).thenReturn(false);
        // ApplicationConfiguration.get(context) uses the (Class, Supplier)
        // getAttribute overload internally — match that exact shape, otherwise
        // the mock returns null and DefaultBindingExceptionHandler NPEs.
        Mockito.when(context.getAttribute(
                ArgumentMatchers.eq(ApplicationConfiguration.class),
                ArgumentMatchers.any())).thenReturn(appConfig);
        Mockito.when(ui.getService().getContext()).thenReturn(context);
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
        // The new response shape doesn't carry a `success` key — the LLM
        // (and these tests) derive success from the `rejected` block being
        // empty.
        return !result.path("rejected").iterator().hasNext();
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
     * Returns the JSON text of the {@code value} field on the rejected entry
     * for the given id (the LLM input that was rejected), or {@code null} if
     * the id is not in {@code rejected}.
     */
    private static String rejectionValue(JsonNode result, String id) {
        for (var entry : result.path("rejected")) {
            if (id.equals(entry.path("id").asString())) {
                return entry.path("value").toString();
            }
        }
        return null;
    }

    /**
     * Returns the entry for the given id in the {@code fields} block, or
     * {@code null} if the id is not present.
     */
    private static JsonNode fieldEntry(JsonNode result, String id) {
        for (var entry : result.path("fields")) {
            if (id.equals(entry.path("id").asString())) {
                return entry;
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
