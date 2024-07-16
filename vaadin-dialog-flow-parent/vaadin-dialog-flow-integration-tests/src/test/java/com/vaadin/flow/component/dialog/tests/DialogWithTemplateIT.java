/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.LabelElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-dialog/dialog-template-test")
public class DialogWithTemplateIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openDialog_clickThreeTimes_containerIsUpdated() {
        $(NativeButtonElement.class).id("open").click();

        waitForElementPresent(By.tagName(DialogTestPageIT.DIALOG_OVERLAY_TAG));
        DialogElement dialog = $(DialogElement.class).first();

        TestBenchElement template = dialog.$("vaadin-dialog-flow-test-template")
                .first();
        NativeButtonElement btn = template.$(NativeButtonElement.class).first();
        DivElement container = template.$(DivElement.class).first();
        List<SpanElement> spans = container.$(SpanElement.class).all();
        Assert.assertTrue(spans.isEmpty());

        for (int i = 0; i < 3; i++) {
            btn.click();

            int size = i + 1;
            LabelElement label = container.$(LabelElement.class)
                    .id("label-" + size);
            Assert.assertEquals("Label " + size, label.getText());
        }
    }
}
