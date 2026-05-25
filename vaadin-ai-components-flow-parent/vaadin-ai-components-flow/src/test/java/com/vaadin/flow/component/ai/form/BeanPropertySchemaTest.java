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

import static com.vaadin.flow.component.ai.form.FormTestSupport.formStateFields;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;

import tools.jackson.databind.JsonNode;

/**
 * Tests for bean-side metadata flowing into the {@code get_form_state} schema:
 * the bean property's Java type overrides a plain {@code String}-typed field's
 * schema entry when a converter sits in the binding chain (e.g. {@code
 * BigDecimal}, {@code LocalDate}, or an {@code enum} reached through a
 * String-to-T converter).
 */
class BeanPropertySchemaTest {

    public static class TypedBean {

        private String name;

        private BigDecimal amount;

        private LocalDate eventDate;

        private Tier tier;

        private LabeledTier labeledTier;

        public enum Tier {
            BRONZE, SILVER, GOLD
        }

        public enum LabeledTier {
            BRONZE {
                @Override
                public String toString() {
                    return "Bronze!";
                }
            },
            SILVER {
                @Override
                public String toString() {
                    return "Silver!";
                }
            };
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public LocalDate getEventDate() {
            return eventDate;
        }

        public void setEventDate(LocalDate eventDate) {
            this.eventDate = eventDate;
        }

        public Tier getTier() {
            return tier;
        }

        public void setTier(Tier tier) {
            this.tier = tier;
        }

        public LabeledTier getLabeledTier() {
            return labeledTier;
        }

        public void setLabeledTier(LabeledTier labeledTier) {
            this.labeledTier = labeledTier;
        }
    }

    @Test
    void stringFieldBoundToBigDecimalPropertyAdoptsBigDecimalSchema() {
        // TextField's HasValue<?, String> alone reads as a plain string. The
        // bean-side BigDecimal property is what the LLM actually needs to
        // satisfy, so the schema must follow the bean — string + the
        // big-decimal pattern, not a bare string.
        var field = new TestField();
        var binder = new Binder<>(TypedBean.class);
        binder.forField(field)
                .withConverter(v -> v.isEmpty() ? null : new BigDecimal(v),
                        v -> v == null ? "" : v.toPlainString())
                .bind("amount");
        var controller = new FormAIController(new Div(field), binder);

        var f = formStateFields(controller).get(0);

        Assertions.assertEquals("string", f.path("type").asString(),
                "BigDecimal property must render as type=string, got: " + f);
        Assertions.assertEquals(FormFieldType.BIG_DECIMAL_PATTERN,
                f.path("pattern").asString(),
                "BigDecimal property must carry the big-decimal pattern, got: "
                        + f);
    }

    @Test
    void stringFieldBoundToLocalDatePropertyAdoptsDateFormat() {
        var field = new TestField();
        var binder = new Binder<>(TypedBean.class);
        binder.forField(field)
                .withConverter(v -> v.isEmpty() ? null : LocalDate.parse(v),
                        v -> v == null ? "" : v.toString())
                .bind("eventDate");
        var controller = new FormAIController(new Div(field), binder);

        var f = formStateFields(controller).get(0);

        Assertions.assertEquals("string", f.path("type").asString());
        Assertions.assertEquals("date", f.path("format").asString(),
                "LocalDate property must carry format=date, got: " + f);
    }

    @Test
    void stringFieldBoundToEnumPropertySurfacesEnumConstants() {
        // The bean property is an enum but the field is a plain TextField
        // (with a Tier-name converter). The schema must list the enum
        // constants so the LLM picks a valid name even though the field
        // itself does not expose any options.
        var field = new TestField();
        var binder = new Binder<>(TypedBean.class);
        binder.forField(field)
                .withConverter(
                        v -> v.isEmpty() ? null : TypedBean.Tier.valueOf(v),
                        v -> v == null ? "" : v.name())
                .bind("tier");
        var controller = new FormAIController(new Div(field), binder);

        var f = formStateFields(controller).get(0);

        var values = new ArrayList<String>();
        f.path("enum").forEach(n -> values.add(n.asString()));
        Assertions.assertEquals(List.of("BRONZE", "SILVER", "GOLD"), values,
                "Enum-typed bean property must enumerate its constants, "
                        + "got: " + f);
    }

