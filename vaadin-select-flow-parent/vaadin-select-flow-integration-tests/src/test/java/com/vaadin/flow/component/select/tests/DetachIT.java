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
package com.vaadin.flow.component.select.tests;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-select/reattach-test")
public class DetachIT extends AbstractComponentIT {

    /**
     * https://github.com/vaadin/vaadin-select-flow/issues/43
     *
     * Test that setting a renderer after detach won't cause an exception on the
     * client side.
     *
     */
    @Test
    public void testReattach_setRenderer() {
        open();
        getCommandExecutor().waitForVaadin();
        $("button").first().click();
        getCommandExecutor().waitForVaadin();
        $("button").first().click();
        getCommandExecutor().waitForVaadin();
        assertFalse($("div.v-system-error").exists());
    }

}
