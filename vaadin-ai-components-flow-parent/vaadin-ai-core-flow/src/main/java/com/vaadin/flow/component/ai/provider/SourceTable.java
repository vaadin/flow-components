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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexUtil;
import org.apache.calcite.schema.FilterableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.DateString;
import org.apache.calcite.util.TimestampString;

import com.vaadin.flow.component.ai.provider.TableSource.Capability;
import com.vaadin.flow.component.ai.provider.TableSource.Column;
import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;

/**
 * Presents a {@link TableSource} to Apache Calcite as a table that can take
 * equality filters. Rows are fetched through the provider so that its cache
 * applies.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 * </p>
 */
final class SourceTable extends AbstractTable implements FilterableTable {

    private final TableSource source;
    private final Function<Pushdown, List<Map<String, Object>>> fetcher;

    SourceTable(TableSource source,
            Function<Pushdown, List<Map<String, Object>>> fetcher) {
        this.source = source;
        this.fetcher = fetcher;
    }

    /**
     * Returns the SQL type of a column's Java type, or {@code null} for a type
     * a source may not use.
     *
     * @param type
     *            the Java type
     * @return the SQL type, or {@code null} if unsupported
     */
    static SqlTypeName sqlType(Class<?> type) {
        if (type == String.class) {
            return SqlTypeName.VARCHAR;
        } else if (type == Integer.class) {
            return SqlTypeName.INTEGER;
        } else if (type == Long.class) {
            return SqlTypeName.BIGINT;
        } else if (type == Double.class) {
            return SqlTypeName.DOUBLE;
        } else if (type == BigDecimal.class) {
            return SqlTypeName.DECIMAL;
        } else if (type == Boolean.class) {
            return SqlTypeName.BOOLEAN;
        } else if (type == LocalDate.class) {
            return SqlTypeName.DATE;
        } else if (type == LocalDateTime.class) {
            return SqlTypeName.TIMESTAMP;
        } else if (type == LocalTime.class) {
            return SqlTypeName.TIME;
        }
        return null;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        var builder = typeFactory.builder();
        for (var column : source.getColumns()) {
            var sqlType = sqlType(column.type());
            var type = sqlType == SqlTypeName.DECIMAL
                    ? typeFactory.createSqlType(sqlType, 19, 4)
                    : typeFactory.createSqlType(sqlType);
            builder.add(column.name(),
                    typeFactory.createTypeWithNullability(type, true));
        }
        return builder.build();
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root, List<RexNode> filters) {
        var columns = source.getColumns();
        var equalityFilters = source.getCapabilities()
                .contains(Capability.EQUALITY_FILTER)
                        ? takeEqualityFilters(root, filters, columns)
                        : Map.<String, Object> of();
        var rows = fetcher.apply(new Pushdown(equalityFilters));
        var result = new ArrayList<Object[]>(rows.size());
        for (var row : rows) {
            // The source may have filtered more loosely than SQL equality,
            // so the pushed filters are checked once more here
            if (!matches(row, equalityFilters)) {
                continue;
            }
            var values = new Object[columns.size()];
            for (int i = 0; i < values.length; i++) {
                var column = columns.get(i);
                values[i] = toInternal(row.get(column.name()), column);
            }
            result.add(values);
        }
        return Linq4j.asEnumerable(result);
    }

    private static boolean matches(Map<String, Object> row,
            Map<String, Object> equalityFilters) {
        for (var filter : equalityFilters.entrySet()) {
            var value = row.get(filter.getKey());
            var expected = filter.getValue();
            var equal = value instanceof Number a
                    && expected instanceof Number b
                            ? new BigDecimal(a.toString()).compareTo(
                                    new BigDecimal(b.toString())) == 0
                            : Objects.equals(value, expected);
            if (!equal) {
                return false;
            }
        }
        return true;
    }

    /**
     * Collects the filters of the form {@code column = literal} as column name
     * to value. Calcite folds comparisons into {@code SEARCH} calls and
     * {@code column = TRUE} into the bare column, so both forms are recognised.
     * A filter whose conjuncts were all collected is removed from the list; the
     * engine applies the ones that remain, which may repeat a collected
     * equality when it shares a filter with another condition.
     */
    private static Map<String, Object> takeEqualityFilters(DataContext root,
            List<RexNode> filters, List<Column> columns) {
        var result = new LinkedHashMap<String, Object>();
        var rexBuilder = new RexBuilder(root.getTypeFactory());
        var iterator = filters.iterator();
        while (iterator.hasNext()) {
            var expanded = RexUtil.expandSearch(rexBuilder, null,
                    iterator.next());
            var handled = true;
            for (var conjunct : RelOptUtil.conjunctions(expanded)) {
                var equality = equality(conjunct, columns);
                if (equality == null || result.containsKey(equality.getKey())) {
                    handled = false;
                } else {
                    result.put(equality.getKey(), equality.getValue());
                }
            }
            if (handled) {
                iterator.remove();
            }
        }
        return result;
    }

