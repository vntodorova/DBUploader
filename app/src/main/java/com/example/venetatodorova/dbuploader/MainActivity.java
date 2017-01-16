package com.example.venetatodorova.dbuploader;

import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<FileModel> list;
    CustomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setListItems();
        DropboxAPIHelper.init();
    }

    private void setListItems() {
        ListView listView = (ListView) findViewById(R.id.list);
        list = new ArrayList<>();
        //Get files from download directory and add them to ArrayList
        File parentDir = new File(getString(R.string.download_directory));
        File[] files = parentDir.listFiles();
        for (File file : files) {
            FileModel model = new FileModel(file.getName(), file, false);
            list.add(model);
        }
        adapter = new CustomListAdapter(this, list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void upload(View view) {
        ArrayList<String> filesList = new ArrayList<>();
        for (FileModel file : list) {
            if (file.setChecked()) {
                filesList.add(file.getFile().getPath());
                file.setChecked(false);
                adapter.notifyDataSetChanged();
            }
        }

        Intent serviceIntent = new Intent(this,UploadService.class);
        serviceIntent.putExtra(getString(R.string.data),filesList);
        startService(serviceIntent);
    }
}
