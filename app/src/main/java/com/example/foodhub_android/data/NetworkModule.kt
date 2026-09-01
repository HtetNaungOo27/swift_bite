package com.example.foodhub_android.data

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.example.foodhub_android.data.local.RoomCacheStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideSession(
        @ApplicationContext context: Context,
        cacheStore: RoomCacheStore
    ): FoodHubSession = FoodHubSession(context, cacheStore)

    @Provides
    @Singleton
    fun provideHttpClient(
        @ApplicationContext context: Context,
        session: FoodHubSession
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request()
                    .newBuilder()
                    .header("X-Package-Name", context.packageName)

                session.getToken()?.let { token ->
                    requestBuilder.header(
                        "Authorization",
                        "Bearer $token"
                    )
                }

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    redactHeader("Authorization")
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()

    @Provides
    fun provideRetrofit(client: OkHttpClient):Retrofit{
        return Retrofit.Builder()
            .client(client)
            .baseUrl("http://10.0.2.2:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideFoodApi(retrofit: Retrofit): FoodApi{
        return retrofit.create(FoodApi::class.java)
    }

}
