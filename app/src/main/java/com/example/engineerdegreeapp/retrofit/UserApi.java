package com.example.engineerdegreeapp.retrofit;

import com.example.engineerdegreeapp.retrofit.entity.Friendship;
import com.example.engineerdegreeapp.retrofit.entity.UserAuth;

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

public interface UserApi {

    @GET("getall")
    Call<List<UserAuth>> getUser(@Header("Authorization") String auth);

    @POST("register")
    Call<UserAuth> registerUser(@Body RequestBody userBody);

    @GET("account")
    Call<UserAuth> getAccount(@Header("Authorization") String auth);

    @GET("getallfriends")
    Call<List<Friendship>> getFriendships(@Header("Authorization") String auth);

    @DELETE("delete")
    Call<Void> deleteFriendship(@Header("Authorization") String auth, @Query("friendUsername") String friendUsername);

    @HTTP(method = "DELETE", path = "deletemany", hasBody = true)
    Call<Void> deleteManyFriendships(@Header("Authorization") String auth, @Body List<String> friendsUsernameList);

    @PATCH("accept")
    Call<Friendship> acceptFriendship(@Header("Authorization") String auth, @Query("requesterUsername") String requesterUsername);

    @POST("add")
    Call<Friendship> addFriendship(@Header("Authorization") String auth, @Query("friendUsername") String friendUsername);

    @GET("getallbybudgetlistid")
    Call<List<UserAuth>> getAllFriendsByBudgetListId(@Header("Authorization") String auth, @Query("budgetListId") Long budgetListId);
}
