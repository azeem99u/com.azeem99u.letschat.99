package com.azeem99u.comazeem99uletschat99.downloadFiles;

import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.work.ListenableWorker.Result.failure;
import static androidx.work.ListenableWorker.Result.success;
import static com.azeem99u.comazeem99uletschat99.Constants.PROGRESS_DOWNLOAD;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.azeem99u.comazeem99uletschat99.App;
import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.eventhandler.UpdateAdapterPosition;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class DownloadFilesWorker extends Worker {

    private static final int PREFERRED_CHUNK_SIZE = 256 * 1024;
    //private static final int PREFERRED_CHUNK_SIZE = 2000;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;


    public DownloadFilesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        builder = new NotificationCompat.Builder(getApplicationContext(), App.DOWNLOAD_CHANNEL_ID);
        builder.setPriority(Notification.PRIORITY_LOW);
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        setProgressAsync(new Data.Builder().putInt(PROGRESS_DOWNLOAD, 0).build());
        String cancel = "Cancel";
        builder.setContentTitle("Downloading");
        PendingIntent intent = WorkManager.getInstance(getApplicationContext())
                .createCancelPendingIntent(getId());
        builder.setSmallIcon(R.drawable.ic_outline_download);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.addAction(android.R.drawable.ic_delete, cancel, intent).build();
    }

    File file;
    long fileSize = -1;
    int position = 0;

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public Result doWork() {

        String filePath = getInputData().getString(Constants.FILE_PATH_KEY);
        String fileName = getInputData().getString(Constants.FILE_NAME_KEY);
        String fileSiz = getInputData().getString(Constants.FILE_SIZE_KEY);

        position = getInputData().getInt(Constants.ADAPTER_POSITION, 0);
        fileSize = Long.parseLong(fileSiz);
        builder.setContentTitle(fileName);
        String downloadUrl = getInputData().getString(Constants.URL_KEY);
        try {


            URL url = new URL(downloadUrl);

            file = new File(filePath);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            long l = 0;

            try {
                long mBytesDownloaded = 0;
                String totalSize = humanReadableByteCountSI(fileSize);
                setForegroundAsync(createForegroundInfo(100, 0, 0, totalSize, true));
                inputStream = url.openStream();
                outputStream = new FileOutputStream(file);
                byte[] data = new byte[PREFERRED_CHUNK_SIZE];
                int count = 0;
                while ((count = fillBuffer(inputStream, data)) != -1) {
                    if (isStopped()) {
                        setRunInForeground(false);
                        break;
                    }
                    outputStream.write(data, 0, count);
                    mBytesDownloaded += count;
                    l = mBytesDownloaded * 100 / fileSize;
                    setProgressAsync(new Data.Builder().putInt(PROGRESS_DOWNLOAD, (int) l).build());
                    if (!isStopped()) {
                        setForegroundAsync(changeNotificationProgress(100, (int) l, mBytesDownloaded, totalSize, false));
                    }
                }

                outputStream.flush();
                SystemClock.sleep(1000);
                EventBus.getDefault().post(new UpdateAdapterPosition(position));
                return success();
            } catch (IOException e) {
                e.printStackTrace();
                return failure();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return failure();
        }
    }


    @Override
    public void onStopped() {
        super.onStopped();
        if (file != null && file.exists() && file.getAbsoluteFile().length() != fileSize | file.getAbsoluteFile().length() == 0 | fileSize == 0) {
            file.delete();
            Log.d("mytag44", "onStopped: d");
        }
    }


    public ForegroundInfo createForegroundInfo(int max, int progress, long downloading, String fileSize, boolean indeterminate) {
        builder.setOngoing(true);
        String downloaded = humanReadableByteCountSI(downloading);
        builder.setContentText(downloaded + " / " + fileSize);
        builder.setProgress(max, progress, indeterminate);
        return new ForegroundInfo(Constants.NOTIFICATION_ID_DOWNLOAD, builder.build());
    }

    public ForegroundInfo changeNotificationProgress(int max, int progress, long downloading, String fileSize, boolean indeterminate) {
        String downloaded = humanReadableByteCountSI(downloading);
        builder.setContentText(downloaded + " / " + fileSize);
        builder.setProgress(max, progress, indeterminate);
        if (!isStopped()) {
            SystemClock.sleep(1000);
            if (isStopped()) {
                return null;
            }
        }
        return new ForegroundInfo(Constants.NOTIFICATION_ID_DOWNLOAD, builder.build());
    }


    public String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    @SuppressLint("RestrictedApi")
    private int fillBuffer(InputStream stream, byte[] data) {
        boolean readData = false;
        int offset = 0;
        int count;

        try {
            while (offset != data.length && (count = stream.read(data, offset, data.length - offset)) != -1) {
                if (isStopped()) {
                    setRunInForeground(false);
                    break;
                }
                readData = true;
                offset += count;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readData ? offset : -1;
    }
}