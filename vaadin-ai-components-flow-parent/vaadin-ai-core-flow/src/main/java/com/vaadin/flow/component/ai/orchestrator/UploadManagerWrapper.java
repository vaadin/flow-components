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
import com.vaadin.flow.component.upload.UploadHelper;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Wrapper for Flow UploadManager component to implement AIFileReceiver
 * interface. Caches uploaded files in memory until they are consumed via
 * {@link #takeAttachments()}.
 */
class UploadManagerWrapper implements UploadTrackingReceiver {

    final UploadManager uploadManager;
    private final List<AIAttachment> pendingAttachments = new CopyOnWriteArrayList<>();
    /**
     * Whether the browser reports that files are uploading. Unlike the
     * server-side state, this is set already before the upload request reaches
     * the server.
     */
    private boolean clientUploading;
    private SerializableRunnable uploadStateListener;

    UploadManagerWrapper(UploadManager uploadManager) {
        this.uploadManager = uploadManager;
        uploadManager.setUploadHandler(UploadHandler.inMemory((meta, data) -> {
            var isDuplicate = pendingAttachments.stream()
                    .anyMatch(a -> a.name().equals(meta.fileName()));
            if (isDuplicate) {
                throw new IllegalArgumentException(
                        "Duplicate file name: " + meta.fileName());
            }
            pendingAttachments.add(new AIAttachment(meta.fileName(),
                    meta.contentType(), data));
        }));
        uploadManager.addFileRemovedListener(event -> pendingAttachments
                .removeIf(a -> a.name().equals(event.getFileName())));
        UploadHelper.addUploadingChangeListener(uploadManager,
                this::setClientUploading);
    }

    private void setClientUploading(boolean clientUploading) {
        this.clientUploading = clientUploading;
        if (uploadStateListener != null) {
            uploadStateListener.run();
        }
    }

    @Override
    public void setUploadStateListener(SerializableRunnable listener) {
        uploadStateListener = listener;
    }

    @Override
    public boolean isClientUploading() {
        return clientUploading;
    }

    @Override
    public boolean isUploading() {
        return uploadManager.isUploading();
    }

    @Override
    public List<AIAttachment> takeAttachments() {
        var result = List.copyOf(pendingAttachments);
        pendingAttachments.clear();
        uploadManager.clearFileList();
        return result;
    }
}
