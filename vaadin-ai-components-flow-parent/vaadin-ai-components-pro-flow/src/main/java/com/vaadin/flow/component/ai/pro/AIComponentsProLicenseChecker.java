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
import java.util.concurrent.atomic.AtomicBoolean;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.pro.licensechecker.BuildType;
import com.vaadin.pro.licensechecker.Capabilities;
import com.vaadin.pro.licensechecker.Capability;
import com.vaadin.pro.licensechecker.LicenseChecker;

/**
 * Checks in development mode that a valid commercial license is available for
 * the Vaadin AI Components Pro product.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public final class AIComponentsProLicenseChecker {

    static final String PRODUCT_NAME = "vaadin-ai-components-pro";

    private static final AtomicBoolean licenseChecked = new AtomicBoolean();

    private AIComponentsProLicenseChecker() {
        // static-only class
    }

    /**
     * Checks the license in development mode. The check is done at most once
     * per JVM and is skipped when no Vaadin service is available or when the
     * application runs in production mode.
     */
    public static void checkLicense() {
        var service = VaadinService.getCurrent();
        if (service == null
                || service.getDeploymentConfiguration().isProductionMode()) {
            return;
        }
        if (licenseChecked.compareAndSet(false, true)) {
            try {
                LicenseChecker.checkLicense(PRODUCT_NAME, getVersion(),
                        Capabilities.of(Capability.PRE_TRIAL),
                        BuildType.DEVELOPMENT);
            } catch (RuntimeException e) {
                licenseChecked.set(false);
                throw e;
            }
        }
    }

    private static String getVersion() {
        var properties = new Properties();
        try (var stream = AIComponentsProLicenseChecker.class
                .getResourceAsStream("ai-components-pro.properties")) {
            if (stream == null) {
                throw new IOException("Properties file not found");
            }
            properties.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to read AI Components Pro properties file", e);
        }
        return properties.getProperty("ai-components-pro.version");
    }
}
