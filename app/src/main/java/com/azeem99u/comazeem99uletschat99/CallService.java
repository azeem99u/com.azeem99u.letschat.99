package com.azeem99u.comazeem99uletschat99;

import android.os.Build;
import android.telecom.InCallService;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.M)
public class CallService extends InCallService {

    private static CallService sInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
    public static CallService getInstance(){
        return sInstance;
    }

}
