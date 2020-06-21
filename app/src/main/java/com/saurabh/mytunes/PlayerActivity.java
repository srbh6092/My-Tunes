package com.saurabh.mytunes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static com.saurabh.mytunes.MainActivity.musicFiles;

public class PlayerActivity extends AppCompatActivity {

    TextView mSongName, mSongArtist, mDurationPlayed, mDurationTotal;
    ImageView mAlbumArt, mPrevious, mNext, mShuffle, mRepeat, mBack, mMenu;
    FloatingActionButton mPlay;
    SeekBar mSeekBar;
    int position;

    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static MediaPlayer mediaPlayer;
    static Uri uri;

    private Handler handler = new Handler();
    private Thread prevThread, playThread, nextThread;

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
        {
            getIntentMethods();
            mSongName.setText(listSongs.get(position).getTitle());
            mSongArtist.setText(listSongs.get(position).getArtist());
        }
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
        setDurationSeekBar();

    }

    @Override
    protected void onResume() {
        prevThreadBtn();
        playThreadBtn();
        nextThreadBtn();
        super.onResume();
    }

    private void prevThreadBtn() {
        prevThread = new Thread(){
            @Override
            public void run() {
                super.run();
                mPrevious.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();;
    }

    private void prevBtnClicked() {
        position=(position-1)<0?listSongs.size()-1:position-1;
        startSongAtPosition();
    }

    private void playThreadBtn() {
        playThread = new Thread(){
            @Override
            public void run() {
                super.run();
                mPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();;
    }

    private void playPauseBtnClicked() {
        if(mediaPlayer.isPlaying()) {
            mPlay.setImageResource(R.drawable.ic_play);
            mediaPlayer.pause();
        } else {
            mPlay.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
        setDurationSeekBar();
    }

    private void nextThreadBtn() {
        nextThread = new Thread(){
            @Override
            public void run() {
                super.run();
                mNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();;
    }

    private void nextBtnClicked() {
        position=((position+1)%listSongs.size());
        startSongAtPosition();
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
        metaData(uri);
    }

    private void metaData(Uri uri)
    {
        byte[] art= null;
        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri.toString());
            int durationTotal = Integer.parseInt(listSongs.get(position).getDuration())/1000;
            mDurationTotal.setText(formattedTime(durationTotal));
            art = retriever.getEmbeddedPicture();
            Bitmap bitmap;
            retriever.release();
            if(art!=null){
                Glide.with(this).asBitmap().load(art).into(mAlbumArt);
                bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@Nullable Palette palette) {
                        Palette.Swatch swatch = palette.getDominantSwatch();
                        ImageView mGradient = findViewById(R.id.gradient);
                        LinearLayout mContainer = findViewById(R.id.linearLayout);
                        if(swatch!=null)
                        {
                            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{swatch.getRgb(),0x00000000});
                            mGradient.setBackground(gradientDrawable);
                            GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{swatch.getRgb(),swatch.getRgb()});
                            mContainer.setBackground(gradientDrawableBg);
                            mSongName.setTextColor(swatch.getTitleTextColor());
                            mSongArtist.setTextColor(swatch.getBodyTextColor());
                        }
                        else
                        {
                            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{0xff000000,0x00000000});
                            mGradient.setBackground(gradientDrawable);
                            GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{0xff000000,0xff000000});
                            mContainer.setBackground(gradientDrawableBg);
                            mSongName.setTextColor(Color.RED);
                            mSongArtist.setTextColor(Color.BLUE);
                        }
                    }
                });
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startSongAtPosition() {
        if(mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mPlay.setImageResource(R.drawable.ic_pause);
        uri=Uri.parse(listSongs.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        metaData(uri);
        mSongName.setText(listSongs.get(position).getTitle());
        mSongArtist.setText(listSongs.get(position).getArtist());
        setDurationSeekBar();
        mediaPlayer.start();
    }

    private void setDurationSeekBar() {
        mSeekBar.setMax(mediaPlayer.getDuration()/1000);
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

}