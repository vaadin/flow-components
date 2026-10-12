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
package com.vaadin.flow.component.progressbar.tests;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.tests.AbstractSignalsTest;

class ProgressBarSignalTest extends AbstractSignalsTest {

    @TestFactory
    Stream<DynamicTest> bindMin() {
        return generateBindingTests(ProgressBar::new, ProgressBar::bindMin,
                ProgressBar::getMin, ProgressBar::setMin, 0.1, 0.2);
    }

    @TestFactory
    Stream<DynamicTest> bindMax() {
        return generateBindingTests(ProgressBar::new, ProgressBar::bindMax,
                ProgressBar::getMax, ProgressBar::setMax, 2.0, 3.0);
    }

    @TestFactory
    Stream<DynamicTest> bindValue() {
        return generateBindingTests(ProgressBar::new, ProgressBar::bindValue,
                ProgressBar::getValue, ProgressBar::setValue, 0.2, 0.5);
    }
}
