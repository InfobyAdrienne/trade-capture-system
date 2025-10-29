package com.technicalchallenge.service;

import java.time.LocalDate;

public class TradeSearchCriteria {

    // Counterparty filters
    private String counterpartyName;
    private Long counterpartyId;

    // Book filters
    private String bookName;
    private Long bookId;

    // Trader filters
    private Long traderUserId;
    private String traderUserName;

    // Trade inputter filters
    private Long tradeInputterUserId;
    private String inputterUserName;

    // Status filters
    private Long tradeStatusId;
    private String tradeStatus;

    // Date range filters
    private LocalDate tradeDate;
    private LocalDate maturityDate;

    // Getters and Setters
    public String getCounterpartyName() { return counterpartyName; }
    public void setCounterpartyName(String counterpartyName) { this.counterpartyName = counterpartyName; }

    public Long getCounterpartyId() { return counterpartyId; }
    public void setCounterpartyId(Long counterpartyId) { this.counterpartyId = counterpartyId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Long getTraderUserId() { return traderUserId; }
    public void setTraderUserId(Long traderUserId) { this.traderUserId = traderUserId; }

    public String getTraderUserName() { return traderUserName; }
    public void setTraderUserName(String traderUserName) { this.traderUserName = traderUserName; }

    public Long getTradeStatusId() { return tradeStatusId; }
    public void setTradeStatusId(Long tradeStatusId) { this.tradeStatusId = tradeStatusId; }

    public String getTradeStatus() { return tradeStatus; }
    public void setTradeStatus(String tradeStatus) { this.tradeStatus = tradeStatus; }

    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }

    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }
}
