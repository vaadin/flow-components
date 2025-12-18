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
package com.vaadin.flow.component.markdown.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-markdown&gt;</code>
 * element.
 */
@Element("vaadin-markdown")
public class MarkdownElement extends TestBenchElement {

    /**
     * Gets the Markdown content of the element.
     * <p>
     * The DOM does not typically contain the Markdown content as such. Use the
     * regular TestBench APIs, such as {@link #$(String)}, to access the
     * effective DOM with the rendered Markdown content.
     */
    public String getContent() {
        return getPropertyString("content");
    }

}
