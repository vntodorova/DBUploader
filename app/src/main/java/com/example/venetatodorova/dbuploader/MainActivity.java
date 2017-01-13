package com.example.venetatodorova.dbuploader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<FileModel> list;
    DropboxAPI<AndroidAuthSession> dropboxAPI;
    private static final String ACCESS_TOKEN = "EbN0cf7SoQ8AAAAAAABC5_1yAO_XUSkL2_E53fNHNbaUPcpj6zoOaE8NUkrjo4hf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setListItems();

        AppKeyPair appKeys = new AppKeyPair(getString(R.string.APP_KEY),getString(R.string.APP_SECRET));
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        dropboxAPI = new DropboxAPI<>(session);

    }

    private void setListItems() {
        ListView listView = (ListView) findViewById(R.id.list);
        list = new ArrayList<>();
        File parentDir = new File(getString(R.string.download_directory));
        File[] files = parentDir.listFiles();
        for (File file : files) {
            FileModel model = new FileModel(file.getName(), file, false);
            list.add(model);
        }
        CustomListAdapter adapter = new CustomListAdapter(this, list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void upload(View view) {
        ArrayList<File> filesList = new ArrayList<>();
        for(FileModel file : list){
            if(file.isChecked()){
                filesList.add(file.getFile());
            }
        }

        File[] filesToUpload = filesList.toArray(new File[filesList.size()]);

        UploadTask uploadTask = new UploadTask();
        uploadTask.setDropboxAPI(dropboxAPI);
        uploadTask.execute(filesToUpload);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dropboxAPI.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                dropboxAPI.getSession().finishAuthentication();

                String accessToken = dropboxAPI.getSession().getOAuth2AccessToken();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        } else {
            dropboxAPI.getSession().startOAuth2Authentication(MainActivity.this);
        }
    }
}
