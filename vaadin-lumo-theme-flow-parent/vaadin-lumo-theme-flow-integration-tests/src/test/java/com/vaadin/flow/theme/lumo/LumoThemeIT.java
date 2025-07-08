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
package com.vaadin.flow.theme.lumo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-lumo-theme/lumo-theme")
public class LumoThemeIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void lumoThemeApplied() {
        TestBenchElement testComponent = $("test-component").first();

        // this is silly, but a concrete way to test that the lumo files are
        // imported by verifying that the lumo css variables introduced in the
        // files work
        Assert.assertEquals("rgba(224, 36, 26, 1)",
                testComponent.getCssValue("color"));
        Assert.assertEquals("40px", testComponent.getCssValue("font-size"));
        Assert.assertEquals("36px solid rgb(0, 0, 0)",
                testComponent.getCssValue("border"));
        Assert.assertEquals("12px 24px", testComponent.getCssValue("margin"));
        Assert.assertEquals("20px", testComponent.getCssValue("border-radius"));
        Assert.assertEquals("lumo-icons", testComponent.getCssValue("font-family"));
    }
}
