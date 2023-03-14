package com.azeem99u.comazeem99uletschat99.SendNotificationPack;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAfjRKZC8:APA91bEjV9KCd-O3gnLfEp84eM0XPcFSJ3gi0gd9y_vPhZwewojp4yuy6HN7rLErDb6Lm70EPgMZPpJ5bxQHHbHQ3DrOlT747MDUtwQ2z14DvQwSLxEdCNEzy9fRJgMUrJs8WkKiauYv" // Your server key refer to video for finding your server key
            }
    )
    @POST("fcm/send")
    Call<ResponseBody> sendNotification(@Body RootModel body);

}

