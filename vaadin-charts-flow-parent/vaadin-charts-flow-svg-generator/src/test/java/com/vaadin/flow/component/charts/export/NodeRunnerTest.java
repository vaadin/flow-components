package com.vaadin.flow.component.charts.export;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.vaadin.flow.server.frontend.FrontendToolsLocator;
import com.vaadin.flow.server.frontend.FrontendUtils;

public class NodeRunnerTest {

    private Pattern vaadinHomeNodeRegexp;
    private ArgumentMatcher<File> matchesVaadinHomeNode;

    @Before
    public void setup() {
        Path vaadinHome = FrontendUtils.getVaadinHomeDirectory().toPath();
        var vaadinNodeHome = vaadinHome.resolve("node/node").toAbsolutePath();
        var nodeExecutableRegex = Pattern.quote(vaadinNodeHome.toString())
                + "(.exe)?";
        vaadinHomeNodeRegexp = Pattern.compile(nodeExecutableRegex);
        matchesVaadinHomeNode = file -> vaadinHomeNodeRegexp
                .matcher(file.getAbsolutePath()).matches();
    }

    @Test()
    public void findNodeExecutable_globalNodeAndVaadinHomeNode_usesGlobalNode() {
        FrontendToolsLocator frontendToolsLocatorMock = Mockito
                .mock(FrontendToolsLocator.class);
        // Mock being able to locate global node installation
        Path mockNodeInstallationPath = Path.of("/usr/local/bin/node")
                .toAbsolutePath();
        Mockito.when(frontendToolsLocatorMock
                .tryLocateTool(Mockito.matches("node(.exe)?")))
                .thenReturn(Optional.of(mockNodeInstallationPath.toFile()));
        // Mock being able to locate node installation in Vaadin home
        Mockito.when(frontendToolsLocatorMock
                .verifyTool(ArgumentMatchers.argThat(matchesVaadinHomeNode)))
                .thenReturn(true);

        NodeRunner nodeRunner = new NodeRunner(frontendToolsLocatorMock);
        String nodeExecutable = nodeRunner.findNodeExecutable();

        Assert.assertEquals(mockNodeInstallationPath.toString(),
                nodeExecutable);
    }

    @Test()
    public void findNodeExecutable_noGlobalNodeAndVaadinHomeNode_usesVaadinHomeNode() {
        FrontendToolsLocator frontendToolsLocatorMock = Mockito
                .mock(FrontendToolsLocator.class);
        // Mock not being able to locate global node installation
        Mockito.when(frontendToolsLocatorMock
                .tryLocateTool(Mockito.matches("node(.exe)?")))
                .thenReturn(Optional.empty());
        // Mock being able to locate node installation in Vaadin home
        Mockito.when(frontendToolsLocatorMock
                .verifyTool(ArgumentMatchers.argThat(matchesVaadinHomeNode)))
                .thenReturn(true);

        NodeRunner nodeRunner = new NodeRunner(frontendToolsLocatorMock);
        String nodeExecutable = nodeRunner.findNodeExecutable();

        Assert.assertTrue(
                vaadinHomeNodeRegexp.matcher(nodeExecutable).matches());
    }

    @Test(expected = IllegalStateException.class)
    public void findNodeExecutable_noGlobalNodeAndNoVaadinHomeNode_throwsException() {
        FrontendToolsLocator frontendToolsLocatorMock = Mockito
                .mock(FrontendToolsLocator.class);
        // Mock not being able to locate any node installation
        Mockito.when(frontendToolsLocatorMock
                .tryLocateTool(Mockito.matches("node(.exe)?")))
                .thenReturn(Optional.empty());
        Mockito.when(frontendToolsLocatorMock
                .verifyTool(ArgumentMatchers.argThat(matchesVaadinHomeNode)))
                .thenReturn(false);

        NodeRunner nodeRunner = new NodeRunner(frontendToolsLocatorMock);
        nodeRunner.findNodeExecutable();
    }
}
