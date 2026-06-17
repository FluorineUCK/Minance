package com.fluorineuck.minance.entity.company;

record CompanyAccountingSnapshot(
        CompanyOperatingStatement operatingStatement,
        CompanyBalanceSheet balanceSheet,
        CompanyFinancialMetrics metrics
) {
    static CompanyAccountingSnapshot fromReportInputs(long openingFunds, long periodIncome, long closingFunds, int totalShares, CompanyFinancialReport previous) {
        CompanyOperatingStatement operating = CompanyOperatingStatement.fromLegacy(openingFunds, periodIncome, closingFunds);
        CompanyBalanceSheet balance = CompanyBalanceSheet.fromLegacy(closingFunds, totalShares);
        CompanyFinancialMetrics metrics = CompanyFinancialMetrics.fromStatements(operating, balance, previous);
        return new CompanyAccountingSnapshot(operating, balance, metrics);
    }
}
