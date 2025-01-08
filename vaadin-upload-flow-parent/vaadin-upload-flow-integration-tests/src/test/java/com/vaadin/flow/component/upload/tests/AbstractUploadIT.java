/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.vaadin.tests.AbstractComponentIT;

public abstract class AbstractUploadIT extends AbstractComponentIT {

    /**
     * Creates a temp file with the provided extension for testing purposes.
     *
     * @param extension
     *            the temp file extension without leading dot
     * @return The generated temp file handle
     * @throws IOException
     */
    File createTempFile(String extension) throws IOException {
        File tempFile = File.createTempFile("TestFileUpload", "." + extension);
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(getTempFileContents());
        writer.close();
        tempFile.deleteOnExit();
        return tempFile;
    }

    String getTempFileContents() {
        return "This is a test file! Row 2 Row3";
    }

}
