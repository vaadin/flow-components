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

/**
 * A rectangular area on a page of the source document. The meaning of the
 * rectangle is fixed — always fractions of the page as the user sees it — but
 * its accuracy is the model's: the rectangle is never checked against the
 * document, so it points a reviewer at an area rather than measuring one.
 *
 * @param page
 *            the 1-based page number; {@code 1} when the source has a single
 *            surface, such as an image
 * @param rect
 *            the area on the page, not {@code null}
 * @author Vaadin Ltd
 * @since 25.3
 */
public record PageRegion(int page, Rect rect) implements SourceLocation {
}
