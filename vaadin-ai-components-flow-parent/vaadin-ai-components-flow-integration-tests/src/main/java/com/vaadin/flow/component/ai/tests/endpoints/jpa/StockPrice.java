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
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Daily open, high, low and close prices of a ticker. */
@Entity
@Table(name = "stock_prices")
public class StockPrice {

    @Id
    private Integer id;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(length = 10, nullable = false)
    private String ticker;

    @Column(name = "open_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal lowPrice;

    @Column(name = "close_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal closePrice;

    @Column(nullable = false)
    private int volume;

    protected StockPrice() {
        // For JPA
    }

    public StockPrice(int id, LocalDate tradeDate, String ticker,
            BigDecimal openPrice, BigDecimal highPrice, BigDecimal lowPrice,
            BigDecimal closePrice, int volume) {
        this.id = id;
        this.tradeDate = tradeDate;
        this.ticker = ticker;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
    }

    public Integer getId() {
        return id;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public String getTicker() {
        return ticker;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public int getVolume() {
        return volume;
    }
}
