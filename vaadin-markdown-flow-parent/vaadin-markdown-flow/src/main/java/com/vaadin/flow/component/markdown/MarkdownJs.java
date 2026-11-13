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
package com.vaadin.flow.component.markdown;

import java.io.Serializable;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that {@link Markdown} runs on its element in the browser, for
 * {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface MarkdownJs extends Serializable {

    /**
     * Appends text to the Markdown content, used when the new content starts
     * with the content that the browser already has.
     *
     * @param content
     *            the text to append
     */
    @JsExpression("this.content += $0")
    void appendContent(String content);

    /**
     * Replaces the Markdown content.
     *
     * @param content
     *            the new content, or <code>null</code>
     */
    @JsExpression("this.content = $0")
    void setContent(String content);
}
