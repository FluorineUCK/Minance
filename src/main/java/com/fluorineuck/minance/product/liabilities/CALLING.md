# product/liabilities Calling Guide

## Scope

Reserved for debt, liabilities, borrower obligations, loans, deposit/funding liabilities, debt securities, securitized credit references, principal, interest, maturity, collateral, institution-issued credit assessments, recovery, and credit spread inputs.

Covered future product families:

- Operating liabilities when they become explicit financeable/defaultable products.
- Loan products: term loans, revolvers, credit lines, mortgages, commercial real estate loans, consumer loans, margin loans, bridge loans, and asset-based loans.
- Trade and receivable finance: trade credit, factoring, invoice finance, inventory finance, equipment finance, and supply-chain finance.
- Debt securities: corporate bonds, municipal bonds, Treasury/agency securities, commercial paper, notes, convertible debt, callable/putable debt, secured/unsecured debt, senior/subordinated debt, and hybrid debt.
- Securitized credit references: ABS, MBS, CMO, CLO, CDO, receivables-backed pools, and loan pools.
- Credit assessment records: institution-issued ratings, credit limits, spread quotes, underwriting decisions, and credit event records. These are assessment relationships, not entity tags.
- Deposit products: demand/checking/current accounts, savings deposits, money-market deposit accounts, time deposits, certificates of deposit, negotiable CDs, notice deposits, call deposits, brokered deposits, wholesale deposits, interbank deposits, escrow/restricted deposits, and insured/uninsured deposit balances.
- Funding liabilities: deposit funding, repo funding, securities-lending cash collateral, interbank borrowing, margin obligations, redemption obligations, and settlement obligations.

Credit derivatives and guarantees are not owned here unless they are represented as cash debt obligations. Bilateral credit derivatives belong in a future `product/component/derivative/credit/` package; insurance/guarantee contracts belong in `product/insurance/`.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Liability product lifecycle | Status: reserved | Future company, structured product, fund, credit, market, command, or UI adapters | To be defined with the first implementation |
| Credit assessment lifecycle | Status: reserved | Future bank/securities/rating institution adapters, product adapters, commands, or UI | To be defined when `product/liabilities/credit/` is created |
| Credit signal extraction | Status: reserved | Product adapters and `market/financial` signal bridge | To be defined when credit assessments exist |
| Deposit lifecycle and rate accrual | Status: reserved | Future institution, market, command, or UI adapters | To be defined when `product/liabilities/deposit/` is created |
| Deposit funding-flow signal extraction | Status: reserved | Institution liquidity adapters and `market/financial` signal bridge | To be defined when `product/liabilities/deposit/` is created |

## Lifecycle

No runtime feature is implemented in this directory yet.

## Inputs

Future implementations must document issuer/borrower, holder/lender, principal, coupon or interest rate, maturity, amortization, seniority, collateral, covenants, credit config, and saved state.

Credit assessment implementations must document assessing institution, subject id, subject type, assessment scope, optional product/facility/tranche id, rating scale, rating grade, outlook/watch state, confidence, credit limit, spread quote, implied default probability, recovery assumption, methodology id, valid-from tick, valid-until tick, and saved state.

Deposit implementations must document depository institution, depositor/account owner, deposit category, balance, posted deposit rate, effective annual yield, rate type, rate index/spread, compounding cadence, interest payment cadence, maturity if any, notice period, early-withdrawal penalty, minimum balance, balance tier, fee schedule, insurance coverage, restricted/escrow state, rollover setting, and saved state.

## Outputs

Future implementations must document liability state, price/yield inputs, credit spread, default probability, recovery estimate, default pressure, credit signals, and saved state.

Credit assessment implementations must document ratings, limits, quotes, credit events, assessment validity, institution-specific methodology output, normalized signal output, and saved state.

Deposit implementations must document deposit balance, accrued interest, withdrawal value after penalties, effective yield, liquidity/run-risk signal, insured/uninsured amount, institution funding-cost signal, and saved state.

## Ownership Rules

Debt and credit product state belongs here. Company solvency facts may originate in `entity/company/`, but final ratings, credit limits, spread quotes, and underwriting decisions belong in `product/liabilities/credit/` as institution-issued records.

Bond funds, credit ETFs, and loan funds belong in `product/component/fund/` while referencing liability holdings. Structured credit notes and tranched payoff waterfalls belong in `product/component/structured/`. Credit derivatives belong in a future `product/component/derivative/credit/` boundary and should reference a credit assessment, spread curve, reference entity, reference obligation, or credit event record.

Deposit products belong under a future `product/liabilities/deposit/` subdirectory when implemented. They are institution funding liabilities and depositor cash-like assets. They should emit funding-cost, liquidity, and run-risk signals, but should not be treated as ordinary market-priced debt unless the specific product is transferable, such as a negotiable CD.

## Forbidden Bypass Calls

Do not hide debt, deposit, or credit assessment state in company reports or generic market state when it should be a liability product. Do not derive final credit from entity tags or generic component attributes. Do not place premium/claim/reserve logic, insurance guarantees, or CDS-style derivative settlement in this package. Do not model deposit rates as generic market price deltas; keep rate setting, accrual, withdrawal rules, and funding-flow signals explicit.
