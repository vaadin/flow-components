/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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

    @Test
    public void setMaxFiles_setMultiFileReceiver_setAnotherMultiFileReceiver_maxFilesArePreserved() {
        Upload upload = new Upload();
        upload.setMaxFiles(3);
        upload.setReceiver(new MultiFileMemoryBuffer());
        upload.setReceiver(new MultiFileMemoryBuffer());
        Assert.assertEquals(3, upload.getElement().getProperty("maxFiles", 0));
    }
}
