/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;

@Route("vaadin-charts/examples")
public class ExamplesListView extends Div {

    private static final String EXAMPLES_PACKAGE = "com.vaadin.flow.component.charts.examples";

    public ExamplesListView() {
        getStyle().setPadding("20px");

        H1 title = new H1("Vaadin Charts Examples");
        add(title);

        Paragraph description = new Paragraph(
                "Click on any example below to view it:");
        add(description);

        List<Class<? extends AbstractChartExample>> exampleClasses = findExampleClasses();

        if (exampleClasses.isEmpty()) {
            add(new Paragraph("No examples found."));
            return;
        }

        for (Class<? extends AbstractChartExample> exampleClass : exampleClasses) {
            String className = exampleClass.getName();
            String packagePath = className
                    .substring(EXAMPLES_PACKAGE.length() + 1);
            String url = "/vaadin-charts/" + packagePath.replace(".", "/");
            String displayName = getDisplayName(exampleClass);

            Anchor link = new Anchor(url, displayName);
            link.getStyle().setDisplay(Style.Display.BLOCK);
            add(link);
        }
    }

    private List<Class<? extends AbstractChartExample>> findExampleClasses() {
        List<Class<? extends AbstractChartExample>> examples = new ArrayList<>();

        try {
            List<String> classNames = scanClassesInPackage();

            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className);
                    if (AbstractChartExample.class.isAssignableFrom(clazz)
                            && clazz != AbstractChartExample.class) {
                        @SuppressWarnings("unchecked")
                        Class<? extends AbstractChartExample> exampleClass = (Class<? extends AbstractChartExample>) clazz;

                        examples.add(exampleClass);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    // Skip classes that can't be loaded
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        examples.sort(Comparator.comparing(Class::getName));
        return examples;
    }

    private List<String> scanClassesInPackage()
            throws IOException, URISyntaxException {
        List<String> classNames = new ArrayList<>();
        String packagePath = ExamplesListView.EXAMPLES_PACKAGE.replace('.',
                '/');

        URL packageUrl = Thread.currentThread().getContextClassLoader()
                .getResource(packagePath);
        if (packageUrl == null) {
            return classNames;
        }

        if (packageUrl.getProtocol().equals("file")) {
            Path packageDir = Paths.get(packageUrl.toURI());
            try (Stream<Path> paths = Files.walk(packageDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".class"))
                        .forEach(path -> {
                            String relativePath = packageDir.relativize(path)
                                    .toString();
                            String className = relativePath.replace('/', '.')
                                    .replace('\\', '.');
                            className = className.substring(0,
                                    className.length() - 6); // Remove .class
                            classNames.add(ExamplesListView.EXAMPLES_PACKAGE
                                    + "." + className);
                        });
            }
        }

        return classNames;
    }

    private String getDisplayName(Class<? extends AbstractChartExample> clazz) {
        String className = clazz.getSimpleName();
        String packageName = clazz.getPackage().getName();
        String category = packageName
                .substring(packageName.lastIndexOf('.') + 1);

        return category + " / " + className;
    }
}
