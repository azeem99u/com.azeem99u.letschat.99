package com.azeem99u.comazeem99uletschat99;

import android.webkit.JavascriptInterface;

public class AudioCallInterfaceJava {

     AudioCallActivity audioCallActivity ;

    public AudioCallInterfaceJava(AudioCallActivity audioCallActivity) {
        this.audioCallActivity = audioCallActivity;
    }

    @JavascriptInterface
    public void onPeerConnected(){
        audioCallActivity.onPeerConnected();
    }

}

