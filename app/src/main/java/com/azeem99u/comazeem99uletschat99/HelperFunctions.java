package com.azeem99u.comazeem99uletschat99;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.content.ContextCompat;
import androidx.work.ListenableWorker;

import com.azeem99u.comazeem99uletschat99.SendNotificationPack.APIService;
import com.azeem99u.comazeem99uletschat99.SendNotificationPack.Client;
import com.azeem99u.comazeem99uletschat99.SendNotificationPack.DataModel;
import com.azeem99u.comazeem99uletschat99.SendNotificationPack.RootModel;
import com.azeem99u.comazeem99uletschat99.activities.PersonalChatActivityAdapter;
import com.azeem99u.comazeem99uletschat99.models.PersonalMessageModel;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HelperFunctions {


    private Uri mUri;
    private long mTotalByteCount;

    @SuppressLint("Range")
    public static String getNameFromURI(Context context, Uri uri) {
        @SuppressLint("Recycle")
        Cursor c = context.getContentResolver().query(uri, null, null, null, null);
        c.moveToFirst();
        c.getColumnIndex(OpenableColumns.SIZE);
        return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
    }

    @SuppressLint("Range")
    public static String getSizeFromUri(Context context, Uri uri) {
        @SuppressLint("Recycle")
        Cursor c = context.getContentResolver().query(uri, null, null, null, null);
        c.moveToFirst();
        return c.getString(c.getColumnIndex(OpenableColumns.SIZE));
    }


    public static boolean checkPermission(Context context) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(context.getApplicationContext(),
                    WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(context.getApplicationContext(),
                    READ_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
        }
    }




    @NonNull
    public static ArrayMap<String, String> observingFileFromMemoryAndCreating(String fileName) {
        boolean imageFile = HelperFunctions.isImageFile(fileName);
        boolean videoFile = HelperFunctions.isVideoFile(fileName);
        boolean audioFile = HelperFunctions.isAudioFile(fileName);

        ArrayMap<String, String> dataFile = new ArrayMap<>();

        if (imageFile) {
            String imageTypeFolderNameDownloaded = "/Images";
            dataFile = createFileName(fileName, imageTypeFolderNameDownloaded);
        } else if (audioFile) {
            String audioTypeFolderNameDownloaded = "/audio";
            dataFile = createFileName(fileName, audioTypeFolderNameDownloaded);
        } else if (videoFile) {

            String imageTypeFolderNameDownloaded = "/Videos";
            dataFile = createFileName(fileName, imageTypeFolderNameDownloaded);
        } else {
            String documentTypeFolderNameDownloaded = "/Documents";
            dataFile = createFileName(fileName, documentTypeFolderNameDownloaded);
        }
        return dataFile;
    }


    public static long convertToLong(byte[] bytes)
    {
        long value = 0l;

        for (byte b : bytes) {
            value = (value << 8) + (b & 255);
        }
        return value;
    }
    public static int getFileSize(String downloadUrl) throws IOException {
        URL url = new URL(downloadUrl);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        return urlConnection.getContentLength();
    }

    public static String convertContentTypeIntoFile(Context context, Uri uri) {

        String path = null;
        Cursor cursor;
        String column = "_data";
        String[] strings = {column};
        try {
            cursor = context.getContentResolver().query(uri, strings, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                path = cursor.getString(index);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return path;
    }


    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        final int bufLen = 4 * 0x400; // 4KB
        byte[] buf = new byte[bufLen];
        int readLen;
        IOException exception = null;

        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                    outputStream.write(buf, 0, readLen);

                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            exception = e;
            throw e;
        } finally {
            if (exception == null) inputStream.close();
            else try {
                inputStream.close();
            } catch (IOException e) {
                exception.addSuppressed(e);
            }
        }
    }

    public static File filePath(String fileName) {
        File filePath = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Lets chat" + "/" + fileName);
        } else {
            filePath = new File(Environment.getExternalStorageDirectory() + "/Lets chat" + "/" + fileName);
        }
        return filePath;

    }

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static boolean isAudioFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("audio");
    }


    public static File saveFileOnLocalStorage(Context context, Uri data, String fileName) {
        byte[] bytes = readFile(context, data);
        File dataFile = null;
        if (bytes != null) {

            boolean imageFile = HelperFunctions.isImageFile(fileName);
            boolean videoFile = HelperFunctions.isVideoFile(fileName);
            boolean audioFile = HelperFunctions.isAudioFile(fileName);


            if (imageFile) {
                String imageTypeFolderNameFromSender = "/Images/sent";
                String imageTypeFolderNameDownloaded = "/Images";
                dataFile = getFileWithFolders(fileName, imageTypeFolderNameFromSender);
            } else if (audioFile) {
                String audioTypeFolderNameFromSender = "/audio/sent";
                String audioTypeFolderNameDownloaded = "/audio";
                dataFile = getFileWithFolders(fileName, audioTypeFolderNameFromSender);
            } else if (videoFile) {
                String videoTypeFolderNameFromSender = "/Videos/sent";
                String imageTypeFolderNameDownloaded = "/Videos";
                dataFile = getFileWithFolders(fileName, videoTypeFolderNameFromSender);
            } else {
                String documentTypeFolderNameFromSender = "/Documents/sent";
                String documentTypeFolderNameDownloaded = "/Documents";
                dataFile = getFileWithFolders(fileName, documentTypeFolderNameFromSender);
            }
            if (dataFile != null) {
                writeFile(dataFile, bytes);
                return dataFile;
            }
        }
        return null;
    }


    public static ArrayMap<String,String> createFileName(String fileName, String documentTypeFolderName) {

        ArrayMap<String,String> map = new ArrayMap<>();
        String mainFolderName = "/Lets Chat";
        File file;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + mainFolderName + documentTypeFolderName);
        } else {
            file = new File(Environment.getExternalStorageDirectory() + mainFolderName + documentTypeFolderName);
        }

        if (!file.exists()) {
            file.mkdirs();
        }

        boolean exists = new File(file.getAbsoluteFile(), fileName).exists();

        if (!exists) {
            map.put(Constants.FLAG,"0");
        }else {
            map.put(Constants.FLAG,"1");
        }
        map.put(Constants.DATA_PATH,new File(file.getAbsoluteFile(), fileName).toString());
        return map;

    }


    public static File getFileWithFolders(String fileName, String documentTypeFolderName) {
        String mainFolderName = "/Lets Chat";
        File file;
        File mainFile = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + mainFolderName + documentTypeFolderName);
        } else {
            file = new File(Environment.getExternalStorageDirectory() + mainFolderName + documentTypeFolderName);
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            mainFile = new File(file.getAbsoluteFile(), fileName);
        }
        return mainFile;
    }


    private static byte[] readFile(Context context, Uri file) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(file);
            return readAllBytes(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void writeFile(File file, byte[] bytes) {
        FileOutputStream outputStream = null;
        try {

            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static Bitmap readDataFromContentUri(Context context, Uri uri) {
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                inputStream = new BufferedInputStream(inputStream);

                bufferedInputStream = new BufferedInputStream(inputStream);
                return BitmapFactory.decodeStream(bufferedInputStream);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;


    }


    public static void downloadFile(Context context, Uri uri) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myImage.jpg");

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setTitle("File Downloading...")
                .setDescription("This is file downloading Demo")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file));
        downloadManager.enqueue(request);
    }





    public static Bitmap imageBlur(Bitmap image, Context context) {
        final float BITMAP_SCALE = 0.4f;
        final float BLUR_RADIUS = 7.5f;
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);
        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }



    public static void sendNotifications(String userToken, String title, String body, String click_action, String channel_id, String image, String message_key, String senderId, String receiverId, CallbackToFutureAdapter.Completer<ListenableWorker.Result> completer ) {
        APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        RootModel rootModel = new RootModel(userToken, "high", new DataModel(title,body,click_action,channel_id,image,message_key,senderId,receiverId));
        apiService.sendNotification(rootModel).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    if (completer != null){
                        completer.set(ListenableWorker.Result.success());
                    }

                }else {
                    if (completer != null){
                        completer.set(ListenableWorker.Result.failure());
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
                if (completer!= null){
                    completer.set(ListenableWorker.Result.failure());
                }
            }
        });
    }





    // Serialize a single object.
    public static String serializeToJsonPersonalMessageModel(PersonalMessageModel bmp) {
        Gson gson = new Gson();
        return gson.toJson(bmp);
    }

    // Deserialize to single object.
    public static PersonalMessageModel deserializeFromJsonPersonalMessageModel(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, PersonalMessageModel.class);
    }
    public static String serializeToJsonHolder(PersonalChatActivityAdapter.MyViewHolder holder) {
        Gson gson = new Gson();
        return gson.toJson(holder);
    }

    // Deserialize to single object.
    public static PersonalChatActivityAdapter.MyViewHolder deserializeFromJsonHolder(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, PersonalChatActivityAdapter.MyViewHolder.class);
    }

    @SuppressLint("DefaultLocale")
    public static String startTimer(long durationSeconds) {
       return String.format("%02d:%02d:%02d", durationSeconds / 3600,
                (durationSeconds % 3600) / 60, (durationSeconds % 60));
    }

}
