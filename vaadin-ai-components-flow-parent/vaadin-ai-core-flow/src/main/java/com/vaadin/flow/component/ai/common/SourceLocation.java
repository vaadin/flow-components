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

/**
 * Where a {@link SourceExtract} sits inside its source document. The only
 * implementation today is {@link PageRegion}; more location kinds (for example
 * a time range for audio, or a character range for plain text) can be added
 * later, so code reading a location should check the concrete type and skip
 * kinds it does not know.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public interface SourceLocation extends Serializable {
}
