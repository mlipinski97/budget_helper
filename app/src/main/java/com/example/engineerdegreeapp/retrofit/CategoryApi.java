package com.example.engineerdegreeapp.retrofit;

import com.example.engineerdegreeapp.retrofit.entity.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface CategoryApi {

    @GET("getall")
    Call<List<Category>> getAllCategories(@Header("Authorization") String auth);
}
