package com.example.foodhub_android.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideSession(@ApplicationContext context: Context): FoodHubSession =
        FoodHubSession(context)

    @Provides
    fun provideHttpClient(session: FoodHubSession): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                session.getToken()?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    fun provideRetrofit(client: OkHttpClient):Retrofit{
        return Retrofit.Builder()
            .client(client)
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideFoodApi(retrofit: Retrofit): FoodApi{
        return retrofit.create(FoodApi::class.java)
    }
}
