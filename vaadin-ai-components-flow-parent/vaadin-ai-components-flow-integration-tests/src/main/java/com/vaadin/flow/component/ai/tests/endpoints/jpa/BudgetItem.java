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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A line of the budget; {@code itemType} is {@code "intermediate"} or
 * {@code "sum"} for computed lines and {@code null} otherwise.
 */
@Entity
@Table(name = "budget_items")
public class BudgetItem {

    @Id
    private Integer id;

    @Column(name = "item_name", length = 100, nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int amount;

    @Column(name = "item_type", length = 20)
    private String itemType;

    protected BudgetItem() {
        // For JPA
    }

    public BudgetItem(int id, String itemName, int amount, String itemType) {
        this.id = id;
        this.itemName = itemName;
        this.amount = amount;
        this.itemType = itemType;
    }

    public Integer getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public int getAmount() {
        return amount;
    }

    public String getItemType() {
        return itemType;
    }
}
