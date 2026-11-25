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

import java.io.Serializable;
import java.util.Objects;

/**
 * One snippet the LLM says it read to produce a value. The text is what the
 * model reports, not a verified quote: the controller never sees the source
 * document, so the snippet records what the AI said its source was rather than
 * proof that the value is correct.
 * <p>
 * The location says where the snippet sits inside its source document, but not
 * which document. Sources are unambiguous only when a single document is
 * attached per prompt.
 *
 * @param text
 *            the snippet as the model reports it, not {@code null}
 * @param location
 *            where the snippet sits inside the source document, or {@code null}
 *            when the source has no position to point at (for example pasted
 *            text)
 * @author Vaadin Ltd
 * @since 25.3
 */
public record SourceExtract(String text,
        SourceLocation location) implements Serializable {

    /**
     * Creates a new source extract.
     *
     * @param text
     *            the snippet as the model reports it, not {@code null}
     * @param location
     *            where the snippet sits inside the source document, or
     *            {@code null} when the source has no position to point at
     * @throws NullPointerException
     *             if {@code text} is {@code null}
     */
    public SourceExtract {
        Objects.requireNonNull(text, "Text must not be null");
    }
}
