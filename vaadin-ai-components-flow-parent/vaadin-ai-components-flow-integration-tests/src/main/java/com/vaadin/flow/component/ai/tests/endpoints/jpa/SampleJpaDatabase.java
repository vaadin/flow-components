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
package com.vaadin.flow.component.ai.tests.endpoints.jpa;

import java.util.HashMap;
import java.util.List;
import javax.sql.DataSource;

import org.hibernate.cfg.JdbcSettings;

import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.component.ai.tests.endpoints.SampleH2Database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceConfiguration;

/**
 * The sample data as a JPA persistence unit: Hibernate generates the tables
 * from the entity mappings in this package into an in-memory H2 database and
 * the rows of {@link SampleData} are persisted as entities. This is the
 * endpoint a Spring Data JPA application has: entities with a schema of their
 * own, an {@link EntityManagerFactory}, and a database reachable both with JPQL
 * and, through the same connection, with native SQL. Once seeded, H2 logs every
 * statement on the database, the SQL Hibernate runs for JPQL included, see
 * {@link SampleH2Database#logStatements(String)}.
 */
public final class SampleJpaDatabase {

    /** URL of the in-memory database the persistence unit uses. */
    public static final String JDBC_URL = "jdbc:h2:mem:sampledb_jpa;DB_CLOSE_DELAY=-1";

    /** The entity classes of the persistence unit, in seeding order. */
    public static final List<Class<?>> ENTITY_CLASSES = List.of(Sale.class,
            Employee.class, Product.class, StockPrice.class, ProjectTask.class,
            OrgMember.class, EnergyFlow.class, TrafficCell.class,
            BudgetItem.class, PipelineStage.class, KpiMetric.class,
            ExpenseCategory.class);

    private static EntityManagerFactory entityManagerFactory;

    private SampleJpaDatabase() {
    }

    /**
     * Returns the entity manager factory of the persistence unit, creating the
     * schema and persisting the sample data on the first call.
     *
     * @return the entity manager factory
     */
    public static synchronized EntityManagerFactory entityManagerFactory() {
        if (entityManagerFactory == null) {
            // H2 logs only the statements of connections opened after its
            // log is switched on, so Hibernate gets a data source without a
            // pool that would keep the connections of the seeding
            var configuration = new PersistenceConfiguration("sample-data")
                    .property(JdbcSettings.JAKARTA_NON_JTA_DATASOURCE,
                            SampleH2Database.dataSource(JDBC_URL,
                                    SampleH2Database.ADMIN_USER,
                                    SampleH2Database.ADMIN_PASSWORD))
                    .property(
                            PersistenceConfiguration.SCHEMAGEN_DATABASE_ACTION,
                            "drop-and-create");
            ENTITY_CLASSES.forEach(configuration::managedClass);
            var factory = configuration.createEntityManagerFactory();
            try (var entityManager = factory.createEntityManager()) {
                seed(entityManager);
            }
            SampleH2Database.logStatements(JDBC_URL);
            entityManagerFactory = factory;
        }
        return entityManagerFactory;
    }

    /**
     * Returns a data source for native SQL access to the database Hibernate
     * generated, as the admin user.
     *
     * @return the data source
     */
    public static DataSource dataSource() {
        entityManagerFactory();
        return SampleH2Database.dataSource(JDBC_URL,
                SampleH2Database.ADMIN_USER, SampleH2Database.ADMIN_PASSWORD);
    }

    private static void seed(EntityManager entityManager) {
        var transaction = entityManager.getTransaction();
        transaction.begin();
        for (var sale : SampleData.SALES) {
            entityManager.persist(new Sale(sale.id(), sale.month(),
                    sale.monthOrder(), sale.revenue(), sale.region()));
        }
        for (var employee : SampleData.EMPLOYEES) {
            entityManager.persist(new Employee(employee.id(), employee.name(),
                    employee.department(), employee.salary(), employee.age()));
        }
        for (var product : SampleData.PRODUCTS) {
            entityManager.persist(new Product(product.id(),
                    product.productName(), product.category(),
                    product.unitsSold(), product.price()));
        }
        for (var price : SampleData.STOCK_PRICES) {
            entityManager.persist(new StockPrice(price.id(), price.tradeDate(),
                    price.ticker(), price.openPrice(), price.highPrice(),
                    price.lowPrice(), price.closePrice(), price.volume()));
        }
        // Rows refer to earlier rows only, so the referenced entities are
        // already in the persistence context
        for (var task : SampleData.PROJECT_TASKS) {
            var dependsOn = task.dependsOn() == null ? null
                    : entityManager.find(ProjectTask.class, task.dependsOn());
            entityManager.persist(new ProjectTask(task.id(), task.taskName(),
                    task.phase(), task.startDate(), task.endDate(),
                    task.progress(), dependsOn));
        }
        for (var member : SampleData.ORG_CHART) {
            var manager = member.managerId() == null ? null
                    : entityManager.find(OrgMember.class, member.managerId());
            entityManager.persist(new OrgMember(member.id(), member.empName(),
                    member.jobTitle(), manager, member.teamColor()));
        }
        for (var flow : SampleData.ENERGY_FLOW) {
            entityManager.persist(new EnergyFlow(flow.id(), flow.sourceName(),
                    flow.targetName(), flow.amount()));
        }
        for (var cell : SampleData.WEBSITE_TRAFFIC) {
            entityManager.persist(new TrafficCell(cell.id(), cell.dayOfWeek(),
                    cell.hourOfDay(), cell.visitors()));
        }
        for (var item : SampleData.BUDGET_ITEMS) {
            entityManager.persist(new BudgetItem(item.id(), item.itemName(),
                    item.amount(), item.itemType()));
        }
        for (var stage : SampleData.SALES_PIPELINE) {
            entityManager.persist(new PipelineStage(stage.id(), stage.stage(),
                    stage.prospects(), stage.sortOrder()));
        }
        for (var metric : SampleData.KPI_METRICS) {
            entityManager.persist(new KpiMetric(metric.id(),
                    metric.metricName(), metric.currentVal(),
                    metric.targetVal(), metric.minVal(), metric.maxVal()));
        }
        var categories = new HashMap<String, ExpenseCategory>();
        for (var category : SampleData.EXPENSE_CATEGORIES) {
            var parent = category.parentCategory() == null ? null
                    : categories.get(category.parentCategory());
            var entity = new ExpenseCategory(category.id(),
                    category.categoryName(), parent, category.amount());
            entityManager.persist(entity);
            categories.put(category.categoryName(), entity);
        }
        transaction.commit();
    }
}
