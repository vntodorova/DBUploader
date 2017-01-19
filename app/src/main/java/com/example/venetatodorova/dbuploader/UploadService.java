package com.example.venetatodorova.dbuploader;

import android.app.Service;
import android.content.Intent;
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

class UploadService
        extends Service
        implements UploadTask.FileUploadedListener {

    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    public static final long KEEP_ALIVE_TIME = 60L;
    public static final int MSG_SET_PROGRESS = 1;
    public static final int MSG_SET_ACTIVITY = 2;
    private UploadTask.FileUploadedListener fileUploadedListener;

    private Queue<String> filesPathQueue;
    private ArrayList<UploadTask> threadPool;
    private int progressStep;
    private int currentProgress = 0;
    static boolean isRunning;
    private Messenger activity;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    public UploadService() {
        super();
        fileUploadedListener = this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ArrayList<String> filesPathList = intent.getExtras().getStringArrayList(getString(R.string.data));
        filesPathQueue = new LinkedList<>();
        if (filesPathList != null) {
            for (String path : filesPathList) {
                filesPathQueue.add(path);
            }
            progressStep = 100 / filesPathList.size();
            Log.v("tag", "filePathList.size = " + filesPathList.size());
            Log.v("tag", "progressStep = " + progressStep);
            startUploadTasks();
        } else {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startUploadTasks() {
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(NUMBER_OF_CORES);
        threadPool = new ArrayList<>();
        Executor threadPoolExecutor = new ThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                workQueue);

        for (int i = 0; i < getResources().getInteger(R.integer.MAX_THREADS); i++) {
            threadPool.add(new UploadTask(fileUploadedListener));
        }

        for (UploadTask thread : threadPool) {
            thread.executeOnExecutor(threadPoolExecutor);
        }
    }

    @Override
    public synchronized File getNextFile(UploadTask currentTask) {
        if (filesPathQueue.isEmpty()) {
            currentTask.stopThread();
            if (isUploadFinished()) {
                currentProgress = 100;
                setProgress();
                stopSelf();
            }
            return null;
        } else {
            File file = new File(filesPathQueue.poll());
            currentProgress += progressStep;
            setProgress();
            return file;
        }
    }

    private void setProgress() {
        Log.v("tag", "Veni e shmatka.");
        if(activity!=null){
            try {
                activity.send(Message.obtain(null, MSG_SET_PROGRESS, currentProgress, 0));
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

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_ACTIVITY:
                    activity = msg.replyTo;
                    break;
                case MSG_SET_PROGRESS:
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
