
package com.vaadin.flow.theme.material;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath(value = "vaadin-material-theme/material-not-themed-template-view")
public class MaterialNotThemedTemplateIT extends AbstractComponentIT {

    @Test
    public void materialThemeUsed_noMaterialTemplateFile_srcBasedTemplateUsed() {
        open();

        // check that all imported templates are available in the DOM
        TestBenchElement template = $("not-themed-template").first();

        TestBenchElement div = template.$("div").first();

        Assert.assertEquals("Template", div.getText());
    }

}
