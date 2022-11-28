package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.upload.Upload;

import org.junit.Test;

public class UploadTest {

    @Test
    public void uploadNewUpload() {
        // Test no NPE due missing UI when setAttribute is called.
        Upload upload = new Upload();
    }
}
