package com.example.venetatodorova.dbuploader;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UploadService
        extends Service
        implements UploadTask.FileUploadedListener, NetworkStateReceiver.NetworkStateReceiverListener {

    public static final int MSG_SET_PROGRESS = 1;
    public static final int MSG_SET_ACTIVITY = 2;

    protected static final String SP_PROGRESS_STEP_KEY = "Progress step";
    protected static final String SP_FILES_COUNT_KEY = "Files count";

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final long KEEP_ALIVE_TIME = 10L;
    private static final int MAX_THREADS = 1;

    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private static boolean isRunning;
    private boolean networkAvailable = true;
    private Queue<String> filesPathQueue;
    private ArrayList<UploadTask> threadPool;
    private UploadTask.FileUploadedListener fileUploadedListener;
    private Messenger activityMessenger;
    private NetworkStateReceiver networkStateReceiver;

    public UploadService() {
        super();
        fileUploadedListener = this;
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.setListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("Service", "running");
        this.registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        isRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(networkStateReceiver);
        Log.v("Service", "stopped");
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ArrayList<String> sharedPref = SharedPreferencesManager.read();
        if (sharedPref.size() == 0) {
            Log.v("Service", "stopped");
            stopSelf();
        }

        filesPathQueue = new LinkedList<>();
        filesPathQueue.addAll(sharedPref);
        startUploadTasks();
        startForeground(startId);
        return super.onStartCommand(intent, flags, startId);
    }

    private void startUploadTasks() {
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(NUMBER_OF_CORES);
        threadPool = new ArrayList<>();
        Executor threadPoolExecutor = new ThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                workQueue);

        for (int i = 0; i < MAX_THREADS; i++) {
            threadPool.add(new UploadTask(fileUploadedListener));
        }

        for (UploadTask thread : threadPool) {
            thread.executeOnExecutor(threadPoolExecutor);
        }
    }

    private void startForeground(int ID) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(ID, notification);
    }

    @Override
    public synchronized File getNextFile(UploadTask currentTask, String currentFilePath) {
        if (currentFilePath != null) {
            SharedPreferencesManager.remove(currentFilePath);
        }

        if(!networkAvailable){
            currentTask.stopThread();
            stopSelf();
            stopForeground(true);
            return null;
        }

        if (filesPathQueue.isEmpty()) {
            currentTask.stopThread();
            if (isUploadFinished()) {
                updateProgress();
                SharedPreferencesManager.clear();
                stopSelf();
                stopForeground(true);
            }
            return null;
        } else {
            File file = new File(filesPathQueue.poll());
            updateProgress();
            return file;
        }
    }

    private void updateProgress() {
        if (activityMessenger != null) {
            try {
                activityMessenger.send(Message.obtain(null, MSG_SET_PROGRESS));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isUploadFinished() {
        for (UploadTask task : threadPool) {
            if (task.isRunning()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    public void networkAvailable() {
        networkAvailable = true;
    }

    @Override
    public void networkUnavailable() {
        networkAvailable = false;
        Log.v("Network", "unavailable");
        isRunning = false;
        Log.v("Service", "stopped");
        stopSelf();
        stopForeground(true);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_ACTIVITY:
                    activityMessenger = msg.replyTo;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
