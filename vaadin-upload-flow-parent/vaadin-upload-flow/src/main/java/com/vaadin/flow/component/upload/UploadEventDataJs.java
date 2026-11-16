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
package com.vaadin.flow.component.upload;

import java.io.Serializable;

import com.vaadin.flow.dom.DomEventData;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsSnippets;

/**
 * The event data that {@link Upload} sends with its events, for
 * {@link JsSnippets#of(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface UploadEventDataJs extends Serializable {

    /**
     * Tells whether any file of the upload element is still uploading.
     *
     * @return event data that is {@code true} if a file is uploading,
     *         {@code false} otherwise
     */
    @JsExpression("return element.files.some(file => file.uploading)")
    DomEventData<Boolean> filesUploading();
}
