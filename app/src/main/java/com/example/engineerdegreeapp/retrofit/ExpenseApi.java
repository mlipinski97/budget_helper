package com.example.engineerdegreeapp.retrofit;

import com.example.engineerdegreeapp.retrofit.entity.Expense;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ExpenseApi {

    @GET("getallbybudgetlist")
    Call<List<Expense>> getExepnsesFromBudgetList(@Header("Authorization") String auth, @Query("id") Long id);

}
