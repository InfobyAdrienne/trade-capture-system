package com.technicalchallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.mapper.TradeMapper;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.service.TradeSearchCriteria;
import com.technicalchallenge.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TradeController.class)
public class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;

    @MockBean
    private TradeMapper tradeMapper;

    private ObjectMapper objectMapper;
    private TradeDTO tradeDTO;
    private Trade trade;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create a sample TradeDTO for testing
        tradeDTO = new TradeDTO();
        tradeDTO.setTradeId(1001L);
        tradeDTO.setVersion(1);
        tradeDTO.setTradeDate(LocalDate.now()); // Fixed: LocalDate instead of LocalDateTime
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2)); // Fixed: correct method name
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(5)); // Fixed: correct method name
        tradeDTO.setTradeStatus("LIVE");
        tradeDTO.setBookName("TestBook");
        tradeDTO.setCounterpartyName("TestCounterparty");
        tradeDTO.setTraderUserName("TestTrader");
        tradeDTO.setInputterUserName("TestInputter");
        tradeDTO.setUtiCode("UTI123456789");

        // Create a sample Trade entity for testing
        trade = new Trade();
        trade.setId(1L);
        trade.setTradeId(1001L);
        trade.setVersion(1);
        trade.setTradeDate(LocalDate.now()); // Fixed: LocalDate instead of LocalDateTime
        trade.setTradeStartDate(LocalDate.now().plusDays(2)); // Fixed: correct method name
        trade.setTradeMaturityDate(LocalDate.now().plusYears(5)); // Fixed: correct method name

        // Set up default mappings
        when(tradeMapper.toDto(any(Trade.class))).thenReturn(tradeDTO);
        when(tradeMapper.toEntity(any(TradeDTO.class))).thenReturn(trade);
    }

    @Test
    void testGetAllTrades() throws Exception {
        // Given
        List<Trade> trades = List.of(trade); // Fixed: use List.of instead of Arrays.asList for single item

        when(tradeService.getAllTrades()).thenReturn(trades);

        // When/Then
        mockMvc.perform(get("/api/trades")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tradeId", is(1001)))
                .andExpect(jsonPath("$[0].bookName", is("TestBook")))
                .andExpect(jsonPath("$[0].counterpartyName", is("TestCounterparty")));

        verify(tradeService).getAllTrades();
    }

    @Test
    void testGetTradeById() throws Exception {
        // Given
        when(tradeService.getTradeById(1001L)).thenReturn(Optional.of(trade));

        // When/Then
        mockMvc.perform(get("/api/trades/1001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId", is(1001)))
                .andExpect(jsonPath("$.bookName", is("TestBook")))
                .andExpect(jsonPath("$.counterpartyName", is("TestCounterparty")));

        verify(tradeService).getTradeById(1001L);
    }

    @Test
    void testGetTradeByIdNotFound() throws Exception {
        // Given
        when(tradeService.getTradeById(9999L)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/trades/9999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(tradeService).getTradeById(9999L);
    }

    @Test
    void testGetTradesByCriteria() throws Exception {
        when(tradeService.getTradesByCriteria(any(TradeSearchCriteria.class)))
                .thenReturn(List.of(trade));

        // When/Then
        mockMvc.perform(get("/api/trades/search")
                .contentType(MediaType.APPLICATION_JSON)
                .param("counterpartyName", "TestCounterparty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].counterpartyName", is("TestCounterparty")));

        ArgumentCaptor<TradeSearchCriteria> cap = ArgumentCaptor.forClass(TradeSearchCriteria.class);
        verify(tradeService).getTradesByCriteria(cap.capture());
        assertEquals("TestCounterparty", cap.getValue().getCounterpartyName());
    }

    @Test
    void testGetTradesByCriteria_NoMatch() throws Exception {
            when(tradeService.getTradesByCriteria(any(TradeSearchCriteria.class)))
                            .thenReturn(List.of());

            // When/Then: send criteria as query param(s)
            mockMvc.perform(get("/api/trades/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("bookName", "NonExistentBook"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$", hasSize(0)));

            ArgumentCaptor<TradeSearchCriteria> cap = ArgumentCaptor.forClass(TradeSearchCriteria.class);
            verify(tradeService).getTradesByCriteria(cap.capture());
            assertEquals("NonExistentBook", cap.getValue().getBookName());
    }

    @Test
    void testGetTradesByFilter_Success() throws Exception {
        when(tradeService.getTradesByFilter(any(TradeSearchCriteria.class), any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(trade)));

        // When/Then
        mockMvc.perform(get("/api/trades/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .param("bookName", "TestBook")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].bookName", is("TestBook")));

        ArgumentCaptor<TradeSearchCriteria> capCriteria = ArgumentCaptor.forClass(TradeSearchCriteria.class);
        ArgumentCaptor<Pageable> capPageable = ArgumentCaptor.forClass(Pageable.class);
        verify(tradeService).getTradesByFilter(capCriteria.capture(), capPageable.capture());
        assertEquals("TestBook", capCriteria.getValue().getBookName());
        assertEquals(0, capPageable.getValue().getPageNumber());
        assertEquals(10, capPageable.getValue().getPageSize());
    }

    @Test
    void testGetTradesByFilter_NoMatch() throws Exception {
        // Stub the service
        when(tradeService.getTradesByFilter(any(TradeSearchCriteria.class), any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of())); // asserting that what's returned in the content is 0

        // When/Then: send criteria as query param(s)
        mockMvc.perform(get("/api/trades/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .param("bookName", "TestBook")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        ArgumentCaptor<TradeSearchCriteria> capCriteria = ArgumentCaptor.forClass(TradeSearchCriteria.class);
        ArgumentCaptor<Pageable> capPageable = ArgumentCaptor.forClass(Pageable.class);
        verify(tradeService).getTradesByFilter(capCriteria.capture(), capPageable.capture());
        // assertEquals("NonExistentCounterparty", capCriteria.getValue().getCounterpartyName());
        assertEquals(0, capPageable.getValue().getPageNumber());
        assertEquals(10, capPageable.getValue().getPageSize());
    }

    @Test
    void testGetTradesByRsql() throws Exception {
            // Given
            String rsqlQuery = "bookName==TestBook";
            when(tradeService.getTradesByRsql(rsqlQuery)).thenReturn(List.of(trade));

            // When/Then
            mockMvc.perform(get("/api/trades/rsql")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("query", rsqlQuery))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$", hasSize(1)))
                            .andExpect(jsonPath("$[0].bookName", is("TestBook")));

            verify(tradeService).getTradesByRsql(rsqlQuery);
    }

    @Test
    void testGetTradesByRsql_NoMatch() throws Exception {
        // Given
        String rsqlQuery = "bookName==NonExistentBook";
        when(tradeService.getTradesByRsql(rsqlQuery)).thenReturn(List.of()); 

        // When/Then
        mockMvc.perform(get("/api/trades/rsql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("query", rsqlQuery))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(0)));

        verify(tradeService).getTradesByRsql(rsqlQuery);
    }

    @Test
    void testCreateTrade() throws Exception {
        // Given
        when(tradeService.saveTrade(any(Trade.class), any(TradeDTO.class))).thenReturn(trade);
        doNothing().when(tradeService).populateReferenceDataByName(any(Trade.class), any(TradeDTO.class));

        // When/Then
        mockMvc.perform(post("/api/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tradeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tradeId", is(1001)));

        verify(tradeService).saveTrade(any(Trade.class), any(TradeDTO.class));
        verify(tradeService).populateReferenceDataByName(any(Trade.class), any(TradeDTO.class));
    }

    @Test
    void testCreateTradeValidationFailure_MissingTradeDate() throws Exception {
        // Given
        TradeDTO invalidDTO = new TradeDTO();
        invalidDTO.setBookName("TestBook");
        invalidDTO.setCounterpartyName("TestCounterparty");
        // Trade date is purposely missing

        // When/Then
        mockMvc.perform(post("/api/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Trade date is required"));

        verify(tradeService, never()).saveTrade(any(Trade.class), any(TradeDTO.class));
    }

    @Test
    void testCreateTradeValidationFailure_MissingBook() throws Exception {
        // Given
        TradeDTO invalidDTO = new TradeDTO();
        invalidDTO.setTradeDate(LocalDate.now());
        invalidDTO.setCounterpartyName("TestCounterparty");
        // Book name is purposely missing

        // When/Then
        mockMvc.perform(post("/api/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Book and Counterparty are required"));

        verify(tradeService, never()).saveTrade(any(Trade.class), any(TradeDTO.class));
    }

    @Test
    void testUpdateTrade() throws Exception {
        // Given
        Long tradeId = 1001L;
        tradeDTO.setTradeId(tradeId);
        when(tradeService.amendTrade(any(Long.class), any(TradeDTO.class))).thenReturn(trade);
        doNothing().when(tradeService).populateReferenceDataByName(any(Trade.class), any(TradeDTO.class));

        // When/Then
        mockMvc.perform(put("/api/trades/{id}", tradeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tradeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId", is(1001)));

        verify(tradeService).amendTrade(any(Long.class), any(TradeDTO.class));
    }

    @Test
    void testUpdateTradeIdMismatch() throws Exception {
        // Given
        Long pathId = 1001L;
        tradeDTO.setTradeId(2002L); // Different from path ID

        // When/Then
        mockMvc.perform(put("/api/trades/{id}", pathId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tradeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Trade ID in path must match Trade ID in request body"));

        verify(tradeService, never()).saveTrade(any(Trade.class), any(TradeDTO.class));
    }

    @Test
    void testDeleteTrade() throws Exception {
        // Given
        doNothing().when(tradeService).deleteTrade(1001L);

        // When/Then
        mockMvc.perform(delete("/api/trades/1001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(tradeService).deleteTrade(1001L);
    }

    @Test
    void testCreateTradeWithValidationErrors() throws Exception {
        // Given
        TradeDTO invalidDTO = new TradeDTO();
        invalidDTO.setTradeDate(LocalDate.now()); // Fixed: LocalDate instead of LocalDateTime
        // Missing required fields to trigger validation errors

        // When/Then
        mockMvc.perform(post("/api/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(tradeService, never()).createTrade(any(TradeDTO.class));
    }
}
