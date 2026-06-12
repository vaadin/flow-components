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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.breadcrumbs.Breadcrumbs;
import com.vaadin.flow.component.breadcrumbs.Breadcrumbs.BreadcrumbsI18n;

import tools.jackson.databind.node.ObjectNode;

class BreadcrumbsI18nTest {

    private Breadcrumbs breadcrumbs;

    @BeforeEach
    void setup() {
        breadcrumbs = new Breadcrumbs();
    }

    @Test
    void setMoreItems_returnsValue() {
        Assertions.assertEquals("Show hidden items", new BreadcrumbsI18n()
                .setMoreItems("Show hidden items").getMoreItems());
    }

    @Test
    void setI18n() {
        BreadcrumbsI18n i18n = new BreadcrumbsI18n()
                .setMoreItems("Show hidden items");
        breadcrumbs.setI18n(i18n);

        Assertions.assertSame(i18n, breadcrumbs.getI18n());
        Assertions.assertEquals("Show hidden items",
                getI18nPropertyAsJson(breadcrumbs).get("moreItems").asString());
    }

    @Test
    void setI18nToNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> breadcrumbs.setI18n(null));
    }

    private ObjectNode getI18nPropertyAsJson(Breadcrumbs breadcrumbs) {
        return (ObjectNode) breadcrumbs.getElement().getPropertyRaw("i18n");
    }
}
