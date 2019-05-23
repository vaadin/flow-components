/*
 * Copyright 2000-2019 Vaadin Ltd.
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
package com.vaadin.flow.component.menubar.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("slotted-in-template")
public class SlottedInTemplateIT extends AbstractComponentIT {

    private MenuBarElement menuBar;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).first();
    }

    @Test // https://github.com/vaadin/vaadin-menu-bar-flow/issues/33
    public void menuBarSlottedInPolymerTemplate_anotherElementIdMapped_componentRendered_noClientSideErrors() {
        checkLogsForErrors();
        Assert.assertEquals("foo", menuBar.getButtons().get(0).getText());
    }

    @Test
    public void menuBarSlottedInPolymerTemplate_anotherElementIdMapped_buttonPropagatesClickToItem() {
        menuBar.getButtons().get(0).click();
        Assert.assertEquals(1, $("label").all().size());
        Assert.assertEquals("clicked", $("label").first().getText());
    }
}
