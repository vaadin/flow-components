package com.vaadin.flow.component.shared;

import java.io.Serializable;

public final class ValidationError implements Serializable {
    public static final String REQUIRED = "VALIDATION_REQUIRED_ERROR";

    public static final String GREATER_THAN_MAX = "VALIDATION_GREATER_THAN_MAX_ERROR";
    public static final String SMALLER_THAN_MIN = "VALIDATION_SMALLER_THAN_MIN_ERROR";

    public static final String MAX_LENGTH_EXCEEDED = "VALIDATION_MAX_LENGTH_EXCEEDED_ERROR";
    public static final String MIN_LENGTH_NOT_REACHED = "VALIDATION_MIN_LENGTH_NOT_REACHED_ERROR";

    public static final String PATTERN_VIOLATED = "VALIDATION_PATTERN_VIOLATED_ERROR";

    public static final String STEP = "VALIDATION_STEP_ERROR";
}
