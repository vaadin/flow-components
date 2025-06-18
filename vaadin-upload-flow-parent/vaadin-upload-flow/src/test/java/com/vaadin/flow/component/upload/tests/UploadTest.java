/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.upload.Upload;

public class UploadTest {

    @Test
    public void uploadNewUpload() {
        // Test no NPE due missing UI when setAttribute is called.
        Upload upload = new Upload();
    }

    @Test
    public void upload_setAutoUpload_isAutoUpload() {
        Upload upload = new Upload();
        upload.setAutoUpload(true);
        Assert.assertEquals(true, upload.isAutoUpload());
    }
}
