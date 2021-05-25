package com.vaadin.flow.component.charts.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException; 
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.frontend.FrontendTools;
import com.vaadin.flow.server.frontend.FrontendUtils;

public class SVGGenerator {

    public static String generateSVG(Configuration chartConfiguration) {
        assert chartConfiguration != null;
        String chartConfigurationString = ChartSerialization.toJSON(chartConfiguration);
        
        try {
            Path tempDirPath = Files.createTempDirectory("svg-export");
            File bundleTempFile = Files.createFile(tempDirPath.resolve("export-svg-bundle.js")).toFile();

            tempDirPath.toFile().deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(bundleTempFile)) {
                IOUtils.copy(SVGGenerator.class
                        .getResourceAsStream("/META-INF/frontend/generated/jsdom-exporter-bundle.js"), out);

                String command = String.format("const hc = require('%s');\n"
                                + "hc({\n"
                                + "options: %s,\n"
                                + "outfile: 'chart.svg',\n"
                                + "})",
                        bundleTempFile.getAbsolutePath().replace("\\", "/"),
                        chartConfigurationString);

                runNodeScript(command);

                Path chart = Paths.get(tempDirPath.toString() + "/chart.svg");
                return Files.readAllLines(chart).get(0);
            } catch (Throwable e) {
                e.printStackTrace();
                LoggerFactory.getLogger(SVGGenerator.class).info("error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerFactory.getLogger(SVGGenerator.class).error("Unable to create temp file for bundle", e);
        }
        return "";
    }
 
    private static int runNodeScript(String script) throws InterruptedException, IOException {
        FrontendTools tools = new FrontendTools("", () -> FrontendUtils.getVaadinHomeDirectory().getAbsolutePath());
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
