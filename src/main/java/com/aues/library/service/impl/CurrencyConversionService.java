package com.aues.library.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CurrencyConversionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "https://api.exchangerate-api.com/v4/latest/KZT"; // Измените на реальный URL API

    public BigDecimal convertToUSD(BigDecimal amountInKZT) {
        BigDecimal exchangeRate = getExchangeRate("USD");
        return amountInKZT.multiply(exchangeRate);
    }

    private BigDecimal getExchangeRate(String targetCurrency) {
        ResponseEntity<Map> response = restTemplate.getForEntity(apiUrl, Map.class);
        Map<String, Object> rates = (Map<String, Object>) response.getBody().get("rates");
        return new BigDecimal(rates.get(targetCurrency).toString());
    }
}

