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

    private static UI mockUI;
    private static long setupThreadId;

    @BeforeClass
    public static void setupUI() {
        setupThreadId = Thread.currentThread().getId();
        System.out.println("[AbstractSignalsUnitTest] @BeforeClass - Thread: "
                + setupThreadId + " - UI before: " + UI.getCurrent());
        mockUI = new MockUI();
        UI.setCurrent(mockUI);
        System.out.println("[AbstractSignalsUnitTest] @BeforeClass - UI after: "
                + System.identityHashCode(mockUI) + " - mockUI field: "
                + System.identityHashCode(mockUI));
    }

    @AfterClass
    public static void teardownUI() {
        System.out.println("[AbstractSignalsUnitTest] @AfterClass - Thread: "
                + Thread.currentThread().getId() + " - UI: " + UI.getCurrent());
        if (UI.getCurrent() != null) {
            UI.getCurrent().removeAll();
            UI.setCurrent(null);
        }
        mockUI = null;
    }

    /**
     * Call this at the start of test methods to diagnose thread/UI issues. If
     * the thread ID differs from setupThreadId, parallel execution is
     * happening.
     */
    protected static void checkUISetup(String testName) {
        long currentThread = Thread.currentThread().getId();
        UI currentUI = UI.getCurrent();
        System.out.println("[AbstractSignalsUnitTest] " + testName
                + " - Thread: " + currentThread + " (setup was: "
                + setupThreadId + ") - UI.getCurrent(): "
                + (currentUI == null ? "NULL!"
                        : System.identityHashCode(currentUI))
                + " - mockUI field: "
                + (mockUI == null ? "NULL!" : System.identityHashCode(mockUI)));

        if (currentThread != setupThreadId) {
            System.err
                    .println("[AbstractSignalsUnitTest] WARNING: Test running "
                            + "on different thread than @BeforeClass! ThreadLocal UI "
                            + "won't be visible. This indicates parallel test execution.");
        }
        if (currentUI == null && mockUI != null) {
            System.err.println(
                    "[AbstractSignalsUnitTest] WARNING: UI.getCurrent() "
                            + "is null but mockUI field is not null. The UI may have been "
                            + "garbage collected (WeakReference) or cleared by another test.");
        }
    }
}
