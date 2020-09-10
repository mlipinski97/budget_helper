package com.example.engineerdegreeapp.util;

import android.util.ArraySet;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class CurrencyUtils {

    public static Set<Currency> getAllCurrencies() {
        Set<Currency> currencySet = new HashSet<Currency>();
        Locale[] locales = Locale.getAvailableLocales();

        for (Locale loc : locales) {
            try {
                Currency currency = Currency.getInstance(loc);

                if (currency != null) {
                    currencySet.add(currency);
                }
            } catch (Exception exc) {
                Log.d("getAllCurrencies()", "failed to get currency from Locale - " + loc.getDisplayCountry());
            }
        }

        return currencySet;
    }

    public static Set<String> getAllCurrencyCodes() {
        ArrayList<Currency> currencies = new ArrayList<>(CurrencyUtils.getAllCurrencies());
        return currencies.stream().map(Currency::getCurrencyCode).collect(Collectors.toCollection(ArraySet::new));
    }

    //gets all ISO currency codes and puts 7 most used currencies in the world, Polish zloty and locale currency + on top of the list
    public static ArrayList<String> getAllCurrencyCodesSortedByPopular() {
        ArrayList<String> sortedCurrencies = new ArrayList<>(getAllCurrencyCodes());
        sortedCurrencies.remove("JPY");
        sortedCurrencies.add(0, "JPY");
        sortedCurrencies.remove("CNY");
        sortedCurrencies.add(0, "CNY");
        sortedCurrencies.remove("CHF");
        sortedCurrencies.add(0, "CHF");
        sortedCurrencies.remove("CAD");
        sortedCurrencies.add(0, "CAD");
        sortedCurrencies.remove("GBP");
        sortedCurrencies.add(0, "GBP");
        sortedCurrencies.remove("PLN");
        sortedCurrencies.add(0, "PLN");
        sortedCurrencies.remove("USD");
        sortedCurrencies.add(0, "USD");
        sortedCurrencies.remove("EUR");
        sortedCurrencies.add(0, "EUR");

        sortedCurrencies.remove(getLocalCurrencyCode());
        sortedCurrencies.add(0, getLocalCurrencyCode());
        return sortedCurrencies;
    }

    public static String getLocalCurrencyCode() {
        Currency currency = Currency.getInstance(Locale.getDefault());
        Log.d("getLocalCurrencyCode()", "current local currency code: " + currency.getCurrencyCode());
        return currency.getCurrencyCode();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
