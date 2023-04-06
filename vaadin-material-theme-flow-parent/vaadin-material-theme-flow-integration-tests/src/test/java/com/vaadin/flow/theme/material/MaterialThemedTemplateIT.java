
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
