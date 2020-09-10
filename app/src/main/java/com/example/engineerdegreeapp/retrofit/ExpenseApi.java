package com.example.engineerdegreeapp.retrofit;

import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.retrofit.entity.Expense;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ExpenseApi {

    @GET("getallbybudgetlist")
    Call<List<Expense>> getExpensesFromBudgetList(@Header("Authorization") String auth, @Query("id") Long id);

    @POST("add")
    Call<Expense> postExpense(@Header("Authorization") String auth,
                              @Query("budgetListId") Long budgetListId,
                              @Query("categoryName") String categoryName,
                              @Body RequestBody expenseBody);

    @PATCH("changedonestate")
    Call<Expense> patchDoneState(@Header("Authorization") String auth, @Query("id") Long id);

    @DELETE("deletebyid")
    Call<Void> deleteExpense(@Header("Authorization") String auth, @Query("id") Long id);

    @HTTP(method = "DELETE", path = "deletemany", hasBody = true)
    Call<Void> deleteManyExpenses(@Header("Authorization") String auth, @Body List<Long> idList);

    @GET("getmonthstatistics")
    Call<List<Expense>> getAllByDateAndExpenseOwner(@Header("Authorization") String auth,
                                                    @Query("startDate") String startDate,
                                                    @Query("endDate") String endDate,
                                                    @Query("username") String username);

}
