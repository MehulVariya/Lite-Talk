package com.group_project.chatapplication.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("get?bid=180965&key=F0VJOeYqDj57U6ad&uid=[uid]")
    Call<ChatBotResponse> getChatBotData(@Query("msg") String msg);
}
