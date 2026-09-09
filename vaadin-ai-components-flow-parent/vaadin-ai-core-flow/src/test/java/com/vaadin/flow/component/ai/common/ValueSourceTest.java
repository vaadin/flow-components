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
package com.vaadin.flow.component.ai.common;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ValueSource}'s constructor normalization: {@code extracts}
 * is never {@code null} and never shared with the caller.
 */
class ValueSourceTest {

    @Test
    void nullExtractsAreNormalizedToEmptyList() {
        var source = new ValueSource(null, null);

        Assertions.assertEquals(List.of(), source.extracts(),
                "Null extracts must read as an empty list, not null");
    }

    @Test
    void extractsAreCopiedAndImmutable() {
        var input = new ArrayList<SourceExtract>();
        input.add(new SourceExtract("snippet", null));

        var source = new ValueSource(ConfidenceLevel.HIGH, input);
        input.add(new SourceExtract("added later", null));

        Assertions.assertEquals(1, source.extracts().size(),
                "Mutating the input list must not change the source");
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> source.extracts()
                        .add(new SourceExtract("injected", null)));
    }
}
