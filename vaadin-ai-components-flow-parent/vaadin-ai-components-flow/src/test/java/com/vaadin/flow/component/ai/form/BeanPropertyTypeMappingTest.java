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
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;

import tools.jackson.databind.JsonNode;

/**
 * Tests for {@link FormFieldType#classifyBeanProperty(Class)} pinned through
 * the {@code get_form_state} schema. Each Java type the binder may report for a
 * property has a distinct mapping into the schema — Long/Short/Byte to
 * {@code integer}, Boolean to {@code boolean}, Double/Float to {@code number},
 * LocalDateTime/LocalTime to {@code date-time}/{@code time}. The
 * {@code String}-typed field on the form side is intentional: the converter
 * sitting in the binding chain is what makes the bean type the source of truth
 * for the schema.
 *
 * <p>
 * Also pins the STRING-only scope of the bean-type override: a non-string field
 * (e.g. {@link NumberField}) must keep its own classification even when the
 * bean side reports a more specific type, because the field's own value type is
 * already specific enough.
 * </p>
 */
class BeanPropertyTypeMappingTest {

    public static class PrimitivesBean {

        private Long longProp;
        private Short shortProp;
        private Byte byteProp;
        private Boolean booleanProp;
        private boolean booleanPrimitiveProp;
        private Double doubleProp;
        private Float floatProp;
        private LocalDateTime dateTimeProp;
        private LocalTime timeProp;
        private BigDecimal amountProp;
        private float floatPrimitiveProp;

        public Long getLongProp() {
            return longProp;
        }

        public void setLongProp(Long longProp) {
            this.longProp = longProp;
        }

        public Short getShortProp() {
            return shortProp;
        }

        public void setShortProp(Short shortProp) {
            this.shortProp = shortProp;
        }

        public Byte getByteProp() {
            return byteProp;
        }

        public void setByteProp(Byte byteProp) {
            this.byteProp = byteProp;
        }

        public Boolean getBooleanProp() {
            return booleanProp;
        }

        public void setBooleanProp(Boolean booleanProp) {
            this.booleanProp = booleanProp;
        }

        public boolean isBooleanPrimitiveProp() {
            return booleanPrimitiveProp;
        }

        public void setBooleanPrimitiveProp(boolean booleanPrimitiveProp) {
            this.booleanPrimitiveProp = booleanPrimitiveProp;
        }

        public Double getDoubleProp() {
            return doubleProp;
        }

        public void setDoubleProp(Double doubleProp) {
            this.doubleProp = doubleProp;
        }

        public Float getFloatProp() {
            return floatProp;
        }

        public void setFloatProp(Float floatProp) {
            this.floatProp = floatProp;
        }

        public LocalDateTime getDateTimeProp() {
            return dateTimeProp;
        }

        public void setDateTimeProp(LocalDateTime dateTimeProp) {
            this.dateTimeProp = dateTimeProp;
        }

        public LocalTime getTimeProp() {
            return timeProp;
        }

        public void setTimeProp(LocalTime timeProp) {
            this.timeProp = timeProp;
        }

        public BigDecimal getAmountProp() {
            return amountProp;
        }

        public void setAmountProp(BigDecimal amountProp) {
            this.amountProp = amountProp;
        }

        public float getFloatPrimitiveProp() {
            return floatPrimitiveProp;
        }

        public void setFloatPrimitiveProp(float floatPrimitiveProp) {
            this.floatPrimitiveProp = floatPrimitiveProp;
        }
    }

    @Test
    void longPropertyMapsToInteger() {
        // Without Long in classifyBeanProperty, a Long-typed bean property
        // would fall through to the default branch (plain string) instead of
        // surfacing as JSON integer. Real beans use Long for ids and counts;
        // a regression here corrupts every Long-based schema.
        var field = new TestField();
        var binder = new Binder<>(PrimitivesBean.class);
        binder.forField(field).withConverter(s -> 0L, v -> "").bind("longProp");
        assertTypeAndFormat(schemaFor(field, binder), "integer", null);
    }

    @Test
    void shortPropertyMapsToInteger() {
        var field = new TestField();
        var binder = new Binder<>(PrimitivesBean.class);
        binder.forField(field).withConverter(s -> (short) 0, v -> "")
                .bind("shortProp");
        assertTypeAndFormat(schemaFor(field, binder), "integer", null);
    }

    @Test
    void bytePropertyMapsToInteger() {
        var field = new TestField();
        var binder = new Binder<>(PrimitivesBean.class);
        binder.forField(field).withConverter(s -> (byte) 0, v -> "")
                .bind("byteProp");
        assertTypeAndFormat(schemaFor(field, binder), "integer", null);
    }

