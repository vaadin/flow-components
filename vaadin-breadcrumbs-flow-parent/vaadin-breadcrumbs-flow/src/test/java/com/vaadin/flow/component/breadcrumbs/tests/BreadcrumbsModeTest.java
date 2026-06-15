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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.breadcrumbs.Breadcrumbs;
import com.vaadin.flow.component.breadcrumbs.Breadcrumbs.Mode;
import com.vaadin.flow.component.breadcrumbs.BreadcrumbsItem;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class BreadcrumbsModeTest extends AbstractSignalsTest {

    @Test
    void defaultConstructor_modeIsRouter() {
        Assertions.assertEquals(Mode.ROUTER, new Breadcrumbs().getMode());
    }

    @Test
    void modeConstructor_modeIsSet() {
        Assertions.assertEquals(Mode.MANUAL,
                new Breadcrumbs(Mode.MANUAL).getMode());
    }

    @Test
    void manualMode_mutatingMethods_doNotThrow() {
        var breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        var home = new BreadcrumbsItem("Home");
        var page = new BreadcrumbsItem("Page");

        Assertions.assertDoesNotThrow(() -> breadcrumbs.add(home));
        Assertions.assertDoesNotThrow(() -> breadcrumbs.remove(home));
        Assertions.assertDoesNotThrow(breadcrumbs::removeAll);
        Assertions.assertDoesNotThrow(() -> breadcrumbs.add(home));
        Assertions.assertDoesNotThrow(() -> breadcrumbs.replace(home, page));
        Assertions.assertDoesNotThrow(
                () -> breadcrumbs.addComponentAsFirst(home));
        Assertions.assertDoesNotThrow(
                () -> breadcrumbs.addComponentAtIndex(0, page));
    }

    @Test
    void routerMode_mutatingMethods_throw() {
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        var home = new BreadcrumbsItem("Home");
        var page = new BreadcrumbsItem("Page");

        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.add(home));
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.remove(home));
        Assertions.assertThrows(IllegalStateException.class,
                breadcrumbs::removeAll);
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.replace(home, page));
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.addComponentAsFirst(home));
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.addComponentAtIndex(0, page));
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.bindChildren(null, null));
    }

    @Test
    void getChildren_worksInBothModes() {
        var manual = new Breadcrumbs(Mode.MANUAL);
        manual.add(new BreadcrumbsItem("Home"));
        Assertions.assertEquals(1, manual.getChildren().count());

        var router = new Breadcrumbs(Mode.ROUTER);
        Assertions.assertEquals(0, router.getChildren().count());
    }

    @Test
    void manualModeWithChildren_setModeRouter_clearsChildrenAndReappliesGuard() {
        var breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        breadcrumbs.add(new BreadcrumbsItem("Home"),
                new BreadcrumbsItem("Page"));

        // MANUAL -> ROUTER clears manually-added children without throwing,
        // exercising the updateChildrenInternal(List.of()) bypass.
        breadcrumbs.setMode(Mode.ROUTER);
        Assertions.assertEquals(0, breadcrumbs.getChildren().count());

        // The bypass flag is reset afterwards, so a guarded add throws again.
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.add(new BreadcrumbsItem("Other")));
    }

    @Test
    void setModeSameValue_isNoOp_childrenUnchanged() {
        var breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        var item = new BreadcrumbsItem("Home");
        breadcrumbs.add(item);
        breadcrumbs.setMode(Mode.MANUAL);
        Assertions.assertEquals(1, breadcrumbs.getChildren().count());
        Assertions.assertEquals(item,
                breadcrumbs.getChildren().findFirst().orElse(null));
    }

    @Test
    void setMode_withActiveChildrenBinding_throws() {
        var breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        var itemSignal = new ValueSignal<>("Home");
        var listSignal = new ValueSignal<>(List.of(itemSignal));
        breadcrumbs.bindChildren(listSignal,
                signal -> new BreadcrumbsItem(signal.peek()));

        // A binding controls the children and cannot be removed; switching
        // modes would clear them, so setMode must fail.
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.setMode(Mode.ROUTER));
    }
}
