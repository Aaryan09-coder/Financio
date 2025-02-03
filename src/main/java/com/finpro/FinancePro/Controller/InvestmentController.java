package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateInvestmentDTO;
import com.finpro.FinancePro.dto.Request.UpdateInvestmentDTO;
import com.finpro.FinancePro.dto.Response.InvestmentResponseDTO;
import com.finpro.FinancePro.dto.Response.StockQuoteDTO;
import com.finpro.FinancePro.exception.CustomAccessDeniedException;
import com.finpro.FinancePro.exception.StockApiException;
import com.finpro.FinancePro.security.SecurityUtils;
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

    @Autowired
    private SecurityUtils securityUtils;

    @PutMapping("/user/{userId}")
    public ResponseEntity<InvestmentResponseDTO> updateInvestment(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateInvestmentDTO updateDTO) {
        // Check if the current user is authorized to update this investment
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to update investments for this user");
        }

        InvestmentResponseDTO investment = investmentService.updateInvestment(userId, updateDTO);
        return ResponseEntity.ok(investment);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InvestmentResponseDTO>> getUserInvestments(@PathVariable Long userId) {
        // Check if the current user is authorized to view these investments
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to view investments for this user");
        }

        List<InvestmentResponseDTO> investments = investmentService.getUserInvestments(userId);
        return ResponseEntity.ok(investments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentResponseDTO> getInvestment(@PathVariable Long id) {
        InvestmentResponseDTO investment = investmentService.getInvestment(id);

        // Check if the current user is authorized to view this investment
        if (!SecurityUtils.isCurrentUserOrAdmin(investment.getUserId())) {
            throw new CustomAccessDeniedException("You are not authorized to view this investment");
        }

        return ResponseEntity.ok(investment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvestment(@PathVariable Long id) {
        // First get the investment to check ownership
        InvestmentResponseDTO investment = investmentService.getInvestment(id);

        // Check if the current user is authorized to delete this investment
        if (!SecurityUtils.isCurrentUserOrAdmin(investment.getUserId())) {
            throw new CustomAccessDeniedException("You are not authorized to delete this investment");
        }

        investmentService.deleteInvestment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/portfolio")
    public ResponseEntity<Map<String, Double>> getPortfolioPerformance(@PathVariable Long userId) {
        // Check if the current user is authorized to view this portfolio
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new CustomAccessDeniedException("You are not authorized to view portfolio performance for this user");
        }

        Map<String, Double> performance = investmentService.calculatePortfolioPerformance(userId);
        return ResponseEntity.ok(performance);
    }

    @PostMapping
    public ResponseEntity<?> createInvestment(@Valid @RequestBody CreateInvestmentDTO createDTO) {
        // Check if the current user is authorized to create investment for this user
        if (!SecurityUtils.isCurrentUserOrAdmin(createDTO.getUserId())) {
            throw new CustomAccessDeniedException("You are not authorized to create investments for this user");
        }

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
