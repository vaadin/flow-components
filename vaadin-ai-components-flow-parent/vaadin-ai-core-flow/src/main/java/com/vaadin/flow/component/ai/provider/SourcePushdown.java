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
package com.vaadin.flow.component.ai.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.prepare.RelOptTableImpl;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelCollations;
import org.apache.calcite.rel.RelFieldCollation;
import org.apache.calcite.rel.RelFieldCollation.NullDirection;
import org.apache.calcite.rel.RelHomogeneousShuttle;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Aggregate;
import org.apache.calcite.rel.logical.LogicalAggregate;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexShuttle;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.SqlKind;

import com.google.common.collect.ImmutableList;
import com.vaadin.flow.component.ai.provider.TableSource.Capability;
import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;
import com.vaadin.flow.component.ai.provider.TableSource.SortKey;

/**
 * Rewrites a logical plan so that a source reads only what the query needs. A
 * limit, with its offset and order, and a count of all rows are handed to the
 * source when it takes everything between them and its scan: projections,
 * filters it can apply exactly, and sorts by its columns. The rewritten plan
 * reads a table that holds the source's answer, so nothing above the scan
 * repeats the work, except for a sort over the page, which is cheap and keeps
 * the order the query asked for.
 * <p>
 * The plan is rewritten before it is optimised, the way the provider merges a
 * limit over a sorted subquery, rather than through planner rules on a
 * convention of its own: the shapes worth pushing are few and known, those a
 * grid writes for its pages and its count and those of a query over one table,
 * and a fixed rewrite keeps the decision visible and testable.
 * </p>
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 * </p>
 */
final class SourcePushdown {

    /**
     * Replaces each limit and count a source can take with a scan of the
     * source's answer, outermost first.
     */
    static final RelHomogeneousShuttle SHUTTLE = new RelHomogeneousShuttle() {
        @Override
        public RelNode visit(RelNode node) {
            RelNode pushed = null;
            if (node instanceof LogicalSort sort) {
                pushed = pushLimit(sort);
            } else if (node instanceof LogicalAggregate aggregate) {
                pushed = pushCount(aggregate);
            }
            return pushed != null ? pushed : super.visit(node);
        }
    };

    private SourcePushdown() {
        // Static helpers only
    }

    /**
     * The scan of a source as seen from the top of the projections, filters and
     * sorts without a limit above it.
     *
     * @param scan
     *            the scan
     * @param table
     *            the scanned table
     * @param columns
     *            for each field at the top, the index of the source column it
     *            is, or {@code -1} for a computed value
     * @param conditions
     *            the filter conditions over the source's columns, or
     *            {@code null} when one refers to a computed value
     * @param order
     *            the order of the uppermost sort over the source's columns,
     *            empty when nothing sorts, or {@code null} when it sorts by a
     *            computed value
     */
    private record Chain(LogicalTableScan scan, SourceTable table,
            List<Integer> columns, List<RexNode> conditions,
            RelCollation order) {
    }

    private static Chain chain(RelNode node) {
        if (node instanceof LogicalTableScan scan) {
            var table = scan.getTable().unwrap(SourceTable.class);
            if (table == null) {
                return null;
            }
            var columns = IntStream.range(0, scan.getRowType().getFieldCount())
                    .boxed().toList();
            return new Chain(scan, table, columns, List.of(),
                    RelCollations.EMPTY);
        }
        if (node instanceof LogicalProject project) {
            var below = chain(project.getInput());
            if (below == null) {
                return null;
            }
            var columns = project.getProjects().stream()
                    .map(expression -> expression instanceof RexInputRef ref
                            ? below.columns().get(ref.getIndex())
                            : -1)
                    .toList();
            return new Chain(below.scan(), below.table(), columns,
                    below.conditions(), below.order());
        }
        if (node instanceof LogicalFilter filter) {
            var below = chain(filter.getInput());
            if (below == null) {
                return null;
            }
            var condition = toSource(filter.getCondition(), below.columns());
            List<RexNode> conditions = null;
            if (below.conditions() != null && condition != null) {
                conditions = new ArrayList<>(below.conditions());
                conditions.add(condition);
            }
            return new Chain(below.scan(), below.table(), below.columns(),
                    conditions, below.order());
        }
        if (node instanceof LogicalSort sort && sort.offset == null
                && sort.fetch == null) {
            var below = chain(sort.getInput());
            if (below == null) {
                return null;
            }
            return new Chain(below.scan(), below.table(), below.columns(),
                    below.conditions(),
                    toSource(sort.getCollation(), below.columns()));
        }
        return null;
    }

    private static RexNode toSource(RexNode condition, List<Integer> columns) {
        for (int index : RelOptUtil.InputFinder.bits(condition)) {
            if (columns.get(index) < 0) {
                return null;
            }
        }
        return condition.accept(new RexShuttle() {
            @Override
            public RexNode visitInputRef(RexInputRef ref) {
                return new RexInputRef(columns.get(ref.getIndex()),
                        ref.getType());
            }
        });
    }

    private static RelCollation toSource(RelCollation collation,
            List<Integer> columns) {
        var keys = new ArrayList<RelFieldCollation>();
        for (var key : collation.getFieldCollations()) {
            var column = columns.get(key.getFieldIndex());
            if (column < 0) {
                return null;
            }
            keys.add(key.withFieldIndex(column));
        }
        return RelCollations.of(keys);
    }

