This document addresses some notes from Step 3: Implement Missing Functionality

### Multi-criteria Trade Search Endpoint

Business terms:
Enables traders to search trades using multiple criteria (e.g., trade date, book, counterparty) rather than retrieving all trades or searching by trade ID only. This improves efficiency, allows more precise trade queries, and supports better decision-making in trade management.

Added functionality:
Implemented a new endpoint: /api/trades/search that accepts a TradeSearchCriteria object. Added service and repository-level logic to handle dynamic search queries based on multiple fields. This allows flexible, multi-criteria trade searches.

### Feature: Advanced Trade Search – /filter and /rsql Endpoints

Business terms: 
Allows users to dynamically filter and query trades with advanced search criteria. Traders can now perform paginated searches and use RSQL expressions to find specific trades, improving efficiency and enabling more precise trade analysis.

Added functionality:
- Added /filter endpoint for paginated, criteria-based trade searches.
- Added /rsql endpoint to support dynamic queries using RSQL syntax.
- Implemented CustomRsqlVisitor and GenericRsqlSpecBuilder to parse RSQL and convert queries into repository specifications, enabling flexible and reusable search logic.

### Feature: Comprehensive Trade Validation Service

Business terms:
Ensures that all new trades are thoroughly validated before creation, enforcing critical business rules such as date correctness, entity status, and trade leg integrity. This prevents invalid trades from entering the system, improving data quality and reducing operational risk.

Added functionality:
- Implemented a Trade Validation Service that performs:
- Date validation
- Entity status validation
- Trade leg validation
