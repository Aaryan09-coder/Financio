package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateInvestmentDTO;
import com.finpro.FinancePro.dto.Request.UpdateInvestmentDTO;
import com.finpro.FinancePro.dto.Response.InvestmentResponseDTO;
import com.finpro.FinancePro.dto.Response.StockQuoteDTO;
import com.finpro.FinancePro.exception.StockApiException;
import com.finpro.FinancePro.service.InvestmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/investments")
public class InvestmentController {

    @Autowired
    private InvestmentService investmentService;

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentResponseDTO> updateInvestment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvestmentDTO updateDTO) {
        InvestmentResponseDTO investment = investmentService.updateInvestment(id, updateDTO);
        return ResponseEntity.ok(investment);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InvestmentResponseDTO>> getUserInvestments(@PathVariable Long userId) {
        List<InvestmentResponseDTO> investments = investmentService.getUserInvestments(userId);
        return ResponseEntity.ok(investments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentResponseDTO> getInvestment(@PathVariable Long id) {
        InvestmentResponseDTO investment = investmentService.getInvestment(id);
        return ResponseEntity.ok(investment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvestment(@PathVariable Long id) {
        investmentService.deleteInvestment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/portfolio")
    public ResponseEntity<Map<String, Double>> getPortfolioPerformance(@PathVariable Long userId) {
        Map<String, Double> performance = investmentService.calculatePortfolioPerformance(userId);
        return ResponseEntity.ok(performance);
    }

    @PostMapping
    public ResponseEntity<?> createInvestment(@Valid @RequestBody CreateInvestmentDTO createDTO) {
        try {
            // Validate stock symbol first
            StockQuoteDTO quote = investmentService.getStockQuote(createDTO.getSymbol());
            if (quote == null) {
                return ResponseEntity.badRequest()
                        .body("Invalid stock symbol: " + createDTO.getSymbol());
            }

            InvestmentResponseDTO investment = investmentService.createInvestment(createDTO);
            return ResponseEntity.ok(investment);
        } catch (StockApiException e) {
            return ResponseEntity.status(503)
                    .body("Unable to verify stock symbol due to API issues: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("An error occurred while creating the investment: " + e.getMessage());
        }
    }

    @GetMapping("/stock/{symbol}")
    public ResponseEntity<?> getStockQuote(@PathVariable String symbol) {
        try {
            StockQuoteDTO quote = investmentService.getStockQuote(symbol);
            return ResponseEntity.ok(quote);
        } catch (StockApiException e) {
            return ResponseEntity.status(503)
                    .body("Error fetching stock quote: " + e.getMessage());
        }
    }
}
