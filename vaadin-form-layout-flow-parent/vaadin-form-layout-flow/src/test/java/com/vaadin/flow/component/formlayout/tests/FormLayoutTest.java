/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.formlayout.FormLayout.FormRow;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Element;

public class FormLayoutTest {

    @Test
    public void getResponsiveSteps_noInitialSteps_emptyListIsReturned() {
        FormLayout layout = new FormLayout();
        Assert.assertTrue(layout.getResponsiveSteps().isEmpty());
    }

    @Test
    public void create_FormLayout() {
        // Just testing that creating form layout actually compiles and doesn't
        // throw. Test is on purpose, so that the implementation not
        // accidentally removed.
        FormLayout formLayout = new FormLayout();
        formLayout.addClickListener(event -> {
        });
    }

    @Test
    public void verifyColspanElement() {
        FormLayout layout = new FormLayout();
        // using layouts as components to avoid importing dependencies.

        // verifying the colspan is correctly set in the element itself
        FormLayout comp1 = new FormLayout();
        layout.add(comp1, 2);
        String strColspan = comp1.getElement().getAttribute("colspan");
        Assert.assertEquals(Integer.parseInt(strColspan), 2);
    }

    @Test
    public void verifyColspanCodeBehaviour() {
        FormLayout layout = new FormLayout();
        // using layouts as components to avoid importing dependencies.

        // verifying normal use cases
        FormLayout comp1 = new FormLayout();
        layout.add(comp1, 2);
        Assert.assertEquals(layout.getColspan(comp1), 2);
        layout.setColspan(comp1, 1);
        Assert.assertEquals(layout.getColspan(comp1), 1);

        // verifying it correctly sets it to 1 if an number lower than 1 is
        // supplied
        FormLayout comp2 = new FormLayout();
        layout.add(comp2, -1);
        Assert.assertEquals(layout.getColspan(comp2), 1);

        // verifying it correctly gets 1 if invalid colspans are supplied
        // outside the API
        FormLayout compInvalid = new FormLayout();
        layout.add(compInvalid);
        compInvalid.getElement().setAttribute("colspan", "qsd4hdsj%f");
        Assert.assertEquals(layout.getColspan(compInvalid), 1);

        // verifying it correctly gets 1 if no colspan was set.
        FormLayout compUnset = new FormLayout();
        layout.add(compUnset);
        Assert.assertEquals(layout.getColspan(compUnset), 1);

    }

    @Test
    public void setResponsiveSteps_getResponsiveSteps() {
        FormLayout formLayout = new FormLayout();

        formLayout.setResponsiveSteps(new ResponsiveStep(null, 1));
        Assert.assertEquals(1, formLayout.getResponsiveSteps().size());

        formLayout.setResponsiveSteps(new ResponsiveStep(null, 1),
                new ResponsiveStep("1px", 1));
        Assert.assertEquals(2, formLayout.getResponsiveSteps().size());

        formLayout.setResponsiveSteps(new ResponsiveStep(null, 1),
                new ResponsiveStep("1px", 1), new ResponsiveStep("1px", 1,
                        ResponsiveStep.LabelsPosition.TOP));
        Assert.assertEquals(3, formLayout.getResponsiveSteps().size());
    }

    @Test
    public void setLabelWidth_getLabelWidth() {
        FormLayout formLayout = new FormLayout();
        Assert.assertFalse(
                formLayout.getStyle().has("--vaadin-form-layout-label-width"));
        Assert.assertNull(formLayout.getLabelWidth());

        formLayout.setLabelWidth("2em");
        Assert.assertEquals("2em",
                formLayout.getStyle().get("--vaadin-form-layout-label-width"));
        Assert.assertEquals("2em", formLayout.getLabelWidth());

        formLayout.setLabelWidth(160, Unit.PIXELS);
        Assert.assertEquals("160.0px",
                formLayout.getStyle().get("--vaadin-form-layout-label-width"));
        Assert.assertEquals("160.0px", formLayout.getLabelWidth());

        formLayout.setLabelWidth(null);
        Assert.assertFalse(
                formLayout.getStyle().has("--vaadin-form-layout-label-width"));
        Assert.assertNull(formLayout.getLabelWidth());
    }

