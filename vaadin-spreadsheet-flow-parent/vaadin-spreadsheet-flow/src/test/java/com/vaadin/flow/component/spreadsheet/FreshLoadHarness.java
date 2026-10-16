/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.PaneInformation;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.tests.CapturedLogs;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.StateNode;
import com.vaadin.flow.internal.change.MapPutChange;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.flow.internal.nodefeature.VirtualChildrenList;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Runs operations on a spreadsheet as its client sees them, and checks each
 * against a new spreadsheet on the same workbook.
 * <p>
 * The client is modelled from what it is sent: the element's properties, and
 * the cells and pop-up buttons the calls give it. When the server reloads the
 * client, the model forgets its cells and reports the viewport again, as the
 * client does. What the client changes itself before the server answers, a
 * value typed or a comment written, is modelled too (see {@link #clientEvent});
 * sizes dragged on a header are not. After an operation:
 * <ol>
 * <li>the client has what a new spreadsheet gives it for the same viewport;
 * </li>
 * <li>nothing was sent for nothing: no cell twice in one response, no cell
 * again as the client has it, nothing taken off the client that it does not
 * have, and no pop-up button added again;</li>
 * <li>nothing was thrown, or logged as a warning or an error;</li>
 * <li>the sheets shown neither before nor after are unchanged;</li>
 * <li>an operation that is refused changes nothing, in the workbook or on the
 * client.</li>
 * </ol>
 * Each check returns the rules broken, in words; none when there are none.
 */
final class FreshLoadHarness {

    /** The rows and columns the client reports it shows, 1-based. */
    record Viewport(int firstRow, int firstColumn, int lastRow,
            int lastColumn) {
        String json() {
            return "{\"firstRow\": " + firstRow + ", \"firstColumn\": "
                    + firstColumn + ", \"lastRow\": " + lastRow
                    + ", \"lastColumn\": " + lastColumn + "}";
        }
    }

    /** What an operation may do that a rule would otherwise report. */
    enum Allowance {
        /** It changes sheets other than the one shown. */
        OTHER_SHEETS,
        /** It sends cells again as the client has them, as it is asked to. */
        RESEND,
        /** It logs warnings or errors. */
        WARNINGS
    }

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    /** Properties that differ between two spreadsheets on the same data. */
    private static final Set<String> IGNORED_PROPERTIES = Set.of(
            // the UI's, and a random one
            "appId", "id",
            // the time of the last reload, repaint and workbook change
            "reload", "dirty", "workbookChangeToggle");

    /** Properties the client takes as false until it is sent one. */
    private static final Set<String> FALSE_BY_DEFAULT = Set.of(
            "hasComponentFactory", "sheetProtected", "workbookProtected",
            "lockFormatColumns", "lockFormatRows", "colGroupingInversed",
            "rowGroupingInversed");

    /** Properties whose order means nothing to the client. */
    private static final Set<String> UNORDERED_PROPERTIES = Set.of(
            "mergedRegions", "invalidFormulaCells", "visibleCellComments",
            "hiddenRowIndexes", "hiddenColumnIndexes", "lockedRowIndexes",
            "lockedColumnIndexes");

    private final MockUIExtension ui;
    private final Consumer<Spreadsheet> configure;
    private final Viewport viewport;

    private Spreadsheet spreadsheet;
    private ClientModel client;

    /**
     * @param configure
     *            what is done to every spreadsheet before it is shown, the new
     *            ones compared with included
     */
    FreshLoadHarness(MockUIExtension ui, Consumer<Spreadsheet> configure,
            Viewport viewport) {
        this.ui = ui;
        this.configure = configure;
        this.viewport = viewport;
    }

    /** Shows the workbook, and the client reports its viewport. */
    Spreadsheet show(Workbook workbook) {
        spreadsheet = new Spreadsheet(workbook);
        configure.accept(spreadsheet);
        ui.add(spreadsheet);
        client = new ClientModel();
        settle(spreadsheet, client, true, null);
        return spreadsheet;
    }

    /** What the client of the spreadsheet shown has. */
    ClientModel clientModel() {
        return client;
    }

    /**
     * Takes the spreadsheet out of the UI and puts it back, as an application
     * moving it does. The browser gets a new element, which has nothing until
     * it tells the server it is there and reports its viewport.
     */
    void reattach() {
        ui.remove(spreadsheet);
        ui.add(spreadsheet);
        client = new ClientModel();
        TestHelper.fireClientEvent(spreadsheet, "onConnectorInit", "{}");
        settle(spreadsheet, client, true, null);
    }

    /**
     * The client sends an event, its data as JSON, after changing what it
     * changes at once without waiting for the server: a value typed into a
     * cell, or a comment written, show at once. Sizes changed by dragging a
     * header are not modelled.
     */
    void clientEvent(String type, String json) {
        JsonNode data = MAPPER.readTree(json);
        switch (type) {
        case "cellValueEdited" -> client.commitLocally(data.get("row").asInt(),
                data.get("col").asInt(), data.get("value").asString());
        case "updateCellComment" ->
            client.writeCommentLocally(data.get("row").asInt(),
                    data.get("col").asInt(), data.get("text").asString());
        default -> {
        }
        }
        TestHelper.fireClientEvent(spreadsheet, type, json);
    }

    /** Runs the operation, and returns the rules it broke. */
    List<String> check(Consumer<Spreadsheet> operation,
            Allowance... allowances) {
        return check(operation, configure, allowances);
    }

    /**
     * Runs the operation, and returns the rules it broke.
     *
     * @param configureFresh
     *            what is done to the new spreadsheet compared with, when the
     *            operation changes how the spreadsheet is set up
     */
    List<String> check(Consumer<Spreadsheet> operation,
            Consumer<Spreadsheet> configureFresh, Allowance... allowances) {
        Set<Allowance> allowed = allowances.length == 0
                ? EnumSet.noneOf(Allowance.class)
                : EnumSet.of(allowances[0], allowances);
        List<String> problems = new ArrayList<>();
        String shown = spreadsheet.getActiveSheet().getSheetName();
        Map<String, String> before = snapshot(spreadsheet.getWorkbook());

        run(operation, problems,
                allowed.contains(Allowance.RESEND) ? null : problems,
                allowed.contains(Allowance.WARNINGS) ? null : problems);

        // A sheet shown is set up as the spreadsheet shows it, as with the
        // default column width: only those never shown are left alone.
        String shownAfter = spreadsheet.getActiveSheet().getSheetName();
        if (!allowed.contains(Allowance.OTHER_SHEETS)) {
            Map<String, String> after = snapshot(spreadsheet.getWorkbook());
            before.forEach((name, content) -> {
                if (!name.equals(shown) && !name.equals(shownAfter)
                        && !name.equals(SHEETS)) {
                    String difference = firstDifference(content,
                            after.get(name));
                    if (difference != null) {
                        problems.add("sheet " + name + " not shown changed: "
                                + difference);
                    }
                }
            });
        }
        problems.addAll(compareWithFresh(configureFresh));
        return problems;
    }

    /** When an operation is run on a spreadsheet its client has not seen. */
    enum Before {
        /** Before the spreadsheet is in the UI. */
        ATTACHED,
        /** In the UI, before the first response. */
        FIRST_RESPONSE
    }

    /**
     * Makes a spreadsheet on the workbook and runs the operation before its
     * client has seen it, as an application setting a spreadsheet up does; then
     * the client is shown it. Returns the rules broken: what was thrown or
     * logged, and how the client differs from a new spreadsheet on the workbook
     * as it is afterwards.
     */
    List<String> checkBeforeShown(Workbook workbook, Before before,
            Consumer<Spreadsheet> operation,
            Consumer<Spreadsheet> configureFresh) {
        List<String> problems = new ArrayList<>();
        spreadsheet = new Spreadsheet(workbook);
        configure.accept(spreadsheet);
        client = new ClientModel();
        try (CapturedLogs.Capture logs = CapturedLogs.capture()) {
            if (before == Before.FIRST_RESPONSE) {
                ui.add(spreadsheet);
            }
            try {
                operation.accept(spreadsheet);
            } catch (RuntimeException failure) {
                problems.add("threw " + failure);
            }
            if (before == Before.ATTACHED) {
                ui.add(spreadsheet);
            }
            try {
                settle(spreadsheet, client, true, null);
            } catch (RuntimeException failure) {
                problems.add("the first response threw " + failure);
            }
            logs.entries().forEach(entry -> problems.add("logged " + entry));
        }
        problems.addAll(compareWithFresh(configureFresh));
        return problems;
    }

    /**
     * Runs an operation that is to be refused, and returns what it changed
     * anyway, in the workbook and on the client. A refusal may throw.
     */
    List<String> checkRefused(Consumer<Spreadsheet> operation) {
        List<String> problems = new ArrayList<>();
        Map<String, String> before = snapshot(spreadsheet.getWorkbook());
        View clientBefore = view(spreadsheet, client);

        run(target -> {
            try {
                operation.accept(target);
            } catch (RuntimeException refused) {
                // refused by throwing
            }
        }, problems, problems, problems);

        Map<String, String> after = snapshot(spreadsheet.getWorkbook());
        Set<String> sheets = new TreeSet<>(before.keySet());
        sheets.addAll(after.keySet());
        for (String sheet : sheets) {
            String difference = firstDifference(before.get(sheet),
                    after.get(sheet));
            if (difference != null) {
                problems.add("sheet " + sheet + " changed: " + difference);
            }
        }
        for (String difference : differences(view(spreadsheet, client),
                clientBefore, "before it had")) {
            problems.add("the client changed: " + difference);
        }
        return problems;
    }

    /**
     * @param thrown
     *            where an exception the operation or a response throws goes
     */
    private void run(Consumer<Spreadsheet> operation, List<String> thrown,
            List<String> waste, List<String> logged) {
        // The reload property holds the time of the reload: one in the same
        // millisecond as the last would not reach the client.
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() == start) {
            Thread.onSpinWait();
        }
        try (CapturedLogs.Capture logs = CapturedLogs.capture()) {
            try {
                operation.accept(spreadsheet);
                settle(spreadsheet, client, false, waste);
            } catch (RuntimeException failure) {
                thrown.add("threw " + failure);
            }
            if (logged != null) {
                logs.entries().forEach(entry -> logged.add("logged " + entry));
            }
        }
    }

    /**
     * What differs between the client and a new spreadsheet on the same
     * workbook, showing the same viewport.
     */
    private List<String> compareWithFresh(
            Consumer<Spreadsheet> configureFresh) {
        View actual = view(spreadsheet, client);
        Workbook workbook = spreadsheet.getWorkbook();
        Map<String, String> before = snapshot(workbook);

        View expected;
        Spreadsheet fresh = null;
        try {
            fresh = new Spreadsheet(workbook);
            configureFresh.accept(fresh);
            ui.add(fresh);
            ClientModel freshClient = new ClientModel();
            settle(fresh, freshClient, true, null);
            expected = view(fresh, freshClient);
        } catch (RuntimeException failure) {
            return List.of("a new spreadsheet threw " + failure);
        } finally {
            if (fresh != null) {
                ui.remove(fresh);
                respond(fresh);
            }
        }

        List<String> differences = new ArrayList<>(
                differences(actual, expected, "a new spreadsheet sends"));
        String changed = firstDifference(before.toString(),
                snapshot(workbook).toString());
        if (changed != null) {
            differences.add("showing the workbook in a new spreadsheet "
                    + "changed it: " + changed);
        }
        return differences;
    }

    /**
     * Sends the responses an operation causes, until the client stops being
     * reloaded.
     *
     * @param announce
     *            whether the client reports its viewport after the first
     *            response even without a reload, as it does when shown
     * @param waste
     *            where what is sent for nothing goes, or {@code null} not to
     *            look
     */
    private void settle(Spreadsheet target, ClientModel model, boolean announce,
            List<String> waste) {
        for (int round = 0;; round++) {
            if (round == 5) {
                throw new AssertionError(
                        "The client was still being reloaded after five responses");
            }
            Response response = respond(target);
            boolean reloaded = response.properties().contains("reload");
            // The client takes the properties of a response before its calls,
            // and comments sent replace those it wrote itself.
            if (response.properties().contains("cellComments")) {
                model.localComments.clear();
            }
            if (reloaded) {
                model.reload();
            }
            model.apply(response.calls(), waste);
            if (reloaded || announce) {
                announce = false;
                TestHelper.fireClientEvent(target, "onSheetScroll",
                        viewport.json());
                continue;
            }
            return;
        }
    }

    /**
     * What a response gives the spreadsheet's element: the properties it sends,
     * by name, and the calls it makes on it.
     */
    record Response(Set<String> properties, List<Call> calls) {
    }

    /** One response, with what it gives the spreadsheet's element. */
    private Response respond(Spreadsheet target) {
        UI current = ui.getUI();
        StateNode node = target.getElement().getNode();
        current.getInternals().getStateTree()
                .runExecutionsBeforeClientResponse();
        Set<String> properties = new HashSet<>();
        current.getInternals().getStateTree().collectChanges(change -> {
            if (change.getNode() == node && change instanceof MapPutChange put
                    && put.getFeature() == ElementPropertyMap.class) {
                properties.add(put.getKey());
            }
        });
        List<Call> calls = new ArrayList<>();
        for (PendingJavaScriptInvocation invocation : current.getInternals()
                .dumpPendingJavaScriptInvocations()) {
            if (invocation.getOwner() == node) {
                calls.add(Call.of(invocation));
            }
        }
        return new Response(properties, calls);
    }

    /** A call on the element: the function and its arguments. */
    record Call(String function, List<Object> arguments) {

        private static final Pattern EXPRESSION_FUNCTION = Pattern
                .compile("this\\.([A-Za-z]+)(\\(|=)");

        static Call of(PendingJavaScriptInvocation invocation) {
            String expression = invocation.getInvocation().getExpression();
            List<Object> parameters = invocation.getInvocation()
                    .getParameters();
            if (expression.contains("$0.split('.')")) {
                // callJsFunction: the function, its arguments, the element
                int end = parameters.size();
                if (parameters.get(end - 1) instanceof Element) {
                    end--;
                }
                return new Call((String) parameters.get(0),
                        Collections.unmodifiableList(
                                new ArrayList<>(parameters.subList(1, end))));
            }
            // executeJs: this.name(...), or this.name=$0 for a property
            Matcher function = EXPRESSION_FUNCTION.matcher(expression);
            String name = !function.find() ? expression
                    : "=".equals(function.group(2)) ? function.group(1) + "="
                            : function.group(1);
            return new Call(name,
                    Collections.unmodifiableList(new ArrayList<>(parameters)));
        }
    }

    /** What the client holds of what the calls gave it. */
    static final class ClientModel {

        private static final Set<String> CELL_CALLS = Set.of("cellsUpdated",
                "updateTopLeftCellValues", "updateTopRightCellValues",
                "updateBottomLeftCellValues", "updateBottomRightCellValues");

        /** Calls that leave nothing the model keeps. */
        private static final Set<String> OTHER_CALLS = Set.of(
                "refreshCellStyles", "invalidCellAddress", "showSelectedCell",
                "setSelectedCellAndRange", "showActions", "editCellComment",
                "updateFormulaBar", "onPopupButtonOpen", "closePopup");

        /** The cells, by name, as A1. */
        final Map<String, JsonNode> cells = new TreeMap<>();

        /** The pop-up buttons, by the name of their cell. */
        final Map<String, JsonNode> popupButtons = new TreeMap<>();

        /** The keys of the resources, as overlay images. */
        final List<String> resources = new ArrayList<>();

        /**
         * The comments written on the client, by the key of their cell, until
         * the server sends the comments again.
         */
        final Map<String, String> localComments = new TreeMap<>();

        /** The client was reloaded: it forgets the cells it had. */
        void reload() {
            cells.clear();
        }

        /**
         * A value typed into a cell shows at once, in the cell's style, until
         * the server sends the cell; a formula waits for the server.
         */
        void commitLocally(int row, int column, String value) {
            if (value.startsWith("=") || value.startsWith("+")) {
                return;
            }
            String name = name(row, column);
            JsonNode previous = cells.get(name);
            ObjectNode shown = MAPPER.createObjectNode();
            shown.put("col", column);
            shown.put("row", row);
            shown.put("value", value);
            shown.put("cellStyle", previous == null ? "cs0"
                    : previous.path("cellStyle").asString("cs0"));
            if (previous != null) {
                for (String kept : List.of("locked", "needsMeasure",
                        "isPercentage", "formulaValue", "originalValue",
                        "textColor")) {
                    if (previous.has(kept)) {
                        shown.set(kept, previous.get(kept));
                    }
                }
            }
            cells.put(name, normalizeCell(shown));
        }

        void writeCommentLocally(int row, int column, String text) {
            localComments.put(SpreadsheetUtil.toKey(column, row), text);
        }

        void apply(List<Call> calls, List<String> waste) {
            Set<String> sentNow = new HashSet<>();
            for (Call call : calls) {
                String function = call.function();
                if (CELL_CALLS.contains(function)) {
                    for (JsonNode sent : MAPPER
                            .readTree((String) call.arguments().get(0))) {
                        JsonNode cell = normalizeCell(sent);
                        String name = name(cell.get("row").asInt(),
                                cell.get("col").asInt());
                        JsonNode had = cells.get(name);
                        if (waste != null) {
                            if (!sentNow.add(name)) {
                                waste.add(name + " sent twice in one response ("
                                        + function + ")");
                            } else if (isRemoval(cell)
                                    && (had == null || isRemoval(had))) {
                                waste.add(name + " taken off the client, which"
                                        + " does not have it");
                            } else if (cell.equals(had)) {
                                waste.add(name + " sent again as the client"
                                        + " has it (" + function + ")");
                            }
                        }
                        cells.put(name, cell);
                    }
                } else if ("addPopupButton".equals(function)) {
                    JsonNode state = MAPPER
                            .readTree((String) call.arguments().get(0));
                    String name = name(state.path("row").asInt(),
                            state.path("col").asInt());
                    if (waste != null && state.equals(popupButtons.get(name))) {
                        waste.add("pop-up button " + name
                                + " added again as the client has it");
                    }
                    popupButtons.put(name, state);
                } else if ("removePopupButton".equals(function)) {
                    JsonNode state = MAPPER
                            .readTree((String) call.arguments().get(0));
                    String name = name(state.path("row").asInt(),
                            state.path("col").asInt());
                    if (popupButtons.remove(name) == null && waste != null) {
                        waste.add("pop-up button " + name + " taken off the"
                                + " client, which does not have it");
                    }
                } else if ("resources=".equals(function)) {
                    resources.clear();
                    ((Iterable<?>) call.arguments().get(0))
                            .forEach(key -> resources.add(String.valueOf(key)));
                } else if (!OTHER_CALLS.contains(function)) {
                    throw new AssertionError("A client call the model does "
                            + "not know: " + function + call.arguments());
                }
            }
        }
    }

    /**
     * The kind of thing sent for nothing a problem reports, or {@code null}
     * when it reports something else.
     */
    static String wasteKind(String problem) {
        if (problem.contains(" sent twice in one response")) {
            return "sent twice";
        } else if (problem.contains("pop-up button ")
                && problem.contains(" added again")) {
            return "pop-up button added again";
        } else if (problem.contains("pop-up button ")
                && problem.contains(" taken off the client")) {
            return "pop-up button taken off but not there";
        } else if (problem.contains(" sent again as the client has it")) {
            return "sent again";
        } else if (problem.contains(" taken off the client, which does not")) {
            return "taken off but not there";
        }
        return null;
    }

    /** Whether the cell data takes a cell off the client. */
    static boolean isRemoval(JsonNode cell) {
        CellData removal = new CellData();
        removal.row = cell.path("row").asInt();
        removal.col = cell.path("col").asInt();
        return cell.equals(
                normalizeCell(MAPPER.readTree(Serializer.serialize(removal))));
    }

    /**
     * The cell data without what the client takes as unset anyway: false, null,
     * and no run into the next cells.
     */
    static JsonNode normalizeCell(JsonNode cell) {
        ObjectNode normalized = (ObjectNode) cell.deepCopy();
        List<String> unset = new ArrayList<>();
        normalized.properties().forEach(field -> {
            JsonNode value = field.getValue();
            if (value.isNull() || (value.isBoolean() && !value.booleanValue())
                    || ("overflowWidth".equals(field.getKey())
                            && value.asInt() == 0)) {
                unset.add(field.getKey());
            }
        });
        normalized.remove(unset);
        return normalized;
    }

    /** The name of a cell, as A1, from its 1-based row and column. */
    static String name(int row, int column) {
        return new CellReference(row - 1, column - 1).formatAsString();
    }

    /** What the client has, as it is compared. */
    record View(Map<String, JsonNode> properties, Map<String, JsonNode> cells,
            Map<String, JsonNode> popupButtons, List<String> virtualChildren) {
    }

    private View view(Spreadsheet target, ClientModel model) {
        Element element = target.getElement();
        Map<String, JsonNode> properties = new TreeMap<>();
        element.getPropertyNames()
                .filter(name -> !IGNORED_PROPERTIES.contains(name))
                .forEach(name -> {
                    JsonNode value = normalize(name,
                            element.getPropertyRaw(name));
                    if (value != null) {
                        properties.put(name, value);
                    }
                });
        if (!model.localComments.isEmpty()) {
            ObjectNode comments = properties
                    .get("cellComments") instanceof ObjectNode sent
                            ? sent.deepCopy()
                            : MAPPER.createObjectNode();
            model.localComments.forEach(comments::put);
            properties.put("cellComments", comments);
        }
        // The keys differ between spreadsheets, as the overlay ids do.
        properties.put("(resource attributes)",
                MAPPER.valueToTree(element.getAttributeNames()
                        .filter(name -> name.startsWith("resource-")).count()));
        properties.put("(resources the client finds)",
                MAPPER.valueToTree(model.resources.stream()
                        .filter(key -> element.hasAttribute("resource-" + key))
                        .count()));

        int frozenRows = target.getLastFrozenRow();
        int frozenColumns = target.getLastFrozenColumn();
        Map<String, JsonNode> cells = new TreeMap<>();
        model.cells.forEach((name, cell) -> {
            int row = cell.get("row").asInt();
            int column = cell.get("col").asInt();
            boolean rowShown = row <= frozenRows || (row >= viewport.firstRow()
                    && row <= viewport.lastRow());
            boolean columnShown = column <= frozenColumns
                    || (column >= viewport.firstColumn()
                            && column <= viewport.lastColumn());
            if (rowShown && columnShown && !isRemoval(cell)) {
                cells.put(name, cell);
            }
        });

        List<String> virtualChildren = new ArrayList<>();
        VirtualChildrenList children = element.getNode()
                .getFeatureIfInitialized(VirtualChildrenList.class)
                .orElse(null);
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                StateNode child = children.get(i);
                virtualChildren.add(Element.get(child).getTag());
            }
        }
        Collections.sort(virtualChildren);
        return new View(properties, cells, new TreeMap<>(model.popupButtons),
                virtualChildren);
    }

    /**
     * The property as the client takes it, or {@code null} for what it takes as
     * unset: nothing, an empty list or map (sent as JSON or as an empty
     * string), or false where it starts out false.
     */
    static JsonNode normalize(String name, Object value) {
        JsonNode node;
        if (value instanceof JsonNode json) {
            node = json;
        } else if (value instanceof String text
                && (text.startsWith("{") || text.startsWith("["))) {
            node = MAPPER.readTree(text);
        } else {
            node = MAPPER.valueToTree(value);
        }
        if (node == null || node.isNull()
                || (node.isString() && node.asString().isEmpty())
                || (node.isContainer() && node.isEmpty())
                || (FALSE_BY_DEFAULT.contains(name) && node.isBoolean()
                        && !node.booleanValue())) {
            return null;
        }
        switch (name) {
        case "overlays":
            // Keyed by a counter all spreadsheets share.
            return sorted(node.values());
        case "componentIDtoCellKeysMap":
            // Keyed by the components' ids.
            return sorted(node.values());
        case "cellKeysToEditorIdMap":
            return sorted(keys(node));
        case "mergedRegions":
            // Numbered in the order they were made; the client does not read
            // the number.
            List<JsonNode> regions = new ArrayList<>();
            node.values().forEach(region -> {
                ObjectNode copy = (ObjectNode) region.deepCopy();
                copy.remove("id");
                regions.add(copy);
            });
            return sorted(regions);
        default:
            return UNORDERED_PROPERTIES.contains(name) && node.isArray()
                    ? sorted(node.values())
                    : node;
        }
    }

    private static Iterable<JsonNode> keys(JsonNode object) {
        List<JsonNode> keys = new ArrayList<>();
        object.propertyNames()
                .forEach(key -> keys.add(MAPPER.valueToTree(key)));
        return keys;
    }

    private static ArrayNode sorted(Iterable<JsonNode> values) {
        List<JsonNode> list = new ArrayList<>();
        values.forEach(list::add);
        list.sort((a, b) -> a.toString().compareTo(b.toString()));
        ArrayNode array = MAPPER.createArrayNode();
        list.forEach(array::add);
        return array;
    }

    /**
     * @param other
     *            what the expected view is, as "a new spreadsheet sends"
     */
    private static List<String> differences(View actual, View expected,
            String other) {
        List<String> differences = new ArrayList<>();
        compare("property", actual.properties(), expected.properties(), other,
                differences);
        compare("cell", actual.cells(), expected.cells(), other, differences);
        compare("pop-up button", actual.popupButtons(), expected.popupButtons(),
                other, differences);
        if (!actual.virtualChildren().equals(expected.virtualChildren())) {
            differences.add("virtual children: the client has "
                    + actual.virtualChildren() + ", " + other + " "
                    + expected.virtualChildren());
        }
        return differences;
    }

    private static void compare(String kind, Map<String, JsonNode> actual,
            Map<String, JsonNode> expected, String other,
            List<String> differences) {
        Set<String> keys = new TreeSet<>(actual.keySet());
        keys.addAll(expected.keySet());
        for (String key : keys) {
            JsonNode has = actual.get(key);
            JsonNode wanted = expected.get(key);
            if (!Objects.equals(has, wanted)) {
                differences.add(
                        kind + " " + key + ": " + describe(has, wanted, other));
            }
        }
    }

    /** Where two values differ first, as short as it can say it. */
    static String describe(JsonNode has, JsonNode expected, String other) {
        if (has != null && expected != null && has.isContainer()
                && expected.isContainer()
                && has.isArray() == expected.isArray()) {
            Set<String> paths = new TreeSet<>();
            if (has.isArray()) {
                if (has.size() != expected.size()) {
                    return "the client has " + has.size() + " entries, " + other
                            + " " + expected.size() + ": " + shorten(has)
                            + " vs " + shorten(expected);
                }
                for (int i = 0; i < has.size(); i++) {
                    if (!has.get(i).equals(expected.get(i))) {
                        return "[" + i + "] "
                                + describe(has.get(i), expected.get(i), other);
                    }
                }
            } else {
                has.propertyNames().forEach(paths::add);
                expected.propertyNames().forEach(paths::add);
                for (String path : paths) {
                    if (!Objects.equals(has.get(path), expected.get(path))) {
                        return "." + path + " " + describe(has.get(path),
                                expected.get(path), other);
                    }
                }
            }
        }
        return "the client has " + shorten(has) + ", " + other + " "
                + shorten(expected);
    }

    private static String shorten(JsonNode node) {
        String text = String.valueOf(node);
        return text.length() > 200 ? text.substring(0, 200) + "…" : text;
    }

    /** The key of the sheets' order and visibility in a snapshot. */
    static final String SHEETS = "(sheets)";

    /**
     * The workbook's content, by sheet, and the sheets' order and visibility
     * under {@link #SHEETS}: what the rules about other sheets and refused
     * operations compare.
     */
    static Map<String, String> snapshot(Workbook workbook) {
        Map<String, String> sheets = new LinkedHashMap<>();
        StringBuilder order = new StringBuilder();
        order.append("active ").append(workbook.getActiveSheetIndex())
                .append('\n');
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            Sheet sheet = workbook.getSheetAt(index);
            order.append(sheet.getSheetName()).append(' ')
                    .append(workbook.getSheetVisibility(index)).append('\n');
            StringBuilder out = new StringBuilder();
            out.append("protected ").append(sheet.getProtect()).append('\n');
            PaneInformation pane = sheet.getPaneInformation();
            if (pane != null) {
                out.append("panes ").append(pane.getVerticalSplitPosition())
                        .append(' ').append(pane.getHorizontalSplitPosition())
                        .append('\n');
            }
            sheet.getMergedRegions().stream()
                    .map(CellRangeAddress::formatAsString).sorted()
                    .forEach(region -> out.append("merged ").append(region)
                            .append('\n'));
            int lastColumn = 0;
            for (Row row : sheet) {
                out.append("row ").append(row.getRowNum() + 1)
                        .append(" height ").append(row.getHeight())
                        .append(row.getZeroHeight() ? " hidden" : "")
                        .append('\n');
                for (Cell cell : row) {
                    lastColumn = Math.max(lastColumn, cell.getColumnIndex());
                    out.append("  ").append(cell.getAddress()).append(' ')
                            .append(content(cell)).append(" style ")
                            .append(cell.getCellStyle().getIndex());
                    Comment comment = cell.getCellComment();
                    if (comment != null) {
                        out.append(" comment ")
                                .append(comment.getString().getString());
                    }
                    Hyperlink link = cell.getHyperlink();
                    if (link != null) {
                        out.append(" link ").append(link.getAddress());
                    }
                    out.append('\n');
                }
            }
            for (int column = 0; column <= lastColumn + 10; column++) {
                out.append("column ").append(column).append(" width ")
                        .append(sheet.getColumnWidth(column))
                        .append(sheet.isColumnHidden(column) ? " hidden" : "")
                        .append('\n');
            }
            out.append("conditional formats ")
                    .append(sheet.getSheetConditionalFormatting()
                            .getNumConditionalFormattings())
                    .append('\n');
            sheets.put(sheet.getSheetName(), out.toString());
        }
        sheets.put(SHEETS, order.toString());
        return sheets;
    }

    private static String content(Cell cell) {
        return switch (cell.getCellType()) {
        case FORMULA -> "=" + cell.getCellFormula();
        case NUMERIC -> String.valueOf(cell.getNumericCellValue());
        case STRING -> '"' + cell.getStringCellValue() + '"';
        case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
        case ERROR -> "#" + cell.getErrorCellValue();
        default -> "blank";
        };
    }

    /** The first line that differs, or {@code null} when none does. */
    static String firstDifference(String before, String after) {
        if (Objects.equals(before, after)) {
            return null;
        }
        if (before == null || after == null) {
            return before == null ? "added" : "removed";
        }
        Iterator<String> was = before.lines().iterator();
        Iterator<String> is = after.lines().iterator();
        while (was.hasNext() || is.hasNext()) {
            String line = was.hasNext() ? was.next() : "(nothing)";
            String now = is.hasNext() ? is.next() : "(nothing)";
            if (!line.equals(now)) {
                return "'" + line.strip() + "' became '" + now.strip() + "'";
            }
        }
        return "the same lines, in another form";
    }
}
