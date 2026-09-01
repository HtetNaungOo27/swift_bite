package com.example.foodhub_android.data

import com.example.foodhub_android.data.models.AddToCartRequest
import com.example.foodhub_android.data.models.AddToCartResponse
import com.example.foodhub_android.data.models.Address
import com.example.foodhub_android.data.models.AddressListResponse
import com.example.foodhub_android.data.models.AuthResponse
import com.example.foodhub_android.data.models.CartResponse
import com.example.foodhub_android.data.models.CategoriesResponse
import com.example.foodhub_android.data.models.ConfirmPaymentRequest
import com.example.foodhub_android.data.models.ConfirmPaymentResponse
import com.example.foodhub_android.data.models.FooditemResponse
import com.example.foodhub_android.data.models.GenericMsgResponse
import com.example.foodhub_android.data.models.FoodItem
import com.example.foodhub_android.data.models.FoodItemListResponse
import com.example.foodhub_android.data.models.FCMRequest
import com.example.foodhub_android.data.models.ImageUploadResponse
import com.example.foodhub_android.data.models.NotificationListResponse
import com.example.foodhub_android.data.models.OAuthRequest
import com.example.foodhub_android.data.models.Order
import com.example.foodhub_android.data.models.OrderListResponse
import com.example.foodhub_android.data.models.PaymentIntentRequest
import com.example.foodhub_android.data.models.PaymentIntentResponse
import com.example.foodhub_android.data.models.RestaurantsResponse
import com.example.foodhub_android.data.models.ReverseGeocodeRequest
import com.example.foodhub_android.data.models.SignInRequest
import com.example.foodhub_android.data.models.SignUpRequest
import com.example.foodhub_android.data.models.UpdateCartItemRequest
import com.example.foodhub_android.data.models.AvailableDeliveriesResponse
import com.example.foodhub_android.data.models.RiderDeliveriesResponse
import com.example.foodhub_android.data.models.DeliveryStatusUpdate
import com.example.foodhub_android.data.models.ReviewRequest
import com.example.foodhub_android.data.models.ReviewSummary
import com.example.foodhub_android.data.models.RestaurantStatistics
import com.example.foodhub_android.data.models.CustomerProfile
import com.example.foodhub_android.data.models.PlaceOrderRequest
import com.example.foodhub_android.data.models.PlaceOrderResponse
import com.example.foodhub_android.data.models.RiderWallet
import com.example.foodhub_android.data.models.UpdateRestaurantRequest
import retrofit2.Response
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodApi {
    @GET("/payouts") suspend fun getPayouts(): Response<com.example.foodhub_android.data.models.PayoutOverview>
    @PUT("/payouts/account") suspend fun savePayoutAccount(@Body request: com.example.foodhub_android.data.models.SavePayoutAccountRequest): Response<GenericMsgResponse>
    @POST("/payouts") suspend fun requestPayout(@Body request: com.example.foodhub_android.data.models.RequestPayout): Response<com.example.foodhub_android.data.models.PayoutItem>
    @GET("/account")
    suspend fun getAccount(): Response<com.example.foodhub_android.data.models.AccountProfile>

    @PUT("/account")
    suspend fun updateAccount(
        @Body request: com.example.foodhub_android.data.models.UpdateAccountRequest
    ): Response<com.example.foodhub_android.data.models.AccountProfile>

    @PUT("/account/password")
    suspend fun changePassword(
        @Body request: com.example.foodhub_android.data.models.ChangePasswordRequest
    ): Response<GenericMsgResponse>

    @POST("/orders")
    suspend fun placeOrder(@Body request: PlaceOrderRequest): Response<PlaceOrderResponse>

    @GET("/customer/profile")
    suspend fun getCustomerProfile(): Response<CustomerProfile>

    @PUT("/customer/profile")
    suspend fun updateCustomerProfile(@Body request: Map<String, String>): Response<GenericMsgResponse>

    @GET("/customer/favorites")
    suspend fun getFavorites(): Response<com.example.foodhub_android.data.models.FavoriteIdsResponse>

    @PUT("/customer/favorites/{menuItemId}")
    suspend fun setFavorite(
        @Path("menuItemId") menuItemId: String,
        @Body request: Map<String, Boolean>
    ): Response<GenericMsgResponse>

    @GET("/restaurants/{id}/reviews")
    suspend fun getRestaurantReviews(@Path("id") restaurantId: String): Response<ReviewSummary>

    @POST("/restaurants/{id}/reviews")
    suspend fun saveRestaurantReview(
        @Path("id") restaurantId: String,
        @Body request: ReviewRequest
    ): Response<ReviewSummary>

    @GET("/categories")
    suspend fun getCategories(): Response<CategoriesResponse>

    @GET("/restaurants")
    suspend fun getRestaurants(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<RestaurantsResponse>

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun signIn(@Body request: SignInRequest): Response<AuthResponse>

    @POST("auth/password/forgot")
    suspend fun requestPasswordReset(@Body request: Map<String, String>): Response<Map<String, String?>>

    @POST("auth/password/reset")
    suspend fun resetPassword(@Body request: Map<String, String>): Response<GenericMsgResponse>

    @POST("/auth/oauth")
    suspend fun oAuth(@Body request: OAuthRequest): Response<AuthResponse>
    @GET("restaurants/{restaurantId}/menu")
    suspend fun getFoodItemForRestaurant(@Path("restaurantId")restaurantId: String): Response<FooditemResponse>

    @POST("/cart")
    suspend fun addToCart(@Body request: AddToCartRequest) : Response<AddToCartResponse>

    @GET("/cart")
    suspend fun getCart(): Response<CartResponse>

    @PATCH("/cart")
    suspend fun updateCart(@Body request: UpdateCartItemRequest): Response<GenericMsgResponse>

    @DELETE("/cart/{cartItemId}")
    suspend fun deleteCartItem(@Path("cartItemId") cartItemId: String): Response<GenericMsgResponse>

    @GET("/addresses")
    suspend fun getUserAddress(): Response<AddressListResponse>

    @POST("addresses/reverse-geocode")
    suspend fun reverseGeocode(@Body request: ReverseGeocodeRequest): Response<Address>

    @POST("/addresses")
    suspend fun storeAddress(@Body address: Address): Response<GenericMsgResponse>

    @PUT("/addresses/{id}")
    suspend fun updateAddress(@Path("id") id: String, @Body address: Address): Response<GenericMsgResponse>

    @DELETE("/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): Response<GenericMsgResponse>

    @POST("/payments/create-intent")
    suspend fun getPaymentIntent(@Body request: PaymentIntentRequest): Response<PaymentIntentResponse>
    @POST("/payments/confirm/{paymentIntentId}")
    suspend fun verifyPurchase(
        @Body request: ConfirmPaymentRequest,
        @Path("paymentIntentId") paymentIntentId: String
    ) : Response<ConfirmPaymentResponse>

    @GET("/orders")
    suspend fun getOrders(): Response<OrderListResponse>

    @GET("/orders/{orderId}")
    suspend fun getOrderDetails(@Path("orderId") orderId: String): Response<Order>

    @POST("/orders/{orderId}/cancel")
    suspend fun cancelOrder(@Path("orderId") orderId: String): Response<Order>

    @POST("/orders/{orderId}/reorder")
    suspend fun reorder(@Path("orderId") orderId: String): Response<GenericMsgResponse>

    @GET("/orders/{orderId}/issue")
    suspend fun getOrderIssue(@Path("orderId") orderId: String): Response<com.example.foodhub_android.data.models.OrderIssueResponse>

    @POST("/orders/{orderId}/issue")
    suspend fun createOrderIssue(
        @Path("orderId") orderId: String,
        @Body request: com.example.foodhub_android.data.models.CreateOrderIssueRequest
    ): Response<com.example.foodhub_android.data.models.OrderIssue>

    @POST("/notifications/{id}/read")
    suspend fun readNotification(@Path("id") id: String): Response<GenericMsgResponse>

    @GET("/notifications")
    suspend fun getNotifications(): Response<NotificationListResponse>

    @PUT("/notifications/fcm-token")
    suspend fun updateToken(@Body request: FCMRequest): Response<GenericMsgResponse>

    @GET("/restaurant-owner/profile")
    suspend fun getRestaurantProfile(): Response<com.example.foodhub_android.data.models.Restaurant>

    @PUT("/restaurant-owner/profile")
    suspend fun updateRestaurantProfile(@Body request: UpdateRestaurantRequest): Response<GenericMsgResponse>

    @GET("/restaurant-owner/hours")
    suspend fun getRestaurantHours(): Response<com.example.foodhub_android.data.models.UpdateRestaurantHoursRequest>

    @PUT("/restaurant-owner/hours")
    suspend fun updateRestaurantHours(
        @Body request: com.example.foodhub_android.data.models.UpdateRestaurantHoursRequest
    ): Response<GenericMsgResponse>

    @GET("/restaurant-owner/statistics")
    suspend fun getRestaurantStatistics(): Response<RestaurantStatistics>

    @GET("/restaurant-owner/orders")
    suspend fun getRestaurantOrders(@Query("status") status: String): Response<OrderListResponse>

    @GET("/restaurant-owner/orders/{orderId}")
    suspend fun getRestaurantOrderDetails(@Path("orderId") orderId: String): Response<Order>

    @PATCH("/restaurant-owner/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body status: com.example.foodhub_android.data.models.UpdateOrderStatusRequest
    ): Response<GenericMsgResponse>

    @POST("/restaurant-owner/orders/{orderId}/action")
    suspend fun performRestaurantOrderAction(
        @Path("orderId") orderId: String,
        @Body request: com.example.foodhub_android.data.models.OrderActionRequest
    ): Response<GenericMsgResponse>

    @GET("/restaurants/{id}/menu")
    suspend fun getRestaurantMenu(@Path("id") restaurantId: String): Response<FoodItemListResponse>

    @GET("/restaurant-owner/menu")
    suspend fun getOwnerMenu(): Response<FoodItemListResponse>

    @POST("/restaurant-owner/menu")
    suspend fun addRestaurantMenu(
        @Body foodItem: FoodItem
    ): Response<GenericMsgResponse>

    @PATCH("/restaurant-owner/menu/{itemId}")
    suspend fun updateRestaurantMenuItem(
        @Path("itemId") itemId: String,
        @Body request: com.example.foodhub_android.data.models.UpdateMenuItemRequest
    ): Response<GenericMsgResponse>

    @DELETE("/restaurant-owner/menu/{itemId}")
    suspend fun deleteRestaurantMenuItem(@Path("itemId") itemId: String): Response<GenericMsgResponse>

    @Multipart
    @POST("/images/upload")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<ImageUploadResponse>

    @GET("/rider/deliveries/available")
    suspend fun getAvailableDeliveries(): Response<AvailableDeliveriesResponse>

    @GET("/rider/availability")
    suspend fun getRiderAvailability(): Response<Map<String, Boolean>>

    @PUT("/rider/availability")
    suspend fun setRiderAvailability(@Body request: Map<String, Boolean>): Response<GenericMsgResponse>

    @POST("/rider/location")
    suspend fun updateRiderLocation(
        @Body request: com.example.foodhub_android.data.models.RiderLocationUpdate
    ): Response<GenericMsgResponse>

    @GET("/rider/deliveries/active")
    suspend fun getActiveDeliveries(): Response<RiderDeliveriesResponse>

    @POST("/rider/deliveries/{orderId}/accept")
    suspend fun acceptDelivery(@Path("orderId") orderId: String): Response<GenericMsgResponse>

    @POST("/rider/deliveries/{orderId}/reject")
    suspend fun rejectDelivery(@Path("orderId") orderId: String): Response<GenericMsgResponse>

    @POST("/rider/deliveries/{orderId}/status")
    suspend fun updateDeliveryStatus(
        @Path("orderId") orderId: String,
        @Body request: DeliveryStatusUpdate
    ): Response<GenericMsgResponse>

    @GET("/rider/wallet")
    suspend fun getRiderWallet(): Response<RiderWallet>

    @POST("/rider/wallet/settle")
    suspend fun settleRiderWallet(): Response<GenericMsgResponse>

}
