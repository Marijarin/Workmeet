package ru.netology.workmeet.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.workmeet.auth.AuthState
import ru.netology.workmeet.dto.*

interface ApiService {

    /***** Auth *****/

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("password") password: String
    ): Response<AuthState>

    @Multipart
    @POST("users/registration")
    suspend fun registerUser(
        @Part("login") login: RequestBody,
        @Part("password") password: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part?,
    ): Response<AuthState>

    /***** Media *****/

    @Multipart
    @POST("media")
    suspend fun upload(@Part file: MultipartBody.Part): Response<Media>

    /***** Users *****/
    @GET("users")
    suspend fun getUsers(): Response <List<User>>

    @GET ("users/{user_id}")
    suspend fun getUserById(@Path("user_id") userId: Long): Response<User>

    /***** Posts *****/

    @GET("posts")
    suspend fun getAllP(): Response<List<Post>>

    @GET("posts/{post_id}/before")
    suspend fun getBeforeP(
        @Path("post_id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{post_id}/after")
    suspend fun getAfterP(
        @Path("post_id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatestP(@Query("count") count: Int): Response<List<Post>>

    @POST("posts/{post_id}/likes")
    suspend fun likeByIdP(@Path("post_id") id: Long): Response<Post>

    @DELETE("posts/{post_id}/likes")
    suspend fun unlikeByIdP(@Path("post_id") id: Long): Response<Post>

    @POST("posts")
    suspend fun saveP(@Body post: Post): Response<Post>

    @DELETE("posts/{post_id}")
    suspend fun removeByIdP(@Path("post_id") id: Long): Response<Unit>

    /***** Events *****/

    @GET("events")
    suspend fun getAllE(): Response<List<Event>>

    @GET("events/{event_id}/before")
    suspend fun getBeforeE(
        @Path("event_id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{event_id}/after")
    suspend fun getAfterE(
        @Path("event_id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/latest")
    suspend fun getLatestE(@Query("count") count: Int): Response<List<Event>>

    @POST("events/{event_id}/likes")
    suspend fun likeByIdE(@Path("event_id") id: Long): Response<Event>

    @DELETE("events/{event_id}/likes")
    suspend fun unlikeByIdE(@Path("event_id") id: Long): Response<Event>

    @POST("events")
    suspend fun saveE(@Body event: Event): Response<Event>

    @DELETE("events/{event_id}")
    suspend fun removeByIdE(@Path("event_id") id: Long): Response<Unit>

    @POST("events/{event_id}/participants")
    suspend fun participateById(@Path("event_id") id: Long): Response<Event>

    @DELETE("events/{event_id}/participants")
    suspend fun avoidById(@Path("event_id") id: Long): Response<Event>

    /***** Jobs *****/

    @GET("{user_id}/jobs")
    suspend fun getAllJ(@Path("user_id") id: Long): Response<List<Job>>

    @GET("my/jobs")
    suspend fun getAllMyJ(): Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveJ(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{job_id}")
    suspend fun removeByIdJ(@Path("job_id") id: Long): Response<Unit>

    /***** My Wall *****/

    @GET("my/wall")
    suspend fun getMyWall(): Response<List<Post>>

    @GET("my/wall/{post_id}/before")
    suspend fun getMyWallBeforeP(
        @Path("post_id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("my/wall/{post_id}/after")
    suspend fun getMyWallAfterP(
        @Path("post_id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("my/wall/latest")
    suspend fun getMyWallLatestP(@Query("count") count: Int): Response<List<Post>>

    /***** User's Wall *****/

    @GET("{author_id}/wall")
    suspend fun getUserWall(): Response<List<Post>>

    @GET("{author_id}/wall/{post_id}/newer")
    suspend fun getUserWallNewerP(
        @Path("author_id") Aid: Long,
        @Path("post_id") id: Long
    ): Response<List<Post>>

    @GET("{author_id}/wall/{post_id}/before")
    suspend fun getUserWallBeforeP(
        @Path("author_id") Aid: Long,
        @Path("post_id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("{author_id}/wall/{post_id}/after")
    suspend fun getUserWallAfterP(
        @Path("author_id") Aid: Long,
        @Path("post_id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("{author_id}/wall/latest")
    suspend fun getUserWallLatestP(
        @Path("author_id") Aid: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

}