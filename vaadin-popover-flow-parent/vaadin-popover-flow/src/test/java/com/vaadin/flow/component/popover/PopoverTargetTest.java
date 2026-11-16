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
package com.vaadin.flow.component.popover;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.js.JsCall;
import com.vaadin.tests.MockUIExtension;

class PopoverTargetTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void setTarget_attachTarget_setsTargetOnClient() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);

        List<JsCall> calls = ui.dumpJsCalls(popover.getElement(),
                PopoverJs.class);
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals("setTarget", calls.get(0).methodName());
        Assertions.assertEquals(List.of(target.getElement()),
                calls.get(0).arguments());
    }

    @Test
    void setTarget_clearTarget_clearsTargetOnClient() {
        Popover popover = new Popover();
        ui.add(popover);
        Div target = new Div();
        ui.add(target);
        popover.setTarget(target);
        ui.dumpPendingJavaScriptInvocations();

        popover.setTarget(null);

        List<JsCall> calls = ui.dumpJsCalls(popover.getElement(),
                PopoverJs.class);
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals("setTarget", calls.get(0).methodName());
        Assertions.assertEquals(Arrays.asList((Object) null),
                calls.get(0).arguments());
    }
}
