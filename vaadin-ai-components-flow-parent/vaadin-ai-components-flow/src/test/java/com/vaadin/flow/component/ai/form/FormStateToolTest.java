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
import static com.vaadin.flow.component.ai.form.FormTestSupport.formStateFields;
import static com.vaadin.flow.component.ai.form.FormTestSupport.idOf;
import static com.vaadin.flow.component.ai.form.FormTestSupport.json;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ai.form.FormTestFields.BigDecField;
import com.vaadin.flow.component.ai.form.FormTestFields.BigIntField;
import com.vaadin.flow.component.ai.form.FormTestFields.BoolField;
import com.vaadin.flow.component.ai.form.FormTestFields.ByteField;
import com.vaadin.flow.component.ai.form.FormTestFields.DataViewField;
import com.vaadin.flow.component.ai.form.FormTestFields.DateField;
import com.vaadin.flow.component.ai.form.FormTestFields.DateTimeField;
import com.vaadin.flow.component.ai.form.FormTestFields.DirectIntegerField;
import com.vaadin.flow.component.ai.form.FormTestFields.DoubleField;
import com.vaadin.flow.component.ai.form.FormTestFields.FloatField;
import com.vaadin.flow.component.ai.form.FormTestFields.IntField;
import com.vaadin.flow.component.ai.form.FormTestFields.IntegerViaNonGenericInterfaceField;
import com.vaadin.flow.component.ai.form.FormTestFields.LabeledStringField;
import com.vaadin.flow.component.ai.form.FormTestFields.LazyDataViewField;
import com.vaadin.flow.component.ai.form.FormTestFields.ListDataViewField;
import com.vaadin.flow.component.ai.form.FormTestFields.LongField;
import com.vaadin.flow.component.ai.form.FormTestFields.MultiSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.Project;
import com.vaadin.flow.component.ai.form.FormTestFields.ShortField;
import com.vaadin.flow.component.ai.form.FormTestFields.SingleSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.ai.form.FormTestFields.ThrowingField;
import com.vaadin.flow.component.ai.form.FormTestFields.TimeField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * Tests for {@link FormAIController}'s {@code get_form_state} tool. Covers
 * field classification, schema rendering (types, formats, enum vs. queryable),
 * value serialization, and the end-to-end shape of the tool result.
 */
class FormStateToolTest {

    @Test
    void getFormStateReturnsAllVisibleFieldsInDocumentOrder() {
        var a = new TestField();
        var b = new DoubleField();
        var c = new BoolField();
        var nested = new Div(b, c);
        var form = new Div(a, nested);
        var controller = new FormAIController(form);

        var fields = formStateFields(controller);

        Assertions.assertEquals(3, fields.size());
        Assertions.assertEquals(idOf(a), fields.get(0).get("id").asString());
        Assertions.assertEquals(idOf(b), fields.get(1).get("id").asString());
        Assertions.assertEquals(idOf(c), fields.get(2).get("id").asString());
    }

    @Test
    void getFormStateOmitsIgnoredFields() {
        var visible = new TestField();
        var hidden = new TestField();
        var controller = new FormAIController(new Div(visible, hidden));
        controller.ignoreField(hidden);

        var fields = formStateFields(controller);

        Assertions.assertEquals(1, fields.size());
        Assertions.assertEquals(idOf(visible),
                fields.get(0).get("id").asString());
    }

    @Test
    void getFormStateOmitsHiddenFields() {
        // A field the application has hidden with setVisible(false) is not
        // something the user can see or edit, so it must not enter the LLM's
        // tool surface either. This is how a conditionally-shown field (e.g.
        // a "Cost center" revealed only for business trips) stays out of
        // get_form_state until its controlling field reveals it.
        var visible = new TestField();
        var hidden = new TestField();
        hidden.setVisible(false);
        var controller = new FormAIController(new Div(visible, hidden));

        var fields = formStateFields(controller);

        Assertions.assertEquals(1, fields.size(),
                "Hidden (setVisible(false)) field must be excluded, got: "
                        + fields);
        Assertions.assertEquals(idOf(visible),
                fields.get(0).get("id").asString());
    }

    @Test
    void getFormStateExcludesFieldInsideInvisibleContainer() {
        // Hiding a container hides the fields inside it from the user. The
        // field's own isVisible() is still true, so the controller must judge
        // EFFECTIVE visibility (ancestors included) to keep such a field off
        // the surface — otherwise the AI reads/writes a field nobody can see.
        var field = new TestField();
        var container = new Div(field);
        container.setVisible(false);
        var controller = new FormAIController(new Div(container));

        var fields = formStateFields(controller);

        Assertions.assertEquals(0, fields.size(),
                "Field inside an invisible container must be excluded, got: "
                        + fields);
    }

    @Test
    void getFormStateExcludesFieldUnderInvisibleAncestor() {
        // The hidden ancestor may be several levels up; effective-visibility
        // must walk the whole chain, not just the immediate parent.
        var field = new TestField();
        var hiddenAncestor = new Div(new Div(new Div(field)));
        hiddenAncestor.setVisible(false);
        var controller = new FormAIController(new Div(hiddenAncestor));

        var fields = formStateFields(controller);

        Assertions.assertEquals(0, fields.size(),
                "Field under an invisible ancestor must be excluded, got: "
                        + fields);
    }

    @Test
    void getFormStateLabelsFieldInsideDisabledContainerAsDisabled() {
        // Disabling a container disables the fields inside it for the user.
        // Such a field is read-only context: it must stay listed but carry the
        // "disabled" flag so the AI does not try to fill it.
        var field = new TestField();
        var container = new Div(field);
        container.setEnabled(false);
        var controller = new FormAIController(new Div(container));

        var fields = formStateFields(controller);

        Assertions.assertEquals(1, fields.size(),
                "Field inside a disabled container must still be listed, got: "
                        + fields);
        Assertions.assertTrue(fields.get(0).path("disabled").asBoolean(false),
                "Field inside a disabled container must be flagged disabled, "
                        + "got: " + fields.get(0));
    }

