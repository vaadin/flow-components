
package com.vaadin.flow.component.avatar.tests;

import com.vaadin.flow.component.avatar.testbench.AvatarElement;
import com.vaadin.flow.component.avatar.testbench.AvatarGroupElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Integration tests for the {@link I18nPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-avatar/i18n-test")
public class AvatarI18nIT extends AbstractComponentIT {

    private WebElement dataTitle;
    private WebElement dataAriaLabel;

    @Before
    public void init() {
        open();
        dataTitle = findElement(By.id("data-title-i18n"));
        dataAriaLabel = findElement(By.id("data-aria-label-i18n"));
    }

    @Test
    public void i18nIsSet() {
        WebElement setI18n = getTestButton("set-i18n");
        WebElement getI18n = getTestButton("get-i18n");
        setI18n.click();
        getI18n.click();

        Assert.assertEquals(dataTitle.getText(),
                $(AvatarElement.class).waitForFirst().getTitle());

        Assert.assertEquals(dataAriaLabel.getText(),
                $(AvatarGroupElement.class).waitForFirst().getAriaLabel());
    }

    private WebElement getTestButton(String id) {
        return findElement(By.id(id));
    }
}
