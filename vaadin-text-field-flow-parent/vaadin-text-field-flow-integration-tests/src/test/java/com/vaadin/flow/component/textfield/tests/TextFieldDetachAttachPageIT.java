
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * Integration tests for attaching / detaching {@link TextField}.
 */
@TestPath("vaadin-text-field/text-field-detach-attach")
public class TextFieldDetachAttachPageIT extends AbstractComponentIT {

    @Test
    public void clientSideValidationIsOverriddenOnAttach() {
        open();

        assertTextFieldIsValidOnTab();

        // Detaching and attaching text field
        WebElement toggleAttach = findElement(By.id("toggle-attached"));
        toggleAttach.click();
        toggleAttach.click();

        assertTextFieldIsValidOnTab();
    }

    private void assertTextFieldIsValidOnTab() {
        WebElement textField = findElement(By.id("text-field"));
        textField.sendKeys(Keys.TAB);
        Assert.assertFalse("Text field should be valid after Tab",
                Boolean.parseBoolean(textField.getAttribute("invalid")));
    }
}
