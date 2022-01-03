/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.upload.receivers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;

/**
 * FileOutputStream with a reference to the output file.
 */
public class UploadOutputStream extends FileOutputStream
        implements Serializable {
    private final File file;

    /**
     * @see FileOutputStream#FileOutputStream(File)
     * @param file
     *            File to write.
     * @throws FileNotFoundException
     *             see {@link FileOutputStream#FileOutputStream(File)}
     */
    public UploadOutputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    /**
     * @return File written by this output stream.
     */
    public File getFile() {
        return file;
    }
}
