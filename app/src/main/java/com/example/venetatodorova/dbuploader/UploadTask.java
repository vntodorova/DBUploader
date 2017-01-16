package com.example.venetatodorova.dbuploader;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class UploadTask extends AsyncTask<String,String,String> {

    @Override
    protected String doInBackground(String... params) {
        for (String path: params) {
            File file = new File(path);
            try(FileInputStream inputStream = new FileInputStream(file)) {
                DropboxAPIHelper.getDropboxAPI().putFile(file.getName(), inputStream, file.length(), null, null);
            } catch (IOException | DropboxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
