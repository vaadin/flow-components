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
package com.vaadin.flow.component.notification.testbench;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-notification&gt;</code>
 * element.
 */
@Element("vaadin-notification")
public class NotificationElement extends TestBenchElement {

    /**
     * Checks whether the notification is shown.
     *
     * @return <code>true</code> if the notification is shown,
     *         <code>false</code> otherwise
     */
    public boolean isOpen() {
        try {
            return getPropertyBoolean("opened");
        } catch (StaleElementReferenceException e) {
            // The element is no longer even attached to the DOM
            // -> it's not open
            return false;
        }
    }

    @Override
    public String getText() {
        return getCard().getText();
    }

    @Override
    public SearchContext getContext() {
        return getCard();
    }

    /**
     * Gets the card for the notification, which is where the content is added.
     *
     * @return the card for the notification
     */
    private TestBenchElement getCard() {
        return getPropertyElement("_card");
    }
}
