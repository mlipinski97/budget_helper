package com.example.engineerdegreeapp.retrofit;

import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.retrofit.entity.UserAuth;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface BudgetListApi {


    @GET("getallbyuser")
    Call<List<BudgetList>> getBudgetLists(@Header("Authorization") String auth, @Query("username") String username);
}
