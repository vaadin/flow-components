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

import com.vaadin.flow.component.ai.ui.AIFileReceiver;
import com.vaadin.flow.component.ai.ui.AIInput;
import com.vaadin.flow.server.VaadinSession;

/**
 * Disables submitting on the input of an {@link AIOrchestrator} while a request
 * is being processed or a file is being uploaded, and enables it again when
 * neither is the case.
 * <p>
 * Submitting that is already disabled is left as it is, and submitting is only
 * enabled again if it was disabled here, so that a value set by the application
 * is kept.
 * <p>
 * Only uploads to an {@code Upload} or an {@code UploadManager} count, as
 * custom receivers do not notify when an upload finishes, and nothing would
 * enable submitting again.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
class SubmitDisabler implements Serializable {

    private final ProcessingFlag processing;
    private AIInput input;
    private UploadTrackingReceiver uploadReceiver;
    /**
     * Whether submitting is disabled by this class, rather than by the
     * application. Accessed only while the session is locked.
     */
    private boolean disabled;
    /**
     * The session that the latest prompt was made in. Submitting is updated in
     * the session rather than in the UI of the prompt, as a view with
     * {@code @PreserveOnRefresh} moves its components to a new UI of the same
     * session when the page is refreshed.
     */
    private transient volatile VaadinSession session;

    /**
     * Creates a disabler that does nothing until an input is set with
     * {@link #attach(AIInput, AIFileReceiver)}.
     *
     * @param processing
     *            the busy flag of the orchestrator
     */
    SubmitDisabler(ProcessingFlag processing) {
        this.processing = processing;
    }

    /**
     * Starts disabling submitting on the given input.
     *
     * @param input
     *            the input of the orchestrator
     * @param fileReceiver
     *            the file receiver of the orchestrator, or {@code null} if
     *            there is none
     */
    void attach(AIInput input, AIFileReceiver fileReceiver) {
        this.input = input;
        processing.setListener(this::processingChanged);
        if (fileReceiver instanceof UploadTrackingReceiver receiver) {
            uploadReceiver = receiver;
            receiver.setUploadStateListener(this::update);
        }
    }

    /**
     * Disables or enables submitting based on the current state. Reads the
     * current state, so that it is correct no matter in which order the updates
     * scheduled by the different threads run. Must be called while the session
     * is locked.
     */
    void update() {
        if (input == null) {
            return;
        }
        var uploading = uploadReceiver != null
                && uploadReceiver.isReceivingFiles();
        if (processing.get() || uploading) {
            if (!disabled && !input.isSubmitDisabled()) {
                input.setSubmitDisabled(true);
                disabled = true;
            }
        } else if (disabled) {
            input.setSubmitDisabled(false);
            disabled = false;
        }
    }

    /**
     * Updates submitting when the flag is released, on whichever thread
     * releases it. Nothing is updated when the flag is claimed: the
     * orchestrator calls {@link #update()} once the prompt is accepted, so that
     * submitting which the application disabled while the prompt was being
     * accepted is left to the application.
     */
    private void processingChanged(boolean claimed) {
        if (claimed) {
            var promptSession = VaadinSession.getCurrent();
            if (promptSession != null) {
                session = promptSession;
            }
            return;
        }
        var currentSession = session;
        if (currentSession != null) {
            currentSession.access(this::update);
        }
    }
}
