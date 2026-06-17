# product/insurance Calling Guide

## Scope

Reserved for insurance-like products.

Covered future product families:

- General insurance: life, annuities, health, disability, long-term care, property/homeowners, auto, flood, travel, cyber, commercial property/casualty, general liability, professional liability, directors-and-officers liability, workers compensation, reinsurance, and catastrophe risk products.
- Credit protection: mortgage insurance, trade credit insurance, credit life, credit disability, payment-protection insurance, financial guarantee or bond insurance, surety/performance/payment bonds, political/sovereign risk insurance, and deposit/resolution guarantees if institution risk is modeled.

Debt instruments remain in `product/liabilities/`. Credit assessment records remain in `product/liabilities/credit/`. Bilateral credit derivatives remain in a future `product/component/derivative/credit/` boundary unless implemented as an issued note or structured product.

## Unified Entry Points

| Feature | Entry point | Allowed callers | Side effects |
| --- | --- | --- | --- |
| Insurance product lifecycle | Status: reserved | Future product adapters, commands, or UI | To be defined with the first implementation |
| Insurance premium and claim signal extraction | Status: reserved | Product adapters and `market/financial` signal bridge | To be defined with the first implementation |

## Lifecycle

No runtime feature is implemented in this directory yet.

## Inputs

Future implementations must document insurer, policyholder, insured exposure, beneficiary, premium schedule, coverage limit, deductible, term, exclusion, claim trigger, reserve, reinsurance, risk config, and saved state.

## Outputs

Future implementations must document policies, claims, reserves, premiums, contract value, insurer solvency signals, and saved state.

## Ownership Rules

The first implementation should introduce a focused service or engine inside this package.

Insurance owns policies, premiums, claims, reserves, guarantees, insurer capacity, and reinsurance. Credit insurance may read institution-issued credit assessments, credit events, and referenced liability facts from `product/liabilities/`, but it must not own the liability instrument or invent its own borrower rating.

## Forbidden Bypass Calls

Do not put insurance payoff logic inside `market/financial/`, `product/component/structured/`, `product/liabilities/`, or company classes. Do not use this package to hide ordinary liability principal, interest, maturity, or collateral state.
