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
package com.vaadin.flow.component.formlayout.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.formlayout.FormLayout.FormRow;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Element;

class FormLayoutTest {

    @Test
    void getResponsiveSteps_noInitialSteps_emptyListIsReturned() {
        FormLayout layout = new FormLayout();
        Assertions.assertTrue(layout.getResponsiveSteps().isEmpty());
    }

    @Test
    void create_FormLayout() {
        // Just testing that creating form layout actually compiles and doesn't
        // throw. Test is on purpose, so that the implementation not
        // accidentally removed.
        FormLayout formLayout = new FormLayout();
        formLayout.addClickListener(event -> {
        });
    }

    @Test
    void verifyColspanElement() {
        FormLayout layout = new FormLayout();
        // using layouts as components to avoid importing dependencies.

        // verifying the colspan is correctly set in the element itself
        FormLayout comp1 = new FormLayout();
        layout.add(comp1, 2);
        String strColspan = comp1.getElement().getAttribute("colspan");
        Assertions.assertEquals(2, Integer.parseInt(strColspan));
    }

    @Test
    void verifyColspanCodeBehaviour() {
        FormLayout layout = new FormLayout();
        // using layouts as components to avoid importing dependencies.

        // verifying normal use cases
        FormLayout comp1 = new FormLayout();
        layout.add(comp1, 2);
        Assertions.assertEquals(2, layout.getColspan(comp1));
        layout.setColspan(comp1, 1);
        Assertions.assertEquals(1, layout.getColspan(comp1));

        // verifying it correctly sets it to 1 if an number lower than 1 is
        // supplied
        FormLayout comp2 = new FormLayout();
        layout.add(comp2, -1);
        Assertions.assertEquals(1, layout.getColspan(comp2));

        // verifying it correctly gets 1 if invalid colspans are supplied
        // outside the API
        FormLayout compInvalid = new FormLayout();
        layout.add(compInvalid);
        compInvalid.getElement().setAttribute("colspan", "qsd4hdsj%f");
        Assertions.assertEquals(1, layout.getColspan(compInvalid));

        // verifying it correctly gets 1 if no colspan was set.
        FormLayout compUnset = new FormLayout();
        layout.add(compUnset);
        Assertions.assertEquals(1, layout.getColspan(compUnset));

    }

    @Test
    void setResponsiveSteps_getResponsiveSteps() {
        FormLayout formLayout = new FormLayout();

        formLayout.setResponsiveSteps(new ResponsiveStep(null, 1));
        Assertions.assertEquals(1, formLayout.getResponsiveSteps().size());

        formLayout.setResponsiveSteps(new ResponsiveStep(null, 1),
                new ResponsiveStep("1px", 1));
        Assertions.assertEquals(2, formLayout.getResponsiveSteps().size());

        formLayout.setResponsiveSteps(new ResponsiveStep(null, 1),
                new ResponsiveStep("1px", 1), new ResponsiveStep("1px", 1,
                        ResponsiveStep.LabelsPosition.TOP));
        Assertions.assertEquals(3, formLayout.getResponsiveSteps().size());
    }

    @Test
    void setLabelWidth_getLabelWidth() {
        FormLayout formLayout = new FormLayout();
        Assertions.assertFalse(
                formLayout.getStyle().has("--vaadin-form-layout-label-width"));
        Assertions.assertNull(formLayout.getLabelWidth());

        formLayout.setLabelWidth("2em");
        Assertions.assertEquals("2em",
                formLayout.getStyle().get("--vaadin-form-layout-label-width"));
        Assertions.assertEquals("2em", formLayout.getLabelWidth());

        formLayout.setLabelWidth(160, Unit.PIXELS);
        Assertions.assertEquals("160.0px",
                formLayout.getStyle().get("--vaadin-form-layout-label-width"));
        Assertions.assertEquals("160.0px", formLayout.getLabelWidth());

        formLayout.setLabelWidth(null);
        Assertions.assertFalse(
                formLayout.getStyle().has("--vaadin-form-layout-label-width"));
        Assertions.assertNull(formLayout.getLabelWidth());
    }

