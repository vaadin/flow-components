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
package com.vaadin.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.vaadin.flow.component.UI;

/**
 * Base class for testing components with full-stack signals. Since signal
 * bindings are only active when components are attached, this class sets up a
 * mock UI instance for attaching components under test.
 */
public class AbstractSignalsUnitTest {

    @BeforeClass
    public static void setupUI() {
        var mockUI = new MockUI();
        UI.setCurrent(mockUI);
    }

    @AfterClass
    public static void teardownUI() {
        if (UI.getCurrent() != null) {
            UI.getCurrent().removeAll();
            UI.setCurrent(null);
        }
    }
}