    @Test
    public void setLabelSpacing_getLabelSpacing() {
        FormLayout formLayout = new FormLayout();
        Assert.assertFalse(formLayout.getStyle()
                .has("--vaadin-form-layout-label-spacing"));
        Assert.assertNull(formLayout.getLabelSpacing());

        formLayout.setLabelSpacing("10em");
        Assert.assertEquals("10em", formLayout.getStyle()
                .get("--vaadin-form-layout-label-spacing"));
        Assert.assertEquals("10em", formLayout.getLabelSpacing());

        formLayout.setLabelSpacing(160, Unit.PIXELS);
        Assert.assertEquals("160.0px", formLayout.getStyle()
                .get("--vaadin-form-layout-label-spacing"));
        Assert.assertEquals("160.0px", formLayout.getLabelSpacing());

        formLayout.setLabelSpacing(null);
        Assert.assertFalse(formLayout.getStyle()
                .has("--vaadin-form-layout-label-spacing"));
        Assert.assertNull(formLayout.getLabelSpacing());
    }

    @Test
    public void setColumnSpacing_getColumnSpacing() {
        FormLayout formLayout = new FormLayout();
        Assert.assertFalse(formLayout.getStyle()
                .has("--vaadin-form-layout-column-spacing"));
        Assert.assertNull(formLayout.getColumnSpacing());

        formLayout.setColumnSpacing("10em");
        Assert.assertEquals("10em", formLayout.getStyle()
                .get("--vaadin-form-layout-column-spacing"));
        Assert.assertEquals("10em", formLayout.getColumnSpacing());

        formLayout.setColumnSpacing(160, Unit.PIXELS);
        Assert.assertEquals("160.0px", formLayout.getStyle()
                .get("--vaadin-form-layout-column-spacing"));
        Assert.assertEquals("160.0px", formLayout.getColumnSpacing());

        formLayout.setColumnSpacing(null);
        Assert.assertFalse(formLayout.getStyle()
                .has("--vaadin-form-layout-column-spacing"));
        Assert.assertNull(formLayout.getColumnSpacing());
    }

    @Test
    public void setRowSpacing_getRowSpacing() {
        FormLayout formLayout = new FormLayout();
        Assert.assertFalse(
                formLayout.getStyle().has("--vaadin-form-layout-row-spacing"));
        Assert.assertNull(formLayout.getRowSpacing());

        formLayout.setRowSpacing("10em");
        Assert.assertEquals("10em",
                formLayout.getStyle().get("--vaadin-form-layout-row-spacing"));
        Assert.assertEquals("10em", formLayout.getRowSpacing());

        formLayout.setRowSpacing(160, Unit.PIXELS);
        Assert.assertEquals("160.0px",
                formLayout.getStyle().get("--vaadin-form-layout-row-spacing"));
        Assert.assertEquals("160.0px", formLayout.getRowSpacing());

        formLayout.setRowSpacing(null);
        Assert.assertFalse(
                formLayout.getStyle().has("--vaadin-form-layout-row-spacing"));
        Assert.assertNull(formLayout.getRowSpacing());
    }

    @Test
    public void setAutoResponsive_getAutoResponsive() {
        FormLayout formLayout = new FormLayout();
        Assert.assertFalse(
                formLayout.getElement().hasProperty("autoResponsive"));

        formLayout.setAutoResponsive(true);
        Assert.assertTrue(
                formLayout.getElement().getProperty("autoResponsive", false));
    }

    @Test
    public void setAutoRows_getAutoRows() {
        FormLayout formLayout = new FormLayout();
        Assert.assertFalse(formLayout.getElement().hasProperty("autoRows"));
        Assert.assertFalse(formLayout.isAutoRows());

        formLayout.setAutoRows(true);
        Assert.assertTrue(
                formLayout.getElement().getProperty("autoRows", false));
        Assert.assertTrue(formLayout.isAutoRows());
    }

    @Test
    public void setColumnWidth_getColumnWidth() {
        FormLayout formLayout = new FormLayout();
        Assert.assertFalse(formLayout.getElement().hasProperty("columnWidth"));
        Assert.assertNull(formLayout.getColumnWidth());

        formLayout.setColumnWidth("10em");
        Assert.assertEquals("10em",
                formLayout.getElement().getProperty("columnWidth"));
        Assert.assertEquals("10em", formLayout.getColumnWidth());

        formLayout.setColumnWidth(160, Unit.PIXELS);
        Assert.assertEquals("160.0px",
                formLayout.getElement().getProperty("columnWidth"));
        Assert.assertEquals("160.0px", formLayout.getColumnWidth());

        formLayout.setColumnWidth(null);
        Assert.assertNull(formLayout.getElement().getProperty("columnWidth"));
        Assert.assertNull(formLayout.getColumnWidth());
    }

