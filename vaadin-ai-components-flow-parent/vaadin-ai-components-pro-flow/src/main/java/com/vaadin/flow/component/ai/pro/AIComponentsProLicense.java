/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.pro.licensechecker.Capabilities;
import com.vaadin.pro.licensechecker.Capability;
import com.vaadin.pro.licensechecker.LicenseChecker;

/**
 * Development-mode license check for the Vaadin AI Components Pro product,
 * called from the static initializer of each controller in this module.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public final class AIComponentsProLicense {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AIComponentsProLicense.class);

    private AIComponentsProLicense() {
        // static-only class
    }

    /**
     * Checks the license, unless the application runs in production mode or no
     * Vaadin service is available.
     */
    public static void check() {
        var service = VaadinService.getCurrent();
        if (service == null
                || service.getDeploymentConfiguration().isProductionMode()) {
            return;
        }

        var properties = new Properties();
        try (var stream = AIComponentsProLicense.class
                .getResourceAsStream("ai-components-pro.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            LOGGER.warn("Unable to read AI Components Pro properties file", e);
            throw new ExceptionInInitializerError(e);
        }

        // A null build type allows trial licensing builds
        LicenseChecker.checkLicenseFromStaticBlock("vaadin-ai-components-pro",
                properties.getProperty("ai-components-pro.version"), null,
                Capabilities.of(Capability.PRE_TRIAL));
    }
}
