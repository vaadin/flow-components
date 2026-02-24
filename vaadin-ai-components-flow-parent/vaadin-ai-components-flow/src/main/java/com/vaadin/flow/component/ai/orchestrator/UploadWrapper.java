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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.ui.AIFileReceiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Wrapper for Flow Upload component to implement AIFileReceiver interface.
 * Caches uploaded files in memory until they are consumed via
 * {@link #takeAttachments()}.
 */
class UploadWrapper implements AIFileReceiver {

    private final Upload upload;
    private final List<AIAttachment> pendingAttachments = new CopyOnWriteArrayList<>();

    UploadWrapper(Upload upload) {
        this.upload = upload;
        upload.setUploadHandler(UploadHandler.inMemory((meta, data) -> {
            var isDuplicate = pendingAttachments.stream()
                    .anyMatch(a -> a.name().equals(meta.fileName()));
            if (isDuplicate) {
                throw new IllegalArgumentException(
                        "Duplicate file name: " + meta.fileName());
            }
            pendingAttachments.add(new AIAttachment(meta.fileName(),
                    meta.contentType(), data));
        }));
        upload.addFileRemovedListener(event -> pendingAttachments
                .removeIf(a -> a.name().equals(event.getFileName())));
    }

    @Override
    public List<AIAttachment> takeAttachments() {
        var result = List.copyOf(pendingAttachments);
        pendingAttachments.clear();
        upload.clearFileList();
        return result;
    }
}
