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
package com.vaadin.flow.component.menubar.trigger;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.trigger.internal.SetPropertyAction;
import com.vaadin.flow.dom.JsFunction;
import com.vaadin.tests.MockUIExtension;

class MenuBarItemClickTriggerTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void triggers_installsClickListenerOnMenuBarItemElement() {
        MenuBar menuBar = new MenuBar();
        MenuItem item = menuBar.addItem("Copy");
        ui.add(menuBar);

        new MenuBarItemClickTrigger(item)
                .triggers(new SetPropertyAction<>(item, "data-fired", true));

        JsFunction install = singleInstallFnFor(item);
        Assertions.assertEquals("this.addEventListener(\"click\", $0);"
                + "return () => this.removeEventListener(\"click\", $0);",
                install.getBody());
    }

    @Test
    void triggers_onSubMenuItem_installsClickListenerOnSubItemElement() {
        MenuBar menuBar = new MenuBar();
        MenuItem parent = menuBar.addItem("File");
        MenuItem subItem = parent.getSubMenu().addItem("Save");
        ui.add(menuBar);

        new MenuBarItemClickTrigger(subItem)
                .triggers(new SetPropertyAction<>(subItem, "data-fired", true));

        JsFunction install = singleInstallFnFor(subItem);
        Assertions.assertEquals("this.addEventListener(\"click\", $0);"
                + "return () => this.removeEventListener(\"click\", $0);",
                install.getBody());
    }

    private JsFunction singleInstallFnFor(MenuItem item) {
        List<PendingJavaScriptInvocation> initializers = ui
                .dumpPendingJavaScriptInvocations().stream()
                .filter(p -> p.getOwner() == item.getElement().getNode())
                .filter(p -> p.getInvocation().getParameters().size() >= 3
                        && p.getInvocation().getParameters()
                                .get(2) instanceof JsFunction)
                .toList();
        Assertions.assertEquals(1, initializers.size(),
                "Expected exactly one trigger install on the menu-bar item");
        JavaScriptInvocation invocation = initializers.get(0).getInvocation();
        return (JsFunction) invocation.getParameters().get(2);
    }
}
