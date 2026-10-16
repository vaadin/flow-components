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
package com.vaadin.flow.component.shared;

import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.js.JsCall;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.tests.MockUIExtension;

class TooltipConfigurationTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @BeforeEach
    void setup() {
        // TooltipConfiguration registers a UI init listener on the current
        // service
        VaadinService.setCurrent(ui.getService());
        ui.fakeClientCommunication();
    }

    @AfterEach
    void tearDown() {
        VaadinService.setCurrent(null);
    }

    @Test
    void setDefaultHideDelay_setsHideDelayOnClient() {
        assertConfigurationCall(TooltipConfiguration::setDefaultHideDelay,
                "setDefaultHideDelay");
    }

    @Test
    void setDefaultFocusDelay_setsFocusDelayOnClient() {
        assertConfigurationCall(TooltipConfiguration::setDefaultFocusDelay,
                "setDefaultFocusDelay");
    }

    @Test
    void setDefaultHoverDelay_setsHoverDelayOnClient() {
        assertConfigurationCall(TooltipConfiguration::setDefaultHoverDelay,
                "setDefaultHoverDelay");
    }

    // The defaults are static, so delays set by other tests are applied as
    // well. Only check that the configuration is initialized first and that
    // the given delay is set.
    private void assertConfigurationCall(IntConsumer setter,
            String methodName) {
        setter.accept(1234);

        List<JsCall> calls = dumpTooltipCalls();
        Assertions.assertEquals("initConfiguration", calls.get(0).methodName());
        Assertions.assertEquals(List.of(), calls.get(0).arguments());
        List<JsCall> delayCalls = calls.stream()
                .filter(call -> call.methodName().equals(methodName)).toList();
        Assertions.assertEquals(1, delayCalls.size());
        Assertions.assertEquals(List.of(1234), delayCalls.get(0).arguments());
    }

    private List<JsCall> dumpTooltipCalls() {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .filter(pending -> pending.getOwner() == ui.getUI().getElement()
                        .getNode())
                .map(PendingJavaScriptInvocation::getInvocation)
                .map(JavaScriptInvocation::getJsCall).filter(Objects::nonNull)
                .filter(call -> call.definitionType() == TooltipJs.class)
                .toList();
    }
}
