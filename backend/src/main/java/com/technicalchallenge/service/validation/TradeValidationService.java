package com.technicalchallenge.service.validation;

import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.repository.ApplicationUserRepository;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CounterpartyRepository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TradeValidationService {
    @Autowired
    public TradeValidationService(CounterpartyRepository counterpartyRepository,
            BookRepository bookRepository,
            ApplicationUserRepository applicationUserRepository) {
        this.counterpartyRepository = counterpartyRepository;
        this.bookRepository = bookRepository;
        this.userRepository = applicationUserRepository;
    }

    private final CounterpartyRepository counterpartyRepository;
    private final BookRepository bookRepository;
    private final ApplicationUserRepository userRepository;

    public ValidationResult validateTradeBusinessRules(TradeDTO tradeDTO) {
        ValidationResult result = new ValidationResult();

        // Date rules
        if (tradeDTO.getTradeStartDate() != null && tradeDTO.getTradeDate() != null) {
            if (tradeDTO.getTradeStartDate().isBefore(tradeDTO.getTradeDate())) {
                throw new RuntimeException("Start date cannot be before trade date");
            }
        }

        if (tradeDTO.getTradeMaturityDate() != null && tradeDTO.getTradeStartDate() != null) {
            if (tradeDTO.getTradeMaturityDate().isBefore(tradeDTO.getTradeStartDate())) {
                throw new RuntimeException("Maturity date cannot be before start date");
            }
        }

        if (tradeDTO.getTradeDate() != null) {
            LocalDate now = LocalDate.now();
            if (tradeDTO.getTradeDate().isBefore(now.minusDays(30))) {
                throw new RuntimeException("Trade date cannot be more than 30 days in the past");
            }
        }

        // --- Entity status rules ---
        if (tradeDTO.getCounterpartyId() != null) {
            boolean counterpartyActive = counterpartyRepository
                    .findById(tradeDTO.getCounterpartyId())
                    .map(cp -> cp.isActive())
                    .orElse(false);
            if (!counterpartyActive) {
                result.addError("Counterparty must be active.");
            }
        } else {
            result.addError("Counterparty ID is required.");
        }

        if (tradeDTO.getBookId() != null) {
            boolean bookActive = bookRepository
                    .findById(tradeDTO.getBookId())
                    .map(book -> book.isActive())
                    .orElse(false);
            if (!bookActive) {
                result.addError("Book must be active.");
            }
        } else {
            result.addError("Book ID is required.");
        }

        if (tradeDTO.getTraderUserId() != null) {
            boolean userActive = userRepository
                    .findById(tradeDTO.getTraderUserId())
                    .map(user -> user.isActive())
                    .orElse(false);
            if (!userActive) {
                result.addError("Trader user must be active.");
            }
        } else {
            result.addError("Trader User ID is required.");
        }
        return result;
    }
    
    public ValidationResult validateTradeLegConsistency(List<TradeLegDTO> legs) {
        ValidationResult result = new ValidationResult();

        // Trade must have exactly 2 legs
        if (legs == null || legs.size() != 2) {
            throw new RuntimeException("Trade must have exactly two legs");
        }

        // Both legs must have identical maturity dates
        TradeLegDTO leg1 = legs.get(0);
        TradeLegDTO leg2 = legs.get(1);

        if (!leg1.getMaturityDate().equals(leg2.getMaturityDate())) {
            throw new RuntimeException("Both legs must have identical maturity dates");
        }

        // Legs must have opposite pay/receive flags
        if (leg1.getPayReceiveFlag() != null && leg2.getPayReceiveFlag() != null) {
            if (leg1.getPayReceiveFlag().equals(leg2.getPayReceiveFlag())) {
                throw new RuntimeException("Legs must have opposite pay/receive flags");
            }
        }

        // Floating legs must have an index specified + Fixed legs must have a valid rate
        for (TradeLegDTO leg : legs) {
            if (leg.getLegType() != null && leg.getLegType().equalsIgnoreCase("Floating")) {
                if (leg.getIndexId() == null) {
                    throw new RuntimeException("Floating legs must have an index specified");
                }
            }
            if (leg.getLegType() != null && leg.getLegType().equalsIgnoreCase("Fixed")) {
                if (leg.getRate() == null || leg.getRate() <= 0) {
                    throw new RuntimeException("Fixed legs must have a valid positive rate");
                }
            }
        }
        return result;
    }
}
