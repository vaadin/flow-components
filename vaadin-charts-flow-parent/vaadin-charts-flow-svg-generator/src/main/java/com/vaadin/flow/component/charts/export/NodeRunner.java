package com.vaadin.flow.component.charts.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.server.frontend.FrontendTools;
import com.vaadin.flow.server.frontend.FrontendUtils;

public class NodeRunner {

    public int runJavascript(String script)
            throws InterruptedException, IOException {
        FrontendTools tools = new FrontendTools("",
                () -> FrontendUtils.getVaadinHomeDirectory().getAbsolutePath());
        String node = tools.getNodeExecutable();
        List<String> command = new ArrayList<>();
        command.add(node);
        command.add("-e");
        command.add(script);
        ProcessBuilder builder = FrontendUtils.createProcessBuilder(command);
        builder.inheritIO();
        Process process = builder.start();
        return process.waitFor();
    }
}