    @Test
    void getFormStateLabelsDisabledFieldsAsContext() {
        // A disabled field cannot be edited by the user, but the AI should
        // still see it (with its description) so it can reason about the form,
        // e.g. set the controlling field that enables it. The "disabled" flag
        // tells the AI it is read-only context; fill_form rejects writes to it.
        var enabled = new TestField();
        var disabled = new TestField();
        disabled.setEnabled(false);
        var controller = new FormAIController(new Div(enabled, disabled));

        var fields = formStateFields(controller);

        Assertions.assertEquals(2, fields.size(),
                "Disabled field must still be listed as context, got: "
                        + fields);
        var disabledNode = fields.get(1);
        Assertions.assertEquals(idOf(disabled),
                disabledNode.get("id").asString());
        Assertions.assertTrue(disabledNode.path("disabled").asBoolean(false),
                "Disabled field must carry \"disabled\": true, got: "
                        + disabledNode);
        Assertions.assertFalse(fields.get(0).has("disabled"),
                "Enabled field must not carry a disabled flag, got: "
                        + fields.get(0));
    }

    @Test
    void getFormStateLabelsReadOnlyFieldsAsContext() {
        // A read-only field is visible to the user but not editable; the AI
        // sees it as context with a "readOnly" flag and fill_form rejects
        // writes to it.
        var editable = new TestField();
        var readOnly = new TestField();
        readOnly.setReadOnly(true);
        var controller = new FormAIController(new Div(editable, readOnly));

        var fields = formStateFields(controller);

        Assertions.assertEquals(2, fields.size(),
                "Read-only field must still be listed as context, got: "
                        + fields);
        var readOnlyNode = fields.get(1);
        Assertions.assertEquals(idOf(readOnly),
                readOnlyNode.get("id").asString());
        Assertions.assertTrue(readOnlyNode.path("readOnly").asBoolean(false),
                "Read-only field must carry \"readOnly\": true, got: "
                        + readOnlyNode);
        Assertions.assertFalse(fields.get(0).has("readOnly"),
                "Editable field must not carry a readOnly flag, got: "
                        + fields.get(0));
    }

    @Test
    void getFormStateDoesNotFlagTurnLockedFieldsAsReadOnly() {
        // During a turn the controller locks every writable field read-only so
        // the user can't race the AI. That lock must not surface as a
        // "readOnly" context flag: only application-set read-only counts. A
        // field the application itself set read-only keeps its flag.
        var editable = new TestField();
        var readOnly = new TestField();
        readOnly.setReadOnly(true);
        var controller = new FormAIController(new Div(editable, readOnly));

        controller.onRequest(); // locks the editable field read-only
        try {
            var fields = formStateFields(controller);

            Assertions.assertEquals(2, fields.size());
            var editableNode = fields.get(0);
            Assertions.assertEquals(idOf(editable),
                    editableNode.get("id").asString());
            Assertions.assertFalse(editableNode.has("readOnly"),
                    "Turn-locked editable field must not be flagged readOnly, "
                            + "got: " + editableNode);
            Assertions.assertTrue(
                    fields.get(1).path("readOnly").asBoolean(false),
                    "Application-read-only field must stay flagged, got: "
                            + fields.get(1));
        } finally {
            controller.onResponse(null);
        }
    }

    @Test
    void getFormStateIncludesFieldOnceItBecomesVisible() {
        // Fields are re-discovered on every state read, so a field hidden at
        // one turn appears at the next once the controlling field reveals it.
        var field = new TestField();
        field.setVisible(false);
        var controller = new FormAIController(new Div(field));

        Assertions.assertEquals(0, formStateFields(controller).size(),
                "Hidden field must be absent while invisible");

        field.setVisible(true);

        Assertions.assertEquals(1, formStateFields(controller).size(),
                "Field must reappear once it is made visible again");
    }

    @Test
    void getFormStateReturnsEmptyFieldsArrayForEmptyForm() {
        var controller = new FormAIController(new Div());

        var fields = formStateFields(controller);

        Assertions.assertEquals(0, fields.size());
    }

    @Test
    void getFormStateRendersBigIntegerWithoutOverflow() {
        var field = new BigIntField();
        var huge = new java.math.BigInteger("99999999999999999999");
        field.setValue(huge);
        var controller = new FormAIController(new Div(field));

        var f = formStateFields(controller).get(0);

        Assertions.assertEquals(huge, f.get("value").bigIntegerValue());
    }

    @Test
    void getFormStateAutoIgnoresPasswordFieldEvenWithHints() {
        var visible = new TestField();
        var password = new PasswordField();
        password.setValue("secret");
        var controller = new FormAIController(new Div(visible, password));
        controller.describeField(password, "Account password");
        controller.fieldValueOptions(
                ValueOptions.forField(password).options(List.of("hunter2")));

        var fields = formStateFields(controller);

        Assertions.assertEquals(1, fields.size());
        Assertions.assertEquals(idOf(visible),
                fields.get(0).get("id").asString());
    }

    @Test
    void getFormStateAutoIgnoresPasswordField() {
        // PasswordField is treated as UNSUPPORTED so secret values do not
        // flow into the LLM tool payload by default. The developer does not
        // have to remember to call .ignoreField() for every form that happens
        // to
        // include one.
        var visible = new TestField();
        var password = new PasswordField();
        var controller = new FormAIController(new Div(visible, password));

        var fields = formStateFields(controller);

        Assertions.assertEquals(1, fields.size(),
                "PasswordField must never appear in the form-state result, "
                        + "got: " + fields);
        Assertions.assertEquals(idOf(visible),
                fields.get(0).get("id").asString());
    }