    private static RelNode pushLimit(LogicalSort top) {
        if (top.offset == null && top.fetch == null) {
            return null;
        }
        var offset = top.offset == null ? Long.valueOf(0)
                : longValue(top.offset);
        var limit = top.fetch == null ? null : longValue(top.fetch);
        if (offset == null || (top.fetch != null && limit == null)) {
            return null;
        }
        var chain = chain(top.getInput());
        if (chain == null || !chain.table().source().getCapabilities()
                .contains(Capability.LIMIT)) {
            return null;
        }
        var filters = filters(chain);
        var topOrdered = !top.getCollation().getFieldCollations().isEmpty();
        var order = topOrdered ? toSource(top.getCollation(), chain.columns())
                : chain.order();
        var sort = order == null ? Optional.<List<SortKey>> empty()
                : sortKeys(order, chain.table().source());
        if (filters.isEmpty() || sort.isEmpty()) {
            return null;
        }
        var pushdown = new Pushdown(filters.get(), sort.get(), offset, limit);
        var scan = scan(chain.scan(), chain.table().pushedTable(pushdown),
                chain.scan().getRowType(), "fetch " + pushdown);
        var result = rebuild(top.getInput(), scan);
        if (topOrdered) {
            result = LogicalSort.create(result, top.getCollation(), null, null);
        }
        return RelOptUtil.areRowTypesEqual(result.getRowType(),
                top.getRowType(), true) ? result : null;
    }

    private static RelNode pushCount(LogicalAggregate aggregate) {
        if (aggregate.getGroupType() != Aggregate.Group.SIMPLE
                || aggregate.getGroupCount() != 0
                || aggregate.getAggCallList().size() != 1) {
            return null;
        }
        var call = aggregate.getAggCallList().get(0);
        if (call.getAggregation().getKind() != SqlKind.COUNT
                || !call.getArgList().isEmpty() || call.isDistinct()
                || call.hasFilter()) {
            return null;
        }
        var chain = chain(aggregate.getInput());
        if (chain == null || !chain.table().source().getCapabilities()
                .contains(Capability.COUNT)) {
            return null;
        }
        var filters = filters(chain);
        if (filters.isEmpty()) {
            return null;
        }
        var pushdown = new Pushdown(filters.get());
        var scan = scan(chain.scan(),
                chain.table().countTable(pushdown, aggregate.getRowType()),
                aggregate.getRowType(), "count " + pushdown);
        return RelOptUtil.areRowTypesEqual(scan.getRowType(),
                aggregate.getRowType(), true) ? scan : null;
    }

    private static Long longValue(RexNode node) {
        return node instanceof RexLiteral literal
                ? literal.getValueAs(Long.class)
                : null;
    }

    /**
     * Returns the chain's filters as the source's equality filters, or nothing
     * when the source cannot apply every one of them.
     */
    private static Optional<Map<String, Object>> filters(Chain chain) {
        if (chain.conditions() == null) {
            return Optional.empty();
        }
        if (chain.conditions().isEmpty()) {
            return Optional.of(Map.of());
        }
        var source = chain.table().source();
        if (!source.getCapabilities().contains(Capability.EQUALITY_FILTER)) {
            return Optional.empty();
        }
        var remaining = new ArrayList<>(chain.conditions());
        var filters = SourceTable.takeEqualityFilters(
                chain.scan().getCluster().getRexBuilder(), remaining,
                source.getColumns());
        return remaining.isEmpty() ? Optional.of(filters) : Optional.empty();
    }

    /**
     * Returns the order as the source's sort keys, or nothing when the source
     * cannot return its rows in that order. Null values have to sort as the
     * greatest values, as they do by default.
     */
    private static Optional<List<SortKey>> sortKeys(RelCollation order,
            TableSource source) {
        if (order.getFieldCollations().isEmpty()) {
            return Optional.of(List.of());
        }
        if (!source.getCapabilities().contains(Capability.SORT)) {
            return Optional.empty();
        }
        var keys = new ArrayList<SortKey>();
        for (var key : order.getFieldCollations()) {
            var direction = key.getDirection();
            if (direction != RelFieldCollation.Direction.ASCENDING
                    && direction != RelFieldCollation.Direction.DESCENDING) {
                return Optional.empty();
            }
            var descending = direction == RelFieldCollation.Direction.DESCENDING;
            var nulls = key.nullDirection == NullDirection.UNSPECIFIED
                    ? direction.defaultNullDirection()
                    : key.nullDirection;
            if (nulls != (descending ? NullDirection.FIRST
                    : NullDirection.LAST)) {
                return Optional.empty();
            }
            keys.add(new SortKey(
                    source.getColumns().get(key.getFieldIndex()).name(),
                    descending));
        }
        return source.supportsSort(keys) ? Optional.of(keys) : Optional.empty();
    }

    /**
     * Creates a scan of a table that stands in for the original scan, named
     * after it with what was pushed, which shows in the plan.
     */
    private static LogicalTableScan scan(LogicalTableScan original, Table table,
            RelDataType rowType, String pushed) {
        var names = ImmutableList.<String> builder()
                .addAll(original.getTable().getQualifiedName()).add(pushed)
                .build();
        var relOptTable = RelOptTableImpl.create(
                original.getTable().getRelOptSchema(), rowType, table, names);
        return LogicalTableScan.create(original.getCluster(), relOptTable,
                List.of());
    }

    /**
     * Rebuilds the chain over the new scan, keeping its projections and
     * dropping its filters and sorts, which the source applied.
     */
    private static RelNode rebuild(RelNode node, RelNode scan) {
        if (node instanceof LogicalTableScan) {
            return scan;
        }
        if (node instanceof LogicalProject project) {
            return LogicalProject.create(rebuild(project.getInput(), scan),
                    project.getHints(), project.getProjects(),
                    project.getRowType().getFieldNames(),
                    project.getVariablesSet());
        }
        return rebuild(node.getInput(0), scan);
    }
}
