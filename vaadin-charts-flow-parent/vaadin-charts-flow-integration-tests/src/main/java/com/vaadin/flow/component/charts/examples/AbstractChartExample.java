/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.Command;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Abstract class for all chart examples.
 */
public abstract class AbstractChartExample extends Div {
    public AbstractChartExample() {
        initDemo();
    }

    public abstract void initDemo();

    protected void showNotification(String message) {
        UI.getCurrent().getPage().executeJs("window.alert($0);", message);
    }

    protected String createEventString(ComponentEvent<Chart> event) {
        JsonMapper mapper = JsonMapper.builder()
                .changeDefaultPropertyInclusion(handler -> handler
                        .withValueInclusion(JsonInclude.Include.NON_NULL))
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .changeDefaultVisibility(handler -> handler
                        .withVisibility(PropertyAccessor.ALL,
                                JsonAutoDetect.Visibility.NONE)
                        .withVisibility(PropertyAccessor.FIELD,
                                JsonAutoDetect.Visibility.ANY))
                .addMixIn(Command.class, JacksonMixinForIgnoreCommand.class)
                .build();

        try {
            return mapper.writeValueAsString(event);
        } catch (JacksonException e) {
            e.printStackTrace(); // NOSONAR
            return "";
        }
    }

    @JsonIgnoreType
    static class JacksonMixinForIgnoreCommand {
    }

    /**
     * Runs given task repeatedly until the reference component is attached
     *
     * @param component
     * @param task
     * @param interval
     * @param initialPause
     *            a timeout after tas is started
     */
    public static void runWhileAttached(Component component, Command task,
            final int interval, final int initialPause) {
        component.addAttachListener(event -> {
            ScheduledExecutorService executor = Executors
                    .newScheduledThreadPool(1);

            component.getUI().ifPresent(ui -> ui.setPollInterval(interval));

            final ScheduledFuture<?> scheduledFuture = executor
                    .scheduleAtFixedRate(() -> {
                        component.getUI().ifPresent(ui -> ui.access(task));
                    }, initialPause, interval, TimeUnit.MILLISECONDS);

            component.addDetachListener(detach -> {
                scheduledFuture.cancel(true);
                detach.getUI().setPollInterval(-1);
            });
        });
    }
}
