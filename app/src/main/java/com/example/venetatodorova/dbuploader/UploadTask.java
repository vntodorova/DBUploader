package com.example.venetatodorova.dbuploader;

import android.os.AsyncTask;
import android.util.Log;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class UploadTask extends AsyncTask<String, String, String> {

    private String ACCESS_TOKEN = "EbN0cf7SoQ8AAAAAAABC5_1yAO_XUSkL2_E53fNHNbaUPcpj6zoOaE8NUkrjo4hf";
    private String DROPBOX_PATH = "dropbox/";
    private final FileUploadedListener listener;
    private boolean isRunning;
    private String currentFilePath;

    UploadTask(FileUploadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        isRunning = true;
        DbxRequestConfig config = new DbxRequestConfig(DROPBOX_PATH);
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        File fileToUpload;
        while ((fileToUpload = listener.getNextFile(this, currentFilePath)) != null) {
            currentFilePath = fileToUpload.getPath();
            try (InputStream in = new FileInputStream(fileToUpload)) {
                client.files().uploadBuilder("/" + fileToUpload.getName()).uploadAndFinish(in);
                Log.v("File","Uploaded "+fileToUpload.getName());
            } catch (DbxException | IOException e) {
                e.printStackTrace();
            }
        }
        isRunning = false;
        return null;
    }

    void stopThread() {
        isRunning = false;
    }

    boolean isRunning() {
        return isRunning;
    }

    interface FileUploadedListener {
        File getNextFile(UploadTask currentTask, String currentFilePath);
    }

}
