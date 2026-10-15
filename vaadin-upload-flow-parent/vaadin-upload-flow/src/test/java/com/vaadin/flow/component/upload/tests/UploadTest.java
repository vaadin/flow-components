/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;

public class UploadTest {

    @Test
    public void uploadNewUpload() {
        // Test no NPE due missing UI when setAttribute is called.
        Upload upload = new Upload();
    }

    @Test
    public void implementsHasEnabled() {
        Assert.assertTrue(HasEnabled.class.isAssignableFrom(Upload.class));
    }

    @Test
    public void switchBetweenSingleAndMultiFileReceiver_assertMaxFilesProperty() {
        Upload upload = new Upload();

        upload.setReceiver(new MemoryBuffer());
        Assert.assertEquals(1, upload.getElement().getProperty("maxFiles", 0));

        upload.setReceiver(new MultiFileMemoryBuffer());
        Assert.assertNull(upload.getElement().getProperty("maxFiles"));

        upload.setReceiver(new MemoryBuffer());
        Assert.assertEquals(1, upload.getElement().getProperty("maxFiles", 0));
    }
}
