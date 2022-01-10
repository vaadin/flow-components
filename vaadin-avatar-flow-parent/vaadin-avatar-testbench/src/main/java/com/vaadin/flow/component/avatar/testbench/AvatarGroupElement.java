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
package com.vaadin.flow.component.avatar.testbench;

import org.openqa.selenium.NoSuchElementException;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-avatar-group&gt;</code>
 * element.
 */
@Element("vaadin-avatar-group")
public class AvatarGroupElement extends TestBenchElement {

    /**
     * Gets the avatar element for the avatar with the given index.
     *
     * @param index
     *            the index to look for in the avatar group
     * @return the first avatar element which matches the given index
     * @throws NoSuchElementException
     *             if no match was found
     */
    public AvatarElement getAvatarElement(int index)
            throws NoSuchElementException {
        return $(AvatarElement.class).get(index);
    }

    /**
     * Gets the aria label that is pronounced by the screen reader.
     *
     * @return the aria label
     */
    public String getAriaLabel() {
        return getAttribute("aria-label");
    }

}