    @Test
    void getFormStateMarksEmailFieldWithEmailFormat() {
        // EmailField shares String as its value type with TextField, so the
        // format=email distinction has to come from a class-name exception
        // in the classifier rather than from HasValue's generic parameter.
        var field = new EmailField();
        var controller = new FormAIController(new Div(field));

        var f = formStateFields(controller).get(0);

        Assertions.assertEquals("string", f.path("type").asString());
        Assertions.assertEquals("email", f.path("format").asString(),
                "EmailField must carry format=email, got: " + f);
    }

    @Test
    void getFormStateMergesLabelDescriptionAndHelperText() {
        var field = new LabeledStringField();
        field.setLabel("Merchant");
        field.setHelperText("As shown on the receipt");
        var controller = new FormAIController(new Div(field));
        controller.describeField(field, "The vendor name");

        var f = formStateFields(controller).get(0);

        Assertions.assertEquals(
                "Merchant | The vendor name | As shown on the receipt",
                f.get("description").asString(),
                "label, registered description, and helper text must be joined "
                        + "in that order");
    }

    @Test
    void getFormStateMergeDescriptionKeepsTrailingPeriodsVerbatim() {
        // Real-world labels and helper texts often end in '.', and
        // user-supplied descriptions almost always do. The merge separator
        // must not collide with trailing punctuation: a '. ' separator would
        // produce "Merchant.. The vendor..". Pin that each part appears
        // intact and no '..' shows up.
        var field = new LabeledStringField();
        field.setLabel("Merchant.");
        field.setHelperText("As shown on the receipt.");
        var controller = new FormAIController(new Div(field));
        controller.describeField(field, "The vendor name.");

        var f = formStateFields(controller).get(0);
        var desc = f.get("description").asString();

        Assertions.assertFalse(desc.contains(".."),
                "Merge separator must not collide with trailing periods, "
                        + "got: " + desc);
        Assertions.assertTrue(desc.contains("Merchant."));
        Assertions.assertTrue(desc.contains("The vendor name."));
        Assertions.assertTrue(desc.contains("As shown on the receipt."));
    }

    @Test
    void getFormStateOmitsBlankDescriptionEntirely() {
        var field = new TestField();
        var controller = new FormAIController(new Div(field));

        var f = formStateFields(controller).get(0);

        Assertions.assertFalse(f.has("description"),
                "Empty merged description must be omitted, got: " + f);
    }

    @Test
    void valuesHiddenDefaultsToFalse() {
        var controller = new FormAIController(new Div(new TestField()));

        Assertions.assertFalse(controller.isFieldValuesHidden(),
                "Values must be sent by default");
    }

    @Test
    void getFormStateMasksEveryValueWhenValuesHidden() {
        // With the controller-level flag on, every field keeps its id,
        // description and type so the AI can still fill it, but its value is
        // masked: null + valueHidden, regardless of whether a value is set.
        var text = new TestField();
        text.setValue("secret");
        var number = new DoubleField();
        number.setValue(58.4);
        var empty = new TestField();
        var controller = new FormAIController(new Div(text, number, empty));
        controller.setFieldValuesHidden(true);

        var fields = formStateFields(controller);

        Assertions.assertEquals(3, fields.size(),
                "Fields must still be listed, got: " + fields);
        for (var f : fields) {
            Assertions.assertTrue(f.path("value").isNull(),
                    "Value must be masked as null, got: " + f);
            Assertions.assertTrue(f.path("valueHidden").asBoolean(false),
                    "Field must carry valueHidden=true, got: " + f);
        }
        var raw = findTool(controller.getTools(), "get_form_state")
                .execute(JacksonUtils.createObjectNode());
        Assertions.assertFalse(raw.contains("secret"),
                "No field value may leak into the payload, got: " + raw);
        Assertions.assertFalse(raw.contains("58.4"),
                "No field value may leak into the payload, got: " + raw);
    }

    @Test
    void getFormStateKeepsTypeMetadataWhenValuesHidden() {
        // Masking the value must not strip the type signal: the AI still needs
        // type/enum to know how to fill the field.
        var combo = new SingleSelectField<String>();
        combo.setItems("EUR", "USD");
        combo.setValue("EUR");
        var controller = new FormAIController(new Div(combo));
        controller.setFieldValuesHidden(true);

        var f = formStateFields(controller).get(0);

        Assertions.assertEquals("string", f.path("type").asString());
        var values = new ArrayList<String>();
        f.path("enum").forEach(n -> values.add(n.asString()));
        Assertions.assertEquals(List.of("EUR", "USD"), values,
                "Type/enum metadata must survive value masking, got: " + f);
        Assertions.assertTrue(f.path("value").isNull());
        Assertions.assertTrue(f.path("valueHidden").asBoolean(false));
    }

    @Test
    void getFormStateMapsStringValueTypeToTypeString() {
        assertTypeOnly(typeNodeFor(new TestField()), "string");
    }

    @Test
    void getFormStateMapsDoubleValueTypeToTypeNumber() {
        assertTypeOnly(typeNodeFor(new DoubleField()), "number");
    }

    @Test
    void getFormStateMapsIntegerValueTypeToTypeInteger() {
        assertTypeOnly(typeNodeFor(new IntField()), "integer");
    }

    @Test
    void getFormStateClassifiesDirectHasValueImplementation() {
        // All other tests reach HasValue<?, V> through AbstractField →
        // HasValueAndElement → HasValue, exercising type-variable
        // propagation. This test covers the simpler case: a Component that
        // declares its V argument directly on HasValue, with no intermediate
        // generic chain.
        assertTypeOnly(typeNodeFor(new DirectIntegerField()), "integer");
    }

    @Test
    void getFormStateMapsBigDecimalValueTypeToStringWithPattern() {
        assertTypeAndPattern(typeNodeFor(new BigDecField()), "string",
                "^-?\\d+(\\.\\d+)?$");
    }

