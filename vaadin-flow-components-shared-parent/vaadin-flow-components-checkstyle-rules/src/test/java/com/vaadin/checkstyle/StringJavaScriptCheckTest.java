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
package com.vaadin.checkstyle;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;

class StringJavaScriptCheckTest {

    private static final String MAIN = "module/src/main/java/Test.java";

    @TempDir
    Path tempDir;

    @Test
    void offMode_nothingFlagged() throws Exception {
        List<Integer> lines = check("off", MAIN, """
                class Test {
                    void run() {
                        getElement().executeJs("this.foo = 1");
                    }
                }
                """);

        Assertions.assertEquals(List.of(), lines);
    }

    @Test
    void executeJs_stringFlagged_classLiteralNot() throws Exception {
        List<Integer> lines = check("strict", MAIN, """
                class Test {
                    void run(String script) {
                        getElement().executeJs("this.foo = $0", 1);
                        ui.getPage().executeJs(script);
                        executeJs(\"""
                                this.foo = 1;
                                \""");
                        getElement().executeJs(TestJs.class).setFoo(1);
                        ui.getPage().executeJs(TestJs.class).init(getElement());
                    }
                }
                """);

        Assertions.assertEquals(List.of(3, 4, 5), lines);
    }

    @Test
    void jsFunctionAndDynamicImport_flagged() throws Exception {
        List<Integer> lines = check("strict", MAIN, """
                class Test {
                    void run() {
                        JsFunction.of("return 1");
                        ui.getPage().addDynamicImport("return import('x')");
                        List.of("a");
                    }
                }
                """);

        Assertions.assertEquals(List.of(3, 4), lines);
    }

    @Test
    void litRendererOf_stringFlagged_declaredTemplateNot() throws Exception {
        List<Integer> lines = check("strict", MAIN, """
                class Test {
                    void run() {
                        LitRenderer.of("<b>${item.name}</b>");
                        LitRenderer.<Person> of("<b>" + "x" + "</b>");
                        LitRenderer.of(JsSnippets.of(Templates.class).name());
                        LitRenderer.<Person> of(TEMPLATES.name());
                    }
                }
                """);

        Assertions.assertEquals(List.of(3, 4), lines);
    }

    @Test
    void addEventData_allButDeclaredEventDataFlagged() throws Exception {
        List<Integer> lines = check("strict", MAIN,
                """
                        class Test {
                            private static final String DETAIL = "event.detail";

                            void run() {
                                element.addEventListener("x", e -> {}).addEventData("event.detail");
                                registration.addEventData(DETAIL);
                                registration.addEventData("event." + "detail");
                                registration.addEventData(Constants.DETAIL);
                                registration.addEventData(getPath());
                                var path = "event.detail";
                                registration.addEventData(path);
                                registration.addEventData(OTHER.detail());
                                registration.addEventDataElement("event.target");
                                registration.addEventDataElement(TARGET);
                            }
                        }
                        """);

        Assertions.assertEquals(List.of(5, 6, 7, 8, 9, 11, 12, 13, 14), lines);
    }

    @Test
    void addEventData_declaredEventDataBeanAndPathsNotFlagged()
            throws Exception {
        List<Integer> lines = check("strict", MAIN,
                """
                        class Test {
                            private static final UploadJs SNIPPETS = JsSnippets.of(UploadJs.class);
                            private static final DomEventData<Boolean> UPLOADING = SNIPPETS.filesUploading();
                            private final Snippets snippets = JsSnippets.of(Snippets.class);

                            void run(DomEventData<Element> target) {
                                registration.addEventData(UPLOADING);
                                registration.addEventData(target);
                                registration.addEventData(SNIPPETS.filesUploading());
                                registration.addEventData(this.snippets.detail());
                                registration.addEventData(JsSnippets.of(UploadJs.class).filesUploading());
                                registration.addEventData(Details.class);
                                registration.addEventDataPath("event.detail");
                                registration.addEventDataElementPath("event.target");
                            }
                        }
                        """);

        Assertions.assertEquals(List.of(), lines);
    }

    @Test
    void addEventData_messageNamesReplacements() throws Exception {
        List<String> messages = checkMessages("strict", MAIN, """
                class Test {
                    void run() {
                        registration.addEventData("event.detail");
                    }
                }
                """);

        Assertions.assertEquals(1, messages.size());
        Assertions.assertTrue(messages.get(0).startsWith(
                "registration.addEventData(...) adds event data that is not a declared DomEventData"),
                messages.get(0));
        Assertions.assertTrue(messages.get(0).contains("addEventDataPath(...)"),
                messages.get(0));
    }

    @Test
    void eventAnnotations_leftToFlow() throws Exception {
        // Flow puts @EventData values into the bundle, and handles the
        // filter of @DomEvent on its own
        List<Integer> lines = check("strict", MAIN, """
                @DomEvent(value = "click", filter = "event.button === 0")
                class Test {
                    Test(@EventData("event.detail.map(f => f.id)") Object ids) {
                    }
                }
                """);

        Assertions.assertEquals(List.of(), lines);
    }

    @Test
    void setFilter_onListenerRegistrationFlagged() throws Exception {
        List<Integer> lines = check("strict", MAIN,
                """
                        class Test {
                            private DomListenerRegistration registration;

                            void run(DomListenerRegistration parameter) {
                                element.addEventListener("x", e -> {}).setFilter("event.button === 0");
                                element.addEventListener("x", e -> {}).addEventDataPath("event.detail").setFilter(FILTER);
                                registration.setFilter("event.isTrusted");
                                this.registration.setFilter(getFilter());
                                parameter.setFilter("event.isTrusted");
                                var local = getElement().addEventListener("x", e -> {});
                                local.setFilter("event.isTrusted");
                            }
                        }
                        """);

        Assertions.assertEquals(List.of(5, 6, 7, 8, 9, 11), lines);
    }

