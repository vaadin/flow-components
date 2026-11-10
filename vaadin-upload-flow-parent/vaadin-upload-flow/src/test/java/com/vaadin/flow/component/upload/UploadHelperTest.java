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
package com.vaadin.flow.component.upload;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class UploadHelperTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Div owner;

    @BeforeEach
    void setup() {
        owner = new Div();
        ui.add(owner);
    }

    @Test
    void hasUploadHandler_withDefaultConstructor_returnsFalse() {
        var manager = new UploadManager(owner);
        Assertions.assertFalse(UploadHelper.hasUploadHandler(manager));
    }

    @Test
    void hasUploadHandler_withHandlerInConstructor_returnsTrue() {
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        var manager = new UploadManager(owner, handler);
        Assertions.assertTrue(UploadHelper.hasUploadHandler(manager));
    }

    @Test
    void hasUploadHandler_withDefaultConstructor_setHandler_returnsTrue() {
        var manager = new UploadManager(owner);
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);
        Assertions.assertTrue(UploadHelper.hasUploadHandler(manager));
    }

    @Test
    void hasUploadHandler_upload_withDefaultConstructor_returnsFalse() {
        var upload = new Upload();
        Assertions.assertFalse(UploadHelper.hasUploadHandler(upload));
    }

    @Test
    void hasUploadHandler_upload_withHandlerInConstructor_returnsTrue() {
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        var upload = new Upload(handler);
        Assertions.assertTrue(UploadHelper.hasUploadHandler(upload));
    }

    @Test
    void hasUploadHandler_upload_withDefaultConstructor_setHandler_returnsTrue() {
        var upload = new Upload();
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        upload.setUploadHandler(handler);
        Assertions.assertTrue(UploadHelper.hasUploadHandler(upload));
    }

    @Test
    void addUploadingChangeListener_uploadManager_uploadStartedAndFinished() {
        var manager = new UploadManager(owner);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(manager, notifications::add);
        var connector = manager.getConnector().getElement();

        fireDomEvent(connector, "upload-start", false);
        Assertions.assertEquals(List.of(true), notifications);

        fireDomEvent(connector, "all-finished", false);
        Assertions.assertEquals(List.of(true, false), notifications);
    }

    @Test
    void addUploadingChangeListener_uploadManager_lastUploadingFileCanceled_notifiesNotUploading() {
        var manager = new UploadManager(owner);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(manager, notifications::add);
        var connector = manager.getConnector().getElement();
        fireDomEvent(connector, "upload-start", false);

        // The file is still uploading when the abort event is dispatched
        fireDomEvent(connector, "upload-abort", true);
        Assertions.assertEquals(List.of(true), notifications);

        fireDomEvent(connector, "file-remove", false);
        Assertions.assertEquals(List.of(true, false), notifications);
    }

    @Test
    void addUploadingChangeListener_uploadManager_fileRemovedWhileOthersUploading_notNotified() {
        var manager = new UploadManager(owner);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(manager, notifications::add);
        var connector = manager.getConnector().getElement();
        fireDomEvent(connector, "upload-start", false);

        fireDomEvent(connector, "file-remove", true);

        Assertions.assertEquals(List.of(true), notifications);
    }

    @Test
    void addUploadingChangeListener_uploadManager_ownerDetached_notifiesNotUploading() {
        var manager = new UploadManager(owner);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(manager, notifications::add);
        fireDomEvent(manager.getConnector().getElement(), "upload-start",
                false);

        ui.remove(owner);

        Assertions.assertEquals(List.of(true, false), notifications);
    }

    @Test
    void addUploadingChangeListener_uploadManager_ownerInert_uploadStarted() {
        var manager = new UploadManager(owner);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(manager, notifications::add);
        openModal();

        fireDomEvent(manager.getConnector().getElement(), "upload-start",
                false);

        Assertions.assertEquals(List.of(true), notifications);
    }

    @Test
    void addUploadingChangeListener_uploadManager_disabledWhileUploading_notifiesNotUploading() {
        var manager = new UploadManager(owner);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(manager, notifications::add);
        var connector = manager.getConnector().getElement();
        fireDomEvent(connector, "upload-start", false);

        manager.setEnabled(false);
        fireDomEvent(connector, "all-finished", false);

        Assertions.assertEquals(List.of(true, false), notifications);
    }

    @Test
    void addUploadingChangeListener_uploadManager_disabledWhileUploading_lastUploadingFileCanceled_notifiesNotUploading() {
        var manager = new UploadManager(owner);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(manager, notifications::add);
        var connector = manager.getConnector().getElement();
        fireDomEvent(connector, "upload-start", false);

        manager.setEnabled(false);
        fireDomEvent(connector, "file-remove", false);

        Assertions.assertEquals(List.of(true, false), notifications);
    }

    @Test
    void addUploadingChangeListener_uploadManager_removed_notNotified() {
        var manager = new UploadManager(owner);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(manager, notifications::add)
                .remove();
        var connector = manager.getConnector().getElement();

        fireDomEvent(connector, "upload-start", false);
        fireDomEvent(connector, "all-finished", false);
        ui.remove(owner);

        Assertions.assertTrue(notifications.isEmpty());
    }

    @Test
    void addUploadingChangeListener_upload_uploadStartedAndFinished() {
        var upload = new Upload();
        owner.add(upload);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(upload, notifications::add);

        fireDomEvent(upload.getElement(), "upload-start", true);
        Assertions.assertEquals(List.of(true), notifications);

        fireDomEvent(upload.getElement(), "upload-success", false);
        Assertions.assertEquals(List.of(true, false), notifications);
    }

    @Test
    void addUploadingChangeListener_upload_lastUploadingFileCanceled_notifiesNotUploading() {
        var upload = new Upload();
        owner.add(upload);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(upload, notifications::add);
        fireDomEvent(upload.getElement(), "upload-start", true);

        // The file is still uploading when the abort event is dispatched
        fireDomEvent(upload.getElement(), "upload-abort", true);
        Assertions.assertEquals(List.of(true), notifications);

        fireDomEvent(upload.getElement(), "file-remove", false);
        Assertions.assertEquals(List.of(true, false), notifications);
    }

    @Test
    void addUploadingChangeListener_upload_fileRemovedWhileOthersUploading_notNotified() {
        var upload = new Upload();
        owner.add(upload);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(upload, notifications::add);
        fireDomEvent(upload.getElement(), "upload-start", true);

        fireDomEvent(upload.getElement(), "file-remove", true);

        Assertions.assertEquals(List.of(true), notifications);
    }

    @Test
    void addUploadingChangeListener_upload_detached_notifiesNotUploading() {
        var upload = new Upload();
        owner.add(upload);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(upload, notifications::add);
        fireDomEvent(upload.getElement(), "upload-start", true);

        owner.remove(upload);

        Assertions.assertEquals(List.of(true, false), notifications);
    }

    @Test
    void addUploadingChangeListener_upload_inert_uploadStarted() {
        var upload = new Upload();
        owner.add(upload);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(upload, notifications::add);
        openModal();

        fireDomEvent(upload.getElement(), "upload-start", true);

        Assertions.assertEquals(List.of(true), notifications);
    }

    @Test
    void addUploadingChangeListener_upload_uploadFailed_notifiesNotUploading() {
        var upload = new Upload();
        owner.add(upload);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(upload, notifications::add);
        fireDomEvent(upload.getElement(), "upload-start", true);

        fireDomEvent(upload.getElement(), "upload-error", false);

        Assertions.assertEquals(List.of(true, false), notifications);
    }

    @Test
    void addUploadingChangeListener_upload_otherFileStillUploading_notNotified() {
        var upload = new Upload();
        owner.add(upload);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(upload, notifications::add);
        fireDomEvent(upload.getElement(), "upload-start", true);

        fireDomEvent(upload.getElement(), "upload-success", true);
        fireDomEvent(upload.getElement(), "upload-error", true);
        fireDomEvent(upload.getElement(), "upload-abort", true);

        Assertions.assertEquals(List.of(true), notifications);
    }

    @Test
    void addUploadingChangeListener_upload_disabledWhileUploading_notifiesNotUploading() {
        var upload = new Upload();
        owner.add(upload);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(upload, notifications::add);
        fireDomEvent(upload.getElement(), "upload-start", true);

        upload.setEnabled(false);
        fireDomEvent(upload.getElement(), "upload-success", false);

        Assertions.assertEquals(List.of(true, false), notifications);
    }

    @Test
    void addUploadingChangeListener_upload_disabledWhileUploading_lastUploadingFileCanceled_notifiesNotUploading() {
        var upload = new Upload();
        owner.add(upload);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(upload, notifications::add);
        fireDomEvent(upload.getElement(), "upload-start", true);

        upload.setEnabled(false);
        fireDomEvent(upload.getElement(), "upload-abort", true);
        fireDomEvent(upload.getElement(), "file-remove", false);

        Assertions.assertEquals(List.of(true, false), notifications);
    }

    @Test
    void addUploadingChangeListener_upload_removed_notNotified() {
        var upload = new Upload();
        owner.add(upload);
        var notifications = new ArrayList<Boolean>();
        UploadHelper.addUploadingChangeListener(upload, notifications::add)
                .remove();

        fireDomEvent(upload.getElement(), "upload-start", true);
        fireDomEvent(upload.getElement(), "upload-success", false);
        owner.remove(upload);

        Assertions.assertTrue(notifications.isEmpty());
    }

    @Test
    void isFileTypeAccepted_mimeOnly_matchingType_accepted() {
        Assertions.assertTrue(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of("text/*"), List.of()));
    }

    @Test
    void isFileTypeAccepted_mimeOnly_nonMatchingType_rejected() {
        Assertions.assertFalse(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of("image/*"), List.of()));
    }

    @Test
    void isFileTypeAccepted_mimeOnly_exactMatch_accepted() {
        Assertions.assertTrue(UploadHelper.isFileTypeAccepted("file.pdf",
                "application/pdf", List.of("application/pdf"), List.of()));
    }

    @Test
    void isFileTypeAccepted_mimeOnly_caseInsensitive_accepted() {
        Assertions.assertTrue(UploadHelper.isFileTypeAccepted("file.pdf",
                "Application/PDF", List.of("application/pdf"), List.of()));
    }

    @Test
    void isFileTypeAccepted_extensionOnly_matchingExt_accepted() {
        Assertions.assertTrue(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of(), List.of(".txt")));
    }

    @Test
    void isFileTypeAccepted_extensionOnly_nonMatchingExt_rejected() {
        Assertions.assertFalse(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of(), List.of(".pdf")));
    }

    @Test
    void isFileTypeAccepted_extensionOnly_caseInsensitive_accepted() {
        Assertions.assertTrue(UploadHelper.isFileTypeAccepted("FILE.TXT",
                "text/plain", List.of(), List.of(".txt")));
    }

    @Test
    void isFileTypeAccepted_bothConfigured_bothMatch_accepted() {
        Assertions.assertTrue(
                UploadHelper.isFileTypeAccepted("file.pdf", "application/pdf",
                        List.of("application/pdf"), List.of(".pdf")));
    }

    @Test
    void isFileTypeAccepted_bothConfigured_mimeMatchesExtDoesNot_rejected() {
        Assertions.assertFalse(UploadHelper.isFileTypeAccepted("file.html",
                "text/html", List.of("text/*"), List.of(".pdf")));
    }

    @Test
    void isFileTypeAccepted_bothConfigured_extMatchesMimeDoesNot_rejected() {
        Assertions.assertFalse(UploadHelper.isFileTypeAccepted("file.pdf",
                "text/plain", List.of("image/*"), List.of(".pdf")));
    }

    @Test
    void isFileTypeAccepted_neitherConfigured_accepted() {
        Assertions.assertTrue(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of(), List.of()));
    }

    @Test
    void isFileTypeAccepted_nullContentType_mimeConfigured_rejected() {
        Assertions.assertFalse(UploadHelper.isFileTypeAccepted("file.txt", null,
                List.of("text/*"), List.of()));
    }

    @Test
    void isFileTypeAccepted_nullFileName_extensionConfigured_rejected() {
        Assertions.assertFalse(UploadHelper.isFileTypeAccepted(null,
                "text/plain", List.of(), List.of(".txt")));
    }

    @Test
    void isFileTypeAccepted_mimeWithParameters_exactMatch_accepted() {
        Assertions.assertTrue(UploadHelper.isFileTypeAccepted("file.html",
                "text/html; charset=utf-8", List.of("text/html"), List.of()));
    }

    @Test
    void isFileTypeAccepted_mimeWithParameters_wildcardMatch_accepted() {
        Assertions.assertTrue(UploadHelper.isFileTypeAccepted("file.html",
                "text/html; charset=utf-8", List.of("text/*"), List.of()));
    }

    @Test
    void isFileTypeAccepted_mimeWithParameters_nonMatch_rejected() {
        Assertions.assertFalse(UploadHelper.isFileTypeAccepted("file.html",
                "text/html; charset=utf-8", List.of("image/*"), List.of()));
    }

    @Test
    void isFileTypeAccepted_multipleMimeTypes_oneMatches_accepted() {
        Assertions.assertTrue(
                UploadHelper.isFileTypeAccepted("file.pdf", "application/pdf",
                        List.of("image/*", "application/pdf"), List.of()));
    }

    @Test
    void isFileTypeAccepted_multipleExtensions_oneMatches_accepted() {
        Assertions.assertTrue(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of(), List.of(".pdf", ".txt")));
    }

    @Test
    void wrapHandlerWithFileTypeValidation_wrapperOverridesAllInterfaceMethods() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        UploadHandler wrapper = UploadHelper
                .wrapHandlerWithFileTypeValidation(handler, List::of, List::of);

        // Collect all non-static methods from UploadHandler and its
        // super-interfaces. These are the methods the wrapper must override
        // to ensure proper delegation.
        // handleRequest is excluded because UploadHandler provides a default
        // that routes to handleUploadRequest, which the wrapper overrides.
        Set<String> interfaceMethods = new LinkedHashSet<>();
        collectInterfaceMethods(UploadHandler.class, interfaceMethods);
        interfaceMethods.removeIf(sig -> sig.startsWith("handleRequest("));

        Set<String> wrapperMethods = new LinkedHashSet<>();
        for (Method m : wrapper.getClass().getDeclaredMethods()) {
            if (!m.isSynthetic()) {
                wrapperMethods.add(methodSignature(m));
            }
        }

        for (String sig : interfaceMethods) {
            Assertions.assertTrue(wrapperMethods.contains(sig),
                    "Validation wrapper must delegate: " + sig
                            + ". See NOTE in wrapWithFileTypeValidation.");
        }
    }

    private void openModal() {
        var modal = new Div();
        ui.add(modal);
        ui.getUI().setChildComponentModal(modal, true);
        ui.fakeClientCommunication();
    }

    private static void fireDomEvent(Element element, String eventType,
            boolean uploading) {
        // Only the expression of the given element is sent, as for the
        // event data of a browser event
        var filesUploading = "vaadin-upload".equals(element.getTag())
                ? "element.files.some(file => file.uploading)"
                : "element.manager.files.some(file => file.uploading)";
        var eventData = JacksonUtils.createObjectNode()
                .put(filesUploading, uploading)
                .put("event.detail.file.name", "file.txt")
                .put("event.detail.fileName", "file.txt");
        element.getNode().getFeature(ElementListenerMap.class)
                .fireEvent(new DomEvent(element, eventType, eventData));
    }

    /**
     * Collects all non-static method signatures from the given interface and
     * all its super-interfaces (at any depth).
     */
    private static void collectInterfaceMethods(Class<?> iface,
            Set<String> signatures) {
        for (Method m : iface.getMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) {
                signatures.add(methodSignature(m));
            }
        }
    }

    private static String methodSignature(Method m) {
        StringBuilder sb = new StringBuilder(m.getName()).append('(');
        Class<?>[] params = m.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(params[i].getName());
        }
        return sb.append(')').toString();
    }
}