    @Test
    void getFormStateMapsBooleanValueTypeToTypeBoolean() {
        assertTypeOnly(typeNodeFor(new BoolField()), "boolean");
    }

    @Test
    void getFormStateMapsLocalDateValueTypeToStringWithDateFormat() {
        assertTypeAndFormat(typeNodeFor(new DateField()), "string", "date");
    }

    @Test
    void getFormStateMapsLocalDateTimeValueTypeToStringWithDateTimeFormat() {
        assertTypeAndFormat(typeNodeFor(new DateTimeField()), "string",
                "date-time");
    }

    @Test
    void getFormStateMapsLocalTimeValueTypeToStringWithTimeFormat() {
        assertTypeAndFormat(typeNodeFor(new TimeField()), "string", "time");
    }

    @Test
    void getFormStateMapsLongValueTypeToTypeInteger() {
        // Integer is the obvious INTEGER case; Long is the case that breaks
        // when the classifier narrows its INTEGER detection to Integer +
        // BigInteger only. Pin Long classification separately so a
        // regression there cannot hide behind the IntField test.
        assertTypeOnly(typeNodeFor(new LongField()), "integer");
    }

    @Test
    void getFormStateMapsShortValueTypeToTypeInteger() {
        assertTypeOnly(typeNodeFor(new ShortField()), "integer");
    }

    @Test
    void getFormStateMapsByteValueTypeToTypeInteger() {
        assertTypeOnly(typeNodeFor(new ByteField()), "integer");
    }

    @Test
    void getFormStateMapsFloatValueTypeToTypeNumber() {
        // Float-valued HasValue fields must classify the same as Double
        // (NUMBER) — both round-trip through JSON's number type. A
        // classifier that recognises only Double would silently demote Float
        // fields to STRING.
        assertTypeOnly(typeNodeFor(new FloatField()), "number");
    }

    @Test
    void getFormStateResolvesValueTypeThroughNonGenericIntermediateInterface() {
        // The reflective walk that resolves HasValue's V argument has two
        // branches: one for parameterized intermediate interfaces, one for
        // raw classes that themselves extend a parameterized HasValue (e.g.
        // `interface MyField extends HasValue<E, Integer> {}`). The
        // raw-class branch must propagate the found type out of the
        // recursive call; if it discards the result, the field is silently
        // misclassified as STRING.
        assertTypeOnly(typeNodeFor(new IntegerViaNonGenericInterfaceField()),
                "integer");
    }

    @Test
    void getFormStateClassifiesHasLazyDataViewFieldAsSingleSelect() {
        // Real Vaadin selection components implement the data-view marker
        // interfaces (HasLazyDataView / HasListDataView / HasDataView) but
        // not HasItems. The classifier must accept any of these markers as a
        // selection signal — otherwise the field is treated as STRING and
        // applyType skips the path that enumerates the ListDataProvider's
        // items as `enum`. The visible-from-tests difference is precisely
        // that enum block.
        var field = new LazyDataViewField();
        field.supplyItems("alpha", "beta");
        var controller = new FormAIController(new Div(field));

        var f = formStateFields(controller).get(0);

        var values = new ArrayList<String>();
        f.path("enum").forEach(n -> values.add(n.asString()));
        Assertions.assertEquals(List.of("alpha", "beta"), values,
                "Field with HasLazyDataView marker must enumerate its "
                        + "ListDataProvider items as `enum`; an empty or "
                        + "missing enum means the field fell through to the "
                        + "non-selection branch. Got: " + f);
    }

    @Test
    void getFormStateClassifiesHasListDataViewFieldAsSingleSelect() {
        var field = new ListDataViewField();
        field.supplyItems("apple", "banana");
        var controller = new FormAIController(new Div(field));

        var f = formStateFields(controller).get(0);

        var values = new ArrayList<String>();
        f.path("enum").forEach(n -> values.add(n.asString()));
        Assertions.assertEquals(List.of("apple", "banana"), values,
                "Field with HasListDataView marker must enumerate its "
                        + "ListDataProvider items as `enum`. Got: " + f);
    }

    @Test
    void getFormStateClassifiesHasDataViewFieldAsSingleSelect() {
        var field = new DataViewField();
        field.supplyItems("x", "y");
        var controller = new FormAIController(new Div(field));

        var f = formStateFields(controller).get(0);

        var values = new ArrayList<String>();
        f.path("enum").forEach(n -> values.add(n.asString()));
        Assertions.assertEquals(List.of("x", "y"), values,
                "Field with HasDataView marker must enumerate its "
                        + "ListDataProvider items as `enum`. Got: " + f);
    }

    @Test
    void getFormStateMultiSelectDoesNotSetTypeAtNodeLevel() {
        // MultiSelect's schema lives entirely in the items block:
        // `{ array: true, items: { type: "string", ... } }`. The node itself
        // must not also carry `type` — a duplicate type at the node level
        // would conflict with the array shape and confuse strict schema
        // consumers.
        var f = typeNodeFor(new MultiSelectField<String>());

        Assertions.assertTrue(f.path("array").asBoolean());
        Assertions.assertTrue(f.path("type").isMissingNode(),
                "Multi-select must not duplicate type at the node level "
                        + "(it belongs inside items), got: " + f);
    }

