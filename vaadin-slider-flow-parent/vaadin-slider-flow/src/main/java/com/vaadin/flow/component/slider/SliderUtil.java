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

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility methods for slider components.
 */
class SliderUtil {

    private SliderUtil() {
        // Utility class
    }

    /**
     * Adjusts the given value to be within the given min/max range.
     *
     * @param value
     *            the value to adjust
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @return the adjusted value
     */
    static double clampToMinMax(double value, double min, double max) {
        return Math.clamp(value, min, max);
    }

    /**
     * Adjusts the given value to align with the step.
     *
     * @param value
     *            the value to adjust
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param step
     *            the step value
     * @return the adjusted value
     */
    static double snapToStep(double value, double min, double max,
            double step) {
        BigDecimal minBd = BigDecimal.valueOf(min);
        BigDecimal maxBd = BigDecimal.valueOf(max);
        BigDecimal stepBd = BigDecimal.valueOf(step);
        BigDecimal valueBd = BigDecimal.valueOf(value);

        // Equivalent to Math.round((value - min) / step)
        BigDecimal stepsFromMin = valueBd.subtract(minBd).divide(stepBd, 0,
                RoundingMode.HALF_UP);

        // Equivalent to Math.min(min + stepsFromMin * step, max)
        return minBd.add(stepsFromMin.multiply(stepBd)).min(maxBd)
                .doubleValue();
    }
}
