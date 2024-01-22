/*
 * Copyright 2000-2022 Vaadin Ltd.
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
 *
 */

package com.vaadin.flow.component.timepicker;

import java.time.Duration;

/**
 * Utility class around the time picker steps functionality. The logic in here
 * is extracted for reuse by the DateTimePicker component.
 *
 * <p>
 * NOTE: This class is not part of the public API surface and for internal use
 * only
 */
public class StepsUtil {
    private static final long MILLISECONDS_IN_A_DAY = 86400000L;
    private static final long MILLISECONDS_IN_AN_HOUR = 3600000L;

    /**
     * Default step value of the web component, as Duration
     */
    public static final Duration DEFAULT_WEB_COMPONENT_STEP = Duration
            .ofHours(1);

    /**
     * Converts a Duration object into a decimal value that is used internally
     * by the time picker web component.
     *
     * @param duration
     * @return
     */
    public static double convertDurationToStepsValue(Duration duration) {
        long stepAsMilliseconds = duration.getSeconds() * 1000
                + (long) (duration.getNano() / 1E6);
        if (duration.isNegative() || stepAsMilliseconds == 0) {
            throw new IllegalArgumentException(
                    "Step cannot be negative and must be larger than 0 milliseconds");
        }

        if (MILLISECONDS_IN_A_DAY % stepAsMilliseconds != 0
                && MILLISECONDS_IN_AN_HOUR % stepAsMilliseconds != 0) {
            throw new IllegalArgumentException("Given step " + duration
                    + " does not divide evenly a day or an hour.");
        }

        return duration.getSeconds() + (duration.getNano() / 1E9);
    }

    /**
     * Converts the decimal steps value use internally by the time picker web
     * component into a Duration instance
     *
     * @param stepsValue
     * @return
     */
    public static Duration convertStepsValueToDuration(double stepsValue) {
        return Duration.ofNanos((long) (stepsValue * 1E9));
    }
}
