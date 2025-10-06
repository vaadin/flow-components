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
package com.vaadin.flow.component.popover.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.google.j2objc.annotations.J2ObjCIncompatible;
import com.vaadin.flow.component.popover.testbench.PopoverElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link PopoverInDialogView}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-popover/dialog")
public class PopoverInDialogIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }
    
    @Test
    public void popoverInDialog_clickTarget_popoverOpensAndCloses() {
        openDialog();
        var popover = $(PopoverElement.class).id("popover-dialog");
        clickElementWithJs("popover-target");
        Assert.assertTrue(popover.isOpen());
        assertLogText("Popover in dialog opened: true");

        clickElementWithJs("popover-target");
        Assert.assertFalse(popover.isOpen());
        assertLogText("Popover in dialog opened: false");
    }
    
    @Test
    public void popoverInDialog_reopenDialog_popoverOpensAndCloses() {
        openDialog();
        closeDialog();
        // Reopen dialog
        openDialog();
        var popover = $(PopoverElement.class).id("popover-dialog");
        clickElementWithJs("popover-target");
        Assert.assertTrue(popover.isOpen());
        assertLogText("Popover in dialog opened: true");

        clickElementWithJs("popover-target");
        Assert.assertFalse(popover.isOpen());
        assertLogText("Popover in dialog opened: false");
    }   
    
    @Test
    public void popoverInGridInDialog_clickTarget_popoverOpensAndCloses() {
        openDialog();
        var popover = $(PopoverElement.class).id("grid-popover-item-1");
        clickElementWithJs("target-item-1");
        Assert.assertTrue(popover.isOpen());
        assertLogText("Popover for Item 1 opened: true");

        clickElementWithJs("target-item-1");
        Assert.assertFalse(popover.isOpen());
        assertLogText("Popover for Item 1 opened: false");

        clickElementWithJs("target-item-2");
        Assert.assertFalse(popover.isOpen());
        assertLogText("Popover for Item 2 opened: true");

        clickElementWithJs("target-item-3");
        Assert.assertFalse(popover.isOpen());
        assertLogText("Popover for Item 3 opened: true");
    }
    
    @Test
    public void popoverInGridInDialog_reopenDialog_popoverOpensAndCloses() {
        openDialog();
        closeDialog();
        // Reopen dialog
        openDialog();
        var popover = $(PopoverElement.class).id("grid-popover-item-1");
        clickElementWithJs("target-item-1");
        Assert.assertTrue(popover.isOpen());
        assertLogText("Popover for Item 1 opened: true");

        clickElementWithJs("target-item-1");
        Assert.assertFalse(popover.isOpen());
        assertLogText("Popover for Item 1 opened: false");
    }
    
    private void assertLogText(String expected) {
        String logText = findElement(By.id("message-log")).getText();
        Assert.assertEquals(expected, logText);
    }

    private void openDialog() {
        clickElementWithJs("open-dialog");
    }
    
    private void closeDialog() {
        clickElementWithJs("close-dialog");
    }
}
