package com.example.venetatodorova.dbuploader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;

class MainActivity extends AppCompatActivity{

    private ArrayList<FileModel> fileModels;
    private CustomListAdapter adapter;
    private boolean isBound = false;
    private ProgressBar progressBar;
    private final Messenger messenger = new Messenger(new IncomingHandler());
    private Messenger uploadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setListItems();
        DropboxAPIHelper.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UploadService.isRunning()) {
            bindService(new Intent(this, UploadService.class), serviceConnection, Context.BIND_AUTO_CREATE);
            isBound = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void setListItems() {
        ListView listView = (ListView) findViewById(R.id.list);
        fileModels = new ArrayList<>();
        File parentDir = new File(getString(R.string.download_directory));
        File[] files = parentDir.listFiles();
        for (File file : files) {
            FileModel model = new FileModel(file.getName(), file, false);
            fileModels.add(model);
        }
        adapter = new CustomListAdapter(this, fileModels);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void upload(View view) {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setProgress(0);
        ArrayList<String> filesList = new ArrayList<>();

        for (FileModel file : fileModels) {
            if (file.getChecked()) {
                filesList.add(file.getFile().getPath());
                file.setChecked(false);
                adapter.notifyDataSetChanged();
            }
        }
        Intent serviceIntent = new Intent(this, UploadService.class);
        serviceIntent.putExtra(getString(R.string.data), filesList);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            uploadService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, UploadService.MSG_SET_ACTIVITY);
                msg.replyTo = messenger;
                uploadService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            isBound = true;
        }
        public void onServiceDisconnected(ComponentName className) {
            isBound = false;
        }
    };

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UploadService.MSG_SET_PROGRESS:
                    progressBar.setProgress(msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
