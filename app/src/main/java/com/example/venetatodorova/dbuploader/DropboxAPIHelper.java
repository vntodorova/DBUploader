package com.example.venetatodorova.dbuploader;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

class DropboxAPIHelper {

    private static String APP_KEY = "9n9zd2rnrqr9d13";
    private static String APP_SECRET = "9n9zd2rnrqr9d13";
    private static String ACCESS_TOKEN = "EbN0cf7SoQ8AAAAAAABC5_1yAO_XUSkL2_E53fNHNbaUPcpj6zoOaE8NUkrjo4hf";
    private static DropboxAPI<AndroidAuthSession> dropboxAPI;

    static void init() {
        AppKeyPair appKeys = new AppKeyPair(APP_KEY,APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        session.setOAuth2AccessToken(ACCESS_TOKEN);
        dropboxAPI = new DropboxAPI<>(session);
    }

    static DropboxAPI<AndroidAuthSession> getDropboxAPI() {
        return dropboxAPI;
    }
}
