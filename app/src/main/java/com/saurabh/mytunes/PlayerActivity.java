package com.saurabh.mytunes;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static com.saurabh.mytunes.MainActivity.musicFiles;

public class PlayerActivity extends AppCompatActivity {

    TextView mSongName, mSongArtist, mDurationPlayed, mDurationTotal;
    ImageView mAlbumArt, mPrevious, mNext, mShuffle, mRepeat, mBack, mMenu;
    FloatingActionButton mPlay;
    SeekBar mSeekBar;
    int position;
    Uri uri;

    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static MediaPlayer mediaPlayer;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        position = getIntent().getIntExtra("position",-1);

        mSongName = findViewById(R.id.songName);
        mSongArtist = findViewById(R.id.songArtist);
        mDurationPlayed = findViewById(R.id.durationPlayed);
        mDurationTotal = findViewById(R.id.durationTotal);

        mAlbumArt = findViewById(R.id.albumArt);
        mPrevious = findViewById(R.id.previous);
        mNext = findViewById(R.id.next);
        mShuffle = findViewById(R.id.shuffle);
        mRepeat = findViewById(R.id.repeat);
        mBack = findViewById(R.id.back);
        mMenu = findViewById(R.id.menu);

        mPlay = findViewById(R.id.play);

        mSeekBar = findViewById(R.id.seekBar);

        listSongs = musicFiles;
        if(listSongs!=null)
            getIntentMethods();
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(mediaPlayer!=null && b)
                    mediaPlayer.seekTo(i*1000);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer!=null)
                {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                    mSeekBar.setProgress(mCurrentPosition);
                    mDurationPlayed.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this,1000);
            }
        });

    }

    private String formattedTime(int mCurrentPosition) {
        String time="";
        String seconds=String.valueOf(mCurrentPosition%60);
        String minutes=String.valueOf(mCurrentPosition/60);
        if(seconds.length()>1)
            time=minutes+":"+seconds;
        else
            time=minutes+":0"+seconds;
        return time;
    }

    private void getIntentMethods() {
        mPlay.setImageResource(R.drawable.ic_pause);
        uri = Uri.parse(listSongs.get(position).getPath());
        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        else
        {
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        mSeekBar.setMax(mediaPlayer.getDuration()/1000);
    }
}