    @Test
    void setLabelSpacing_getLabelSpacing() {
        FormLayout formLayout = new FormLayout();
        Assertions.assertFalse(formLayout.getStyle()
                .has("--vaadin-form-layout-label-spacing"));
        Assertions.assertNull(formLayout.getLabelSpacing());

        formLayout.setLabelSpacing("10em");
        Assertions.assertEquals("10em", formLayout.getStyle()
                .get("--vaadin-form-layout-label-spacing"));
        Assertions.assertEquals("10em", formLayout.getLabelSpacing());

        formLayout.setLabelSpacing(160, Unit.PIXELS);
        Assertions.assertEquals("160.0px", formLayout.getStyle()
                .get("--vaadin-form-layout-label-spacing"));
        Assertions.assertEquals("160.0px", formLayout.getLabelSpacing());

        formLayout.setLabelSpacing(null);
        Assertions.assertFalse(formLayout.getStyle()
                .has("--vaadin-form-layout-label-spacing"));
        Assertions.assertNull(formLayout.getLabelSpacing());
    }

    @Test
    void setColumnSpacing_getColumnSpacing() {
        FormLayout formLayout = new FormLayout();
        Assertions.assertFalse(formLayout.getStyle()
                .has("--vaadin-form-layout-column-spacing"));
        Assertions.assertNull(formLayout.getColumnSpacing());

        formLayout.setColumnSpacing("10em");
        Assertions.assertEquals("10em", formLayout.getStyle()
                .get("--vaadin-form-layout-column-spacing"));
        Assertions.assertEquals("10em", formLayout.getColumnSpacing());

        formLayout.setColumnSpacing(160, Unit.PIXELS);
        Assertions.assertEquals("160.0px", formLayout.getStyle()
                .get("--vaadin-form-layout-column-spacing"));
        Assertions.assertEquals("160.0px", formLayout.getColumnSpacing());

        formLayout.setColumnSpacing(null);
        Assertions.assertFalse(formLayout.getStyle()
                .has("--vaadin-form-layout-column-spacing"));
        Assertions.assertNull(formLayout.getColumnSpacing());
    }

    @Test
    void setRowSpacing_getRowSpacing() {
        FormLayout formLayout = new FormLayout();
        Assertions.assertFalse(
                formLayout.getStyle().has("--vaadin-form-layout-row-spacing"));
        Assertions.assertNull(formLayout.getRowSpacing());

        formLayout.setRowSpacing("10em");
        Assertions.assertEquals("10em",
                formLayout.getStyle().get("--vaadin-form-layout-row-spacing"));
        Assertions.assertEquals("10em", formLayout.getRowSpacing());

        formLayout.setRowSpacing(160, Unit.PIXELS);
        Assertions.assertEquals("160.0px",
                formLayout.getStyle().get("--vaadin-form-layout-row-spacing"));
        Assertions.assertEquals("160.0px", formLayout.getRowSpacing());

        formLayout.setRowSpacing(null);
        Assertions.assertFalse(
                formLayout.getStyle().has("--vaadin-form-layout-row-spacing"));
        Assertions.assertNull(formLayout.getRowSpacing());
    }

    @Test
    void setAutoResponsive_getAutoResponsive() {
        FormLayout formLayout = new FormLayout();
        Assertions.assertFalse(
                formLayout.getElement().hasProperty("autoResponsive"));

        formLayout.setAutoResponsive(true);
        Assertions.assertTrue(
                formLayout.getElement().getProperty("autoResponsive", false));
    }

    @Test
    void setAutoRows_getAutoRows() {
        FormLayout formLayout = new FormLayout();
        Assertions.assertFalse(formLayout.getElement().hasProperty("autoRows"));
        Assertions.assertFalse(formLayout.isAutoRows());

        formLayout.setAutoRows(true);
        Assertions.assertTrue(
                formLayout.getElement().getProperty("autoRows", false));
        Assertions.assertTrue(formLayout.isAutoRows());
    }

    @Test
    void setColumnWidth_getColumnWidth() {
        FormLayout formLayout = new FormLayout();
        Assertions.assertFalse(
                formLayout.getElement().hasProperty("columnWidth"));
        Assertions.assertNull(formLayout.getColumnWidth());

        formLayout.setColumnWidth("10em");
        Assertions.assertEquals("10em",
                formLayout.getElement().getProperty("columnWidth"));
        Assertions.assertEquals("10em", formLayout.getColumnWidth());

        formLayout.setColumnWidth(160, Unit.PIXELS);
        Assertions.assertEquals("160.0px",
                formLayout.getElement().getProperty("columnWidth"));
        Assertions.assertEquals("160.0px", formLayout.getColumnWidth());

        formLayout.setColumnWidth(null);
        Assertions
                .assertNull(formLayout.getElement().getProperty("columnWidth"));
        Assertions.assertNull(formLayout.getColumnWidth());
    }