    @Test
    void getFormStateMultiSelectWithValueOptionsKeepsNodeShape() {
        // Combining multi-select with fieldValueOptions must still leave the
        // node-level shape clean: array=true, no node-level type, and
        // queryable/enum sit inside the items block.
        var multi = new MultiSelectField<String>();
        var controller = new FormAIController(new Div(multi));
        controller.fieldValueOptions(ValueOptions.forField(multi)
                .options((filter, limit) -> List.of("a", "b")));

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.path("array").asBoolean());
        Assertions.assertTrue(f.path("type").isMissingNode(),
                "Multi-select with fieldValueOptions must not set node-level "
                        + "type, got: " + f);
        Assertions.assertTrue(f.path("queryable").isMissingNode(),
                "queryable must live inside items, not on the node, got: " + f);
        Assertions.assertTrue(f.path("items").path("queryable").asBoolean(),
                "Queryable signal must sit on items, got: " + f);
    }

    @Test
    void getFormStateMultiSelectWithValueOptionsRendersValueAsArray() {
        // Multi-select value is a Set; serialising it as a single string
        // (the path taken when fieldValueOptions on non-selection fields
        // rewrites
        // to string) would corrupt the payload. Pin that multi-select wins
        // over the fieldValueOptions value-rewrite.
        var multi = new MultiSelectField<String>();
        multi.setItems("a", "b", "c");
        multi.setValue(Set.of("a", "c"));
        var controller = new FormAIController(new Div(multi));
        controller.fieldValueOptions(ValueOptions.forField(multi)
                .options((filter, limit) -> List.of("a", "b", "c")));

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.path("value").isArray(),
                "Multi-select with fieldValueOptions must still render its value "
                        + "as a JSON array, got: " + f.path("value"));
        var rendered = new ArrayList<String>();
        f.path("value").forEach(n -> rendered.add(n.asString()));
        Assertions.assertEquals(2, rendered.size());
        Assertions.assertTrue(rendered.containsAll(List.of("a", "c")),
                "Array must carry the selected labels, got: " + rendered);
    }

    @Test
    void getFormStateSingleSelectWithValueOptionsRendersValueAsLabel() {
        // The mirror case for the multi-select test above: the
        // selection-aware path must keep using the field's own label
        // renderer for the current value, not the generic fieldValueOptions
        // value-rewrite branch.
        var combo = new SingleSelectField<Project>();
        combo.setItems(new Project("P-1", "Alpha"), new Project("P-2", "Beta"));
        combo.setItemLabelGenerator(p -> p.code() + " " + p.name());
        combo.setValue(new Project("P-2", "Beta"));
        var controller = new FormAIController(new Div(combo));
        controller.fieldValueOptions(
                ValueOptions.forField(combo).options(
                        (filter, limit) -> List.of("P-1 Alpha", "P-2 Beta")),
                label -> null);

        var f = formStateFields(controller).get(0);

        Assertions.assertEquals("P-2 Beta", f.path("value").asString(),
                "Single-select value must come from the field's label "
                        + "generator, not Object#toString(), got: "
                        + f.path("value"));
    }

    @Test
    void getFormStateDescribedFieldHasNoQueryableFlag() {
        // describe() registers a hint without a fieldValueOptions callback. The
        // schema must distinguish "has a hint entry" from "has a query
        // callback": only the latter is queryable. Otherwise the LLM would
        // call query_field_options against a field that has no registered
        // query and get a generic error back. Use a selection-typed field
        // so applySelectionOptions runs; a plain STRING field skips that
        // branch entirely and would not exercise this regression.
        var combo = new SingleSelectField<String>();
        var controller = new FormAIController(new Div(combo));
        controller.describeField(combo, "Some descriptive text");

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.path("queryable").isMissingNode(),
                "A field with describe() but no fieldValueOptions must not carry "
                        + "queryable=true, got: " + f);
    }

    @Test
    void getFormStateQueryableFieldDoesNotMixListDataProviderEnum() {
        // When fieldValueOptions(BiFunction) is registered on a selection
        // component whose backing ListDataProvider also has items,
        // queryable=true must short-circuit before the data-provider items
        // would be enumerated as enum. Both signals in the same payload
        // would tell the LLM "either query me, or pick from this list" —
        // the registration says only one is authoritative.
        var combo = new SingleSelectField<String>();
        combo.setItems("apple", "banana", "cherry");
        var controller = new FormAIController(new Div(combo));
        controller.fieldValueOptions(ValueOptions.forField(combo)
                .options((filter, limit) -> List.of("apple", "banana")));

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.path("queryable").asBoolean());
        Assertions.assertTrue(f.path("enum").isMissingNode(),
                "queryable signal must suppress the ListDataProvider enum "
                        + "fallback, got: " + f);
    }

    @Test
    void getFormStateRendersNonFiniteNumericValueAsExplicitJsonNull() {
        // Sister test to
        // getFormStateRendersNumericInfinityAsFiniteJsonNumber: that one
        // only checks the raw output lacks "Infinity"/"NaN" tokens, which
        // is also satisfied by emitting no value key at all. Pin
        // explicitly that the key is present and JSON null, so missing-key
        // regressions don't slip through.
        var field = new DoubleField();
        field.setValue(Double.NaN);
        var controller = new FormAIController(new Div(field));

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.has("value"),
                "value key must be present even for non-finite numerics, "
                        + "got: " + f);
        Assertions.assertTrue(f.path("value").isNull(),
                "Non-finite numeric must serialize as JSON null, got: "
                        + f.path("value"));
    }

    @Test
    void getFormStateRendersListDataProviderItemsViaLabelGenerator() {
        // ListDataProvider items typically aren't strings — a
        // ComboBox<Project> backed by a list of Project records needs its
        // ItemLabelGenerator to be invoked when the enum block is built.
        // Without the generator, items render via toString(), which leaks
        // Java internals (e.g. "Project[code=P-1, ...]") to the LLM.
        var combo = new SingleSelectField<Project>();
        combo.setItems(new Project("P-1", "Alpha"), new Project("P-2", "Beta"));
        combo.setItemLabelGenerator(p -> p.code() + " " + p.name());
        var controller = new FormAIController(new Div(combo));

        var f = formStateFields(controller).get(0);

        var values = new ArrayList<String>();
        f.path("enum").forEach(n -> values.add(n.asString()));
        Assertions.assertEquals(List.of("P-1 Alpha", "P-2 Beta"), values,
                "Enum entries must use the field's label generator output, "
                        + "got: " + values);
    }

    @Test
    void getFormStateMapsSingleSelectFieldToStringType() {
        // A selection component without registered options or items
        // surfaces as bare "type": "string" — no enum, no queryable.
        var f = typeNodeFor(new SingleSelectField<String>());

        Assertions.assertEquals("string", f.path("type").asString());
        Assertions.assertTrue(f.path("enum").isMissingNode());
        Assertions.assertTrue(f.path("queryable").isMissingNode());
    }

    @Test
    void getFormStateMapsMultiSelectFieldToArrayWithItemsBlock() {
        var f = typeNodeFor(new MultiSelectField<String>());

        Assertions.assertTrue(f.path("array").asBoolean(),
                "MultiSelect must be encoded with array=true");
        Assertions.assertEquals("string",
                f.path("items").path("type").asString());
    }

    @Test
    void getFormStateEncodesEnumForFixedValueOptions() {
        // Fixed fieldValueOptions registered against a selection component
        // surface in the JSON output as enum. The same registration against
        // a non-selection field would not (LLM uses query_field_options
        // instead).
        var combo = new SingleSelectField<String>();
        var controller = new FormAIController(new Div(combo));
        controller.fieldValueOptions(ValueOptions.forField(combo)
                .options(List.of("EUR", "USD", "GBP")));

        var f = formStateFields(controller).get(0);

        Assertions.assertEquals("string", f.path("type").asString());
        Assertions.assertTrue(f.path("queryable").isMissingNode(),
                "Fixed-options field must not carry queryable=true");
        var values = new ArrayList<String>();
        f.path("enum").forEach(n -> values.add(n.asString()));
        Assertions.assertEquals(List.of("EUR", "USD", "GBP"), values);
    }

    @Test
    void getFormStateEncodesQueryableForBiFunctionValueOptions() {
        var combo = new SingleSelectField<String>();
        var controller = new FormAIController(new Div(combo));
        controller.fieldValueOptions(ValueOptions.forField(combo)
                .options((filter, limit) -> List.of("Apollo", "Polaris")));

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.path("queryable").asBoolean(),
                "Queryable-options field must carry queryable=true, got: " + f);
        Assertions.assertTrue(f.path("enum").isMissingNode(),
                "Queryable-options field must not enumerate options inline, "
                        + "got: " + f);
    }

    @Test
    void getFormStateExposesEnumForFixedValueOptionsOnStringField() {
        var field = new TestField();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(ValueOptions.forField(field)
                .options(List.of("EUR", "USD", "GBP")));

        var f = formStateFields(controller).get(0);

        var values = new ArrayList<String>();
        f.path("enum").forEach(n -> values.add(n.asString()));
        Assertions.assertEquals(List.of("EUR", "USD", "GBP"), values);
    }

    @Test
    void getFormStateExposesQueryableForBiFunctionValueOptionsOnStringField() {
        var field = new TestField();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(ValueOptions.forField(field)
                .options((filter, limit) -> List.of("Apollo", "Polaris")));

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.path("queryable").asBoolean());
    }

    @Test
    void getFormStateUsesFallbackWhenLabelGeneratorReturnsNull() {
        var combo = new SingleSelectField<String>();
        combo.setItems("only");
        combo.setItemLabelGenerator(item -> null);
        combo.setValue("only");
        var controller = new FormAIController(new Div(combo));

        var f = formStateFields(controller).get(0);

        Assertions.assertFalse(f.path("value").isNull());
    }

    @Test
    void getFormStateExposesListDataProviderItemsAsEnum() {
        var combo = new SingleSelectField<String>();
        combo.setItems("alpha", "beta", "gamma");
        var controller = new FormAIController(new Div(combo));

        var f = formStateFields(controller).get(0);

        var values = new ArrayList<String>();
        f.path("enum").forEach(n -> values.add(n.asString()));
        Assertions.assertEquals(List.of("alpha", "beta", "gamma"), values,
                "ListDataProvider items must populate the enum when no "
                        + "fieldValueOptions hint is registered");
    }

    @Test
    void getFormStateEnumIsCappedForLargeListDataProvider() {
        var combo = new SingleSelectField<String>();
        var items = new ArrayList<String>();
        for (var i = 0; i < 250; i++) {
            items.add("item-" + i);
        }
        combo.setItems(items);
        var controller = new FormAIController(new Div(combo));

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.path("enum").size() <= 200,
                "ListDataProvider items must be capped at 200 entries to "
                        + "match the query_field_options cap, got: "
                        + f.path("enum").size());
    }

    @Test
    void getFormStateBigDecimalValueMatchesPatternForScientificInput() {
        var field = new BigDecField();
        field.setValue(new java.math.BigDecimal("1E2"));
        var controller = new FormAIController(new Div(field));

        var f = formStateFields(controller).get(0);
        var rendered = f.get("value").asString();

        Assertions.assertTrue(rendered.matches("^-?\\d+(\\.\\d+)?$"),
                "Rendered BigDecimal value must satisfy the schema pattern, "
                        + "got: " + rendered);
    }

    @Test
    void getFormStateRendersNumericInfinityAsFiniteJsonNumber() {
        var field = new DoubleField();
        field.setValue(Double.POSITIVE_INFINITY);
        var controller = new FormAIController(new Div(field));

        var raw = findTool(controller.getTools(), "get_form_state")
                .execute(JacksonUtils.createObjectNode());

        Assertions.assertFalse(raw.contains("Infinity") || raw.contains("NaN"),
                "Tool result must not embed non-standard JSON tokens for "
                        + "non-finite doubles, got: " + raw);
    }

    @Test
    void getFormStateBackendDataProviderProducesNoEnum() {
        var combo = new SingleSelectField<String>();
        combo.setDataProvider(new CallbackDataProvider<String, String>(
                q -> List.of("a", "b").stream(), q -> 2));
        var controller = new FormAIController(new Div(combo));

        var f = formStateFields(controller).get(0);

        Assertions.assertEquals("string", f.path("type").asString());
        Assertions.assertTrue(f.path("enum").isMissingNode(),
                "Backend data provider must not contribute an enum");
        Assertions.assertTrue(f.path("queryable").isMissingNode(),
                "Backend data provider alone must not flag queryable");
    }

    @Test
    void getFormStateRendersCurrentValuesPerType() {
        var text = new TestField();
        text.setValue("Trattoria");
        var integer = new IntField();
        integer.setValue(42);
        var number = new DoubleField();
        number.setValue(58.4);
        var bigDecimal = new BigDecField();
        bigDecimal.setValue(new BigDecimal("58.40"));
        var date = new DateField();
        date.setValue(LocalDate.of(2026, 5, 4));
        var bool = new BoolField();
        bool.setValue(true);
        var combo = new SingleSelectField<String>();
        combo.setItems("Meals", "Travel");
        combo.setValue("Meals");
        var controller = new FormAIController(
                new Div(text, integer, number, bigDecimal, date, bool, combo));

        var fields = formStateFields(controller);

        Assertions.assertEquals("Trattoria",
                fields.get(0).get("value").asString());
        Assertions.assertEquals(42, fields.get(1).get("value").asInt());
        Assertions.assertEquals(58.4, fields.get(2).get("value").asDouble(),
                1e-9);
        Assertions.assertEquals("58.40", fields.get(3).get("value").asString());
        Assertions.assertEquals("2026-05-04",
                fields.get(4).get("value").asString());
        Assertions.assertTrue(fields.get(5).get("value").asBoolean());
        Assertions.assertEquals("Meals", fields.get(6).get("value").asString());
    }

    @Test
    void getFormStateContinuesPastFieldWhoseGetValueThrows() {
        var good = new TestField();
        good.setValue("ok");
        var bad = new ThrowingField();
        var trailing = new TestField();
        trailing.setValue("trailing");
        var controller = new FormAIController(new Div(good, bad, trailing));

        var fields = formStateFields(controller);

        Assertions.assertEquals(3, fields.size(),
                "Failing field must still produce an entry so the LLM can "
                        + "see that the field exists");
        Assertions.assertEquals("ok", fields.get(0).path("value").asString(),
                "Fields before a failing field must still be present");
        Assertions.assertEquals("trailing",
                fields.get(2).path("value").asString(),
                "Fields after a failing field must still be present");
        Assertions.assertEquals(idOf(bad), fields.get(1).path("id").asString(),
                "Failed field entry must carry its id so the LLM can "
                        + "correlate, got: " + fields.get(1));
        Assertions.assertFalse(fields.get(1).path("error").isMissingNode(),
                "Failed field entry must carry an error marker, got: "
                        + fields.get(1));
    }

    @Test
    void getFormStateRendersStringValueWhenValueOptionsOverrideType() {
        // fieldValueOptions on a non-String field rewrites the schema type to
        // "string" + enum, but applyValue keeps following the field's
        // original FormFieldType — so the value half of the payload
        // disagrees with the schema half. A strict consumer validating
        // type=string against a JSON integer would reject the result.
        var field = new IntField();
        field.setValue(2);
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(
                ValueOptions.forField(field).options(List.of("1", "2", "3")),
                Integer::parseInt);

        var f = formStateFields(controller).get(0);

        Assertions.assertEquals("string", f.path("type").asString());
        Assertions.assertTrue(f.path("value").isString(),
                "value must match the declared schema type, got: "
                        + f.path("value"));
    }

    @Test
    void getFormStateRendersNullForEmptyValues() {
        var text = new TestField();
        var date = new DateField();
        var multi = new MultiSelectField<String>();
        var controller = new FormAIController(new Div(text, date, multi));

        var fields = formStateFields(controller);

        Assertions.assertTrue(fields.get(0).get("value").isNull(),
                "Empty text value must serialize as JSON null");
        Assertions.assertTrue(fields.get(1).get("value").isNull(),
                "Empty date value must serialize as JSON null");
        Assertions.assertTrue(fields.get(2).get("value").isNull(),
                "Empty multi-select value must serialize as JSON null");
    }

    @Test
    void getFormStateMultiSelectUsesItemsBlock() {
        var multi = new MultiSelectField<String>();
        multi.setItems("a", "b", "c");
        multi.setValue(Set.of("a", "c"));
        var controller = new FormAIController(new Div(multi));

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.path("array").asBoolean());
        Assertions.assertEquals("string",
                f.path("items").path("type").asString());
        var enumValues = new ArrayList<String>();
        f.path("items").path("enum").forEach(n -> enumValues.add(n.asString()));
        Assertions.assertTrue(enumValues.containsAll(List.of("a", "b", "c")),
                "Items block must enumerate the data-provider labels, got: "
                        + enumValues);
        var rendered = new ArrayList<String>();
        f.path("value").forEach(n -> rendered.add(n.asString()));
        Assertions.assertTrue(
                rendered.containsAll(List.of("a", "c")) && rendered.size() == 2,
                "Multi-select value must render as a JSON array of label "
                        + "strings, got: " + rendered);
    }

    @Test
    void getFormStateMultiSelectQueryableUsesItemsQueryable() {
        var multi = new MultiSelectField<String>();
        var controller = new FormAIController(new Div(multi));
        controller.fieldValueOptions(ValueOptions.forField(multi)
                .options((filter, limit) -> List.of("x")));

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.path("array").asBoolean());
        Assertions.assertTrue(f.path("items").path("queryable").asBoolean(),
                "Queryable multi-select must carry queryable=true on the "
                        + "items block, got: " + f);
        Assertions.assertTrue(f.path("items").path("enum").isMissingNode(),
                "Queryable multi-select must not enumerate options inline, "
                        + "got: " + f);
    }

    @Test
    void getFormStateSchemaIsStaticAndEmpty() {
        var controller = new FormAIController(new Div(new TestField()));

        var schema = findTool(controller.getTools(), "get_form_state")
                .getParametersSchema();
        var node = json(schema);

        Assertions.assertEquals("object", node.path("type").asString());
        Assertions.assertTrue(node.path("properties").isObject());
        Assertions.assertEquals(0, node.path("properties").size(),
                "get_form_state must take no parameters");
    }

    @Test
    void getFormStateReflectsLiveLabelChanges() {
        var field = new LabeledStringField();
        field.setLabel("Old");
        var controller = new FormAIController(new Div(field));

        Assertions.assertEquals("Old", formStateFields(controller).get(0)
                .get("description").asString());

        field.setLabel("New");

        Assertions.assertEquals("New",
                formStateFields(controller).get(0).get("description")
                        .asString(),
                "description must be re-read from the live field on each "
                        + "call");
    }

    @Test
    void getFormStateAssignsStableIdAcrossCalls() {
        var field = new TestField();
        var controller = new FormAIController(new Div(field));

        var firstId = formStateFields(controller).get(0).get("id").asString();
        var secondId = formStateFields(controller).get(0).get("id").asString();

        Assertions.assertEquals(firstId, secondId);
        Assertions.assertEquals(firstId, idOf(field),
                "id in get_form_state must match the id stored on the "
                        + "component");
    }

    @Test
    void getFormStateProducesCompletePayloadForRealisticForm() {
        // End-to-end smoke test that mirrors the RFC's expense-form example.
        // Catches regressions in how the per-field slices interact when N >
        // 1 — schema/value/description leaks across fields, ordering bugs
        // that only surface with multiple entries, hint registrations
        // affecting the wrong id.
        var merchant = new LabeledStringField();
        merchant.setLabel("Merchant");
        merchant.setHelperText("As shown on the receipt");
        merchant.setValue("Trattoria Toscana");

        var amount = new DoubleField();
        var currency = new SingleSelectField<String>();
        var date = new DateField();
        var category = new SingleSelectField<String>();
        var notes = new TestField();

        var form = new Div(merchant, amount, currency, date, category, notes);
        var controller = new FormAIController(form);
        controller.describeField(merchant, "The vendor name");
        controller.fieldValueOptions(ValueOptions.forField(currency)
                .options(List.of("EUR", "USD", "GBP")));
        controller.fieldValueOptions(ValueOptions.forField(category).options(
                List.of("Travel", "Meals", "Software", "Office", "Other")));

        // Execute first so the controller walks the form and assigns ids to
        // fields that had no hints registered.
        var actual = json(findTool(controller.getTools(), "get_form_state")
                .execute(JacksonUtils.createObjectNode()));

        var expected = json(
                """
                        {
                          "fields": [
                            {
                              "id": "<merchant>",
                              "description": "Merchant | The vendor name | As shown on the receipt",
                              "type": "string",
                              "value": "Trattoria Toscana"
                            },
                            {
                              "id": "<amount>",
                              "type": "number",
                              "value": null
                            },
                            {
                              "id": "<currency>",
                              "type": "string",
                              "enum": ["EUR", "USD", "GBP"],
                              "value": null
                            },
                            {
                              "id": "<date>",
                              "type": "string",
                              "format": "date",
                              "value": null
                            },
                            {
                              "id": "<category>",
                              "type": "string",
                              "enum": ["Travel", "Meals", "Software", "Office", "Other"],
                              "value": null
                            },
                            {
                              "id": "<notes>",
                              "type": "string",
                              "value": null
                            }
                          ]
                        }"""
                        .replace("<merchant>", idOf(merchant))
                        .replace("<amount>", idOf(amount))
                        .replace("<currency>", idOf(currency))
                        .replace("<date>", idOf(date))
                        .replace("<category>", idOf(category))
                        .replace("<notes>", idOf(notes)));

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getToolsAlwaysIncludesFormState() {
        // Unlike query_field_options, the form-state tool is always present
        // — there is no per-field configuration that gates it.
        var controllerNoHints = new FormAIController(new Div());
        Assertions.assertTrue(
                controllerNoHints.getTools().stream()
                        .anyMatch(t -> t.getName().equals("get_form_state")),
                "get_form_state must be exposed even when the form has no "
                        + "fields");

        var field = new TestField();
        var controllerWithIgnored = new FormAIController(new Div(field));
        controllerWithIgnored.ignoreField(field);
        Assertions.assertTrue(
                controllerWithIgnored.getTools().stream()
                        .anyMatch(t -> t.getName().equals("get_form_state")),
                "get_form_state must be exposed even when every field is "
                        + "ignored");
    }

    private JsonNode typeNodeFor(HasValue<?, ?> field) {
        var controller = new FormAIController(new Div((Component) field));
        return formStateFields(controller).get(0);
    }

    private void assertTypeOnly(JsonNode field, String expectedType) {
        Assertions.assertEquals(expectedType, field.path("type").asString(),
                "Expected type=" + expectedType + " for " + field);
        Assertions.assertTrue(field.path("format").isMissingNode(),
                "Expected no format for " + field);
        Assertions.assertTrue(field.path("pattern").isMissingNode(),
                "Expected no pattern for " + field);
    }

    private void assertTypeAndFormat(JsonNode field, String expectedType,
            String expectedFormat) {
        Assertions.assertEquals(expectedType, field.path("type").asString());
        Assertions.assertEquals(expectedFormat,
                field.path("format").asString());
    }

    private void assertTypeAndPattern(JsonNode field, String expectedType,
            String expectedPattern) {
        Assertions.assertEquals(expectedType, field.path("type").asString());
        Assertions.assertEquals(expectedPattern,
                field.path("pattern").asString());
    }
}
