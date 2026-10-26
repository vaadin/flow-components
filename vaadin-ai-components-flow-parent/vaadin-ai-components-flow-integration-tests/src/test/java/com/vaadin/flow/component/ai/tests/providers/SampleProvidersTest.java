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
package com.vaadin.flow.component.ai.tests.providers;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.vaadin.flow.component.ai.provider.ToolException;

class SampleProvidersTest {

    static List<String> names() {
        return SampleProviders.names();
    }

    @ParameterizedTest
    @MethodSource("names")
    void everyProvider_hasALanguageAndASchema(String name) {
        var provider = SampleProviders.get(name);
        Assertions.assertSame(provider, SampleProviders.get(name),
                "providers are shared");
        Assertions.assertFalse(provider.getQueryLanguage().getName().isBlank(),
                name);
        Assertions.assertFalse(provider.getQueryLanguage().getRules().isBlank(),
                name);
        Assertions.assertFalse(provider.getSchema().isBlank(), name);
        Assertions.assertNotNull(SampleProviders.describe(name));
    }

    @Test
    void unknownName_isRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> SampleProviders.get("oracle"));
        Assertions.assertNull(SampleProviders.describe("oracle"));
    }

    @Test
    void tenantProviders_seeOnlyTheirRegion() {
        var north = SampleProviders.get("tenant-north");
        var rows = north.executeQuery("SELECT DISTINCT region FROM sales");
        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals("North",
                rows.getFirst().values().iterator().next());
        Assertions.assertEquals(6,
                north.executeQuery("SELECT id FROM sales").size());
        Assertions.assertTrue(north.getSchema().contains("North region only"));

        var e = Assertions.assertThrows(ToolException.class,
                () -> north.executeQuery("SELECT * FROM PUBLIC.sales"));
        Assertions.assertTrue(e.getMessage().contains("rights"),
                e.getMessage());
    }
}
