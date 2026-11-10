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

import com.vaadin.flow.component.ai.ui.AIFileReceiver;
import com.vaadin.flow.function.SerializableRunnable;

/**
 * A file receiver that notifies when a file starts uploading and when all
 * uploads have finished, so that the orchestrator can disable submitting on the
 * input meanwhile.
 */
interface UploadTrackingReceiver extends AIFileReceiver {

    /**
     * Checks whether the browser reports that files are uploading. Unlike
     * {@link #isUploading()}, which reports whether the server is receiving
     * files, this already includes the time before the upload request reaches
     * the server, and does not include uploads that the server is still
     * receiving after the browser canceled them, as nothing would notify when
     * the server finishes those.
     * <p>
     * Prompts are not ignored based on this state, as the browser reports that
     * all uploads have finished through the same event as the
     * {@code AllFinishedEvent}, and an application listener for that event may
     * run before this state is updated.
     *
     * @return {@code true} if the browser reports that files are uploading
     */
    boolean isClientUploading();

    /**
     * Sets the listener that is notified when {@link #isClientUploading()} may
     * have changed. The listener is notified while the session is locked,
     * during a client-server round-trip.
     *
     * @param listener
     *            the listener to notify
     */
    void setUploadStateListener(SerializableRunnable listener);
}
