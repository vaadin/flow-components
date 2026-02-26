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
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for Markdown component.
 */
@Route(value = "markdown", layout = MainLayout.class)
@PageTitle("Markdown | Vaadin Kitchen Sink")
public class MarkdownDemoView extends VerticalLayout {

    public MarkdownDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Markdown Component"));
        add(new Paragraph("Markdown renders markdown text as formatted HTML."));

        // Basic markdown
        Markdown basic = new Markdown("# Hello World\n\nThis is a **basic** markdown example with *italic* text.");
        addSection("Basic Markdown", basic);

        // Headings
        Markdown headings = new Markdown(
            "# Heading 1\n" +
            "## Heading 2\n" +
            "### Heading 3\n" +
            "#### Heading 4\n" +
            "##### Heading 5\n" +
            "###### Heading 6"
        );
        addSection("Headings", headings);

        // Text formatting
        Markdown formatting = new Markdown(
            "**Bold text** and *italic text* and ***bold italic***\n\n" +
            "~~Strikethrough~~ text\n\n" +
            "`Inline code` formatting\n\n" +
            "This is a [link](https://vaadin.com)"
        );
        addSection("Text Formatting", formatting);

        // Lists
        Markdown lists = new Markdown(
            "## Unordered List\n" +
            "- Item one\n" +
            "- Item two\n" +
            "  - Nested item\n" +
            "  - Another nested\n" +
            "- Item three\n\n" +
            "## Ordered List\n" +
            "1. First item\n" +
            "2. Second item\n" +
            "3. Third item"
        );
        addSection("Lists", lists);

        // Code blocks
        Markdown code = new Markdown(
            "## Code Block\n\n" +
            "```java\n" +
            "public class HelloWorld {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello, World!\");\n" +
            "    }\n" +
            "}\n" +
            "```"
        );
        addSection("Code Blocks", code);

        // Blockquotes
        Markdown quotes = new Markdown(
            "> This is a blockquote.\n" +
            "> It can span multiple lines.\n\n" +
            "> > And blockquotes can be nested."
        );
        addSection("Blockquotes", quotes);

        // Tables
        Markdown tables = new Markdown(
            "| Header 1 | Header 2 | Header 3 |\n" +
            "|----------|----------|----------|\n" +
            "| Cell 1   | Cell 2   | Cell 3   |\n" +
            "| Cell 4   | Cell 5   | Cell 6   |\n" +
            "| Cell 7   | Cell 8   | Cell 9   |"
        );
        addSection("Tables", tables);

        // Horizontal rules
        Markdown rules = new Markdown(
            "Content above the line\n\n" +
            "---\n\n" +
            "Content below the line"
        );
        addSection("Horizontal Rules", rules);

        // Complete document example
        Markdown document = new Markdown(
            "# Project Documentation\n\n" +
            "Welcome to our project! This document provides an overview.\n\n" +
            "## Installation\n\n" +
            "```bash\n" +
            "npm install my-package\n" +
            "```\n\n" +
            "## Features\n\n" +
            "- **Fast** - Optimized for performance\n" +
            "- **Simple** - Easy to use API\n" +
            "- **Flexible** - Highly configurable\n\n" +
            "## Configuration\n\n" +
            "| Option | Type | Default | Description |\n" +
            "|--------|------|---------|-------------|\n" +
            "| debug | boolean | false | Enable debug mode |\n" +
            "| timeout | number | 5000 | Request timeout |\n\n" +
            "> **Note:** Always test in development before deploying.\n\n" +
            "## Support\n\n" +
            "Visit our [website](https://example.com) for more information."
        );
        addSection("Complete Document Example", document);
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM,
                LumoUtility.Padding.MEDIUM);
        section.add(layout);
        add(section);
    }
}
