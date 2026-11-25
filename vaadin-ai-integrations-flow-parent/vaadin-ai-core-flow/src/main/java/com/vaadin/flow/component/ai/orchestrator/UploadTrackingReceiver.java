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
 * A file receiver that notifies when a file starts or stops being received, so
 * that the orchestrator can disable submitting on the input meanwhile.
 */
interface UploadTrackingReceiver extends AIFileReceiver {

    /**
     * Checks whether a file is being received. Unlike {@link #isUploading()},
     * this changes only right before the listener is notified. The upload
     * request is still being handled when the listener is notified that the
     * last file has been received, so {@link #isUploading()} would still return
     * {@code true} at that point.
     *
     * @return {@code true} if a file is being received
     */
    boolean isReceivingFiles();

    /**
     * Sets the listener that is notified when {@link #isReceivingFiles()}
     * changes. The listener is notified while the session is locked, during the
     * upload request rather than a client-server round-trip.
     *
     * @param listener
     *            the listener to notify
     */
    void setUploadStateListener(SerializableRunnable listener);
}
