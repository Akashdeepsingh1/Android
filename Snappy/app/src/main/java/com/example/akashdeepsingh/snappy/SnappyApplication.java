package com.example.akashdeepsingh.snappy;

import android.app.Application;
import android.util.Log;


import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.PushService;

/**
 * Created by akashdeepsingh on 5/13/15.
 */
public class SnappyApplication extends Application {

    @Override
    public void onCreate(){
        //Parse.enableLocalDatastore(this);

        super.onCreate();

        Parse.initialize(this, "mnFNgrEVnS0QvldNAh6FFTIz7MHNU4v7mj3naZBQ", "Yvr48D6ehN71f3Yd6juB3ckVOJuLFDgtM65LGpSn");

        //PushService.setDefaultPushCallback(this,MainActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        Log.d("Akashdeep Singh", "Check Push Notification");




    }

    public static void updateParseInstallation( ParseUser user){

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID,user.getObjectId());
        installation.saveInBackground();
    }
}
