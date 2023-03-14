package com.azeem99u.comazeem99uletschat99;

public class UploadEventImage {
    int position;
    int progress;
    String requestId;

    public UploadEventImage(int position, int progress, String requestId) {
        this.position = position;
        this.progress = progress;
        this.requestId = requestId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
