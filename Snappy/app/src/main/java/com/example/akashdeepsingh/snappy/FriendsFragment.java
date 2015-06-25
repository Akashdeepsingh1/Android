package com.example.akashdeepsingh.snappy;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akashdeepsingh on 5/20/15.
 */
public class FriendsFragment extends ListFragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    public List<ParseUser> mFriends;
    public ListView mListView;
    public ParseRelation<ParseUser> mFriendsRelation;
    public ParseUser mCurrentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        return rootView;


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
                   ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(),
                           android.R.layout.simple_list_item_1, username);
                   setListAdapter(adapter);
               }else{
                   Log.e(TAG,e.getMessage());
                   AlertDialog.Builder builder = new AlertDialog.Builder(getListView().getContext());
                   builder.setMessage(e.getMessage());
                   builder.setTitle("OOPS!!");
                   builder.setPositiveButton(android.R.string.ok,null);
                   AlertDialog dialog = builder.create();
                 // this dialog during sign up
                  // dialog.show();

               }

            }
        });
    }
}
