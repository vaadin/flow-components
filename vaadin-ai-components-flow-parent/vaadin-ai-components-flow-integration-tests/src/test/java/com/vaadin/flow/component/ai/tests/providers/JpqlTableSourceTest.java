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
package com.vaadin.flow.component.ai.tests.providers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.FederatedDatabaseProvider;
import com.vaadin.flow.component.ai.provider.TableSource.Column;
import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;
import com.vaadin.flow.component.ai.provider.TableSource.SortKey;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.component.ai.tests.endpoints.jpa.Employee;
import com.vaadin.flow.component.ai.tests.endpoints.jpa.OrgMember;
import com.vaadin.flow.component.ai.tests.endpoints.jpa.SampleJpaDatabase;
import com.vaadin.flow.component.ai.tests.endpoints.jpa.StockPrice;

class JpqlTableSourceTest {

    private static final FederatedDatabaseProvider PROVIDER = FederatedSampleProvider
            .jpql();

    @Test
    void columns_areBasicAttributesAndAssociationIds_inNameOrder() {
        var source = new JpqlTableSource(
                SampleJpaDatabase::entityManagerFactory, OrgMember.class);

        Assertions.assertEquals("OrgMember", source.getName());
        Assertions.assertEquals(
                List.of(new Column("empName", String.class),
                        new Column("id", Integer.class),
                        new Column("jobTitle", String.class),
                        new Column("managerId", Integer.class),
                        new Column("teamColor", String.class)),
                source.getColumns());
    }

    @Test
    void fetch_pushesEqualityFiltersIntoTheJpql() {
        var employees = new JpqlTableSource(
                SampleJpaDatabase::entityManagerFactory, Employee.class);
        var members = new JpqlTableSource(
                SampleJpaDatabase::entityManagerFactory, OrgMember.class);
        var prices = new JpqlTableSource(
                SampleJpaDatabase::entityManagerFactory, StockPrice.class);
        var date = SampleData.STOCK_PRICES.get(0).tradeDate();

        var engineering = employees
                .fetch(new Pushdown(Map.of("department", "Engineering")));
        var reportsOfTheCeo = members
                .fetch(new Pushdown(Map.of("managerId", 1)));
        var onDate = prices.fetch(new Pushdown(Map.of("tradeDate", date)));

        Assertions
                .assertEquals(
                        SampleData.EMPLOYEES.stream()
                                .filter(employee -> employee.department()
                                        .equals("Engineering"))
                                .count(),
                        engineering.size());
        Assertions
                .assertEquals(
                        SampleData.ORG_CHART.stream()
                                .filter(member -> Integer.valueOf(1)
                                        .equals(member.managerId()))
                                .count(),
                        reportsOfTheCeo.size());
        Assertions.assertFalse(onDate.isEmpty());
        onDate.forEach(
                row -> Assertions.assertEquals(date, row.get("tradeDate")));
    }

    @Test
    void fetch_withoutFilters_returnsEveryRow_withNullForNoAssociation() {
        var members = new JpqlTableSource(
                SampleJpaDatabase::entityManagerFactory, OrgMember.class);

        var rows = members.fetch(Pushdown.NONE);

        Assertions.assertEquals(SampleData.ORG_CHART.size(), rows.size());
        Assertions.assertTrue(
                rows.stream().anyMatch(row -> row.get("managerId") == null));
    }

    @Test
    void fetch_sortsAnAssociationIdWithNullsAfterOthersWhenAscending() {
        var members = new JpqlTableSource(
                SampleJpaDatabase::entityManagerFactory, OrgMember.class);

        var ascending = members.fetch(new Pushdown(Map.of(),
                List.of(new SortKey("managerId", false)), 0, null));
        var descending = members.fetch(new Pushdown(Map.of(),
                List.of(new SortKey("managerId", true)), 0, 2L));

        Assertions.assertEquals("Sarah CEO",
                ascending.getLast().get("empName"));
        Assertions.assertEquals(List.of("Sarah CEO", "Anna Dev"),
                descending.stream().map(row -> row.get("empName")).toList());
    }

    @Test
    void fetch_pagesInIdOrderWithoutASort() {
        var members = new JpqlTableSource(
                SampleJpaDatabase::entityManagerFactory, OrgMember.class);

        var rows = members.fetch(new Pushdown(Map.of(), List.of(), 2, 2L));

        Assertions.assertEquals(List.of(3, 4),
                rows.stream().map(row -> row.get("id")).toList());
    }

    @Test
    void count_countsTheFilteredEntities() {
        var employees = new JpqlTableSource(
                SampleJpaDatabase::entityManagerFactory, Employee.class);

        Assertions.assertEquals(SampleData.EMPLOYEES.size(),
                employees.count(Pushdown.NONE));
        Assertions.assertEquals(
                SampleData.EMPLOYEES.stream()
                        .filter(employee -> employee.department()
                                .equals("Sales"))
                        .count(),
                employees.count(new Pushdown(Map.of("department", "Sales"))));
    }

    @Test
    void provider_readsAGridPageFromTheEntityQuery() {
        var page = PROVIDER.executeQuery(
                "SELECT * FROM (SELECT * FROM (SELECT empName AS \"Name\" FROM OrgMember WHERE managerId = 1) AS _t ORDER BY \"Name\" DESC) AS _limited LIMIT 1 OFFSET 1");
        var plan = PROVIDER.explain(
                "SELECT * FROM (SELECT empName AS \"Name\" FROM OrgMember WHERE managerId = 1) AS _limited LIMIT 1");

        Assertions.assertEquals(List.of(Map.of("Name", "Lisa VP Sales")), page);
        Assertions.assertTrue(plan.contains(
                "fetch Pushdown[equalityFilters={managerId=1}, sort=[], offset=0, limit=1]"),
                plan);
    }

    @Test
    void provider_aggregatesAndJoins() {
        var byRegion = PROVIDER.executeQuery(
                "SELECT region, SUM(revenue) AS total FROM Sale GROUP BY region ORDER BY region");
        var reports = PROVIDER.executeQuery(
                "SELECT m.empName AS manager, COUNT(*) AS reports FROM OrgMember r JOIN OrgMember m ON r.managerId = m.id GROUP BY m.empName ORDER BY m.empName");
        var date = SampleData.STOCK_PRICES.get(0).tradeDate();
        var byDate = PROVIDER.executeQuery(
                "SELECT ticker, openPrice FROM StockPrice WHERE tradeDate = DATE '"
                        + date + "' ORDER BY ticker");

        Assertions.assertEquals(List.of("North", "South"),
                byRegion.stream().map(row -> row.get("region")).toList());
        Assertions.assertFalse(reports.isEmpty());
        Assertions.assertFalse(byDate.isEmpty());
        Assertions.assertInstanceOf(BigDecimal.class,
                byDate.get(0).get("openPrice"));
    }
}
