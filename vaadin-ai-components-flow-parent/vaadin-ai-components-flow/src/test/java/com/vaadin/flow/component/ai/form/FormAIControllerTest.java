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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ai.provider.LLMProvider.ToolSpec;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

class FormAIControllerTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Nested
    class Discovery {

        @Test
        void labelsAreNormalizedToAsciiIdentifiers() {
            var first = new TextField("Project / Customer #");
            var form = new FormLayout(first);
            var controller = new FormAIController(form);

            Assertions.assertEquals("project_customer",
                    controller.getIdentifier(first));
        }

        @Test
        void passwordFieldsAreIgnoredImplicitly() {
            var visible = new TextField("Name");
            var secret = new PasswordField("Password");
            var form = new FormLayout(visible, secret);
            var controller = new FormAIController(form);

            var tool = fillFormTool(controller);
            Assertions.assertTrue(tool.getParametersSchema().contains("name"));
            Assertions.assertFalse(
                    tool.getParametersSchema().contains("password"));
        }

        @Test
        void duplicateIdentifierThrowsAtBuildTime() {
            var first = new TextField("Amount");
            var second = new NumberField("Amount");
            var form = new FormLayout(first, second);
            var controller = new FormAIController(form);

            var ex = Assertions.assertThrows(IllegalStateException.class,
                    controller::getTools);
            Assertions.assertTrue(ex.getMessage().contains("Duplicate"));
            Assertions.assertTrue(ex.getMessage().contains(".as("));
        }

        @Test
        void asOverrideBreaksLabelCollision() {
            var amountMin = new NumberField("Amount");
            var amountMax = new NumberField("Amount");
            var form = new FormLayout(amountMin, amountMax);
            var controller = new FormAIController(form)
                    .as(amountMin, "amount_min")
                    .as(amountMax, "amount_max");

            var schema = fillFormTool(controller).getParametersSchema();
            Assertions.assertTrue(schema.contains("amount_min"));
            Assertions.assertTrue(schema.contains("amount_max"));
        }

        @Test
        void unlabeledFieldThrowsWithGuidance() {
            var unlabeled = new TextField();
            var form = new FormLayout(unlabeled);
            var controller = new FormAIController(form);

            var ex = Assertions.assertThrows(IllegalStateException.class,
                    controller::getTools);
            Assertions.assertTrue(ex.getMessage().contains(".as("));
        }

        @Test
        void ignoredFieldNotExposedToLLM() {
            var visible = new TextField("Visible");
            var hidden = new TextField("Internal");
            var form = new FormLayout(visible, hidden);
            var controller = new FormAIController(form).ignore(hidden);

            var schema = fillFormTool(controller).getParametersSchema();
            Assertions.assertTrue(schema.contains("visible"));
            Assertions.assertFalse(schema.contains("internal"));
        }
    }

    @Nested
    class Schema {

        @Test
        void coversCommonFieldTypes() {
            var name = new TextField("Name");
            var notes = new TextArea("Notes");
            var email = new EmailField("Email");
            var amount = new NumberField("Amount");
            var count = new IntegerField("Count");
            var price = new BigDecimalField("Price");
            var date = new DatePicker("Date");
            var ts = new DateTimePicker("Timestamp");
            var time = new TimePicker("Time");
            var done = new Checkbox("Done");
            var form = new FormLayout(name, notes, email, amount, count, price,
                    date, ts, time, done);
            var controller = new FormAIController(form);

            var schema = parseSchema(controller);
            Assertions.assertEquals("string",
                    schema.path("properties").path("name").path("type")
                            .asString());
            Assertions.assertEquals("string",
                    schema.path("properties").path("notes").path("type")
                            .asString());
            Assertions.assertEquals("email",
                    schema.path("properties").path("email").path("format")
                            .asString());
            Assertions.assertEquals("number",
                    schema.path("properties").path("amount").path("type")
                            .asString());
            Assertions.assertEquals("integer",
                    schema.path("properties").path("count").path("type")
                            .asString());
            Assertions.assertEquals("string",
                    schema.path("properties").path("price").path("type")
                            .asString());
            Assertions.assertFalse(schema.path("properties").path("price")
                    .path("pattern").isMissingNode());
            Assertions.assertEquals("date", schema.path("properties")
                    .path("date").path("format").asString());
            Assertions.assertEquals("date-time", schema.path("properties")
                    .path("timestamp").path("format").asString());
            Assertions.assertEquals("time", schema.path("properties")
                    .path("time").path("format").asString());
            Assertions.assertEquals("boolean", schema.path("properties")
                    .path("done").path("type").asString());
        }

        @Test
        void inMemoryComboBoxItemsBecomeEnum() {
            var currency = new ComboBox<String>("Currency");
            currency.setItems("EUR", "USD", "GBP");
            var form = new FormLayout(currency);
            var controller = new FormAIController(form);

            JsonNode schema = parseSchema(controller);
            JsonNode enumNode = schema.path("properties").path("currency")
                    .path("enum");
            Assertions.assertTrue(enumNode.isArray());
            Assertions.assertEquals(3, enumNode.size());
        }

        @Test
        void multiSelectEmitsArrayWithItemEnum() {
            var tags = new MultiSelectComboBox<String>("Tags");
            tags.setItems("alpha", "beta");
            var form = new FormLayout(tags);
            var controller = new FormAIController(form);

            JsonNode schema = parseSchema(controller);
            Assertions.assertEquals("array",
                    schema.path("properties").path("tags").path("type")
                            .asString());
            JsonNode items = schema.path("properties").path("tags")
                    .path("items");
            Assertions.assertEquals("string", items.path("type").asString());
            Assertions.assertEquals(2, items.path("enum").size());
        }

        @Test
        void allowedValuesOverrideTakesPrecedence() {
            var currency = new ComboBox<String>("Currency");
            var form = new FormLayout(currency);
            var controller = new FormAIController(form).allowedValues(currency,
                    List.of("EUR", "USD"));

            JsonNode schema = parseSchema(controller);
            JsonNode enumNode = schema.path("properties").path("currency")
                    .path("enum");
            Assertions.assertEquals(2, enumNode.size());
        }

        @Test
        void descriptionMergesLabelHelperAndOverride() {
            var amount = new NumberField("Amount");
            amount.setHelperText("In euros");
            var form = new FormLayout(amount);
            var controller = new FormAIController(form).describe(amount,
                    "Total cost incl. tax");

            JsonNode schema = parseSchema(controller);
            String desc = schema.path("properties").path("amount")
                    .path("description").asString();
            Assertions.assertTrue(desc.contains("Amount"));
            Assertions.assertTrue(desc.contains("Total cost"));
            Assertions.assertTrue(desc.contains("In euros"));
        }

        @Test
        void currentValueSuffixAppearsForPrePopulatedFields() {
            var merchant = new TextField("Merchant");
            merchant.setValue("Trattoria Toscana");
            var form = new FormLayout(merchant);
            var controller = new FormAIController(form);

            JsonNode schema = parseSchema(controller);
            String desc = schema.path("properties").path("merchant")
                    .path("description").asString();
            Assertions.assertTrue(
                    desc.contains("(current: Trattoria Toscana)"),
                    "Expected current-value suffix, got: " + desc);
        }
    }

    @Nested
    class FillForm {

        @Test
        void writesPrimitiveValues() {
            var merchant = new TextField("Merchant");
            var amount = new NumberField("Amount");
            var date = new DatePicker("Date");
            var done = new Checkbox("Done");
            var form = new FormLayout(merchant, amount, date, done);
            var controller = new FormAIController(form);

            String result = fillFormTool(controller).execute(json("""
                    {"merchant":"Trattoria","amount":58.4,"date":"2026-05-04",\
                    "done":true}"""));

            Assertions.assertEquals("Trattoria", merchant.getValue());
            Assertions.assertEquals(58.4, amount.getValue());
            Assertions.assertEquals(LocalDate.of(2026, 5, 4), date.getValue());
            Assertions.assertTrue(done.getValue());
            Assertions.assertTrue(result.contains("Current state"));
        }

        @Test
        void writesTypedValuesForDateTimePickerAndTimePicker() {
            var ts = new DateTimePicker("Timestamp");
            var time = new TimePicker("Time");
            var form = new FormLayout(ts, time);
            var controller = new FormAIController(form);

            fillFormTool(controller).execute(json(
                    "{\"timestamp\":\"2026-05-04T10:15:00\",\"time\":\"14:30:00\"}"));

            Assertions.assertEquals(LocalDateTime.of(2026, 5, 4, 10, 15),
                    ts.getValue());
            Assertions.assertEquals(LocalTime.of(14, 30), time.getValue());
        }

        @Test
        void bigDecimalFieldParsesIsoNumericString() {
            var price = new BigDecimalField("Price");
            var form = new FormLayout(price);
            var controller = new FormAIController(form);

            fillFormTool(controller).execute(json("{\"price\":\"1500.00\"}"));

            Assertions.assertEquals(new BigDecimal("1500.00"), price.getValue());
        }

        @Test
        void invalidDateLeavesFieldUntouchedAndReportsRejection() {
            var date = new DatePicker("Date");
            var form = new FormLayout(date);
            var controller = new FormAIController(form);

            String result = fillFormTool(controller)
                    .execute(json("{\"date\":\"yesterday\"}"));

            Assertions.assertNull(date.getValue());
            Assertions.assertTrue(result.contains("Rejected"),
                    "Expected a Rejected block, got: " + result);
            Assertions.assertTrue(result.contains("date"));
        }

        @Test
        void singleSelectResolvesFromLabel() {
            var category = new ComboBox<String>("Category");
            category.setItems("Travel", "Meals", "Software");
            var form = new FormLayout(category);
            var controller = new FormAIController(form);

            fillFormTool(controller).execute(json("{\"category\":\"Meals\"}"));

            Assertions.assertEquals("Meals", category.getValue());
        }

        @Test
        void singleSelectRejectsUnknownEnumValue() {
            var currency = new ComboBox<String>("Currency");
            currency.setItems("EUR", "USD");
            var form = new FormLayout(currency);
            var controller = new FormAIController(form);

            String result = fillFormTool(controller)
                    .execute(json("{\"currency\":\"Euro\"}"));

            Assertions.assertNull(currency.getValue());
            Assertions.assertTrue(result.contains("Rejected"));
            Assertions.assertTrue(result.contains("Allowed: EUR, USD"));
        }

        @Test
        void multiSelectResolvesArrayOfStrings() {
            var tags = new MultiSelectComboBox<String>("Tags");
            tags.setItems("alpha", "beta", "gamma");
            var form = new FormLayout(tags);
            var controller = new FormAIController(form);

            fillFormTool(controller)
                    .execute(json("{\"tags\":[\"alpha\",\"gamma\"]}"));

            Assertions.assertEquals(new LinkedHashSet<>(List.of("alpha", "gamma")),
                    tags.getValue());
        }

        @Test
        void resolveItemFromStringMapsLabelToBackendItem() {
            record Project(String code, String name) {
            }
            var projects = List.of(new Project("P-1", "Apollo"),
                    new Project("P-2", "Polaris"));
            var picker = new ComboBox<Project>("Project");
            picker.setItems(projects);
            picker.setItemLabelGenerator(p -> p.name() + " #" + p.code());
            var form = new FormLayout(picker);
            var controller = new FormAIController(form).resolveItemFromString(
                    picker, label -> projects.stream()
                            .filter(p -> (p.name() + " #" + p.code())
                                    .equals(label))
                            .findFirst().orElse(null));

            fillFormTool(controller)
                    .execute(json("{\"project\":\"Apollo #P-1\"}"));

            Assertions.assertEquals(projects.get(0), picker.getValue());
        }

        @Test
        void unknownFieldsInPayloadAreIgnored() {
            var name = new TextField("Name");
            var form = new FormLayout(name);
            var controller = new FormAIController(form);

            String result = fillFormTool(controller)
                    .execute(json("{\"name\":\"Pat\",\"unknown\":42}"));

            Assertions.assertEquals("Pat", name.getValue());
            Assertions.assertFalse(result.contains("unknown"));
        }

        @Test
        void resultListsCurrentStateForAllFields() {
            var name = new TextField("Name");
            var amount = new NumberField("Amount");
            var form = new FormLayout(name, amount);
            var controller = new FormAIController(form);

            String result = fillFormTool(controller)
                    .execute(json("{\"name\":\"Pat\"}"));

            Assertions.assertTrue(result.contains("name: Pat"));
            Assertions.assertTrue(result.contains("amount: <empty>"));
        }
    }

    @Nested
    class Queryable {

        @Test
        void toolIsExposedOnlyWhenAtLeastOneFieldIsQueryable() {
            var name = new TextField("Name");
            var controllerWithout = new FormAIController(new FormLayout(name));
            Assertions.assertEquals(1, controllerWithout.getTools().size());

            var picker = new ComboBox<String>("Project");
            var controllerWith = new FormAIController(new FormLayout(picker))
                    .queryable(picker, (filter, limit) -> List.of("a", "b"));
            Assertions.assertEquals(2, controllerWith.getTools().size());
            Assertions.assertTrue(controllerWith.getTools().stream()
                    .anyMatch(t -> t.getName().equals("query_field_options")));
        }

        @Test
        void executeReturnsLinesAndPopulatesReverseCache() {
            record Project(String code, String name) {
            }
            var p1 = new Project("P-1", "Apollo");
            var p2 = new Project("P-2", "Polaris");
            var picker = new ComboBox<Project>("Project");
            picker.setItems(List.of(p1, p2));
            picker.setItemLabelGenerator(p -> p.name() + " #" + p.code());
            var controller = new FormAIController(new FormLayout(picker))
                    .queryable(picker, (filter, limit) -> List.of(p1, p2));

            ToolSpec query = findTool(controller, "query_field_options");
            String result = query.execute(json(
                    "{\"field\":\"project\",\"filter\":\"\",\"limit\":50}"));
            Assertions.assertTrue(result.contains("Apollo #P-1"));
            Assertions.assertTrue(result.contains("Polaris #P-2"));

            // fill_form should now find typed items via the per-turn cache.
            fillFormTool(controller)
                    .execute(json("{\"project\":\"Apollo #P-1\"}"));
            Assertions.assertEquals(p1, picker.getValue());
        }

        @Test
        void unknownFieldArgumentReturnsError() {
            var picker = new ComboBox<String>("Project");
            var controller = new FormAIController(new FormLayout(picker))
                    .queryable(picker, (filter, limit) -> List.of());

            String result = findTool(controller, "query_field_options").execute(
                    json("{\"field\":\"unknown\",\"filter\":\"\"}"));
            Assertions.assertTrue(result.startsWith("Error"));
        }
    }

    @Nested
    class Validation {

        @Test
        void binderValidatorRejectionsRollBackValue() {
            class Item {
                String name;
            }
            var name = new TextField("Name");
            var form = new FormLayout(name);
            Binder<Item> binder = new Binder<>(Item.class);
            binder.forField(name)
                    .withValidator(value -> value != null && value.length() >= 3,
                            "Name must be at least 3 characters")
                    .bind(it -> it.name, (it, v) -> it.name = v);

            var controller = new FormAIController(form, binder);
            String result = fillFormTool(controller)
                    .execute(json("{\"name\":\"X\"}"));

            Assertions.assertEquals("", name.getValue(),
                    "Rejected value should be reverted to the previous value");
            Assertions.assertTrue(result.contains("Rejected"));
            Assertions.assertTrue(result.contains("Name must be at least 3"));
        }

        @Test
        void binderValidatorAcceptingValueStaysWritten() {
            class Item {
                String name;
            }
            var name = new TextField("Name");
            var form = new FormLayout(name);
            Binder<Item> binder = new Binder<>(Item.class);
            binder.forField(name)
                    .withValidator(value -> value != null && value.length() >= 3,
                            "Name must be at least 3 characters")
                    .bind(it -> it.name, (it, v) -> it.name = v);

            var controller = new FormAIController(form, binder);
            fillFormTool(controller).execute(json("{\"name\":\"Patrick\"}"));

            Assertions.assertEquals("Patrick", name.getValue());
        }

        @Test
        void binderPropertyNamesProvideIdentifiers() {
            record Customer(String name, String email) {
            }
            var name = new TextField("Name");
            var email = new EmailField("Email");
            var form = new FormLayout(name, email);

            Binder<Customer> binder = new Binder<>(Customer.class);
            binder.bind(name, "name");
            binder.bind(email, "email");

            var controller = new FormAIController(form, binder);
            Assertions.assertEquals("name", controller.getIdentifier(name));
            Assertions.assertEquals("email", controller.getIdentifier(email));
        }
    }

    @Nested
    class Lifecycle {

        @Test
        void onRequestStartLocksAllNonIgnoredFields() {
            var name = new TextField("Name");
            var hidden = new TextField("Internal");
            var form = new FormLayout(name, hidden);
            var controller = new FormAIController(form).ignore(hidden);

            controller.onRequestStart();

            Assertions.assertTrue(name.isReadOnly());
            Assertions.assertFalse(hidden.isReadOnly());
        }

        @Test
        void onResponseCompleteRestoresReadOnly() {
            var name = new TextField("Name");
            var form = new FormLayout(name);
            var controller = new FormAIController(form);

            controller.onRequestStart();
            controller.onResponseComplete();

            Assertions.assertFalse(name.isReadOnly());
        }

        @Test
        void onResponseFailedRestoresReadOnly() {
            var name = new TextField("Name");
            var form = new FormLayout(name);
            var controller = new FormAIController(form);

            controller.onRequestStart();
            controller.onResponseFailed(new RuntimeException("boom"));

            Assertions.assertFalse(name.isReadOnly());
        }

        @Test
        void onRequestStartPreservesPreExistingReadOnlyOnRestore() {
            var name = new TextField("Name");
            name.setReadOnly(true);
            var form = new FormLayout(name);
            var controller = new FormAIController(form);

            controller.onRequestStart();
            controller.onResponseComplete();

            Assertions.assertTrue(name.isReadOnly(),
                    "Pre-existing read-only should survive a turn");
        }

        @Test
        void queryCacheClearsBetweenTurns() {
            record Project(String code, String name) {
            }
            var p1 = new Project("P-1", "Apollo");
            var picker = new ComboBox<Project>("Project");
            picker.setItemLabelGenerator(p -> p.name() + " #" + p.code());
            AtomicReference<List<Project>> next = new AtomicReference<>(
                    List.of(p1));
            var controller = new FormAIController(new FormLayout(picker))
                    .queryable(picker,
                            (filter, limit) -> List.copyOf(next.get()));

            controller.onRequestStart();
            findTool(controller, "query_field_options").execute(
                    json("{\"field\":\"project\",\"filter\":\"\"}"));
            controller.onResponseComplete();

            // After the cache is cleared, fill_form for the same label
            // cannot resolve without the queryable being re-invoked.
            controller.onRequestStart();
            String result = fillFormTool(controller)
                    .execute(json("{\"project\":\"Apollo #P-1\"}"));
            Assertions.assertTrue(result.contains("Rejected"),
                    "Without a fresh queryable call, the label should "
                            + "not resolve.");
            controller.onResponseComplete();
        }
    }

    // ----- Helpers -------------------------------------------------------

    private static JsonNode json(String text) {
        return JacksonUtils.readTree(text);
    }

    private static JsonNode parseSchema(FormAIController controller) {
        return JacksonUtils.readTree(
                fillFormTool(controller).getParametersSchema());
    }

    private static ToolSpec fillFormTool(FormAIController controller) {
        return findTool(controller, "fill_form");
    }

    private static ToolSpec findTool(FormAIController controller, String name) {
        return controller.getTools().stream()
                .filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow();
    }
}
