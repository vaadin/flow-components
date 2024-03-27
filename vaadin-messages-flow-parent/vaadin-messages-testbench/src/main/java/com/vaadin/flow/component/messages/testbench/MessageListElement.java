/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.messages.testbench;

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-message-list&gt;</code>
 * element.
 *
 * @author Vaadin Ltd.
 */
@Element("vaadin-message-list")
public class MessageListElement extends TestBenchElement {

    /**
     * Gets the <code>&lt;vaadin-message&gt;</code> elements rendered in this
     * message list.
     *
     * @return
     */
    public List<MessageElement> getMessageElements() {
        return $(MessageElement.class).all();
    }

}
