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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.tests.EnableFeatureFlagRule;
import com.vaadin.tests.MockUIRule;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadHelperTest {
    @Rule
    public MockUIRule ui = new MockUIRule();
    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            ModularUploadFeatureFlagProvider.MODULAR_UPLOAD);

    private Div owner;

    @Before
    public void setup() {
        owner = new Div();
        ui.add(owner);
    }

    @Test
    public void hasUploadHandler_withDefaultConstructor_returnsFalse() {
        var manager = new UploadManager(owner);
        Assert.assertFalse(UploadHelper.hasUploadHandler(manager));
    }

    @Test
    public void hasUploadHandler_withHandlerInConstructor_returnsTrue() {
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        var manager = new UploadManager(owner, handler);
        Assert.assertTrue(UploadHelper.hasUploadHandler(manager));
    }

    @Test
    public void hasUploadHandler_withDefaultConstructor_setHandler_returnsTrue() {
        var manager = new UploadManager(owner);
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);
        Assert.assertTrue(UploadHelper.hasUploadHandler(manager));
    }

    @Test
    public void hasUploadHandler_upload_withDefaultConstructor_returnsFalse() {
        var upload = new Upload();
        Assert.assertFalse(UploadHelper.hasUploadHandler(upload));
    }

    @Test
    public void hasUploadHandler_upload_withHandlerInConstructor_returnsTrue() {
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        var upload = new Upload(handler);
        Assert.assertTrue(UploadHelper.hasUploadHandler(upload));
    }

    @Test
    public void hasUploadHandler_upload_withDefaultConstructor_setHandler_returnsTrue() {
        var upload = new Upload();
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        upload.setUploadHandler(handler);
        Assert.assertTrue(UploadHelper.hasUploadHandler(upload));
    }

    @Test
    public void isFileTypeAccepted_mimeOnly_matchingType_accepted() {
        Assert.assertTrue(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of("text/*"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_mimeOnly_nonMatchingType_rejected() {
        Assert.assertFalse(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of("image/*"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_mimeOnly_exactMatch_accepted() {
        Assert.assertTrue(UploadHelper.isFileTypeAccepted("file.pdf",
                "application/pdf", List.of("application/pdf"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_mimeOnly_caseInsensitive_accepted() {
        Assert.assertTrue(UploadHelper.isFileTypeAccepted("file.pdf",
                "Application/PDF", List.of("application/pdf"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_extensionOnly_matchingExt_accepted() {
        Assert.assertTrue(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of(), List.of(".txt")));
    }

    @Test
    public void isFileTypeAccepted_extensionOnly_nonMatchingExt_rejected() {
        Assert.assertFalse(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of(), List.of(".pdf")));
    }

    @Test
    public void isFileTypeAccepted_extensionOnly_caseInsensitive_accepted() {
        Assert.assertTrue(UploadHelper.isFileTypeAccepted("FILE.TXT",
                "text/plain", List.of(), List.of(".txt")));
    }

    @Test
    public void isFileTypeAccepted_bothConfigured_bothMatch_accepted() {
        Assert.assertTrue(
                UploadHelper.isFileTypeAccepted("file.pdf", "application/pdf",
                        List.of("application/pdf"), List.of(".pdf")));
    }

    @Test
    public void isFileTypeAccepted_bothConfigured_mimeMatchesExtDoesNot_rejected() {
        Assert.assertFalse(UploadHelper.isFileTypeAccepted("file.html",
                "text/html", List.of("text/*"), List.of(".pdf")));
    }

    @Test
    public void isFileTypeAccepted_bothConfigured_extMatchesMimeDoesNot_rejected() {
        Assert.assertFalse(UploadHelper.isFileTypeAccepted("file.pdf",
                "text/plain", List.of("image/*"), List.of(".pdf")));
    }

    @Test
    public void isFileTypeAccepted_neitherConfigured_accepted() {
        Assert.assertTrue(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of(), List.of()));
    }

    @Test
    public void isFileTypeAccepted_nullContentType_mimeConfigured_rejected() {
        Assert.assertFalse(UploadHelper.isFileTypeAccepted("file.txt", null,
                List.of("text/*"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_nullFileName_extensionConfigured_rejected() {
        Assert.assertFalse(UploadHelper.isFileTypeAccepted(null, "text/plain",
                List.of(), List.of(".txt")));
    }

    @Test
    public void isFileTypeAccepted_mimeWithParameters_exactMatch_accepted() {
        Assert.assertTrue(UploadHelper.isFileTypeAccepted("file.html",
                "text/html; charset=utf-8", List.of("text/html"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_mimeWithParameters_wildcardMatch_accepted() {
        Assert.assertTrue(UploadHelper.isFileTypeAccepted("file.html",
                "text/html; charset=utf-8", List.of("text/*"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_mimeWithParameters_nonMatch_rejected() {
        Assert.assertFalse(UploadHelper.isFileTypeAccepted("file.html",
                "text/html; charset=utf-8", List.of("image/*"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_multipleMimeTypes_oneMatches_accepted() {
        Assert.assertTrue(
                UploadHelper.isFileTypeAccepted("file.pdf", "application/pdf",
                        List.of("image/*", "application/pdf"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_multipleExtensions_oneMatches_accepted() {
        Assert.assertTrue(UploadHelper.isFileTypeAccepted("file.txt",
                "text/plain", List.of(), List.of(".pdf", ".txt")));
    }

    @Test
    public void wrapHandlerWithFileTypeValidation_wrapperOverridesAllInterfaceMethods() {
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
            Assert.assertTrue(
                    "Validation wrapper must delegate: " + sig
                            + ". See NOTE in wrapWithFileTypeValidation.",
                    wrapperMethods.contains(sig));
        }
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
