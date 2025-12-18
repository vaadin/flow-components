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
package com.vaadin.flow.component.upload;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * A component that can be used as a drop zone for uploading files with
 * {@link Upload} when using the {@link Upload#setDropZone(Component)} method.
 * <p>
 * The component has no styling by default. When files are dragged over it, the
 * {@code dragover} attribute is set on the element, which can be used for
 * styling.
 * <p>
 * Example usage:
 *
 * <pre>
 * UploadDropZone dropZone = new UploadDropZone();
 * dropZone.add(new Span("Drop files here"));
 *
 * Upload upload = new Upload();
 * upload.setHeadless(true);
 * upload.setDropZone(dropZone);
 * </pre>
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-upload-drop-zone")
@NpmPackage(value = "@vaadin/upload", version = "25.0.0")
@JsModule("@vaadin/upload/src/vaadin-upload-drop-zone.js")
@JsModule("./vaadin-upload-orchestrator-connector.js")
public class UploadDropZone extends Component implements HasComponents {

    /**
     * Creates a new empty drop zone.
     */
    public UploadDropZone() {
    }
}
