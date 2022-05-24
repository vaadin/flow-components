package com.vaadin.flow.component.upload.tests;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-upload/non-immediate-upload")
public class NonImmediateUploadIT extends AbstractUploadIT {

    @Test
    public void uploadMultipleFiles_shouldNotThrowException_onStart()
            throws Exception {
        uploadMultipleFiles_shouldNotThrowException("start-button");
    }

    @Test
    public void uploadMultipleFiles_shouldNotThrowException_onRemove()
            throws Exception {
        uploadMultipleFiles_shouldNotThrowException("remove-button");
    }

    private void uploadMultipleFiles_shouldNotThrowException(String buttonType)
            throws Exception {
        open();
        File file1 = createTempFile();
        File file2 = createTempFile();
        UploadElement upload = $(UploadElement.class).waitForFirst();
        upload.upload(file1);
        upload.upload(file2);
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
