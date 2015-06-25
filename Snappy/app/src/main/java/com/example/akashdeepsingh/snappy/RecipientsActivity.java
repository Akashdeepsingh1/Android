package com.example.akashdeepsingh.snappy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class RecipientsActivity extends Activity {

    public static final String TAG = RecipientsActivity.class.getSimpleName();

    public List<ParseUser> mFriends;
    public ListView mListView;
    public ParseRelation<ParseUser> mFriendsRelation;
    public ParseUser mCurrentUser;
    protected MenuItem mMenuItemSend;
    public Button mButton;
    public Uri mMediaUri;
    public String mFileType;
    public int i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipients);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mListView = (ListView)findViewById(R.id.listRecipient);
        mListView.setChoiceMode(mListView.CHOICE_MODE_MULTIPLE);
        mButton = (Button)findViewById(R.id.buttonSend);
        mMediaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);



        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mMenuItemSend.setVisible(true);
               // Toast.makeText(RecipientsActivity.this,position + "",Toast.LENGTH_LONG).show();

            }
        });

        mButton.setOnClickListener(new View.OnClickListener(){

            @Override
        public void onClick(View v) {

                    ParseObject message = createMessage();
                    if (message == null) {
                        //error
                        AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                        builder.setMessage(R.string.send_error).setTitle(R.string.send_error_title).setPositiveButton("OK", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        send(message);
                        finish();
                    }

                }


        });

    }

    private void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    Toast.makeText(RecipientsActivity.this,R.string.success_message,Toast.LENGTH_LONG).show();
                    sendPushNotification();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(R.string.sending_message_error).setTitle(R.string.send_error_title).setPositiveButton("OK",null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private void sendPushNotification() {

        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        query.whereContainedIn(ParseConstants.KEY_USER_ID,getRecipientIds());

        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage("Snappy from " + ParseUser.getCurrentUser().getUsername() +"!");
        push.sendInBackground();
    }

    private ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGE);
        message.put(ParseConstants.KEY_SENDER_ID,ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME,ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_ID,getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE,mFileType);

        byte fileBytes[] = FileHelper.getByteArrayFromFile(this,mMediaUri);

        int num = getRecipientIds().size();
        if(fileBytes == null || i == 0){
            return null;
            }
        else{
            if(mFileType.equals(ParseConstants.TYPE_IMAGE)){
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }
            String fileName = FileHelper.getFileName(this,mMediaUri,mFileType);
            ParseFile parseFile = new ParseFile(fileName,fileBytes);
            message.put(ParseConstants.KEY_FILE,parseFile);
            return message;
        }
    }

    public ArrayList<String> getRecipientIds(){
        ArrayList<String> recipientIds = new ArrayList<String>();
        for(i = 0; i<mListView.getCount();i++){
            if(mListView.isItemChecked(i)){
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }
            return recipientIds;
    }

    @Override
    public void onResume(){
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation= mCurrentUser.getRelation("friendsRelation");

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                if(e==null) {
                    mFriends = friends;

                    String username[] = new String[mFriends.size()];
                    int i = 0;
                    for (ParseUser user : mFriends) {
                        username[i] = user.getUsername();
                        i++;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(mListView.getContext(),
                            android.R.layout.simple_list_item_checked, username);
                    mListView.setAdapter(adapter);
                }else{
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage("Sorry!! You have no friends to send message to. Please Add Friends first");
                    builder.setTitle("OOPS!!");
                    builder.setPositiveButton(android.R.string.ok,null);
                    AlertDialog dialog = builder.create();
                    // this dialog during sign up
                    dialog.show();

                }

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipients, menu);
        mMenuItemSend = menu.getItem(0);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_send:{

                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


}
