package com.example.engineerdegreeapp.retrofit;

import com.example.engineerdegreeapp.retrofit.entity.Friendship;
import com.example.engineerdegreeapp.retrofit.entity.UserAuth;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
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

    @PATCH("accept")
    Call<Friendship> acceptFriendship(@Header("Authorization") String auth, @Query("requesterUsername") String requesterUsername);
}
