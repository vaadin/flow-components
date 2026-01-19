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
package com.vaadin.flow.component.slider;

import java.io.Serializable;

/**
 * Represents the value of a {@link RangeSlider}, consisting of a start and end
 * value.
 *
 * @param start
 *            the start value of the range
 * @param end
 *            the end value of the range
 *
 * @author Vaadin Ltd
 */
public record RangeSliderValue(double start,
        double end) implements Serializable {
}
