package com.vaadin.flow.component.richtexteditor.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.richtexteditor.testbench.RichTextEditorElement;
import com.vaadin.testbench.TestBenchElement;

public class BasicUseIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
        ButtonElement setValue = getTestButton("setValue");
        setValue.click();
    }

    @Test
    public void rteIsPresent() {
        Assert.assertTrue($(RichTextEditorElement.class).exists());
    }

    @Test
    @Ignore("For some reasons neither sendKeys nor setting innerHTML don't "
            + "send new value to the server being testing via sauce, but the test works locally")
    public void setValueCorrectly() {
        TestBenchElement editor = $(RichTextEditorElement.class).waitForFirst()
                .getEditor();
        editor.setProperty("innerHTML", "");
        editor.sendKeys("Bar");
        ButtonElement getValue = getTestButton("getValue");
        ButtonElement getHtmlValue = getTestButton("getHtmlValue");

        waitUntil(driver -> {
            new Actions(getDriver()).click(editor).build().perform();
            new Actions(getDriver()).click(getValue).build().perform();
            new Actions(getDriver()).click(getHtmlValue).build().perform();
            return getLastValue().equals("[{\"insert\":\"Bar\\n\"}]")
                    && getLastHtmlValue().equals("<p>Bar</p>");
        });

        Assert.assertEquals("[{\"insert\":\"Bar\\n\"}]", getLastValue());
        Assert.assertEquals("<p>Bar</p>", getLastHtmlValue());
    }

    @Test
    public void getValueCorrect() {
        ButtonElement getValue = getTestButton("getValue");
        getValue.click();

        Assert.assertEquals("[{\"insert\":\"Foo\"}]", getLastValue());
    }

    @Test
    public void getHtmlValueCorrect() {
        ButtonElement getHtmlValue = getTestButton("getHtmlValue");
        getHtmlValue.click();

        Assert.assertEquals("<p>Foo</p>", getLastHtmlValue());
    }

    @Test
    public void setAndGetI18nCorrect() {
        ButtonElement setI18n = getTestButton("setI18n");
        ButtonElement getI18n = getTestButton("getI18n");
        setI18n.click();
        getI18n.click();

        Assert.assertEquals(getLastI18nValue(), $(RichTextEditorElement.class)
                .waitForFirst().getTitles().toString());
    }

    // Binder

    @Test
    public void useBinderWithRichTextEditor() {
        WebElement info = findElement(By.id("binder-info"));
        ButtonElement save = getTestButton("binder-save");
        ButtonElement reset = getTestButton("binder-reset");
        ButtonElement getValue = getTestButton("get-binder-rte-value");
        save.click();

        // Empty rte validation: there is an error
        waitUntil(
                driver -> "There are errors: Delta value should contain something"
                        .equals(getLastBinderInfoValue()));

        $(RichTextEditorElement.class).get(1).getEditor()
                .setProperty("innerHTML", "<p>Foo</p>");

        // Rte validation
        waitUntil(driver -> {
            save.click();
            return info.getText().startsWith("Saved bean values");
        });

        Assert.assertTrue(getLastBinderInfoValue().contains("Foo"));

        reset.click();

        // Wait for everything to update.
        waitUntil(driver -> info.getText().isEmpty());

        getValue.click();
        Assert.assertEquals("", getLastRteBinderValue());
    }

    @Test
    @Ignore("For some reasons neither sendKeys nor setting innerHTML don't "
            + "send new value to the server being testing via sauce, but the test works locally")
    public void richTextEditorInATemplate_settingAndGettingValueCorrectly() {
        RichTextEditorElement templateRte = $("rte-in-a-template")
                .id("template").$(RichTextEditorElement.class).first();
        templateRte.getEditor().sendKeys("Bar");
        ButtonElement getValue = getTestButton("get-template-rte-value");

        waitUntil(driver -> {
            new Actions(getDriver()).click(templateRte.getEditor()).build()
                    .perform();
            new Actions(getDriver()).click(getValue).build().perform();
            return getLastRteTemplateValue()
                    .equals("[{\"insert\":\"Bar\\n\"}]");
        });
    }

    private ButtonElement getTestButton(String id) {
        return $(ButtonElement.class).onPage().id(id);
    }
}