    private static Map.Entry<String, Object> equality(RexNode node,
            List<Column> columns) {
        if (node instanceof RexInputRef ref) {
            return booleanEquality(ref, true, columns);
        }
        if (!(node instanceof RexCall call)) {
            return null;
        }
        var operands = call.getOperands();
        switch (call.getKind()) {
        case NOT, IS_FALSE -> {
            return operands.get(0) instanceof RexInputRef ref
                    ? booleanEquality(ref, false, columns)
                    : null;
        }
        case IS_TRUE -> {
            return operands.get(0) instanceof RexInputRef ref
                    ? booleanEquality(ref, true, columns)
                    : null;
        }
        case EQUALS -> {
            var left = unwrapCast(operands.get(0));
            var right = unwrapCast(operands.get(1));
            RexInputRef ref;
            RexLiteral literal;
            if (left instanceof RexInputRef r
                    && right instanceof RexLiteral l) {
                ref = r;
                literal = l;
            } else if (right instanceof RexInputRef r
                    && left instanceof RexLiteral l) {
                ref = r;
                literal = l;
            } else {
                return null;
            }
            var column = columns.get(ref.getIndex());
            var value = literalValue(literal, column.type());
            return value == null ? null : Map.entry(column.name(), value);
        }
        default -> {
            return null;
        }
        }
    }

    private static Map.Entry<String, Object> booleanEquality(RexInputRef ref,
            boolean value, List<Column> columns) {
        var column = columns.get(ref.getIndex());
        return column.type() == Boolean.class ? Map.entry(column.name(), value)
                : null;
    }

    private static RexNode unwrapCast(RexNode node) {
        while (node instanceof RexCall call && call.getKind() == SqlKind.CAST) {
            node = call.getOperands().get(0);
        }
        return node;
    }

    /**
     * Converts a literal to the column's Java type, or {@code null} when the
     * literal cannot be pushed down as that type.
     */
    private static Object literalValue(RexLiteral literal, Class<?> type) {
        if (literal.isNull()) {
            return null;
        }
        try {
            if (type == String.class) {
                return literal.getValueAs(String.class);
            } else if (type == Integer.class) {
                return literal.getValueAs(Integer.class);
            } else if (type == Long.class) {
                return literal.getValueAs(Long.class);
            } else if (type == Double.class) {
                return literal.getValueAs(Double.class);
            } else if (type == BigDecimal.class) {
                return literal.getValueAs(BigDecimal.class);
            } else if (type == Boolean.class) {
                return literal.getValueAs(Boolean.class);
            } else if (type == LocalDate.class) {
                var date = literal.getValueAs(DateString.class);
                return date == null ? null : LocalDate.parse(date.toString());
            } else if (type == LocalDateTime.class) {
                var timestamp = literal.getValueAs(TimestampString.class);
                return timestamp == null ? null
                        : LocalDateTime
                                .parse(timestamp.toString().replace(' ', 'T'));
            }
            return null;
        } catch (RuntimeException e) {
            // A literal of another type than the column, for example a
            // string compared with a number: the engine evaluates it
            return null;
        }
    }

    /**
     * Converts a source value to Calcite's internal representation of the
     * column's SQL type.
     */
    private static Object toInternal(Object value, Column column) {
        if (value == null) {
            return null;
        }
        var type = column.type();
        if (type == String.class) {
            return value instanceof String ? value : String.valueOf(value);
        } else if (type == Integer.class) {
            return number(value, column).intValue();
        } else if (type == Long.class) {
            return number(value, column).longValue();
        } else if (type == Double.class) {
            return number(value, column).doubleValue();
        } else if (type == BigDecimal.class) {
            var number = number(value, column);
            return number instanceof BigDecimal decimal ? decimal
                    : new BigDecimal(number.toString());
        } else if (type == Boolean.class) {
            return expect(value, Boolean.class, column);
        } else if (type == LocalDate.class) {
            return (int) expect(value, LocalDate.class, column).toEpochDay();
        } else if (type == LocalDateTime.class) {
            return expect(value, LocalDateTime.class, column)
                    .toInstant(ZoneOffset.UTC).toEpochMilli();
        } else if (type == LocalTime.class) {
            return (int) (expect(value, LocalTime.class, column).toNanoOfDay()
                    / 1_000_000);
        }
        throw new IllegalStateException(
                "Unsupported column type " + type.getName());
    }

    private static Number number(Object value, Column column) {
        return expect(value, Number.class, column);
    }

    private static <T> T expect(Object value, Class<T> type, Column column) {
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException("Column " + column.name()
                    + " expects " + column.type().getSimpleName()
                    + " values but got " + value.getClass().getName());
        }
        return type.cast(value);
    }
}
