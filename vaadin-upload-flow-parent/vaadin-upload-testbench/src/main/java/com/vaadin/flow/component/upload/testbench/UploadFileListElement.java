/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.upload.testbench;

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-upload-file-list&gt;</code> element.
 */
@Element("vaadin-upload-file-list")
public class UploadFileListElement extends TestBenchElement {

    /**
     * Gets the number of files in the list.
     *
     * @return the number of files
     */
    public int getFileCount() {
        List<TestBenchElement> files = getFiles();
        return files != null ? files.size() : 0;
    }

    /**
     * Gets all file elements in the list.
     *
     * @return a list of file elements
     */
    public List<TestBenchElement> getFiles() {
        return $("vaadin-upload-file").all();
    }
}
