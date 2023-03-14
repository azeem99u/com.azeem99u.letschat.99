package com.azeem99u.comazeem99uletschat99;

import android.webkit.JavascriptInterface;

public class EarpieceCallInterfaceJava {


    AudioCallActivity audioCallActivity ;
    public EarpieceCallInterfaceJava(AudioCallActivity audioCallActivity) {
        this.audioCallActivity = audioCallActivity;
    }

    @JavascriptInterface
    public void onCallConnected(){
        audioCallActivity.onCallConnected();
    }
}
