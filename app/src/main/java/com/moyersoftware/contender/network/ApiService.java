package com.moyersoftware.contender.network;

import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.game.data.GameInvite;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("event/get_events.php")
    Call<ArrayList<Event>> getEvents();

    @GET("event/get_event.php")
    Call<Event> getEvent(@Query("id") String id);

    @POST("event/create_event.php")
    Call<Void> createEvent(@Body Event event);

    @GET("game/get_games.php")
    Call<ArrayList<GameInvite.Game>> getGames();

    @GET("game/get_game.php")
    Call<GameInvite.Game> getGame(@Query("id") String id);

    @POST("game/update_game.php")
    Call<Void> updateGame(@Body GameInvite.Game game);

    @POST("game/create_game.php")
    Call<Void> createGame(@Body GameInvite.Game game);

    @FormUrlEncoded
    @POST("game/delete_game.php")
    Call<Void> deleteGame(@Field("id") String id);

    @FormUrlEncoded
    @POST("user/update_token.php")
    Call<Void> updateToken(@Field("user_id") String userId, @Field("token") String token);
}