    @Test
    void setMaxColumns_getMaxColumns() {
        FormLayout formLayout = new FormLayout();
        Assertions.assertEquals(0, formLayout.getMaxColumns());
        Assertions
                .assertFalse(formLayout.getElement().hasProperty("maxColumns"));

        formLayout.setMaxColumns(4);
        Assertions.assertEquals(4, formLayout.getMaxColumns());
        Assertions.assertEquals(4,
                formLayout.getElement().getProperty("maxColumns", 0));
    }

    @Test
    void minColumnsPropertyIsEmptyByDefault() {
        var formLayout = new FormLayout();
        Assertions.assertEquals(0, formLayout.getMinColumns());
        Assertions
                .assertFalse(formLayout.getElement().hasProperty("minColumns"));
    }

    @Test
    void setMinColumns_minColumnsIsCorrectlyUpdated() {
        var formLayout = new FormLayout();
        var minColumns = 4;
        formLayout.setMinColumns(minColumns);
        Assertions.assertEquals(minColumns, formLayout.getMinColumns());
        Assertions.assertEquals(minColumns,
                formLayout.getElement().getProperty("minColumns", 0));
    }

    @Test
    void setExpandColumns_isExpandColumns() {
        FormLayout formLayout = new FormLayout();
        Assertions.assertFalse(
                formLayout.getElement().hasProperty("expandColumns"));
        Assertions.assertFalse(formLayout.isExpandColumns());

        formLayout.setExpandColumns(true);
        Assertions.assertTrue(
                formLayout.getElement().getProperty("expandColumns", false));
        Assertions.assertTrue(formLayout.isExpandColumns());
    }

    @Test
    void setExpandFields_isExpandFields() {
        FormLayout formLayout = new FormLayout();
        Assertions.assertFalse(
                formLayout.getElement().hasProperty("expandFields"));
        Assertions.assertFalse(formLayout.isExpandFields());

        formLayout.setExpandFields(true);
        Assertions.assertTrue(
                formLayout.getElement().getProperty("expandFields", false));
        Assertions.assertTrue(formLayout.isExpandFields());
    }

    @Test
    void setLabelsAside_isLabelsAside() {
        FormLayout formLayout = new FormLayout();
        Assertions.assertFalse(
                formLayout.getElement().hasProperty("labelsAside"));
        Assertions.assertFalse(formLayout.isLabelsAside());

        formLayout.setLabelsAside(true);
        Assertions.assertTrue(
                formLayout.getElement().getProperty("labelsAside", false));
        Assertions.assertTrue(formLayout.isLabelsAside());
    }

    @Test
    void addFormRow() {
        FormLayout formLayout = new FormLayout();
        FormRow row = formLayout.addFormRow(new Input(), new Input());
        Assertions.assertEquals(2, row.getElement().getChildCount());
        Assertions.assertEquals(formLayout.getElement(),
                row.getElement().getParent());
    }

    @Test
    void formRow_addFormItem() {
        FormRow row = new FormRow();
        FormItem item = row.addFormItem(new Input(), "custom label");
        Assertions.assertEquals(2, item.getElement().getChildCount());

        Element input = item.getElement().getChild(0);
        Assertions.assertNotNull(input);
        Assertions.assertEquals("input", input.getTag());

        Element label = item.getElement().getChild(1);
        Assertions.assertNotNull(label);
        Assertions.assertEquals("label", label.getTag());
        Assertions.assertEquals("custom label", label.getText());
    }

    @Test
    void formRow_addFormItemWithComponent() {
        FormRow row = new FormRow();
        FormItem item = row.addFormItem(new Input(), new Span("custom label"));
        Assertions.assertEquals(2, item.getElement().getChildCount());

        Element input = item.getElement().getChild(0);
        Assertions.assertNotNull(input);
        Assertions.assertEquals("input", input.getTag());

        Element label = item.getElement().getChild(1);
        Assertions.assertNotNull(label);
        Assertions.assertEquals("span", label.getTag());
        Assertions.assertEquals("custom label", label.getText());
    }

    @Test
    void formRow_setColspan_getColspan() {
        Input input = new Input();
        FormRow row = new FormRow();
        row.add(input);

        Assertions.assertEquals(1, row.getColspan(input));

        row.setColspan(input, 2);
        Assertions.assertEquals(2, row.getColspan(input));

        row.setColspan(input, -1);
        Assertions.assertEquals(1, row.getColspan(input));
    }

    @Test
    void formRow_addComponentWithColspan() {
        Input input = new Input();
        FormRow row = new FormRow();
        row.add(input, 2);
        Assertions.assertEquals(2, row.getColspan(input));
    }
}