    @Test
    void booleanWrapperPropertyMapsToBoolean() {
        // Boolean and the boolean primitive both have to flow through; the
        // classifier's check covers both via `Boolean.class || boolean.class`.
        var field = new TestField();
        var binder = new Binder<>(PrimitivesBean.class);
        binder.forField(field).withConverter(s -> Boolean.FALSE, v -> "")
                .bind("booleanProp");
        assertTypeAndFormat(schemaFor(field, binder), "boolean", null);
    }

    @Test
    void booleanPrimitivePropertyMapsToBoolean() {
        var field = new TestField();
        var binder = new Binder<>(PrimitivesBean.class);
        binder.forField(field).withConverter(s -> false, v -> "")
                .bind("booleanPrimitiveProp");
        assertTypeAndFormat(schemaFor(field, binder), "boolean", null);
    }

    @Test
    void doublePropertyMapsToNumber() {
        var field = new TestField();
        var binder = new Binder<>(PrimitivesBean.class);
        binder.forField(field).withConverter(s -> 0.0, v -> "")
                .bind("doubleProp");
        assertTypeAndFormat(schemaFor(field, binder), "number", null);
    }

    @Test
    void floatPropertyMapsToNumber() {
        var field = new TestField();
        var binder = new Binder<>(PrimitivesBean.class);
        binder.forField(field).withConverter(s -> 0.0f, v -> "")
                .bind("floatProp");
        assertTypeAndFormat(schemaFor(field, binder), "number", null);
    }

    @Test
    void floatPrimitivePropertyMapsToNumber() {
        // The primitive float (separate from java.lang.Float) appears when a
        // bean exposes `public float getX()`. Without it in
        // classifyBeanProperty, the schema falls back to plain string and
        // the LLM can't see that the field expects a JSON number.
        var field = new TestField();
        var binder = new Binder<>(PrimitivesBean.class);
        binder.forField(field).withConverter(s -> 0.0f, v -> "")
                .bind("floatPrimitiveProp");
        assertTypeAndFormat(schemaFor(field, binder), "number", null);
    }

    @Test
    void localDateTimePropertyMapsToDateTimeFormat() {
        var field = new TestField();
        var binder = new Binder<>(PrimitivesBean.class);
        binder.forField(field)
                .withConverter(s -> s.isEmpty() ? null : LocalDateTime.parse(s),
                        v -> v == null ? "" : v.toString())
                .bind("dateTimeProp");
        assertTypeAndFormat(schemaFor(field, binder), "string", "date-time");
    }

    @Test
    void localTimePropertyMapsToTimeFormat() {
        var field = new TestField();
        var binder = new Binder<>(PrimitivesBean.class);
        binder.forField(field)
                .withConverter(s -> s.isEmpty() ? null : LocalTime.parse(s),
                        v -> v == null ? "" : v.toString())
                .bind("timeProp");
        assertTypeAndFormat(schemaFor(field, binder), "string", "time");
    }

    @Test
    void nonStringFieldKeepsItsOwnTypeEvenWhenBeanPropertyIsMoreSpecific() {
        // NumberField holds Double. The bean property is BigDecimal. Without
        // the STRING-scope guard on the bean override, the schema would
        // switch from `number` to `string` + the big-decimal pattern, which
        // contradicts the JSON the field's own setValue accepts. The field's
        // value type is already specific enough; the bean side only wins for
        // String-typed fields where the field itself can't disambiguate.
        var field = new NumberField();
        var binder = new Binder<>(PrimitivesBean.class);
        binder.forField(field)
                .withConverter(d -> d == null ? null : BigDecimal.valueOf(d),
                        bd -> bd == null ? null : bd.doubleValue())
                .bind("amountProp");
        var controller = new FormAIController(new Div(field), binder);

        var f = formStateFields(controller).get(0);

        Assertions.assertEquals("number", f.path("type").asString(),
                "Non-String field's type must win over bean-side BigDecimal, "
                        + "got: " + f);
        Assertions.assertTrue(f.path("pattern").isMissingNode(),
                "Bean-side BigDecimal pattern must not bleed onto a NumberField "
                        + "(its own value type already pins the schema), got: "
                        + f);
    }

    private static JsonNode schemaFor(TestField field, Binder<?> binder) {
        var controller = new FormAIController(new Div(field), binder);
        return formStateFields(controller).get(0);
    }

    private static void assertTypeAndFormat(JsonNode f, String expectedType,
            String expectedFormat) {
        Assertions.assertEquals(expectedType, f.path("type").asString(),
                "Expected type=" + expectedType + ", got: " + f);
        if (expectedFormat == null) {
            Assertions.assertTrue(f.path("format").isMissingNode(),
                    "Expected no format, got: " + f);
        } else {
            Assertions.assertEquals(expectedFormat, f.path("format").asString(),
                    "Expected format=" + expectedFormat + ", got: " + f);
        }
    }
}
