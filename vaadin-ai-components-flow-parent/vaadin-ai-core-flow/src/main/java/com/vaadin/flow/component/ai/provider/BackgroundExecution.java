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
package com.vaadin.flow.component.ai.provider;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.communication.PushMode;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * Background execution state and scheduling shared by the built-in
 * {@link LLMProvider} implementations, together with the check that warns when
 * a response produced off the request thread cannot reach the browser.
 * <p>
 * Intended only for internal use and can be removed in the future.
 */
final class BackgroundExecution {

    private final Logger logger;
    private final AtomicBoolean deliveryWarned = new AtomicBoolean(false);
    private boolean enabled;

    /**
     * Creates the support object for a provider.
     *
     * @param ownerType
     *            the provider class the warnings are logged under
     */
    BackgroundExecution(Class<?> ownerType) {
        this.logger = LoggerFactory.getLogger(ownerType);
    }

    boolean isEnabled() {
        return enabled;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Prepares a response whose tokens already arrive on the LLM client's own
     * threads. Nothing is rescheduled — the subscription is cheap and the work
     * never occupied the caller's thread to begin with — but the response still
     * reaches the browser asynchronously, so the delivery check applies.
     *
     * @param response
     *            the provider's response stream
     * @return the response stream, unchanged
     */
    Flux<String> applyToStreamingResponse(Flux<String> response) {
        warnIfDeliveryBlocked();
        return response;
    }

    /**
     * Prepares a response that performs its blocking LLM call at subscription
     * time. When background execution is enabled the subscription is moved off
     * the subscribing thread, so the caller — the UI thread, for a prompt
     * triggered from the browser — is released as soon as the turn starts.
     * Otherwise the response is returned unchanged and the call runs on
     * whichever thread subscribes.
     *
     * @param response
     *            the provider's response stream
     * @return the response stream, scheduled on a background thread when
     *         background execution is enabled
     */
    Flux<String> applyToBlockingResponse(Flux<String> response) {
        if (!enabled) {
            return response;
        }
        warnIfDeliveryBlocked();
        // boundedElastic is Reactor's blocking-tolerant pool, and it is backed
        // by virtual threads when the application sets
        // reactor.schedulers.defaultBoundedElasticOnVirtualThreads.
        return response.subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Warns once per provider when a response produced off the request thread
     * has no way to reach the browser. Automatic push and polling both deliver
     * it; manual push does not, because neither the provider nor the
     * orchestrator ever calls {@code ui.push()}.
     * <p>
     * Called while assembling the response, which happens on the thread that
     * asked for it — the only point where {@link UI#getCurrent()} is still
     * bound once the subscription moves to a background thread.
     */
    private void warnIfDeliveryBlocked() {
        var ui = UI.getCurrent();
        if (ui == null
                || PushMode.AUTOMATIC
                        .equals(ui.getPushConfiguration().getPushMode())
                || ui.getPollInterval() > 0) {
            return;
        }
        if (deliveryWarned.compareAndSet(false, true)) {
            logger.warn("The LLM response is produced on a background thread, "
                    + "but neither automatic push nor polling is active, so it "
                    + "will not appear in the browser until the next request "
                    + "(with manual push mode, until the application calls "
                    + "ui.push()). Annotate the application shell or UI class "
                    + "with @Push, or enable polling with "
                    + "UI.setPollInterval().");
        }
    }
}
