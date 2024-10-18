/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.orderedlayout.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.orderedlayout.testbench.ScrollerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-ordered-layout/scroller")
public class ScrollerIT extends AbstractComponentIT {

    private ScrollerElement scroller;

    @Before
    public void init() {
        open();
        scroller = $(ScrollerElement.class).first();
    }

    @Test
    public void scrollToBottom() {
        $("button").id("scroll-to-bottom-button").click();
        int scrollTop = scroller.getPropertyInteger("scrollTop");
        int scrollHeight = scroller.getPropertyInteger("scrollHeight");
        int clientHeight = scroller.getPropertyInteger("clientHeight");
        Assert.assertEquals(scrollHeight - clientHeight, scrollTop);
        Assert.assertNotEquals(0, scrollTop);
    }

    @Test
    public void scrollToTop() {
        $("button").id("scroll-to-bottom-button").click();
        $("button").id("scroll-to-top-button").click();
        int scrollTop = scroller.getPropertyInteger("scrollTop");
        Assert.assertEquals(0, scrollTop);
    }
}