    @Test
    void setFilter_messageNamesReplacement() throws Exception {
        List<String> messages = checkMessages("strict", MAIN,
                """
                        class Test {
                            void run() {
                                element.addEventListener("x", e -> {}).setFilter("event.isTrusted");
                            }
                        }
                        """);

        Assertions.assertEquals(1, messages.size());
        Assertions.assertTrue(messages.get(0).startsWith(
                "addEventListener(...).setFilter(...) sets an event filter as a string"),
                messages.get(0));
        Assertions.assertTrue(
                messages.get(0).contains("setFilterExpression(...)"),
                messages.get(0));
    }

    @Test
    void setFilter_nullOrOtherApisNotFlagged() throws Exception {
        List<Integer> lines = check("strict", MAIN,
                """
                        class Test {
                            private DomListenerRegistration registration;
                            private ConfigurableFilterDataProvider<String, Void, String> provider;

                            void run(String text) {
                                registration.setFilter(null);
                                element.addEventListener("x", e -> {}).setFilterExpression(FILTERS.primaryButton());
                                dataView.setFilter("one"::equals);
                                provider.setFilter("text");
                                provider.setFilter(text);
                                getRegistration().setFilter("event.isTrusted");
                            }
                        }
                        """);

        Assertions.assertEquals(List.of(), lines);
    }

    @Test
    void testsAndIntegrationTests_notChecked() throws Exception {
        String source = """
                class Test {
                    void run() {
                        getElement().executeJs("this.foo = 1");
                    }
                }
                """;

        Assertions.assertEquals(List.of(),
                check("strict", "module/src/test/java/Test.java", source));
        Assertions.assertEquals(List.of(), check("strict",
                "module-integration-tests/src/main/java/Test.java", source));
    }

    @Test
    void message_namesTheCall() throws Exception {
        List<String> messages = checkMessages("strict", MAIN, """
                class Test {
                    void run() {
                        getElement().executeJs("this.foo = 1");
                        tooltip.tooltipElement.executeJs("this.foo = 1");
                        executeJs("this.foo = 1");
                    }
                }
                """);

        Assertions.assertEquals(3, messages.size());
        Assertions.assertTrue(messages.get(0).startsWith(
                "getElement(...).executeJs(...) sends JavaScript as a string"),
                messages.get(0));
        Assertions.assertTrue(messages.get(1).startsWith(
                "tooltipElement.executeJs(...) sends JavaScript as a string"),
                messages.get(1));
        Assertions.assertTrue(
                messages.get(2).startsWith(
                        "executeJs(...) sends JavaScript as a string"),
                messages.get(2));
    }

    @Test
    void projectConfiguration_strictMode_stringJavaScriptError()
            throws Exception {
        Properties properties = new Properties();
        properties.setProperty("cspMode", "strict");
        Configuration configuration = ConfigurationLoader.loadConfiguration(
                Path.of("../../checkstyle/checkstyle.xml").toString(),
                new PropertiesExpander(properties));
        List<String> findings = new ArrayList<>();

        run(configuration, MAIN, """
                class Test {
                    void run() {
                        getElement().executeJs("this.foo = 1");
                        registration.addEventDataPath("event.detail");
                        registration.addEventData("event.detail");
                    }
                }
                """, event -> findings
                .add(event.getLine() + " " + event.getSeverityLevel()));

        Assertions.assertEquals(List.of("3 error", "5 error"), findings);
    }

    private List<Integer> check(String cspMode, String path, String source)
            throws Exception {
        List<Integer> lines = new ArrayList<>();
        run(cspMode, path, source, event -> lines.add(event.getLine()));
        return lines;
    }

    private List<String> checkMessages(String cspMode, String path,
            String source) throws Exception {
        List<String> messages = new ArrayList<>();
        run(cspMode, path, source, event -> messages.add(event.getMessage()));
        return messages;
    }

    private void run(String cspMode, String path, String source,
            Consumer<AuditEvent> onError) throws Exception {
        run(configuration(cspMode), path, source, onError);
    }

    private static Configuration configuration(String cspMode) {
        DefaultConfiguration check = new DefaultConfiguration(
                StringJavaScriptCheck.class.getName());
        check.addProperty("cspMode", cspMode);
        DefaultConfiguration treeWalker = new DefaultConfiguration(
                "TreeWalker");
        treeWalker.addChild(check);
        DefaultConfiguration root = new DefaultConfiguration("Checker");
        root.addChild(treeWalker);
        return root;
    }

    private void run(Configuration root, String path, String source,
            Consumer<AuditEvent> onError) throws Exception {
        Path file = tempDir.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, source);

        Checker checker = new Checker();
        checker.setModuleClassLoader(getClass().getClassLoader());
        checker.configure(root);
        checker.addListener(new AuditListener() {
            @Override
            public void auditStarted(AuditEvent event) {
            }

            @Override
            public void auditFinished(AuditEvent event) {
            }

            @Override
            public void fileStarted(AuditEvent event) {
            }

            @Override
            public void fileFinished(AuditEvent event) {
            }

            @Override
            public void addError(AuditEvent event) {
                onError.accept(event);
            }

            @Override
            public void addException(AuditEvent event, Throwable throwable) {
                throw new AssertionError(throwable);
            }
        });
        try {
            checker.process(List.<File> of(file.toFile()));
        } finally {
            checker.destroy();
        }
    }
}
