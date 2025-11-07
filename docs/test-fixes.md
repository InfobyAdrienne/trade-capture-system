
This document addresses some notes from Step 2: Fix Failing Test Cases

## Trade Controller

### Test fix 1

`TradeControllerTest.testCreateTrade:138 Status expected:<200> but was:<201>` 

**Business terms:** This test verifies that when a new trade is created via the API (POST /api/trades) in the Controller, the system responds correctly by confirming successful creation of a new trade record. In business terms, it ensures that users or other systems receive proper confirmation when submitting a new trade, aligning with expected REST API behavior for resource creation.

**Fix:** The test incorrectly assumed the endpoint should return HTTP 200 (OK) on success. However, the controller’s logic dictate that HTTP 201 (Created) is the correct response when a new resource is successfully created. Therefore, the test expectation was inconsistent with the actual, correct API design.

### Test fix 2 
`TradeControllerTest.testCreateTradeValidationFailure_MissingBook:176 Response content expected:<Book and Counterparty are required> but was:<>`

**Business terms:** This test ensures that the API correctly rejects invalid trade creation requests specifically when required fields such as the book name are missing. From a business perspective, it enforces data integrity by preventing incomplete or invalid trades from being created, ensuring all trades are associated with a valid book.

**Fix:** Added @Valid to TradeDTO in the controller which activates bean validation on incoming trade requests. Introduced a global exception handler to capture and format validation errors. This ensures that when validation fails, the client receives a 400 Bad Request along with a clear, human-readable error message explaining the issue.

### Test fix 3
`TradeControllerTest.testDeleteTrade:223 Status expected:<204> but was:<200>`

**Business terms:** This test verifies that when a trade is successfully deleted via the API (DELETE /api/trades/{id}), the system responds with the correct HTTP status code. In business terms, it confirms that when a trade record is removed, the API signals successful deletion without returning any content, following REST best practices.

**Fix:** The test now passes, and the deleteTrade endpoint adheres to proper REST conventions by returning HTTP 204 after deletion. This improves API consistency and clarifies that no content is returned upon successful deletion.

### Test fix 4
`TradeControllerTest.testUpdateTrade:194 No value at JSON path "$. tradeId"`

**Business terms:** This test ensures that the API correctly updates an existing trade when a client submits a modification request. In business terms, it validates the trade amendment workflow by confirming that the system properly handles updates to existing trades rather than creating new ones. 

**Fix:** Instead of calling the method designed for updating trades (amendTrade), the test was calling the save method (saveTrade), leading to incorrect behaviour and a failed test. The test was corrected to invoke the amendTrade method in the TradeService, ensuring that the update logic is properly executed. This change aligns the test with the intended functionality of updating existing trades.

### Test fix 5
`TradeControllerTest.testUpdateTradeIdMismatch:209 Status expected:<400> but was:<200>`

**Business terms:** testUpdateTradeIdMismatch was failing because the endpoint returned HTTP 200 (OK) even when the trade ID in the request body did not match the ID specified in the path variable. The test expected a HTTP 400 (Bad Request) status to indicate invalid input. The controller’s updateTrade() method lacked validation to compare the path variable id and the tradeId inside the request body (TradeDTO).
As a result, the system accepted mismatched IDs and proceeded with the update, allowing incorrect trade records to be modified.

**Fix:** A validation check was added in the updateTrade() method to enforce ID consistency. This ensures that if the IDs differ, the controller immediately responds with HTTP 400 (Bad Request) and an explanatory message.
  

## Trade Service

### Test fix 1
`TradeServiceTest.testCashflowGeneration MonthlySchedule:181 expected: <l> but was: <12>`

**Business terms:** Validates that trades with a monthly payment schedule correctly generate 12 monthly cashflows over a one-year period. In business terms, it confirms the system’s ability to accurately project recurring cashflows for trades following a monthly schedule — essential for financial forecasting and settlement accuracy.

**Fix:** Added proper repository mocking for trade legs, cashflows, and schedules; configured a trade leg with a "MONTHLY" schedule; and updated assertions to verify that 12 cashflows are produced for a 1-year period.

### Test fix 2

`TradeServiceTest.testCreateTrade_Success:80 » Runtime Book not found or not set`
**Business terms:** Validates that a trade can be successfully created when all required data (book, counterparty, trade status, trade leg) is provided. In business terms, it ensures that the system can process a valid trade submission end-to-end, creating all necessary records without errors.

**Fix:** Added proper mock setup for repositories (book, counterparty, trade status) and created a trade leg. Initialized runtime data in tradeDTO so that repository calls return valid data, preventing NullPointerException and allowing the test to correctly verify successful trade creation.

### Test fix 3

`TradeServiceTest.testAmendTrade_Success:148 » NullPointer Cannot invoke "java.lang.Integer.intValue()" because the return value of "com.technicalchallenge.model.Trade.getVersion() is null" `

**Business terms:** Validates that an existing trade can be successfully amended with the correct version increment and a valid trade status. In business terms, it ensures that trade updates maintain proper versioning and status tracking, which is essential for trade lifecycle management and auditability.

**Fix:** Updated the TradeStatus constructor to correctly initialize the tradeStatus field. Adjusted test mocks so repository saves return the actual trade object with the incremented version. This prevents NullPointerException and allows the test to correctly validate that the amended trade has the proper version and status.

## Book Service

### Test fix 1

`BookServiceTest.testFindBookById:29 » NullPointerException`

**Business terms:** Validates that the system can retrieve a book by its ID correctly. In business terms, it ensures that clients or other services can reliably fetch book data, which is critical for trade and reporting operations that depend on accurate book information.

**Fix:** Added a mock for BookMapper and provided a mapping stub. Included setup in @BeforeEach to initialize the mocks properly. This prevents NullPointerException and allows the test to successfully validate getBookById() behavior.

### Test fix 2

`BookServiceTest.testSaveBook:42 » NullPointer Cannot invoke "com.technicalchallenge.mapper.BookMapper. toEntity(com.technicalchallenge.dto.BookDTO)" because "<local3>.bookMapper" is null
TradeServiceTest. testAmendTrade_Success:148 » `

**Business terms:** Validates that a book can be saved correctly and returned as a DTO. In business terms, it ensures that the system correctly handles book creation or updates, converting between the entity and DTO for consistent data handling and API responses.

**Fix:** Added stubs for bookMapper.toEntity() and bookMapper.toDto() methods. This prevents NullPointerException and allows the saveBook test to pass, verifying that entity-to-DTO mapping works correctly during book persistence.