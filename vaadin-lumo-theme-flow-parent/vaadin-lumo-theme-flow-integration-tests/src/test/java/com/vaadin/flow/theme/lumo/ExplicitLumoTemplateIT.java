
package com.vaadin.flow.theme.lumo;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;

@TestPath(value = "vaadin-lumo-theme/explicit-template-view")
public class ExplicitLumoTemplateIT extends AbstractThemedTemplateIT {

    @Test
    public void darkVariantIsUsed_htmlElementHasThemeAttribute() {
        open();

        WebElement html = findElement(By.tagName("html"));
        Assert.assertEquals(Lumo.DARK, html.getAttribute("theme"));
    }

    @Override
    protected String getTagName() {
        return "explicit-lumo-themed-template";
    }

    @Override
    protected String getThemedTemplate() {
        return "theme/lumo/LumoThemedTemplate.js";
    }

}
