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
package com.vaadin.flow.component.masterdetaillayout.testbench;

import org.openqa.selenium.By;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-master-detail-layout&gt;</code> element.
 */
@Element("vaadin-master-detail-layout")
public class MasterDetailLayoutElement extends TestBenchElement {
    /**
     * Gets the element in the master area.
     *
     * @return the element in the master area, or {@code null} if not set
     */
    public TestBenchElement getMaster() {
        return (TestBenchElement) findElements(By.xpath("./*[ @slot='' ]"))
                .stream().findFirst().orElse(null);
    }

    /**
     * Gets the element in the detail area.
     *
     * @return the element in the detail area, or {@code null} if not set
     */
    public TestBenchElement getDetail() {
        return (TestBenchElement) findElements(
                By.xpath("./*[ @slot='detail' ]")).stream().findFirst()
                .orElse(null);
    }
}
