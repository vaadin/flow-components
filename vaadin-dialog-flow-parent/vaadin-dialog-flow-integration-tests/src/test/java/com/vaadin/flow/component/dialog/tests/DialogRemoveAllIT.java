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
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-dialog/remove-all")
public class DialogRemoveAllIT extends AbstractComponentIT {

    private DialogElement dialog;

    @Before
    public void init() {
        open();
        dialog = $(DialogElement.class).waitForFirst();
    }

    @Test
    public void removeAll_dialogContentUpdated() {
        Assert.assertTrue(dialog.$("span").withText("Main content").exists());
        Assert.assertTrue(
                dialog.$("button").withText("Replace content").exists());

        dialog.$("button").id("replace-content").click();

        Assert.assertFalse(
                dialog.$("button").withText("Replace content").exists());
        Assert.assertFalse(dialog.$("span").withText("Main content").exists());
        Assert.assertTrue(
                dialog.$("span").withText("Updated content").exists());
    }

    @Test
    public void removeAll_preservesHeaderAndFooterContent() {
        Assert.assertTrue(dialog.$("span").withText("Header content").exists());
        Assert.assertTrue(dialog.$("span").withText("Footer content").exists());

        dialog.$("button").id("replace-content").click();

        Assert.assertTrue(dialog.$("span").withText("Header content").exists());
        Assert.assertTrue(dialog.$("span").withText("Footer content").exists());
    }
}
