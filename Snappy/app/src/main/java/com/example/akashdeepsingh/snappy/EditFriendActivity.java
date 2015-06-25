package com.example.akashdeepsingh.snappy;

import android.app.Activity;
import android.app.AlertDialog;


import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class EditFriendActivity extends Activity {

    public Context mContext;
    public static final String TAG = EditFriendActivity.class.getName();
    public List<ParseUser> mUsers;
    public ListView mListView;
    public ParseRelation<ParseUser> mFriendsRelation;
    public ParseUser mCurrentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_edit_friend);
        mListView = (ListView)findViewById(R.id.list);
        mListView.setChoiceMode(mListView.CHOICE_MODE_MULTIPLE);
     //getListView().setChoiceMode(ListView);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               if(mListView.isItemChecked(position)){
                     mFriendsRelation.add(mUsers.get(position));
                  }else{
                   mFriendsRelation.remove(mUsers.get(position));
               }
                 mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e!=null){
                            Log.e(TAG,e.getMessage());
                        }

                    }
                });
            }
        });
    }

   @Override
    protected void onResume() {
       super.onResume();
        setProgressBarIndeterminateVisibility(true);

       mCurrentUser = ParseUser.getCurrentUser();
       mFriendsRelation = mCurrentUser.getRelation("friendsRelation");
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending("username");
        query.setLimit(500);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
           public void done(List<ParseUser> users, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
            if(e==null){
                mUsers = users;
                String usernames[] = new String[mUsers.size()];
                int count=0;
                for(ParseUser user: mUsers){
                    usernames[count]=user.getUsername();
                    count++;
                   // Toast.makeText(EditFriendActivity.this,usernames[count] + "",Toast.LENGTH_LONG).show();
                    Log.v("Username Found Here",usernames+"");                }
                System.out.println("See Output Here :- "+ usernames);
                ArrayAdapter<String> adapter =
                      new ArrayAdapter<>(EditFriendActivity.this,
                        android.R.layout.simple_list_item_checked,usernames);
                mListView.setAdapter(adapter);
             //setListAdapter(adapter);
                addFriendsCheckmarks();

            }else{
                Log.e(TAG,e.getMessage());
                AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendActivity.this);
                builder.setMessage("Sorry! Not Connected to internet.");
                builder.setTitle("OOPS!!");
                builder.setPositiveButton(android.R.string.ok,null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            }
        });
    }

    private void addFriendsCheckmarks() {
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if(e==null){
                    for(int i = 0;i<mUsers.size();i++){
                       ParseUser user =  mUsers.get(i);

                        for(ParseUser friend: friends){
                            if(friend.getObjectId().equals(user.getObjectId())){
                                mListView.setItemChecked(i,true);
                            }
                        }
                    }

                }else{
                    Log.e(TAG,e.getMessage());
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_friend, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
