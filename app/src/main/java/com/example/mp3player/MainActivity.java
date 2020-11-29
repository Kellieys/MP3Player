package com.example.mp3player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

import static com.example.mp3player.Services.player;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;

    private String path;
    private Spinner dropdown;
    private String music;

    //Music playing activities
    private TextView title;
    private ProgressBar progress_bar;
    private Button pause_resume;
    private Button stop;
    private int progress;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        handler = new Handler();

        //when enter the app, check if user had allowed permission
        //if no permission, run permission(), else start the app startApp()
        if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            permission();
        }
        else {
            startApp();
        }
    }

    //If no permission, request permission from user
    private void permission()
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}
            , REQUEST_CODE);
        }
        else
        {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }

    } //end of permission()

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == REQUEST_CODE)
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //Permission Granted
                    startApp();
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Please allow permission for app to work.", Toast.LENGTH_SHORT)
                            .show();
                    //re-request permission
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}
                            , REQUEST_CODE);
                }
            }

        } //end of onRequestPermissionResult()

    public void startApp()
    {

        setContentView(R.layout.activity_main);

        dropdown = findViewById(R.id.dropdown);
        //Retrieve files
        path = Environment.getExternalStorageDirectory().getPath()+ "/Music/";
        String[] files =  new File(path).list();

        //Initialize dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, files);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);

    }


    public void select_onclick(View view)
    {
        title = findViewById(R.id.music_title_textview);
        progress_bar = findViewById(R.id.progressBar);
        pause_resume = findViewById(R.id.play_pause_button);
        stop = findViewById(R.id.stop_button);
        progress = 0;

        music = dropdown.getSelectedItem().toString();
        title.setText(music);

        Intent serviceIntent = new Intent(this, Services.class);
        serviceIntent.putExtra("selected music", path + music);
        serviceIntent.putExtra("music title", music);
        serviceIntent.setAction("");


        MP3Player.MP3PlayerState MP3State = player.getState();

        // if there is song playing, stop mp3player first
        if (MP3State == MP3Player.MP3PlayerState.PLAYING) {
            player.stop();
        }

        // start service
        startService(serviceIntent);

        onStartingFunc();
    }


    public void pauseplay_onclick(View view)
    {
        // for pause button
        MP3Player.MP3PlayerState state = player.getState();

        // if it is playing then pause,if it is paused then resume
        if (state.equals(MP3Player.MP3PlayerState.PLAYING))
        {
            player.pause();
        }
        else if (state.equals(MP3Player.MP3PlayerState.PAUSED))
        {
            player.play();
        }
    }

    public void stop_onclick(View view)
    {
        // for stop button
        player.stop();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public void onStartingFunc()
    {
        ProgressBarListen();
        super.onStart();
    }

    protected void ProgressBarListen()
    {
        runnable = new Runnable() {
            @Override
            public void run() {
                    if(player != null){
                        // get current progress and convert into seconds
                        int current_position = player.getProgress()/1000;

                        // set progress
                        progress_bar.setProgress(current_position);
                    }
                    // delay by 1 second so the progress bar updates every 1 second
                    handler.postDelayed(runnable,1000);
                }
            };
            handler.postDelayed(runnable,1000);
    }

}