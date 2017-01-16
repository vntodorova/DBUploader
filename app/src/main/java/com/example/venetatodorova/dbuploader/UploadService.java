package com.example.venetatodorova.dbuploader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.dropbox.client2.exception.DropboxException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class UploadService extends Service {

    ArrayList<String> paths;

    public UploadService(){
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        paths = intent.getExtras().getStringArrayList(getString(R.string.data));

        assert paths != null;
        for (String path: paths) {
            File file = new File(path);
            try(FileInputStream inputStream = new FileInputStream(file)) {
                DropboxAPIHelper.getDropboxAPI().putFile(file.getName(), inputStream, file.length(), null, null);
            } catch (IOException | DropboxException e) {
                e.printStackTrace();
            }
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}
