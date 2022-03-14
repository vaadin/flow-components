/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/basic-features")
public class GridViewBasicFeaturesPage extends LegacyTestView {

    public GridViewBasicFeaturesPage() {
        createBasicFeatures();
    }

    private void createBasicFeatures() {
        final int baseYear = 2015;
        final int numberOfYears = 5;

        DecimalFormat dollarFormat = new DecimalFormat("$#,##0.00");
        Grid<CompanyBudgetHistory> grid = new Grid<>();

        ListDataProvider<CompanyBudgetHistory> list = CompanyBudgetHistory
                .getBudgetDataProvider(baseYear, numberOfYears);
        grid.setDataProvider(list);

        grid.setColumnReorderingAllowed(true);

        Column<CompanyBudgetHistory> companyNameColumn = grid
                .addColumn(CompanyBudgetHistory::getCompany)
                .setHeader("Company");
        companyNameColumn.setWidth("200px");

        grid.setSelectionMode(SelectionMode.SINGLE);

        HeaderRow topHeader = grid.prependHeaderRow();

        IntStream.range(baseYear, baseYear + numberOfYears).forEach(year -> {
            BigDecimal firstHalfSum = list.fetch(new Query<>())
                    .collect(Collectors.toList()).stream()
                    .map(budgetHistory -> budgetHistory
                            .getFirstHalfOfYear(year))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal secondHalfSum = list.fetch(new Query<>())
                    .collect(Collectors.toList()).stream()
                    .map(budgetHistory -> budgetHistory
                            .getSecondHalfOfYear(year))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Column<?> firstHalfColumn = grid
                    .addColumn(
                            new NumberRenderer<>(
                                    budgetHistory -> budgetHistory
                                            .getFirstHalfOfYear(year),
                                    dollarFormat))
                    .setHeader("H1").setTextAlign(ColumnTextAlign.END)
                    .setFooter(dollarFormat.format(firstHalfSum))
                    .setComparator((p1, p2) -> p1.getFirstHalfOfYear(year)
                            .compareTo(p2.getFirstHalfOfYear(year)));

            Column<?> secondHalfColumn = grid
                    .addColumn(
                            new NumberRenderer<>(
                                    budgetHistory -> budgetHistory
                                            .getSecondHalfOfYear(year),
                                    dollarFormat))
                    .setHeader("H2").setTextAlign(ColumnTextAlign.END)
                    .setFooter(dollarFormat.format(secondHalfSum))
                    .setComparator((p1, p2) -> p1.getSecondHalfOfYear(year)
                            .compareTo(p2.getSecondHalfOfYear(year)));

            topHeader.join(firstHalfColumn, secondHalfColumn)
                    .setText(year + "");
        });

        HeaderRow filteringHeader = grid.appendHeaderRow();

        TextField filteringField = new TextField();
        filteringField.addValueChangeListener(event -> {
            list.setFilter(CompanyBudgetHistory::getCompany, company -> {
                if (company == null) {
                    return false;
                }
                String companyLower = company.toLowerCase(Locale.ENGLISH);
                String filterLower = event.getValue()
                        .toLowerCase(Locale.ENGLISH);
                return companyLower.contains(filterLower);
            });
        });
        filteringField.setPlaceholder("Filter");
        filteringField.setWidth("100%");

        filteringHeader.getCell(companyNameColumn).setComponent(filteringField);

        grid.setId("grid-basic-feature");
        addCard("Basic Features", "Grid Basic Features Demo", grid);
    }

