/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai;

import com.vaadin.flow.component.ai.orchestrator.ResponseListener;

/**
 * Turn-outcome events for driving {@code AIController#onResponse} directly from
 * tests, standing in for the event the orchestrator would build.
 */
public final class AITurnEvents {

    private AITurnEvents() {
    }

    /**
     * A turn that ended successfully, with no provider metadata reported.
     *
     * @return the event, never {@code null}
     */
    public static ResponseListener.ResponseEvent success() {
        return new ResponseListener.ResponseEvent("", null, null);
    }

    /**
     * A turn that ended with the given failure.
     *
     * @param error
     *            the cause of failure, not {@code null}
     * @return the event, never {@code null}
     */
    public static ResponseListener.ResponseEvent failure(Throwable error) {
        return new ResponseListener.ResponseEvent("", error, null);
    }
}
