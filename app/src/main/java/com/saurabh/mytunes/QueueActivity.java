package com.saurabh.mytunes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import static com.saurabh.mytunes.MainActivity.musicFiles;
import static com.saurabh.mytunes.MainActivity.queuePlayer;
import static com.saurabh.mytunes.PlayerActivity.listSongs;

public class QueueActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MusicAdapter musicAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        queuePlayer=true;
        if(!(musicFiles.size()<1))
        {
            musicAdapter = new MusicAdapter(this, listSongs);
            recyclerView.setAdapter(musicAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        }
    }
}