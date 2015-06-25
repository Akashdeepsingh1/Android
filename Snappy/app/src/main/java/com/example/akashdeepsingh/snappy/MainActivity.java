package com.example.akashdeepsingh.snappy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import android.content.Context;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    public static int REQUEST_CAMERA = 0;
    public static int REQUEST_VIDEO = 1;
    public static int CHOOSE_PHOTO_FROM_LIBRARY = 2;
    public static int CHOOSE_VIDEO_FROM_LIBRARY = 3;

    public static int MEDIA_TYPE_IMAGE = 4;
    public static int MEDIA_TYPE_VIDEO = 5;

    public static int FILESIZELIMIT = 1024*1024*10;

    public Uri mMediaUri;

    SectionsPagerAdapter mSectionsPagerAdapter;
    public static final String TAG = MainActivity.class.getSimpleName();

    public DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch(which){
                case 0:
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    if(mMediaUri==null){
                        Toast.makeText(MainActivity.this,R.string.external_storage_error,Toast.LENGTH_LONG).show();
                    }else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);

                        startActivityForResult(takePhotoIntent, REQUEST_CAMERA);
                    }
                    break;
                case 1:
                    Intent videointent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    if(mMediaUri==null){
                        Toast.makeText(MainActivity.this,R.string.external_storage_error,Toast.LENGTH_LONG).show();
                    }else{
                        videointent.putExtra(MediaStore.EXTRA_OUTPUT,mMediaUri);
                        videointent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);
                        videointent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
                        startActivityForResult(videointent,REQUEST_VIDEO);
                    }
                        break;
                case 2:
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent,CHOOSE_PHOTO_FROM_LIBRARY);

                    break;
                case 3:
                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseVideoIntent.setType("video/*");
                    Toast.makeText(MainActivity.this,R.string.video_size_warning,Toast.LENGTH_LONG).show();
                    startActivityForResult(chooseVideoIntent, CHOOSE_VIDEO_FROM_LIBRARY);
                    break;

            }
        }

        private Uri getOutputMediaFileUri(int mediatype) {
            if(isExternalStorageAvaliable()){
                    // Android Developer:- saving file in directory
                String appName = MainActivity.this.getString(R.string.app_name);
                File mediaStorageDir =  new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),appName);

                if(!mediaStorageDir.exists()){
                    if(!mediaStorageDir.mkdirs()){
                        Log.e(TAG,"Failed To create directory");
                        return null;
                    };
                }

                File mediaFile;
                Date now = new Date();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(now);

                String path = mediaStorageDir.getPath() + File.separator;
                if(mediatype ==MEDIA_TYPE_IMAGE ){
                    mediaFile = new File(path + "IMG_" + timeStamp + ".jpg");

                }else if(mediatype == MEDIA_TYPE_VIDEO){
                    mediaFile = new File(path + "VID_" + timeStamp + ".mp4");
                }
                else{
                    return null;
                }
             //   Toast.makeText(MainActivity.this, mediaFile + " " ,Toast.LENGTH_LONG).show();
                Log.d(TAG, Uri.fromFile(mediaFile) + "");
                return Uri.fromFile(mediaFile);
            }else {
                return null;
            }
        }
        public boolean isExternalStorageAvaliable(){
            String state = Environment.getExternalStorageState();
            if(state.equals(Environment.MEDIA_MOUNTED)){
                return true;
            }else{
                return false;
            }
        }

    };

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ParseAnalytics.trackAppOpened(getIntent());


        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ParseUser currentLogedInUser = ParseUser.getCurrentUser();
        if(currentLogedInUser==null){
            goToLoginScreen();
        }

        else{
            Log.i(TAG,"User Activity");

        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this,getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setIcon(mSectionsPagerAdapter.getIcon(i))
                            .setTabListener(this));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_PHOTO_FROM_LIBRARY || requestCode == CHOOSE_VIDEO_FROM_LIBRARY) {
                if (data == null) {
                    Toast.makeText(this, getString(R.string.generalerror), Toast.LENGTH_LONG).show();
                } else {
                    mMediaUri = data.getData();
                }
                InputStream inputStream = null;
                Log.i(TAG,"Media Uri :- "+ mMediaUri);
                if(requestCode==CHOOSE_VIDEO_FROM_LIBRARY){
                    int fileSize = 0;
                   try {
                       inputStream = getContentResolver().openInputStream(mMediaUri);
                       fileSize = inputStream.available();
                   }catch(FileNotFoundException e)
                   {
                       Toast.makeText(this, getString(R.string.file_opening_error), Toast.LENGTH_LONG).show();
                       return;
                   }catch(IOException e){
                       Toast.makeText(this, getString(R.string.file_opening_error), Toast.LENGTH_LONG).show();
                       return;
                   }
                    finally {
                       try {
                           inputStream.close();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
                    if(fileSize>= FILESIZELIMIT){
                        Toast.makeText(MainActivity.this,R.string.file_size_exceed,Toast.LENGTH_LONG).show();
                        return;
                    }
                }

            } else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);

                sendBroadcast(mediaScanIntent);
            }

//            Intent intent = new Intent(this,SeeActionBar.class);
//            startActivity(intent);

            Intent recipientsActivityIntent = new Intent(this,RecipientsActivity.class);
            recipientsActivityIntent.setData(mMediaUri);

            String File_type;

            if(requestCode == REQUEST_CAMERA || requestCode == CHOOSE_PHOTO_FROM_LIBRARY ){
                File_type = ParseConstants.TYPE_IMAGE;
            }else{
                File_type = ParseConstants.TYPE_VIDEO;
            }
            recipientsActivityIntent.putExtra(ParseConstants.KEY_FILE_TYPE,File_type);
            startActivity(recipientsActivityIntent);

        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, R.string.generalerror, Toast.LENGTH_LONG).show();

        }
    }

    public void goToLoginScreen() {
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.logout: {
                ParseUser.logOut();
                goToLoginScreen();
                return true;
            }
            case R.id.action_edit_friends: {
                Intent intent = new Intent(this, EditFriendActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_camera:{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_option,mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;

            }

            case R.id.action_help:{
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.help_string);
                builder.setTitle(R.string.item_help);
                builder.setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;


            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }


    }

}
