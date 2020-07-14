package com.example.engineerdegreeapp.retrofit;

import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.retrofit.entity.Expense;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ExpenseApi {

    @GET("getallbybudgetlist")
    Call<List<Expense>> getExepnsesFromBudgetList(@Header("Authorization") String auth, @Query("id") Long id);

    @POST("add")
    Call<Expense> postExpense(@Header("Authorization") String auth, @Query("budgetListId") Long budgetListId, @Body RequestBody expenseBody);

}
