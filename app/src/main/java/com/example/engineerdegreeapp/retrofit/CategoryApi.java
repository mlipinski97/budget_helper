package com.example.engineerdegreeapp.retrofit;

import com.example.engineerdegreeapp.retrofit.entity.Category;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


public interface CategoryApi {

    @GET("getall")
    Call<List<Category>> getAllCategories(@Header("Authorization") String auth);

    @Multipart
    @POST("add")
    Call<Category> postCategory(@Header("Authorization") String auth, @Query("categoryName") String categoryName, @Part MultipartBody.Part categoryImage);

    @Multipart
    @PATCH("edit")
    Call<Category> editCategory(@Header("Authorization") String auth,
                                @Query("oldCategoryName") String oldCategoryName,
                                @Query("newCategoryName") String newCategoryName,
                                @Part MultipartBody.Part categoryImage);

    @DELETE("delete")
    Call<Void> deleteBudgetList(@Header("Authorization") String auth, @Query("categoryName") String categoryName);
    
}
