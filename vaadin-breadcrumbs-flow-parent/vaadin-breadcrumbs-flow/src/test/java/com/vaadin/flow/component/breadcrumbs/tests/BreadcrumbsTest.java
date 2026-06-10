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

import com.vaadin.flow.component.breadcrumbs.Breadcrumbs;
import com.vaadin.flow.component.breadcrumbs.Breadcrumbs.Mode;
import com.vaadin.flow.component.breadcrumbs.BreadcrumbsItem;
import com.vaadin.flow.component.shared.HasThemeVariant;

class BreadcrumbsTest {

    @Test
    void create_instanceWithCorrectTag() {
        var breadcrumbs = new Breadcrumbs();
        Assertions.assertNotNull(breadcrumbs);
        Assertions.assertEquals("vaadin-breadcrumbs",
                breadcrumbs.getElement().getTag());
    }

    @Test
    void createItem_instanceWithCorrectTagAndText() {
        var item = new BreadcrumbsItem("Home");
        Assertions.assertNotNull(item);
        Assertions.assertEquals("vaadin-breadcrumbs-item",
                item.getElement().getTag());
        Assertions.assertEquals("Home", item.getText());
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(Breadcrumbs.class));
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
    void routerMode_add_throws() {
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.add(new BreadcrumbsItem("Home")));
    }

    @Test
    void routerMode_remove_throws() {
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.remove(new BreadcrumbsItem("Home")));
    }

    @Test
    void routerMode_removeAll_throws() {
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        Assertions.assertThrows(IllegalStateException.class,
                breadcrumbs::removeAll);
    }

    @Test
    void routerMode_replace_throws() {
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.replace(new BreadcrumbsItem("Home"),
                        new BreadcrumbsItem("Page")));
    }

    @Test
    void routerMode_addComponentAsFirst_throws() {
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        Assertions.assertThrows(IllegalStateException.class, () -> breadcrumbs
                .addComponentAsFirst(new BreadcrumbsItem("Home")));
    }

    @Test
    void routerMode_addComponentAtIndex_throws() {
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        Assertions.assertThrows(IllegalStateException.class, () -> breadcrumbs
                .addComponentAtIndex(0, new BreadcrumbsItem("Home")));
    }

    @Test
    void routerMode_bindChildren_throws() {
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
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
    void routerMode_setModeManualThenAdd_clearsAndAllowsAdd() {
        var manual = new Breadcrumbs(Mode.MANUAL);
        manual.add(new BreadcrumbsItem("Home"), new BreadcrumbsItem("Page"));

        // MANUAL -> ROUTER clears manually-added children without throwing,
        // exercising the updateChildrenInternal(List.of()) bypass.
        Assertions.assertDoesNotThrow(() -> manual.setMode(Mode.ROUTER));
        Assertions.assertEquals(0, manual.getChildren().count());

        // The bypass flag is reset afterwards, so a guarded add still throws.
        Assertions.assertThrows(IllegalStateException.class,
                () -> manual.add(new BreadcrumbsItem("Other")));
    }
}
