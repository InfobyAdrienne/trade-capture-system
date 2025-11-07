package com.technicalchallenge.service;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.Cashflow;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.TradeLeg;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CashflowRepository;
import com.technicalchallenge.repository.TradeLegRepository;
import com.technicalchallenge.repository.TradeRepository;
import com.technicalchallenge.repository.TradeStatusRepository;
import com.technicalchallenge.specifications.TradeSpecifications;

import io.cucumber.java.lu.a;
import io.micrometer.core.instrument.Counter;

import com.technicalchallenge.repository.CounterpartyRepository;
import com.technicalchallenge.repository.ApplicationUserRepository;
import com.technicalchallenge.model.Schedule;
import com.technicalchallenge.repository.ScheduleRepository;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.ApplicationUser;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeLegRepository tradeLegRepository;

    @Mock
    private CashflowRepository cashflowRepository;

    @Mock
    private TradeStatusRepository tradeStatusRepository;

    @Mock
    private AdditionalInfoService additionalInfoService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @InjectMocks
    private TradeService tradeService;

    @Mock
    private AuthorizationService authorizationService;

    private TradeDTO tradeDTO;
    private Trade trade;
    private Book book;
    private Counterparty counterparty;
    private ApplicationUser applicationUser;

    @BeforeEach
    void setUp() {
        // Set up test data
        tradeDTO = new TradeDTO();
        tradeDTO.setTradeId(100001L);
        tradeDTO.setTradeDate(LocalDate.of(2025, 10, 15));
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 10, 17));
        tradeDTO.setTradeMaturityDate(LocalDate.of(2026, 10, 17));
        tradeDTO.setTraderUserId(1L);

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(1000000));
        leg1.setRate(0.05);
        leg1.setMaturityDate(LocalDate.of(2026, 10, 17));

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(1000000));
        leg2.setRate(0.0);
        leg2.setMaturityDate(LocalDate.of(2026, 10, 17));

        tradeDTO.setTradeLegs(Arrays.asList(leg1, leg2));

        trade = new Trade();
        trade.setId(1L);
        trade.setTradeId(100001L);

        book = new Book();
        book.setId(1L);
        book.setActive(true);

        counterparty = new Counterparty();
        counterparty.setId(1L);
        counterparty.setActive(true);

        applicationUser = new ApplicationUser();
        applicationUser.setId(1L);
        applicationUser.setActive(true);
    }

    @Test
    void testCreateTrade_Success() {
        // Given
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        when(tradeLegRepository.save(any(TradeLeg.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(applicationUserRepository.findById(1L))
                .thenReturn(Optional.of(applicationUser));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(counterpartyRepository.findById(1L))
                .thenReturn(Optional.of(counterparty));

        when(tradeStatusRepository.findByTradeStatus("LIVE"))
                .thenReturn(Optional.of(new TradeStatus("LIVE")));

        tradeDTO.setBookId(1L);
        tradeDTO.setCounterpartyId(1L);
        tradeDTO.setTradeStatus("LIVE");

        when(authorizationService.validateUserPrivileges(1L, "CREATE_TRADE")).thenReturn(true);

        // When
        Trade result = tradeService.createTrade(tradeDTO);

        // Then
        assertNotNull(result);
        assertEquals(100001L, result.getTradeId());
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void testCreateTrade_InactiveBook_ShouldFail() {
        // Given
        book.setActive(false);

        when(applicationUserRepository.findById(1L))
                .thenReturn(Optional.of(applicationUser));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(counterpartyRepository.findById(1L))
                .thenReturn(Optional.of(counterparty));

        tradeDTO.setBookId(1L);
        tradeDTO.setCounterpartyId(1L);

        when(authorizationService.validateUserPrivileges(1L, "CREATE_TRADE")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        assertEquals("Trade validation failed: Book must be active.", exception.getMessage());
    }

    @Test
    void testCreateTrade_InvalidDates_ShouldFail() {
        // Given
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 10)); // Before tradeDate that is set in BeforeEach

        when(authorizationService.validateUserPrivileges(1L, "CREATE_TRADE")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        assertEquals("Start date cannot be before trade date", exception.getMessage());
    }

    @Test
    void testCreateTrade_InvalidLegCount_ShouldFail() {
        // Given
        tradeDTO.setTradeLegs(Arrays.asList(tradeDTO.getTradeLegs().get(0)));

        when(applicationUserRepository.findById(1L))
                .thenReturn(Optional.of(applicationUser));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(counterpartyRepository.findById(1L))
                .thenReturn(Optional.of(counterparty));

        tradeDTO.setBookId(1L);
        tradeDTO.setCounterpartyId(1L);

        when(authorizationService.validateUserPrivileges(1L, "CREATE_TRADE")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        // Then
        assertEquals("Trade must have exactly two legs", exception.getMessage());
    }

    @Test
    void testGetTradeById_Found() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));

        // When
        Optional<Trade> result = tradeService.getTradeById(100001L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(100001L, result.get().getTradeId());
    }

    @Test
    void testGetTradeById_NotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When
        Optional<Trade> result = tradeService.getTradeById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetTradesByCriteria() {
        // Given
        TradeSearchCriteria criteria = new TradeSearchCriteria();
        criteria.setCounterpartyName("TestCounterparty");

        // Stub repository call (you don't care what the spec looks like)
        when(tradeRepository.findAll(ArgumentMatchers.<Specification<Trade>>any()))
                .thenReturn(List.of(trade));

        // When
        List<Trade> result = tradeService.getTradesByCriteria(criteria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100001L, result.get(0).getTradeId());

        // Verify repository call
        verify(tradeRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testGetTradesByCriteria_NoMatch() {
        // Given
        TradeSearchCriteria criteria = new TradeSearchCriteria();
        criteria.setBookName("NonExistentBook");

        // Stub repository call to return empty list
        when(tradeRepository.findAll(ArgumentMatchers.<Specification<Trade>>any()))
                .thenReturn(List.of());

        // When
        List<Trade> result = tradeService.getTradesByCriteria(criteria);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify repository call
        verify(tradeRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testAmendTrade_Success() {
        // Given
        when(applicationUserRepository.findById(1L))
                .thenReturn(Optional.of(applicationUser));

        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));

        when(tradeRepository.save(any(Trade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(tradeStatusRepository.findByTradeStatus("AMENDED"))
                .thenAnswer(invocation -> Optional.of(new TradeStatus("AMENDED")));

        when(tradeLegRepository.save(any(TradeLeg.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // set version of the existing trade
        trade.setVersion(1);
        trade.setTradeStatus(new TradeStatus("LIVE"));
        trade.setTraderUser(applicationUser);

        when(authorizationService.validateUserPrivileges(1L, "AMEND_TRADE")).thenReturn(true);


        TradeStatus amendedStatus = new TradeStatus("AMENDED");
        when(tradeStatusRepository.findByTradeStatus("AMENDED"))
                .thenReturn(Optional.of(amendedStatus));

        when(authorizationService.validateUserPrivileges(1L, "AMEND_TRADE")).thenReturn(true);

        // When
        Trade result = tradeService.amendTrade(100001L, tradeDTO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getVersion());
        assertEquals("AMENDED", result.getTradeStatus().getTradeStatus());
        verify(tradeRepository, times(2)).save(any(Trade.class)); // Save old and new
    }

    @Test
    void testAmendTrade_TradeNotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.amendTrade(999L, tradeDTO);
        });

        assertTrue(exception.getMessage().contains("Trade not found"));
    }

    // This test has a deliberate bug for candidates to find and fix
    @Test
    void testCashflowGeneration_MonthlySchedule() {
        // Given - setup is incomplete
        when(tradeLegRepository.save(any(TradeLeg.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cashflowRepository.save(any(Cashflow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setSchedule("1M");

        when(scheduleRepository.findBySchedule("MONTHLY"))
                .thenReturn(Optional.of(schedule));

        TradeLegDTO leg1 = tradeDTO.getTradeLegs().get(0);
        leg1.setCalculationPeriodSchedule("MONTHLY");
        leg1.setRate(0.05);

        tradeDTO.setTradeLegs(Arrays.asList(leg1));

        tradeService.createTradeLegsWithCashflows(tradeDTO, trade);

        // Capture saved cashflows
        ArgumentCaptor<Cashflow> captor = ArgumentCaptor.forClass(Cashflow.class);
        verify(cashflowRepository, atLeastOnce()).save(captor.capture());

        List<Cashflow> saved = captor.getAllValues();

        assertEquals(12, saved.size());
    }
}