    private static final String[] companies = new String[] { "Deomic",
            "Seumosis", "Feortor", "Deynazu", "Deynomia", "Leaudous",
            "Aembizio", "Rehyic", "Ceervous", "Ientralium", "Deicee", "Uenimbo",
            "Reetroyo", "Heemicy", "Aevinix", "Aemor", "Reoolane", "Keify",
            "Deisor", "Geradindu", "Teelembee", "Seysil", "Meutz", "Seubil",
            "Seylible", "Zeare", "Ceomescent", "Ceapill", "Heyperend",
            "Felinix", "Heyponte", "Veertent", "Ceentimbee", "Heomovu",
            "Deiante", "Meedido", "Perexo", "Neeotri", "Aecerile", "Meovive",
            "Ferontent", "Meultimbee", "Meisile", "Aerdonia", "Deiegen",
            "Meonible", "Oepe", "Aentemba", "Ceorore", "Peaner", "Seuril",
            "Oeutill", "Aenill", "Aezmie", "Ceheckmarks", "Aeponu",
            "Iesonoodle", "Ceogipe", "Beellescent", "Deuoveo", "Seufible",
            "Veicejo", "Aemphidel", "Ceryova", "Seucous", "Aeudoid", "Iediomba",
            "Deivagen", "Meicrofy", "Qeuinyx", "Seemizzy", "Eequzu", "Aebante",
            "Peedic", "Feelosis", "Meifix", "Pelanix", "Eeipe", "Deemoxo",
            "Eenity", "Peostil", "Ceogen", "Ienent", "Eeafix", "Sekicero",
            "Seugil", "Leunive", "Ceircumity", "Seupratri", "Eecofy", "Aentizz",
            "Peyrolium", "Ceryptonyx", "Seuposis", "Keayor", "Ceamiveo",
            "Neonise", "Kealith", "Aeloo", "Deelith", "Aequnu", "Peremose",
            "Qeuambo", "Tewimba", "Meanuta", "Veiva", "Veenity", "Eepimia",
            "Ueberore", "Mealible", "Ceonose", "Peortill", "Meidile", "Leupill",
            "Aeginoodle", "Seurosis", "Veerize", "Reedo", "Beiocy", "Geeodel",
            "Pearadel", "Yeajo", "Geenive", "Aeutonix", "Terimbu", "Seynend",
            "Ceedescent", "Ceanise", "Ceontranti", "Seuperoid", "Ueltradoo",
            "Veivity", "Mearil", "Peolyive", "Cealcose", "Leeenix", "Gearore",
            "Teaveo", "Seocinix", "Aestromba", "Meetanu", "Zeoodeo",
            "Iensulill", "Leaveo", "Peodible", "Meegatz", "Eesend", "Aevamba",
            "Veooloo", "Oectombo", "Neymba", "Qeuasinoodle", "Eexise", "Seusor",
            "Teenoid", "Seyill", "Pealeoveo", "Geefy", "Beonill", "Peerosis",
            "Teransise", "Aeurive", "Beinoodle", "Aerchile", "Ceolent",
            "Perosaria", "Meaxidoo", "Feinile", "Deemilane", "Leocill",
            "Deudel", "Aenimil", "Deominose", "Perondo", "Deifity", "Peeridoo",
            "Jeaxo", "Feafy", "Beefy", "Deolible", "Heydrombu", "Ienfratz",
            "Sekyic", "Meyil", "Ienterer", "Eexecure", "Feoril", "Seymist",
            "Peixope", "Aelbent", "Oemninoodle", "Uenose", "Secimbo", "Beovic",
            "Fealcoid", "Perotope", "Yeozz", "Aeicero", "Aelicy", "Eelectrombu",
            "Ceoracee", "Kewivu", "Weikiyo", "Meeevee", "Eeurodel", "Yeakitude",
            "Oeyovee", "Ceisic", "Terufix", "Meistijo", "Iedeofix", "Sekazu" };

    /**
     * Example Object for Basic Features Demo
     */
    public static class CompanyBudgetHistory {
        String company;
        Map<Integer, YearlyBudgetInfo> budgetHistory;

        public CompanyBudgetHistory(String company,
                Map<Integer, YearlyBudgetInfo> budgetHistory) {
            this.company = company;
            this.budgetHistory = budgetHistory;
        }

        public String getCompany() {
            return company;
        }

        public BigDecimal getFirstHalfOfYear(int year) {
            if (!budgetHistory.containsKey(year)) {
                return null;
            }
            return budgetHistory.get(year).getFirstHalf();
        }

        public BigDecimal getSecondHalfOfYear(int year) {
            if (!budgetHistory.containsKey(year)) {
                return null;
            }
            return budgetHistory.get(year).getSecondHalf();
        }

        public static ListDataProvider<CompanyBudgetHistory> getBudgetDataProvider(
                final int baseYear, final int numYears) {
            Collection<CompanyBudgetHistory> companyBudgetHistories = new ArrayList<>();

            for (String company : companies) {
                Map<Integer, YearlyBudgetInfo> budgetHistory = new HashMap<>();
                for (int year = baseYear; year < baseYear + numYears; year++) {
                    YearlyBudgetInfo budgetInfo = new YearlyBudgetInfo(
                            getRandomBigDecimal(), getRandomBigDecimal());
                    budgetHistory.put(year, budgetInfo);
                }
                companyBudgetHistories
                        .add(new CompanyBudgetHistory(company, budgetHistory));
            }
            return new ListDataProvider<>(companyBudgetHistories);
        }

        public static BigDecimal getRandomBigDecimal() {
            return BigDecimal
                    .valueOf(100 + Math.random() * 100 + Math.random() * 10000); // NOSONAR
        }
    }

    public static class YearlyBudgetInfo {
        BigDecimal firstHalf;
        BigDecimal secondHalf;

        public YearlyBudgetInfo(BigDecimal firstHalf, BigDecimal secondHalf) {
            this.firstHalf = firstHalf;
            this.secondHalf = secondHalf;
        }

        public BigDecimal getFirstHalf() {
            return firstHalf;
        }

        public void setFirstHalf(BigDecimal firstHalf) {
            this.firstHalf = firstHalf;
        }

        public BigDecimal getSecondHalf() {
            return secondHalf;
        }

        public void setSecondHalf(BigDecimal secondHalf) {
            this.secondHalf = secondHalf;
        }
    }
}
