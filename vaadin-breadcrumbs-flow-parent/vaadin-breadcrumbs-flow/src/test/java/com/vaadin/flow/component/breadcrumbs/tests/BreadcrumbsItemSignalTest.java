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
package com.vaadin.flow.component.breadcrumbs.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.breadcrumbs.BreadcrumbsItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class BreadcrumbsItemSignalTest extends AbstractSignalsTest {

    @Test
    void bindText_updatesTextReactively() {
        var signal = new ValueSignal<>("foo");
        var item = new BreadcrumbsItem("init");
        item.bindText(signal);
        ui.add(item);

        Assertions.assertEquals("foo", item.getText());

        signal.set("bar");
        Assertions.assertEquals("bar", item.getText());
    }

    @Test
    void bindText_prefixPreserved() {
        var signal = new ValueSignal<>("foo");
        var item = new BreadcrumbsItem("init");
        var prefix = new Div();
        item.setPrefixComponent(prefix);
        item.bindText(signal);
        ui.add(item);

        signal.set("bar");
        Assertions.assertEquals("bar", item.getText());
        Assertions.assertEquals(prefix, item.getPrefixComponent());
    }

    @Test
    void bindText_setTextThrows() {
        var signal = new ValueSignal<>("foo");
        var item = new BreadcrumbsItem("init");
        item.bindText(signal);

        Assertions.assertThrows(BindingActiveException.class,
                () -> item.setText("bar"));
    }
}
