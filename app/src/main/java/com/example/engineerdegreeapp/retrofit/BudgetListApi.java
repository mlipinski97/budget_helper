package com.example.engineerdegreeapp.retrofit;

import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.retrofit.entity.Expense;
import com.example.engineerdegreeapp.retrofit.entity.StringResponse;

import java.util.ArrayList;
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

public interface BudgetListApi {

    @GET("getallbyuser")
    Call<List<BudgetList>> getBudgetLists(@Header("Authorization") String auth, @Query("username") String username);

    @POST("add")
    Call<BudgetList> postBudgetList(@Header("Authorization") String auth, @Body RequestBody budgetListBody);

    @PATCH("edit")
    Call<BudgetList> patchBudgetList(@Header("Authorization") String auth, @Query("id") Long id, @Body RequestBody budgetListBody);

    @DELETE("delete")
    Call<Void> deleteBudgetList(@Header("Authorization") String auth, @Query("id") Long id);

    @HTTP(method = "DELETE", path = "deletemany", hasBody = true)
    Call<Void> deleteManyBudgetLists(@Header("Authorization") String auth, @Body List<Long> idList);

    @HTTP(method = "DELETE", path = "revokemany", hasBody = true)
    Call<Void> revokeManyBudgetLists(@Header("Authorization") String auth, @Body List<String> usernameList, @Query("budgetListId") Long budgetListId);

    @HTTP(method = "PATCH", path = "sharemany", hasBody = true)
    Call<Void> shareManyBudgetLists(@Header("Authorization") String auth, @Body List<String> usernameList, @Query("budgetListId") Long budgetListId);

    @PATCH("share")
    Call<Void> shareBudgetList(@Header("Authorization") String auth, @Query("username") String username, @Query("budgetListId") Long budgetListId);

    @DELETE("revoke")
    Call<Void> revokeBudgetList(@Header("Authorization") String auth, @Query("username") String username, @Query("budgetListId") Long budgetListId);

    @GET("getearliestdateforuser")
    Call<StringResponse> getEarliestDateForUser(@Header("Authorization") String auth, @Query("username") String username);

}
