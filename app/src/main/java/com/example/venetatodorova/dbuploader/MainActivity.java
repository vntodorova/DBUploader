package com.example.venetatodorova.dbuploader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;

import static com.example.venetatodorova.dbuploader.UploadService.SP_FILES_COUNT_KEY;
import static com.example.venetatodorova.dbuploader.UploadService.SP_PROGRESS_STEP_KEY;

public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    public static final String DATA = "data";
    private ProgressBar progressBar;
    private CheckBox selectAllCheckBox;
    private ListView listView;

    private ArrayList<FileModel> fileModels;
    private CustomListAdapter adapter;
    private NetworkStateReceiver networkStateReceiver;
    private boolean isBound = false;
    private int progressStep;
    private Messenger messenger = new Messenger(new IncomingHandler());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        if (savedInstanceState != null) {
            fileModels = savedInstanceState.getParcelableArrayList(DATA);
            adapter = new CustomListAdapter(this, fileModels);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            setListItems();
        }
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.setListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void init() {
        SharedPreferencesManager.init(getApplicationContext());
        listView = (ListView) findViewById(R.id.list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        selectAllCheckBox = (CheckBox) findViewById(R.id.checkbox);
        selectAllCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox cb = (CheckBox) view;
                for (FileModel model : fileModels) {
                    model.setChecked(cb.isChecked());
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UploadService.isRunning()) {
            bindService();
        }
    }

    private void bindService() {
        bindService(new Intent(this, UploadService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService();
    }

    private void unbindService() {
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(networkStateReceiver);
        networkStateReceiver.removeListener();
    }

    private void setListItems() {
        fileModels = new ArrayList<>();
        File[] files = new File(getString(R.string.download_directory)).listFiles();
        for (File file : files) {
            FileModel model = new FileModel(file.getName(), file.getPath(), false);
            fileModels.add(model);
        }
        adapter = new CustomListAdapter(this, fileModels);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void onUploadButtonClick(View view) {
        progressBar.setProgress(0);
        ArrayList<String> filesList = getSelectedFiles();
        progressStep = 100 / filesList.size();
        SharedPreferencesManager.write(SP_PROGRESS_STEP_KEY, progressStep);
        SharedPreferencesManager.write(SP_FILES_COUNT_KEY, filesList.size());
        SharedPreferencesManager.write(filesList);
        startUploadService();
    }

    private void startUploadService() {
        startService(new Intent(this, UploadService.class));
        bindService();
    }

    private ArrayList<String> getSelectedFiles() {
        ArrayList<String> filesList = new ArrayList<>();
        for (FileModel file : fileModels) {
            if (file.getChecked()) {
                filesList.add(file.getPath());
                file.setChecked(false);
            }
            adapter.notifyDataSetChanged();
        }
        selectAllCheckBox.setChecked(false);
        return filesList;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Messenger uploadServiceMessenger = new Messenger(service);
            try {
                Message msg = Message.obtain(null, UploadService.MSG_SET_ACTIVITY);
                msg.replyTo = messenger;
                uploadServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            isBound = false;
        }
    };

    @Override
    public void networkAvailable() {
        ArrayList<String> sharedPrefArray = SharedPreferencesManager.read();
        if (sharedPrefArray.size() > 0) {
            int filesCount = SharedPreferencesManager.read(SP_FILES_COUNT_KEY, -1);
            progressStep = SharedPreferencesManager.read(SP_PROGRESS_STEP_KEY, -1);
            progressBar.setProgress(progressStep * (filesCount - sharedPrefArray.size()));
            startUploadService();
        }
    }

    @Override
    public void networkUnavailable() {
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UploadService.MSG_SET_PROGRESS:
                    progressBar.setProgress(progressBar.getProgress() + progressStep);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(DATA, fileModels);
    }
}
