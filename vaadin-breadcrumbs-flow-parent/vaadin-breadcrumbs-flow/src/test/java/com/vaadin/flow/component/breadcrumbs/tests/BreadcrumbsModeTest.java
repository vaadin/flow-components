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

class BreadcrumbsModeTest {

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
    void manualModeWithChildren_setModeRouter_clearsChildren() {
        var breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        breadcrumbs.add(new BreadcrumbsItem("Home"),
                new BreadcrumbsItem("Page"));
        breadcrumbs.setMode(Mode.ROUTER);
        Assertions.assertEquals(0, breadcrumbs.getChildren().count());
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
}
