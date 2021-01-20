package com.saurabh.mytunes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;

import static com.saurabh.mytunes.MainActivity.musicFiles;
import static com.saurabh.mytunes.MainActivity.queuePlayer;
import static com.saurabh.mytunes.MainActivity.songRepeat;
import static com.saurabh.mytunes.MainActivity.songShuffle;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener{

    TextView mNowPlaying, mSongName, mSongArtist, mDurationPlayed, mDurationTotal;
    ImageView mAlbumArt, mPrevious, mNext, mShuffle, mRepeat, mBack, mQueue;
    CardView mCardView;
    FloatingActionButton mPlay;
    SeekBar mSeekBar;
    int position, positionAdded;

    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static MediaPlayer mediaPlayer;
    static Uri uri;

    private Handler handler = new Handler();
    private Thread prevThread, playThread, nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        positionAdded = getIntent().getIntExtra("position",-1);

        mNowPlaying = findViewById(R.id.nowPlaying);
        mSongName = findViewById(R.id.songName);
        mSongArtist = findViewById(R.id.songArtist);
        mDurationPlayed = findViewById(R.id.durationPlayed);
        mDurationTotal = findViewById(R.id.durationTotal);

        mCardView=findViewById(R.id.cardView);

        mAlbumArt = findViewById(R.id.albumArt);
        mPrevious = findViewById(R.id.previous);
        mNext = findViewById(R.id.next);
        mShuffle = findViewById(R.id.shuffle);
        mRepeat = findViewById(R.id.repeat);
        mBack = findViewById(R.id.back);
        mQueue = findViewById(R.id.queue);

        mPlay = findViewById(R.id.play);

        mSeekBar = findViewById(R.id.seekBar);

        if(!listSongs.contains(musicFiles.get(positionAdded))&&!queuePlayer){
            listSongs.add(musicFiles.get(positionAdded));
            Toast.makeText(this,listSongs.get(position).getTitle()+" is added to the queue",Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this,listSongs.get(position).getTitle()+" is already present the queue",Toast.LENGTH_SHORT).show();
        position=listSongs.size()-1;
        if(queuePlayer)
            position=positionAdded;
        if(listSongs!=null){
            getIntentMethods();
            mSongName.setText(listSongs.get(position).getTitle());
            mSongArtist.setText(listSongs.get(position).getArtist());
            mediaPlayer.setOnCompletionListener(this);
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

        mShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(songShuffle){
                    songShuffle=false;
                    mShuffle.setImageResource(R.drawable.ic_shuffle_off);
                }
                else{
                    songShuffle=true;
                    mShuffle.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });
        mRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(songRepeat){
                    songRepeat=false;
                    mRepeat.setImageResource(R.drawable.ic_repeat_one_off);
                }
                else{
                    songRepeat=true;
                    mRepeat.setImageResource(R.drawable.ic_repeat_one_on);
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), QueueActivity.class);
                startActivity(intent);
                finish();
            }
        });

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
                        position=(position-1)<0?listSongs.size()-1:position-1;
                        startSongAtPosition();
                    }
                });
            }
        };
        prevThread.start();;
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
                        next();
                    }
                });
            }
        };
        nextThread.start();;
    }

    public void next(){
        if(songShuffle && !songRepeat)
            position = (new Random()).nextInt(listSongs.size());
        else if(!songShuffle && !songRepeat)
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
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        Log.e("No. of songs", Integer.toString(listSongs.size()));
        Log.e("Playing", Integer.toString(position+1));
        mediaPlayer.start();
        mSongName.setText(listSongs.get(position).getTitle());
        mSongArtist.setText(listSongs.get(position).getArtist());
        setDurationSeekBar();
        mSeekBar.setMax(mediaPlayer.getDuration()/1000);
        metaData(uri);
        mediaPlayer.setOnCompletionListener(this);
    }

    private void metaData(Uri uri)
    {
        byte[] art = null;
        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri.toString());
            int durationTotal = Integer.parseInt(listSongs.get(position).getDuration())/1000;
            mDurationTotal.setText(formattedTime(durationTotal));
            art = retriever.getEmbeddedPicture();
            Bitmap bitmap;
            retriever.release();
            if(art==null)
            {
                Drawable drawable = getResources().getDrawable(R.drawable.defaultart);
                bitmap = ((BitmapDrawable)drawable).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                art = stream.toByteArray();
            }
            if(art!=null){
                bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
                ImageAnimation(this,mAlbumArt,bitmap);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@Nullable Palette palette) {
                        Palette.Swatch swatch = palette.getDominantSwatch();
                        LinearLayout mContainer = findViewById(R.id.linearLayout);
                        GradientDrawable gradientDrawableBg;
                        if(swatch!=null)
                        {
                            gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{swatch.getRgb(),swatch.getRgb()});
                            mNowPlaying.setTextColor(swatch.getTitleTextColor());
                            mDurationPlayed.setTextColor(swatch.getBodyTextColor());
                            mDurationTotal.setTextColor(swatch.getBodyTextColor());
                        }
                        else
                            gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{0xff000000,0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                        mSongName.setTextColor(swatch.getTitleTextColor());
                        mSongArtist.setTextColor(swatch.getBodyTextColor());
                    }
                });
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startSongAtPosition() {
        Log.e("No. of songs", Integer.toString(listSongs.size()));
        Log.e("Playing", Integer.toString(position+1));
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
        mediaPlayer.setOnCompletionListener(this);
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

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(mediaPlayer!=null){
            next();
            getIntentMethods();
            mediaPlayer.setOnCompletionListener(this);
        }
    }

    public void ImageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap){
        final Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        imageView.startAnimation(animOut);
    }
}