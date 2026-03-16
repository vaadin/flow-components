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
package com.vaadin.flow.component.ai.dashboard;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Provides LLM tool definitions for dashboard layout operations.
 * <p>
 * This class encapsulates tools that allow an LLM to manage dashboard widgets:
 * listing widgets, updating widget properties (title, colspan, rowspan), and
 * removing widgets.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class DashboardTools implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DashboardTools.class);

    private final Dashboard dashboard;

    /**
     * Creates a new dashboard tools instance bound to the given dashboard.
     *
     * @param dashboard
     *            the dashboard component to manage, not {@code null}
     */
    public DashboardTools(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    /**
     * Returns the recommended system prompt for dashboard management
     * capabilities.
     *
     * @return the system prompt text
     */
    public static String getSystemPrompt() {
        return """
                You have access to dashboard management capabilities:

                TOOLS:
                1. getDashboardState() - Returns the current state of all widgets (id, title, type, colspan, rowspan)
                2. updateWidget(widgetId, title, colspan, rowspan) - Updates a widget's properties

                WORKFLOW:
                1. ALWAYS call getDashboardState() FIRST to see existing widgets
                2. Use updateWidget() to change widget titles, sizes, or positions
                3. Widget IDs are assigned when widgets are created and shown in getDashboardState()

                WIDGET PROPERTIES:
                - title: Display title shown on the widget header
                - colspan: Number of columns the widget spans (default: 1, minimum: 1)
                - rowspan: Number of rows the widget spans (default: 1, minimum: 1)
                - Larger colspan/rowspan values make the widget take more space in the dashboard grid
                """;
    }

    /**
     * Returns the tool definitions for dashboard operations.
     *
     * @return list of tool definitions
     */
    public List<LLMProvider.ToolDefinition> getTools() {
        return List.of(createGetDashboardStateTool(),
                createUpdateWidgetTool());
    }

    /**
     * Returns the dashboard component bound to this tools instance.
     *
     * @return the dashboard component
     */
    public Dashboard getDashboard() {
        return dashboard;
    }

    // ===== Tool Implementations =====

    private LLMProvider.ToolDefinition createGetDashboardStateTool() {
        return new LLMProvider.ToolDefinition() {
            @Override
            public String getName() {
                return "getDashboardState";
            }

            @Override
            public String getDescription() {
                return "Returns the current state of the dashboard including all widgets with their IDs, titles, types, colspan, and rowspan. Takes no parameters.";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                List<DashboardWidget> widgets = dashboard.getWidgets();
                if (widgets.isEmpty()) {
                    return "{\"status\":\"empty\",\"message\":\"Dashboard has no widgets\",\"widgets\":[]}";
                }
                StringBuilder sb = new StringBuilder("{\"widgets\":[");
                for (int i = 0; i < widgets.size(); i++) {
                    DashboardWidget widget = widgets.get(i);
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append("{\"widgetId\":\"").append(widget.getId()
                            .orElse("widget-" + i)).append("\"");
                    sb.append(",\"title\":\"")
                            .append(widget.getTitle() != null
                                    ? widget.getTitle().replace("\"", "\\\"")
                                    : "")
                            .append("\"");
                    sb.append(",\"colspan\":").append(widget.getColspan());
                    sb.append(",\"rowspan\":").append(widget.getRowspan());
                    String contentType = "unknown";
                    if (widget.getContent() instanceof com.vaadin.flow.component.charts.Chart) {
                        contentType = "chart";
                    } else if (widget.getContent() instanceof com.vaadin.flow.component.grid.Grid) {
                        contentType = "grid";
                    }
                    sb.append(",\"contentType\":\"").append(contentType)
                            .append("\"");
                    sb.append("}");
                }
                sb.append("]}");
                return sb.toString();
            }
        };
    }

    private LLMProvider.ToolDefinition createUpdateWidgetTool() {
        return new LLMProvider.ToolDefinition() {
            @Override
            public String getName() {
                return "updateWidget";
            }

            @Override
            public String getDescription() {
                return """
                    Updates a dashboard widget's properties.
                    Use getDashboardState() first to get the widget IDs.

                    Parameters:
                    - widgetId (string, required): The ID of the widget to update
                    - title (string, optional): New title for the widget
                    - colspan (integer, optional): Number of columns the widget spans (minimum: 1)
                    - rowspan (integer, optional): Number of rows the widget spans (minimum: 1)
                    """;
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "widgetId": {
                              "type": "string",
                              "description": "The ID of the widget to update"
                            },
                            "title": {
                              "type": "string",
                              "description": "New title for the widget"
                            },
                            "colspan": {
                              "type": "integer",
                              "description": "Number of columns the widget spans (minimum: 1)",
                              "minimum": 1
                            },
                            "rowspan": {
                              "type": "integer",
                              "description": "Number of rows the widget spans (minimum: 1)",
                              "minimum": 1
                            }
                          },
                          "required": ["widgetId"]
                        }
                        """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    String widgetId = node.get("widgetId").asString();

                    DashboardWidget targetWidget = findWidgetById(widgetId);
                    if (targetWidget == null) {
                        return "Error: Widget with ID '" + widgetId
                                + "' not found";
                    }

                    dashboard.getUI().ifPresentOrElse(ui -> {
                        ui.access(() -> {
                            JsonNode titleNode = node.get("title");
                            if (titleNode != null && !titleNode.isNull()) {
                                targetWidget
                                        .setTitle(titleNode.asString());
                            }
                            JsonNode colspanNode = node.get("colspan");
                            if (colspanNode != null
                                    && !colspanNode.isNull()) {
                                targetWidget.setColspan(
                                        Math.max(1, colspanNode.asInt()));
                            }
                            JsonNode rowspanNode = node.get("rowspan");
                            if (rowspanNode != null
                                    && !rowspanNode.isNull()) {
                                targetWidget.setRowspan(
                                        Math.max(1, rowspanNode.asInt()));
                            }
                        });
                    }, () -> {
                        throw new IllegalStateException(
                                "Dashboard is not attached to a UI");
                    });

                    return "Widget '" + widgetId + "' updated successfully";
                } catch (Exception e) {
                    return "Error updating widget: " + e.getMessage();
                }
            }
        };
    }

    private DashboardWidget findWidgetById(String widgetId) {
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getId().isPresent()
                    && widget.getId().get().equals(widgetId)) {
                return widget;
            }
        }
        return null;
    }
}
