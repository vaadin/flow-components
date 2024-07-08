/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import java.io.File;

import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-upload/non-immediate-upload")
public class NonImmediateUploadIT extends AbstractUploadIT {

    @Before
    public void init() {
        if (getRunLocallyBrowser() == null) {
            // Multiple file upload does not work with Remotewebdriver
            // and autoUpload=false
            // Related to
            // https://github.com/SeleniumHQ/selenium/issues/7408
            throw new AssumptionViolatedException(
                    "Skipped <Multiple file upload does not work with Remotewebdriver>");
        }
    }

    @Test
    public void uploadMultipleFiles_shouldNotThrowException_onStart()
            throws Exception {
        uploadMultipleFiles_shouldNotThrowException("start-button");
    }

    @Test
    public void uploadMultipleFiles_shouldNotThrowException_onRemove()
            throws Exception {
        uploadMultipleFiles_shouldNotThrowException("clear-button");
    }

    private void uploadMultipleFiles_shouldNotThrowException(String buttonType)
            throws Exception {
        open();
        File file1 = createTempFile();
        File file2 = createTempFile();
        UploadElement upload = $(UploadElement.class).waitForFirst();
        WebElement input = getInput(upload);
        fillPathToUploadInput(input, file1.getPath(), file2.getPath());
        WebElement button = findButtonInVaadinUploadFile(upload, buttonType);
        button.click();
        TestBenchElement element = $("span").id("error-handler-message");
        Assert.assertEquals("No errors", element.getText());
    }

    private WebElement findButtonInVaadinUploadFile(UploadElement upload,
            String buttonType) {
        final String QUERY = String.format(
                "return arguments[0]"
                        + ".shadowRoot.querySelector('vaadin-upload-file')"
                        + ".shadowRoot.querySelector('[part=\"%s\"]')",
                buttonType);
        return (WebElement) getCommandExecutor().executeScript(QUERY, upload);
    }
}
