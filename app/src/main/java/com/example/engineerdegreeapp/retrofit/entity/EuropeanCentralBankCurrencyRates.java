package com.example.engineerdegreeapp.retrofit.entity;

import java.util.HashMap;
import java.util.Map;

public class EuropeanCentralBankCurrencyRates {

    Map<String, Double> rates = new HashMap<>();

    public EuropeanCentralBankCurrencyRates() {
    }

    public EuropeanCentralBankCurrencyRates(Map<String, Double> rates) {
        this.rates = rates;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }
}
