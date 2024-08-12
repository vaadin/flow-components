/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.formlayout.tests;

import java.util.stream.Stream;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.testutil.ClassesSerializableTest;

public class FormLayoutSerializableTest extends ClassesSerializableTest {

    private static final UI fakeUI = new UI();

    @Override
    protected void resetThreadLocals() {
        super.resetThreadLocals();
        com.vaadin.flow.component.UI.setCurrent(null);
    }

    @Override
    protected void setupThreadLocals() {
        super.setupThreadLocals();
        com.vaadin.flow.component.UI.setCurrent(fakeUI);
    }

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(Stream.of(
                "com\\.vaadin\\.flow\\.component\\.datepicker\\.osgi\\.DatePickerResource"),
                super.getExcludedPatterns());
    }
}
