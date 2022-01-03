/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.theme.material;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath(value = "vaadin-material-theme/material-themed-template-view")
public class MaterialThemedTemplateIT extends AbstractComponentIT {

    @Test
    public void materialThemeUsed_themedTemplateAndThemeResourcesLoaded() {
        open();

        // check that all imported templates are available in the DOM
        TestBenchElement template = $("material-themed-template").first();

        TestBenchElement div = template.$("div").first();

        // From here we test whether Material or Lumo is used.
        // The reason is that theme is unique per application, then
        // when merging all modules in one application during CI
        // lumo is used and material removed.
        Assert.assertTrue("Should be Lumo or Material themed",
                div.getText().matches("(Material|Lumo) themed Template"));

        // this is silly, but a concrete way to test that the theme files are
        // imported by verifying that the material css variables introduced in
        // the
        // files work

        // Material css property values
        // rgba(176, 0, 32, 1)
        // 16px
        // Lumo css property values
        // rgba(224, 36, 26, 1)
        // 40px

        String color = div.getCssValue("color");
        Assert.assertTrue("Should set correct color but it was " + color,
                color.matches("rgba\\((176, 0, 32, 1|224, 36, 26, 1)\\)"));

        String font = div.getCssValue("font-size");
        Assert.assertTrue("Should set correct font size but it was " + font,
                font.matches("(16|40)px"));
    }

}
