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

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that components implementing {@link HasUploadManager} run on
 * their element in the browser, for {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface HasUploadManagerJs extends Serializable {

    /**
     * Links the element to the client-side manager of an upload manager
     * connector, or unlinks it.
     *
     * @param connector
     *            the element of the upload manager connector, or {@code null}
     *            to unlink the element
     */
    @JsExpression("this.manager = $0 ? $0.manager : null")
    void setManager(Element connector);
}
