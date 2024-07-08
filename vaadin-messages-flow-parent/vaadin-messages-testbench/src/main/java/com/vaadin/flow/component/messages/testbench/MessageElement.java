/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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

}
