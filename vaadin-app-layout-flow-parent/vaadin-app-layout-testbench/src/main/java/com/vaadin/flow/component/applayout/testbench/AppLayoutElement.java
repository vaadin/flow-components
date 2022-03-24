package com.vaadin.flow.component.applayout.testbench;

/*
 * #%L
 * Vaadin App Layout Testbench API
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-app-layout")
public class AppLayoutElement extends TestBenchElement {

    @SuppressWarnings("unchecked")
    public TestBenchElement getContent() {
        TestBenchElement contentPlaceholder = $(TestBenchElement.class)
                .attribute("content", "").first();

        return (TestBenchElement) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0];",
                contentPlaceholder);
    }

    public boolean isDrawerFirst() {
        return Boolean.TRUE.equals(getPropertyBoolean("drawerFirst"));
    }

    public void setDrawerFirst(boolean drawerFirst) {
        setProperty("drawerFirst", drawerFirst);
    }

    public boolean isDrawerOpened() {
        return getPropertyBoolean("drawerOpened");
    }

    public void setDrawerOpened(boolean drawerOpened) {
        setProperty("drawerOpened", drawerOpened);
    }

    public boolean isOverlay() {
        return getPropertyBoolean("overlay");
    }

    public DrawerToggleElement getDrawerToggle() {
        return $(DrawerToggleElement.class).first();
    }

}
