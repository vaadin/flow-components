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
package com.vaadin.flow.component.textfield.tests;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.Constants;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Tests that the module pins the npm versions of the web components it ships,
 * in a versions file of its own rather than in the versions file of the
 * platform.
 * <p>
 * Flow reads every json file of {@link Constants#PINNED_NPM_VERSIONS_FOLDER} on
 * the classpath and pins the packages they declare, so the versions the Java
 * code is written against travel in the same jar as that code.
 */
class PinnedNpmVersionsTest {

    /**
     * The components of the module, each declaring the npm package it needs.
     */
    private static final List<Class<? extends Component>> COMPONENTS = List.of(
            EmailField.class, IntegerField.class, NumberField.class,
            PasswordField.class, TextArea.class, TextField.class);

    @Test
    void everyNpmPackageOfTheModuleIsPinnedToTheAnnotatedVersion() {
        Map<String, JsonNode> pinnedPackages = readPinnedPackages();

        for (Class<? extends Component> component : COMPONENTS) {
            for (NpmPackage npmPackage : component
                    .getDeclaredAnnotationsByType(NpmPackage.class)) {
                JsonNode declaration = pinnedPackages.get(npmPackage.value());
                Assertions.assertNotNull(declaration,
                        () -> "The versions file of the module does not declare '"
                                + npmPackage.value() + "' of "
                                + component.getSimpleName()
                                + ", so the package is not pinned");
                Assertions.assertEquals(npmPackage.version(),
                        declaration.get("jsVersion").asString(),
                        "The version the versions file pins '"
                                + npmPackage.value()
                                + "' to differs from the version its @NpmPackage annotation asks for");
                Assertions.assertEquals("lit",
                        declaration.get("mode").asString(),
                        "The versions file must pin '" + npmPackage.value()
                                + "' for the Lit mode only, as a React application gets the package from @vaadin/react-components instead");
            }
        }
    }

    @Test
    void versionsFileDoesNotDeclareTheVaadinVersion() {
        for (JsonNode versionsFile : readVersionsFiles().values()) {
            Assertions.assertFalse(versionsFile.has("platform"),
                    "Only a versions file of the platform tells what the Vaadin version is, so the file of the module must not declare 'platform'");
        }
    }

    /**
     * Reads the packages the versions files of the module declare, by npm
     * package name.
     */
    private static Map<String, JsonNode> readPinnedPackages() {
        Map<String, JsonNode> packages = new LinkedHashMap<>();
        readVersionsFiles().values().forEach(
                versionsFile -> collectPackages(versionsFile, packages));
        return packages;
    }

    private static void collectPackages(JsonNode node,
            Map<String, JsonNode> packages) {
        for (String key : JacksonUtils.getKeys(node)) {
            JsonNode value = node.get(key);
            if (!(value instanceof ObjectNode)) {
                continue;
            }
            if (value.has("npmName")) {
                packages.put(value.get("npmName").asString(), value);
            } else {
                collectPackages(value, packages);
            }
        }
    }

    /**
     * Reads the versions files the module ships, by file name.
     */
    private static Map<String, JsonNode> readVersionsFiles() {
        URL folder = PinnedNpmVersionsTest.class.getClassLoader()
                .getResource(Constants.PINNED_NPM_VERSIONS_FOLDER);
        Assertions.assertNotNull(folder, "The module ships no versions file in "
                + Constants.PINNED_NPM_VERSIONS_FOLDER
                + ", so the npm versions of its web components are not pinned");
        File[] files;
        try {
            files = new File(folder.toURI())
                    .listFiles(file -> file.getName().endsWith(".json"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        Assertions.assertFalse(files == null || files.length == 0,
                "The versions folder " + Constants.PINNED_NPM_VERSIONS_FOLDER
                        + " of the module contains no json file");
        Map<String, JsonNode> versionsFiles = new LinkedHashMap<>();
        Arrays.stream(files).sorted().forEach(
                file -> versionsFiles.put(file.getName(), readJson(file)));
        return versionsFiles;
    }

    private static JsonNode readJson(File file) {
        try {
            return JacksonUtils.readTree(Files.readString(file.toPath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
