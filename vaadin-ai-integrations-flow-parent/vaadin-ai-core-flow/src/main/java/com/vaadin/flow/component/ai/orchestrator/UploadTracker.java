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

import java.io.IOException;
import java.io.Serializable;

import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.server.streams.InMemoryUploadCallback;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadEvent;

/**
 * Tracks the files that an upload handler is receiving, and notifies a listener
 * when a file starts or stops being received.
 * <p>
 * The listener is notified with the session locked while the upload request is
 * being handled, which is not a client-server round-trip. The changes that the
 * listener makes therefore reach the browser during the upload only with server
 * push or polling.
 */
class UploadTracker implements Serializable {

    /**
     * The number of files being received. Accessed only while the session is
     * locked. Not serialized, as a file is no longer being received after
     * serialization.
     */
    private transient int receiving;
    private SerializableRunnable listener;

    /**
     * Creates an upload handler that keeps the received files in memory and
     * whose uploads are tracked.
     * <p>
     * A file is tracked from the start to the end of its upload request, in the
     * session of the request. The progress notifications of the handler are not
     * used, as they run in the UI of the upload and are skipped once that UI is
     * detached. A view with {@code @PreserveOnRefresh} moves its components to
     * a new UI when the page is refreshed, so a file that was being received
     * during the refresh would never stop being tracked.
     *
     * @param callback
     *            the callback that gets the received files
     * @return the upload handler
     */
    InMemoryUploadHandler createHandler(InMemoryUploadCallback callback) {
        return new InMemoryUploadHandler(callback) {
            @Override
            public void handleUploadRequest(UploadEvent event)
                    throws IOException {
                var session = event.getSession();
                session.access(() -> receivingChanged(1));
                try {
                    super.handleUploadRequest(event);
                } finally {
                    session.access(() -> receivingChanged(-1));
                }
            }
        };
    }

    /**
     * Checks whether a file is being received.
     *
     * @return {@code true} if a file is being received
     */
    boolean isReceiving() {
        return receiving > 0;
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

    private void receivingChanged(int change) {
        receiving += change;
        if (listener != null) {
            listener.run();
        }
    }
}
