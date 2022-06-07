package com.vaadin.flow.component.customfield.tests;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-custom-field/validation")
public class ValidationIT extends AbstractComponentIT {

    private TestBenchElement customField;
    private TestBenchElement setInvalid;
    private TestBenchElement attach;
    private TestBenchElement detach;
    private TestBenchElement logInvalidState;
    private TestBenchElement logOutput;

    @Before
    public void init() {
        open();

        customField = $("vaadin-custom-field").waitForFirst();
        setInvalid = $("button").id("set-invalid");
        attach = $("button").id("attach");
        detach = $("button").id("detach");
        logInvalidState = $("button").id("log-invalid-state");
        logOutput = $("span").id("log-output");
    }

    @Test
    public void overridesClientValidation() {
        setInvalid.click();

        executeScript("arguments[0].validate()", customField);

        Assert.assertEquals(true, customField.getPropertyBoolean("invalid"));
    }

    @Test
    public void detach_reattach_overridesClientValidation() {
        setInvalid.click();
        detach.click();
        attach.click();

        customField = $("vaadin-custom-field").waitForFirst();
        executeScript("arguments[0].validate()", customField);

        Assert.assertEquals(true, customField.getPropertyBoolean("invalid"));
    }

    @Test
    public void changeInvalidOnClient_notSynchronizedToServer() {
        setInvalid.click();

        executeScript("arguments[0].invalid = false", customField);
        logInvalidState.click();

        Assert.assertEquals("true", logOutput.getText());
    }
}
