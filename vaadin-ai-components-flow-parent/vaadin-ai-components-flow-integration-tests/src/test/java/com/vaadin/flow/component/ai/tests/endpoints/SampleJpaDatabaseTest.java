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
package com.vaadin.flow.component.ai.tests.endpoints;

import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.tests.endpoints.jpa.Employee;
import com.vaadin.flow.component.ai.tests.endpoints.jpa.ExpenseCategory;
import com.vaadin.flow.component.ai.tests.endpoints.jpa.OrgMember;
import com.vaadin.flow.component.ai.tests.endpoints.jpa.SampleJpaDatabase;

import jakarta.persistence.Table;
import jakarta.persistence.Tuple;

class SampleJpaDatabaseTest {

    @Test
    void persistsEveryEntity() {
        try (var entityManager = SampleJpaDatabase.entityManagerFactory()
                .createEntityManager()) {
            var metamodel = entityManager.getMetamodel();
            Assertions.assertEquals(SampleJpaDatabase.ENTITY_CLASSES.size(),
                    metamodel.getEntities().size());
            for (var entity : metamodel.getEntities()) {
                var count = entityManager.createQuery(
                        "SELECT COUNT(e) FROM " + entity.getName() + " e",
                        Long.class).getSingleResult();
                var table = SampleData.table(
                        entity.getJavaType().getAnnotation(Table.class).name());
                Assertions.assertEquals(table.rows().size(), count,
                        entity.getName());
            }
        }
    }

    @Test
    void jpqlNavigatesAssociations() {
        try (var entityManager = SampleJpaDatabase.entityManagerFactory()
                .createEntityManager()) {
            var rows = entityManager.createQuery(
                    "SELECT t.taskName AS task, t.dependsOn.taskName AS dependency "
                            + "FROM ProjectTask t WHERE t.dependsOn IS NOT NULL ORDER BY t.id",
                    Tuple.class).getResultList();
            Assertions.assertEquals(4, rows.size());
            Assertions.assertEquals("Design", rows.getFirst().get("task"));
            Assertions.assertEquals("Requirements",
                    rows.getFirst().get("dependency"));

            var ceo = entityManager.find(OrgMember.class, 1);
            Assertions.assertNull(ceo.getManager());
            Assertions.assertEquals(2, ceo.getReports().size());

            var marketing = entityManager.createQuery(
                    "SELECT c FROM ExpenseCategory c WHERE c.categoryName = 'Marketing'",
                    ExpenseCategory.class).getSingleResult();
            Assertions.assertEquals(3, marketing.getChildren().size());
            Assertions.assertEquals("Marketing", marketing.getChildren()
                    .getFirst().getParent().getCategoryName());

            var totals = entityManager.createQuery(
                    "SELECT e.department AS department, SUM(e.salary) AS total "
                            + "FROM Employee e GROUP BY e.department ORDER BY e.department",
                    Tuple.class).getResultList();
            Assertions.assertEquals("Engineering",
                    totals.getFirst().get("department"));
            Assertions.assertEquals(180000L, totals.getFirst().get("total"));
            Assertions.assertEquals("John Doe",
                    entityManager.find(Employee.class, 1).getName());
        }
    }

    @Test
    void generatedSchemaMatchesTheSqlTables() throws SQLException {
        var dataSource = SampleJpaDatabase.dataSource();
        for (var table : SampleData.TABLES) {
            Assertions.assertEquals(table.rows().size(),
                    Jdbc.count(dataSource, table.sqlName()), table.sqlName());
        }
        Assertions.assertEquals("January", Jdbc.scalar(dataSource,
                "SELECT \"MONTH\" FROM sales WHERE month_order = 1 AND region = 'North'"));
        Assertions.assertEquals("Operating", Jdbc.scalar(dataSource,
                "SELECT parent_category FROM expense_categories WHERE category_name = 'Salaries'"));
        Assertions.assertEquals(2, Jdbc.scalar(dataSource,
                "SELECT depends_on FROM project_tasks WHERE task_name = 'Backend Dev'"));
        Assertions.assertEquals(3, Jdbc.scalar(dataSource,
                "SELECT manager_id FROM org_chart WHERE emp_name = 'Dan Sales'"));
    }
}