    @Test
    void enumPropertySurfacesNamesEvenWhenToStringIsOverridden() {
        var field = new TestField();
        var binder = new Binder<>(TypedBean.class);
        binder.forField(field).withConverter(
                v -> v.isEmpty() ? null : TypedBean.LabeledTier.valueOf(v),
                v -> v == null ? "" : v.name()).bind("labeledTier");
        var controller = new FormAIController(new Div(field), binder);

        var f = formStateFields(controller).get(0);

        var values = new ArrayList<String>();
        f.path("enum").forEach(n -> values.add(n.asString()));
        Assertions.assertEquals(List.of("BRONZE", "SILVER"), values,
                "Enum constants must surface as Enum.name() so the LLM "
                        + "picks values the canonical Enum.valueOf converter "
                        + "accepts; toString labels can diverge from names "
                        + "and break the round-trip. Got: " + f);
    }

    @Test
    void selectionFieldKeepsItsOwnOptionsEvenWhenBeanPropertyIsEnum() {
        // ComboBox-style field already classifies as SINGLE_SELECT; its
        // options come from the data provider or .valueOptions(...). The
        // bean-side enum constants must not be layered on top — that would
        // either duplicate or contradict the field's option set.
        var combo = new FormTestFields.SingleSelectField<TypedBean.Tier>();
        combo.setItems(TypedBean.Tier.BRONZE, TypedBean.Tier.SILVER);
        combo.setItemLabelGenerator(Enum::name);
        var binder = new Binder<>(TypedBean.class);
        binder.forField(combo).bind("tier");
        var controller = new FormAIController(new Div(combo), binder);

        var f = formStateFields(controller).get(0);

        var values = collectEnumValues(f);
        Assertions.assertEquals(List.of("BRONZE", "SILVER"), values,
                "Selection field's enum must come from the data provider, "
                        + "not the bean side, got: " + f);
    }

    @Test
    void lambdaBoundBindingSkipsBeanDrivenTypeOverride() {
        // A lambda-bound binding (getter/setter pair) carries no property
        // name, so there is no bean-side anchor to resolve the type override
        // against — the schema must fall back to the field's view alone, no
        // BigDecimal pattern.
        var field = new TestField();
        var binder = new Binder<>(TypedBean.class);
        binder.forField(field)
                .withConverter(v -> v.isEmpty() ? null : new BigDecimal(v),
                        v -> v == null ? "" : v.toPlainString())
                .bind(TypedBean::getAmount, TypedBean::setAmount);
        var controller = new FormAIController(new Div(field), binder);

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.path("pattern").isMissingNode(),
                "Lambda-bound binding must not pick up BigDecimal pattern "
                        + "from the bean, got: " + f);
    }

    @Test
    void noBinderConstructorEmitsNoBeanDrivenTypeOverride() {
        var field = new TestField();
        var controller = new FormAIController(new Div(field));

        var f = formStateFields(controller).get(0);

        Assertions.assertTrue(f.path("pattern").isMissingNode(),
                "Without a binder there is no bean to read from, got: " + f);
    }

    @Test
    void resolveReturnsNullForNullBeanClass() {
        Assertions.assertNull(BeanPropertyMetadata.resolve(null, "name"));
    }

    @Test
    void resolveReturnsNullForNullPropertyPath() {
        Assertions.assertNull(
                BeanPropertyMetadata.resolve(TypedBean.class, null));
    }

    @Test
    void resolveReturnsNullForEmptyPropertyPath() {
        Assertions
                .assertNull(BeanPropertyMetadata.resolve(TypedBean.class, ""));
    }

    @Test
    void resolveMissingPropertyReturnsNullWithoutWarning() {
        var logger = TestLoggerFactory
                .getTestLogger(BeanPropertyMetadata.class);
        logger.clearAll();

        var result = BeanPropertyMetadata.resolve(TypedBean.class,
                "nonexistentProperty");

        Assertions.assertNull(result);
        var warnings = logger.getLoggingEvents().stream()
                .filter(e -> e.getLevel() == Level.WARN).toList();
        Assertions.assertTrue(warnings.isEmpty(),
                "Resolving a non-existent property must not warn-log "
                        + "(LLM hands the controller arbitrary names); got: "
                        + warnings);
    }

    private static List<String> collectEnumValues(JsonNode field) {
        var values = new ArrayList<String>();
        field.path("enum").forEach(n -> values.add(n.asString()));
        return values;
    }
}
