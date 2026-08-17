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
 * A rectangle expressed as fractions of the surface it sits on, measured from
 * the top-left corner. All values are in the {@code 0..1} range, so the
 * rectangle holds at any zoom level and rendering size: multiply by the
 * rendered width and height to get pixel coordinates.
 * <p>
 * The fractions describe the surface as the user sees it — for a PDF page
 * marked as rotated, they refer to the rotated page rather than the unrotated
 * one.
 *
 * @param x
 *            the left edge as a fraction of the surface width
 * @param y
 *            the top edge as a fraction of the surface height
 * @param width
 *            the width as a fraction of the surface width
 * @param height
 *            the height as a fraction of the surface height
 * @author Vaadin Ltd
 * @since 25.3
 */
public record Rect(double x, double y, double width,
        double height) implements Serializable {
}
