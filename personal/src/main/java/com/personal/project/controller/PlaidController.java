package com.personal.project.controller;

import com.personal.project.dto.PlaidExchangeRequest;
import com.personal.project.dto.PlaidLinkTokenRequest;
import com.personal.project.service.PlaidService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/plaid")
public class PlaidController {
    private final PlaidService plaidService;

    public PlaidController(PlaidService plaidService) {
        this.plaidService = plaidService;
    }

    @PostMapping("/link-token")
    public Map<String, Object> createLinkToken(@RequestBody PlaidLinkTokenRequest request) {
        return plaidService.createLinkToken(request.getUserId());
    }

    @PostMapping("/exchange-public-token")
    public Map<String, Object> exchangePublicToken(@RequestBody PlaidExchangeRequest request) {
        return plaidService.exchangePublicToken(request.getPublicToken(), request.getUserId());
    }

    @GetMapping("/transactions")
    public Map<String, Object> getTransactions(
            @RequestParam String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return plaidService.getTransactions(userId, startDate, endDate);
    }
}
