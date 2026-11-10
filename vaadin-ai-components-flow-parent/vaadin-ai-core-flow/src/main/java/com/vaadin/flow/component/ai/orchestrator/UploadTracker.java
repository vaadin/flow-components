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
package com.vaadin.flow.component.ai.orchestrator;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.TransferContext;

/**
 * Tracks the files that an upload handler is receiving, based on the progress
 * notifications of the handler, and notifies a listener when a file starts or
 * stops being received.
 * <p>
 * The handler runs the notifications with the session locked while it handles
 * the upload request, which is not a client-server round-trip. The changes that
 * the listener makes therefore reach the browser during the upload only with
 * server push or polling.
 */
class UploadTracker implements Serializable {

    /**
     * The transfers that have started and not yet ended. A transfer is
     * identified by its context, which is equal in all the notifications of the
     * same transfer, so that a transfer that is reported to end twice ends only
     * once. That happens when closing the request fails after the transfer has
     * completed, which reports the transfer as failed as well. Accessed only
     * while the session is locked. Not serialized, as the contexts refer to the
     * requests, and a transfer does not continue after serialization.
     */
    private transient Set<TransferContext> transfers;
    private SerializableRunnable listener;

    /**
     * Tracks the transfers of the given handler.
     *
     * @param handler
     *            the handler to track
     */
    void track(InMemoryUploadHandler handler) {
        handler.whenStart(this::transferStarted)
                .whenComplete((context, success) -> transferEnded(context));
    }

    /**
     * Checks whether a file is being received.
     *
     * @return {@code true} if a file is being received
     */
    boolean isReceiving() {
        return transfers != null && !transfers.isEmpty();
    }

    /**
     * Sets the listener to notify when a file starts or stops being received.
     *
     * @param listener
     *            the listener to notify
     */
    void setListener(SerializableRunnable listener) {
        this.listener = listener;
    }

    private void transferStarted(TransferContext context) {
        if (transfers == null) {
            transfers = new HashSet<>();
        }
        transfers.add(context);
        notifyListener();
    }

    private void transferEnded(TransferContext context) {
        if (transfers != null && transfers.remove(context)) {
            notifyListener();
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.run();
        }
    }
}
