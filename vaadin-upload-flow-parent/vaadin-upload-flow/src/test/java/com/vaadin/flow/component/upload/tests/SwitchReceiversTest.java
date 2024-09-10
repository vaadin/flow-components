package com.vaadin.flow.component.upload.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;

public class SwitchReceiversTest {
    @Test
    public void switchBetweenSingleAndMultiFileReceiver_assertMaxFilesProperty() {
        Upload upload = new Upload();
        upload.setReceiver(new MemoryBuffer());
        Assert.assertEquals(1, upload.getElement().getProperty("maxFiles", 0));

        upload.setReceiver(new MultiFileMemoryBuffer());
        Assert.assertFalse(upload.getElement().hasProperty("maxFiles"));

        upload.setReceiver(new MemoryBuffer());
        Assert.assertEquals(1, upload.getElement().getProperty("maxFiles", 0));
    }

    @Test
    public void setMaxFiles_setSingleFileReceiver_maxFilesAreSetToOne() {
        Upload upload = new Upload();
        upload.setMaxFiles(3);
        upload.setReceiver(new MemoryBuffer());
        Assert.assertEquals(1, upload.getElement().getProperty("maxFiles", 0));
    }

    @Test
    public void setMaxFiles_setMultiFileReceiver_maxFilesArePreserved() {
        Upload upload = new Upload();
        upload.setMaxFiles(3);
        upload.setReceiver(new MultiFileMemoryBuffer());
        Assert.assertEquals(3, upload.getElement().getProperty("maxFiles", 0));
    }
}
