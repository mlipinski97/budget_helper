package com.example.engineerdegreeapp.retrofit;

import com.example.engineerdegreeapp.retrofit.entity.Category;
import com.example.engineerdegreeapp.retrofit.entity.EuropeanCentralBankCurrencyRates;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

//API site: https://exchangeratesapi.io/
public interface ExchangeRatesApi {

    @GET("latest")
    Call<EuropeanCentralBankCurrencyRates> getRatesForCurrency(@Query("base") String baseCurrencyCode);

}
