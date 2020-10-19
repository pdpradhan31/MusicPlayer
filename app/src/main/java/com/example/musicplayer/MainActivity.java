package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final int REQUEST_CODE = 1;
    static ArrayList<MusicFiles> musicFiles;
    static boolean shuffleBoolean = false , repeatBoolean = false;
    static ArrayList<MusicFiles> albums = new ArrayList<>();
    private String My_Sort_Pref = "SortOrder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionMethod();

    }

    private void permissionMethod() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
            , REQUEST_CODE);
        }
        else
        {
            musicFiles = getAllAudio(this);
            initViewpager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
               //Do whatever you want permission related;
                musicFiles = getAllAudio(this);
                initViewpager();
            }
            else
            {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , REQUEST_CODE);
            }
        }
    }

    private void initViewpager() {
        ViewPager viewPager = findViewById(R.id.viewpager_container);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new SongFragment(),"Songs");
        viewPagerAdapter.addFragment(new AlbumFragment(),"Aibums");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }


    public static class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;
        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }
        void addFragment(Fragment fragment,String title)
        {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }


        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    public ArrayList<MusicFiles> getAllAudio(Context context)
    {
        SharedPreferences preferences = getSharedPreferences(My_Sort_Pref,MODE_PRIVATE);
        String sortOrder =  preferences.getString("sorting","sortByName");
        ArrayList<String> duplicate = new ArrayList<>();
        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        String order = null;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        switch (sortOrder)
        {
            case "sortByName":
                order = MediaStore.MediaColumns._COUNT + "ASC";
                break;
            case "sortByDate":
                order = MediaStore.MediaColumns._ID + "ASC";
                break;
            case "sortBySize":
                order = MediaStore.MediaColumns.SIZE + "DESC";
                break;
        }
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,//for path
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };
        Cursor cursor = context.getContentResolver().query(uri,projection, null,null,null);
        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);

                MusicFiles musicFiles = new MusicFiles(path,title,artist,album,duration,id);
                //take log.e for check
                Log.e("path: "+path,"Album: "+ album);
                tempAudioList.add(musicFiles);
                if(!duplicate.contains(album))
                {
                    albums.add(musicFiles);
                    duplicate.add(album);
                }
            }
            cursor.close();

        }
        return  tempAudioList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchmenu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<MusicFiles> myFiles = new ArrayList<>();
        for(MusicFiles song : musicFiles)
        {
            if(song.getTitle().toLowerCase().contains(userInput))
            {
                myFiles.add(song);
            }
        }
        SongFragment.musicAdapter.updateList(myFiles);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(My_Sort_Pref,MODE_PRIVATE).edit();
        switch (item.getItemId())
        {
            case R.id.by_name :
                editor.putString("sorting","sortByName");
                editor.apply();
                this.recreate();
                break;

            case R.id.by_date :
                editor.putString("sorting","sortByDate");
                editor.apply();
                this.recreate();
                break;

            case R.id.by_size :
                editor.putString("sorting","sortBySize");
                editor.apply();
                this.recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}