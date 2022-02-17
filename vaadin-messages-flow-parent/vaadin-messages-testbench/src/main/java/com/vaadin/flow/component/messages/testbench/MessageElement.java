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
package com.vaadin.flow.component.messages.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-message&gt;</code>
 * element.
 *
 * @author Vaadin Ltd.
 */
@Element("vaadin-message")
public class MessageElement extends TestBenchElement {

    /**
     * Gets the text content of the message body.
     *
     * @return the text content of the message body
     */
    @Override
    public String getText() {
        return this.getPropertyString("textContent");
    }

    /**
     * Gets the {@code time} property of this element.
     *
     * @return the {@code time} property
     */
    public String getTime() {
        return getPropertyString("time");
    }

    /**
     * Gets the {@code userName} property of this element.
     *
     * @return the {@code userName} property
     */
    public String getUserName() {
        return getPropertyString("userName");
    }

    /**
     * Gets the {@code userAbbr} property of this element.
     *
     * @return the {@code userAbbr} property
     */
    public String getUserAbbr() {
        return getPropertyString("userAbbr");
    }

    /**
     * Gets the {@code userImg} property of this element.
     *
     * @return the {@code userImg} property
     */
    public String getUserImg() {
        return getPropertyString("userImg");
    }

    /**
     * Gets the {@code userColorIndex} property of this element.
     *
     * @return the {@code userColorIndex} property
     */
    public int getUserColorIndex() {
        return getPropertyInteger("userColorIndex");
    }

    /**
     * Gets the {@code theme} attribute of this element.
     *
     * @return the {@code theme} attribute
     */
    public String getTheme() {
        return getAttribute("theme");
    }

}