    @Test
    public void setMaxColumns_getMaxColumns() {
        FormLayout formLayout = new FormLayout();
        Assert.assertEquals(0, formLayout.getMaxColumns());
        Assert.assertFalse(formLayout.getElement().hasProperty("maxColumns"));

        formLayout.setMaxColumns(4);
        Assert.assertEquals(4, formLayout.getMaxColumns());
        Assert.assertEquals(4,
                formLayout.getElement().getProperty("maxColumns", 0));
    }

    @Test
    public void minColumnsPropertyIsEmptyByDefault() {
        var formLayout = new FormLayout();
        Assert.assertEquals(0, formLayout.getMinColumns());
        Assert.assertFalse(formLayout.getElement().hasProperty("minColumns"));
    }

    @Test
    public void setMinColumns_minColumnsIsCorrectlyUpdated() {
        var formLayout = new FormLayout();
        var minColumns = 4;
        formLayout.setMinColumns(minColumns);
        Assert.assertEquals(minColumns, formLayout.getMinColumns());
        Assert.assertEquals(minColumns,
                formLayout.getElement().getProperty("minColumns", 0));
    }

    @Test
    public void setExpandColumns_isExpandColumns() {
        FormLayout formLayout = new FormLayout();
        Assert.assertFalse(
                formLayout.getElement().hasProperty("expandColumns"));
        Assert.assertFalse(formLayout.isExpandColumns());

        formLayout.setExpandColumns(true);
        Assert.assertTrue(
                formLayout.getElement().getProperty("expandColumns", false));
        Assert.assertTrue(formLayout.isExpandColumns());
    }

    @Test
    public void setExpandFields_isExpandFields() {
        FormLayout formLayout = new FormLayout();
        Assert.assertFalse(formLayout.getElement().hasProperty("expandFields"));
        Assert.assertFalse(formLayout.isExpandFields());

        formLayout.setExpandFields(true);
        Assert.assertTrue(
                formLayout.getElement().getProperty("expandFields", false));
        Assert.assertTrue(formLayout.isExpandFields());
    }

    @Test
    public void setLabelsAside_isLabelsAside() {
        FormLayout formLayout = new FormLayout();
        Assert.assertFalse(formLayout.getElement().hasProperty("labelsAside"));
        Assert.assertFalse(formLayout.isLabelsAside());

        formLayout.setLabelsAside(true);
        Assert.assertTrue(
                formLayout.getElement().getProperty("labelsAside", false));
        Assert.assertTrue(formLayout.isLabelsAside());
    }

    @Test
    public void addFormRow() {
        FormLayout formLayout = new FormLayout();
        FormRow row = formLayout.addFormRow(new Input(), new Input());
        Assert.assertEquals(2, row.getElement().getChildCount());
        Assert.assertEquals(formLayout.getElement(),
                row.getElement().getParent());
    }

    @Test
    public void formRow_addFormItem() {
        FormRow row = new FormRow();
        FormItem item = row.addFormItem(new Input(), "custom label");
        Assert.assertEquals(2, item.getElement().getChildCount());

        Element input = item.getElement().getChild(0);
        Assert.assertNotNull(input);
        Assert.assertEquals("input", input.getTag());

        Element label = item.getElement().getChild(1);
        Assert.assertNotNull(label);
        Assert.assertEquals("label", label.getTag());
        Assert.assertEquals("custom label", label.getText());
    }

    @Test
    public void formRow_addFormItemWithComponent() {
        FormRow row = new FormRow();
        FormItem item = row.addFormItem(new Input(), new Span("custom label"));
        Assert.assertEquals(2, item.getElement().getChildCount());

        Element input = item.getElement().getChild(0);
        Assert.assertNotNull(input);
        Assert.assertEquals("input", input.getTag());

        Element label = item.getElement().getChild(1);
        Assert.assertNotNull(label);
        Assert.assertEquals("span", label.getTag());
        Assert.assertEquals("custom label", label.getText());
    }

    @Test
    public void formRow_setColspan_getColspan() {
        Input input = new Input();
        FormRow row = new FormRow();
        row.add(input);

        Assert.assertEquals(1, row.getColspan(input));

        row.setColspan(input, 2);
        Assert.assertEquals(2, row.getColspan(input));

        row.setColspan(input, -1);
        Assert.assertEquals(1, row.getColspan(input));
    }

    @Test
    public void formRow_addComponentWithColspan() {
        Input input = new Input();
        FormRow row = new FormRow();
        row.add(input, 2);
        Assert.assertEquals(2, row.getColspan(input));
    }
}
