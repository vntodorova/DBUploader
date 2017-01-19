package com.example.venetatodorova.dbuploader;

import android.os.AsyncTask;

import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class UploadTask extends AsyncTask<String,String,String> {

    private final FileUploadedListener listener;
    private boolean isRunning;

    UploadTask(FileUploadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        isRunning = true;
        File fileToUpload;
        while ((fileToUpload = listener.getNextFile(this)) != null) {
            try(FileInputStream inputStream = new FileInputStream(fileToUpload)) {
                DropboxAPIHelper.getDropboxAPI().putFile(fileToUpload.getName(), inputStream, fileToUpload.length(), null, null);
            } catch (IOException | DropboxException e) {
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
        File getNextFile(UploadTask currentTask);
    }

}
