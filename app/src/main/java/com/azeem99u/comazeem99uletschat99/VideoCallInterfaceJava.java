package com.azeem99u.comazeem99uletschat99;

import android.webkit.JavascriptInterface;

public class VideoCallInterfaceJava {

    VideoCallActivity videoCallActivity;
    public VideoCallInterfaceJava(VideoCallActivity videoCallActivity) {
        this.videoCallActivity = videoCallActivity;
    }

    @JavascriptInterface
    public void onPeerConnected(){
        videoCallActivity.onPeerConnected();
    }
}

