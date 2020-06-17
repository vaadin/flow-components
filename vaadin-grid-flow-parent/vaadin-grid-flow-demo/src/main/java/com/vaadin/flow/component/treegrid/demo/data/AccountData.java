package com.vaadin.flow.component.treegrid.demo.data;

import com.vaadin.flow.component.treegrid.demo.entity.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountData {
    private static final List<Account> ACCOUNT_LIST = createAccountList();

    private static List<Account> createAccountList() {
        List<Account> accountList = new ArrayList<>();

        accountList.add(new Account("100", "Asset", null));
        accountList.add(
                new Account("101", "Bank/Cash at Bank", accountList.get(0)));
        accountList.add(new Account("102", "Cash", accountList.get(0)));
        accountList.add(
                new Account("108", "Deferred Expense", accountList.get(0)));
        accountList.add(new Account("110", "Other A52312", accountList.get(0)));
        accountList.add(
                new Account("112", "Accounts Receivable", accountList.get(0)));
        accountList.add(new Account("116", "Supplies", accountList.get(0)));
        accountList.add(
                new Account("130", "Prepaid Insurance", accountList.get(0)));
        accountList.add(new Account("157", "Equipment", accountList.get(0)));
        accountList.add(new Account("158", "Accumulated Depreciation Equipment",
                accountList.get(0)));
        accountList.add(new Account("200", "Liability", null));
        accountList
                .add(new Account("201", "Notes Payable", accountList.get(10)));
        accountList.add(
                new Account("202", "Accounts Payable", accountList.get(10)));
        accountList.add(new Account("209", "Unearned Service Revenue",
                accountList.get(10)));
        accountList.add(
                new Account("230", "Interest Payable", accountList.get(10)));
        accountList.add(new Account("231", "Deferred Gross profit",
                accountList.get(10)));
        accountList.add(new Account("300", "Equity", null));
        accountList.add(new Account("301",
                "Equity (for sole proprietorship and partnerships)",
                accountList.get(16)));
        accountList.add(
                new Account("3001", "Owner's capital", accountList.get(17)));
        accountList.add(new Account("3011", "Share Capital-Ordinary",
                accountList.get(17)));
        accountList.add(
                new Account("3020", "Retained Earnings", accountList.get(17)));
        accountList.add(new Account("3030", "Capital contributions",
                accountList.get(17)));
        accountList.add(new Account("3032", "Dividends", accountList.get(17)));
        accountList.add(
                new Account("3050", "Income Summary", accountList.get(17)));
        accountList.add(new Account("3060", "Drawings (Distributions)",
                accountList.get(17)));
        accountList.add(new Account("302", "Equity Accounts (for corporations)",
                accountList.get(16)));
        accountList.add(new Account("3001", "Dividend", accountList.get(25)));
        accountList.add(new Account("3010", "Capital in excess of par",
                accountList.get(25)));
        accountList.add(
                new Account("3030", "Retained earnings", accountList.get(25)));
        accountList.add(new Account("400", "Revenue", null));
        accountList
                .add(new Account("401", "Rental Income", accountList.get(29)));
        accountList
                .add(new Account("410", "Sales Income", accountList.get(29)));
        accountList.add(
                new Account("420", "Interest Income", accountList.get(29)));
        accountList
                .add(new Account("430", "Other Income", accountList.get(29)));
        accountList.add(new Account("500", "Expense", null));
        accountList
                .add(new Account("570", "Office Expense", accountList.get(34)));
        accountList.add(
                new Account("585", "Computer Expenses", accountList.get(34)));
        accountList.add(new Account("595", "Communication Expense",
                accountList.get(34)));
        accountList.add(new Account("597", "Labour & Welfare Expenses",
                accountList.get(34)));
        accountList.add(new Account("598", "Advertising Expenses",
                accountList.get(34)));
        accountList.add(new Account("599", "Printing & Stationery Expenses",
                accountList.get(34)));
        accountList.add(
                new Account("507", "Supplies Expense", accountList.get(34)));
        accountList.add(new Account("508", "Depreciation Expense",
                accountList.get(34)));
        accountList.add(
                new Account("509", "Insurance Expense", accountList.get(34)));
        accountList.add(new Account("510", "Salaries and Wages Expense",
                accountList.get(34)));
        accountList
                .add(new Account("511", "Rent Expense", accountList.get(34)));
        accountList.add(
                new Account("512", "Utilities Expense", accountList.get(34)));
        accountList.add(
                new Account("513", "Interest Expense", accountList.get(34)));

        return accountList;
    }

    public List<Account> getAccounts() {
        return ACCOUNT_LIST;
    }
}
