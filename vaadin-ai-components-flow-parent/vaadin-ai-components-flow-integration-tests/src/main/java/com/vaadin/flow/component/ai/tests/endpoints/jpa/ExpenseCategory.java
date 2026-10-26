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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * An expense category in a two-level hierarchy. The parent is referenced by its
 * unique name rather than by its id, as the legacy table does.
 */
@Entity
@Table(name = "expense_categories")
public class ExpenseCategory {

    @Id
    private Integer id;

    @Column(name = "category_name", length = 100, nullable = false, unique = true)
    private String categoryName;

    @ManyToOne
    @JoinColumn(name = "parent_category", referencedColumnName = "category_name")
    private ExpenseCategory parent;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @OneToMany(mappedBy = "parent")
    private List<ExpenseCategory> children = new ArrayList<>();

    protected ExpenseCategory() {
        // For JPA
    }

    public ExpenseCategory(int id, String categoryName, ExpenseCategory parent,
            BigDecimal amount) {
        this.id = id;
        this.categoryName = categoryName;
        this.parent = parent;
        this.amount = amount;
    }

    public Integer getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public ExpenseCategory getParent() {
        return parent;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public List<ExpenseCategory> getChildren() {
        return children;
    }
}
