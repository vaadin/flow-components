/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2021 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

package com.vaadin.flow.component.charts.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.server.frontend.FrontendTools;
import com.vaadin.flow.server.frontend.FrontendUtils;

class NodeRunner {

    int runJavascript(String script) throws InterruptedException, IOException {
        FrontendTools tools = new FrontendTools("",
                () -> FrontendUtils.getVaadinHomeDirectory().getAbsolutePath());
        String node = tools.getNodeExecutable();
        List<String> command = new ArrayList<>();
        command.add(node);
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

    int runJavascript(Path script) throws InterruptedException, IOException {
        FrontendTools tools = new FrontendTools("",
                () -> FrontendUtils.getVaadinHomeDirectory().getAbsolutePath());
        String node = tools.getNodeExecutable();
        List<String> command = new ArrayList<>();
        command.add(node);
        command.add(script.toFile().getAbsolutePath());
        ProcessBuilder builder = FrontendUtils.createProcessBuilder(command);
        builder.inheritIO();
        Process process = builder.start();
        return process.waitFor();
    }
}
