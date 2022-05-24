/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

package com.vaadin.flow.component.charts.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.server.frontend.FrontendToolsLocator;
import com.vaadin.flow.server.frontend.FrontendUtils;

class NodeRunner {

    private final FrontendToolsLocator frontendToolsLocator;

    NodeRunner() {
        this(new FrontendToolsLocator());
    }

    NodeRunner(FrontendToolsLocator frontendToolsLocator) {
        this.frontendToolsLocator = frontendToolsLocator;
    }

    /**
     * Returns the absolute file path to a Node.js executable. First tries to
     * find a global installation, and as a fallback looks for an installation
     * in the Vaadin home directory. Throws if no Node.js installation could be
     * found.
     *
     * @return the absolute path to the Node.js executable
     * @throws IllegalStateException
     *             when no Node.js installation could be found
     */
    String findNodeExecutable() {
        // Try resolve global node installation
        String nodeExecutableName = FrontendUtils.isWindows() ? "node.exe"
                : "node";
        File nodeExecutableFile = frontendToolsLocator
                .tryLocateTool(nodeExecutableName).orElse(null);
        if (nodeExecutableFile != null) {
            return nodeExecutableFile.getAbsolutePath();
        }
        // Try resolve installation from Vaadin home
        // This covers development setups where developers rely on the Node.js
        // installation provided by Flow's frontend toolchain
        File vaadinHomeDirectory = FrontendUtils.getVaadinHomeDirectory();
        nodeExecutableFile = new File(vaadinHomeDirectory,
                FrontendUtils.isWindows() ? "node/node.exe" : "node/node");
        if (frontendToolsLocator.verifyTool(nodeExecutableFile)) {
            return nodeExecutableFile.getAbsolutePath();
        }

        throw new IllegalStateException(String.format(
                "The SVG generator requires a Node.js installation, however none could be found. Searched for a global installation in PATH, and in the Vaadin home directory: %s",
                vaadinHomeDirectory.getAbsolutePath()));
    }

    int runJavascript(String script) throws InterruptedException, IOException {
        String nodeExecutable = findNodeExecutable();
        List<String> command = new ArrayList<>();
        command.add(nodeExecutable);
        command.add("-e");
        // this check is necessary since running a script on windows eats up
        // double quotes
        if (FrontendUtils.isWindows()) {
            command.add(script.replace("\"", "\\\""));
        } else {
            command.add(script);
        }
        ProcessBuilder builder = FrontendUtils.createProcessBuilder(command);
        builder.inheritIO();
        Process process = builder.start();
        return process.waitFor();
    }
}
