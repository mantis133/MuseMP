package com.example.muse


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_PREPARE
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import java.lang.IllegalStateException
import java.io.File


class MainActivity : ComponentActivity(R.layout.music_controls) {
    private lateinit var PlayButton: ImageButton
    private lateinit var NextButton : ImageButton
    private lateinit var LastButton : ImageButton
    private lateinit var ShuffleButton : ImageButton
    private lateinit var LoopButton : ImageButton

    private lateinit var SongSeekBar: SeekBar
    private lateinit var CurrentSongPosText: TextView
    private lateinit var TotalSongTime: TextView

    private lateinit var ThumbnailImage: ImageView
    private lateinit var SongNameTexView: TextView
    private lateinit var ArtistTextView : TextView

    private lateinit var receiver : BroadcastReceiver

    private lateinit var con: MediaController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setContentView(R.layout.music_controls)

        PlayButton = findViewById<ImageButton>(R.id.PlayButton)
        NextButton = findViewById(R.id.NextButton)
        LastButton = findViewById(R.id.LastButton)
        ShuffleButton = findViewById(R.id.ShuffleButton)
        LoopButton = findViewById(R.id.LoopButton)

        SongSeekBar = findViewById(R.id.SongSeekBar)
        CurrentSongPosText = findViewById(R.id.CurrentSongPositionText)
        TotalSongTime = findViewById(R.id.TotalSongTimeText)

        ThumbnailImage = findViewById(R.id.ThumbNailImg)
        SongNameTexView = findViewById(R.id.SongName)
        ArtistTextView = findViewById(R.id.ArtistsNames)

//        ThumbnailImage.scaleType = ImageView.ScaleType.CENTER_CROP

        updateSongInfoUI()
        listeners()


        SongNameTexView.isSelected = true

        SongSeekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                CurrentSongPosText.text = MilliSecsToMins(progress)

                if (fromUser) {
                    Log.d("SeekBar Update", "pos:${progress}")
                    Log.d("SeekBar Update", "max:${SongSeekBar.max}")
                    Log.d("SeekBar Update", "should be:${con.currentPosition}")
                    Log.d("SeekBar Update", "should be:${con.duration}")
                    con.seekTo(progress.toLong())
                }

                if (progress == SongSeekBar.max){
//                    MediaManager.pause()
                    PlayButton.setImageResource(R.drawable.play_button)
//                    MediaManager.skipNext()
                    SongSeekBar.progress = 0
                    updateSongInfoUI()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })


        onSkipNext(NextButton)
        onSkipLast(LastButton)
        onPlayPause(PlayButton)
        onShuffle(ShuffleButton)
        onLoop(LoopButton)
        loadNav()

        val externalStorageVolumes: Array<File?> = ContextCompat.getExternalFilesDirs(this, null)
        val sdCardStorage = externalStorageVolumes
        for (file in sdCardStorage){
            Log.d("dirs","$file")
        }

    }

    override fun onStart() {
        super.onStart()
        Log.d("creation","start")
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                con = controllerFuture.get()
                MediaManager.loadTrack(con.currentMediaItemIndex) // using this instead of initialize() leads the shuffle not being reset
                updateSongInfoUI() // call loadTrack before here so that

                val updateSeekBar = object : Runnable {
                    override fun run() {
                        try {
                            SongSeekBar.progress = con.currentPosition.toInt() ?: 0
                            SongSeekBar.postDelayed(this, 1000)
                        } catch (e:IllegalStateException) {
                            // this just happens when the app swaps activity
                            // this is because the media player is released causing the top call in the try block to access a variable that doesn't exist
                            // idk what to put here cause leaving it blank just works
                        } catch (e:UninitializedPropertyAccessException){
                            // happens when the MediaController is not yet initialized
                            Log.d("SeekBar Update", "it happened again")
                        }
                    }
                }
                SongSeekBar.post(updateSeekBar)

                con.addListener(
                    object : Player.Listener {
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            super.onMediaItemTransition(mediaItem, reason)
                            MediaManager.loadTrack(con.currentMediaItemIndex)
                            updateSongInfoUI()
                            Log.d("listener", "heard")
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            super.onPlaybackStateChanged(playbackState)
                            updateSongInfoUI()
                        }
                    }
                )

            },
            MoreExecutors.directExecutor())

    }

    override fun onStop() {
        super.onStop()
        Log.d("creation","stop")
        con.release()
//        MediaController.releaseFuture(controllerFuture)
    }



    private fun MilliSecsToMins(Milliseconds: Int): String {
        val seconds = Milliseconds / 1000
        val mins = seconds / 60
        val secs = seconds % 60
        val totalStringMins = String.format("%02d", mins)
        val totalStringSecs = String.format("%02d", secs)
        return "$totalStringMins:$totalStringSecs"
    }

    private fun loadNav(){
        // I cbf writing a separate function per page so Im copy and pasting a template in every file.
        val playlistItemsButton = findViewById<ImageButton>(R.id.SongListButton)
        val musicControlsButton = findViewById<ImageButton>(R.id.mediaControlsButton)
        val playlistSelectionButton = findViewById<ImageButton>(R.id.SongSelectionButton)

        playlistItemsButton.setOnClickListener {
            val i = Intent(this@MainActivity, PlaylistSongSelection::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.enter_from_left,R.anim.exit_to_right)
        }

//        musicControlsButton.setOnClickListener {
//
//        }

        playlistSelectionButton.setOnClickListener {
            val i = Intent(this@MainActivity, SongSelection::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.enter_from_right,R.anim.exit_to_left)
        }
    }

    private fun updateSongInfoUI(){
        try{
            val dur = con.duration ?: 0
            SongSeekBar.max = dur.toInt()
            TotalSongTime.text = MilliSecsToMins(dur.toInt())

//            val mediaMetaData = con.currentMediaItem!!.mediaMetadata

            SongNameTexView.text = MediaManager.SongName
            ArtistTextView.text = MediaManager.ArtistName
            when(MediaManager.AlbumArtBitMap){
                null -> ThumbnailImage.setImageResource(R.drawable.home_icon)
                else -> ThumbnailImage.setImageBitmap(MediaManager.AlbumArtBitMap)
            }
        } catch (e: UninitializedPropertyAccessException){
            Log.d("here","path 2")
            SongNameTexView.text = "No Song Playing"
            ArtistTextView.text = "Unknown"
            ThumbnailImage.setImageResource(R.drawable.home_icon)
        }
        if (MediaManager.isPaused) {
            PlayButton.setImageResource(R.drawable.play_button)
        } else {
            PlayButton.setImageResource(R.drawable.pause_button)
        }


//        media.artist
//        media.title
//        media.artworkUri

    }

    private fun listeners(){
        receiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                when(p1?.action){
                    "com.muse.update_song_info" -> {
                        updateSongInfoUI()
                    }
                }
            }
        }
        val intentFilter = IntentFilter().apply {
            addAction("com.muse.update_song_info")
        }

        registerReceiver(receiver,intentFilter)
    }

    private fun onPlayPause(button:ImageButton){
        if (MediaManager.isPaused) {
            button.setImageResource(R.drawable.play_button)
        } else {
            button.setImageResource(R.drawable.pause_button)
        }
        button.setOnClickListener{
            try {
                MediaManager.isPaused = con.isPlaying
                // check that con is initialized -> check if con does not have a media item loaded
                // if both of these are true then reload the playlist from the one stored in media manager
                if (this::con.isInitialized && con.currentMediaItem == null) {
                    if (con.isCommandAvailable(COMMAND_PREPARE)){
                        if (MediaManager.loadPlaylistIntoMediaSession(MediaManager.songs, con)) {
                            con.prepare()
                        }
                    }
                }
                if (MediaManager.isPaused) {
                    button.setImageResource(R.drawable.play_button)
                    con.pause()
                } else {
                    button.setImageResource(R.drawable.pause_button)
                    con.play()
                }
            } catch (e: UninitializedPropertyAccessException){
                // this will happen if the MediaController does not currently exist
                // i.e the media cannot be played, so this does not need to change
                // so we can return out
                return@setOnClickListener
            }
        }
    }

    private fun onSkipNext(button: ImageButton){
        button.setOnClickListener {
            updateSongInfoUI()
            con.seekToNextMediaItem()


        }
    }

    private fun onSkipLast(button: ImageButton){
        button.setOnClickListener {
            updateSongInfoUI()
            con.seekToPreviousMediaItem()
            Log.d("TEST SESSION", con.currentPosition.toString())
            Log.d("TEST SESSION", con.currentPosition.toInt().toString())
            Log.d("TEST SESSION", con.duration.toString())
            Log.d("TEST SESSION", con.currentMediaItem!!.mediaMetadata.artist.toString())
            Log.d("TEST SESSION", con.currentMediaItem!!.mediaMetadata.title.toString())
            Log.d("TEST SESSION", con.playlistMetadata.artist.toString())
            Log.d("TEST SESSION", con.nextMediaItemIndex.toString())
            Log.d("TEST SESSION", con.isPlaying.toString())


        }

    }

    private fun onShuffle(button: ImageButton){

//        if (MediaManager.playlist == null){return}

        if (MediaManager.shuffled) {
            button.setColorFilter(R.color.banana)
        } else {
            button.setColorFilter(null)
        }
        button.setOnClickListener{
            MediaManager.toggleShuffle(con)
            if (MediaManager.shuffled) {
                button.setColorFilter(R.color.banana)
            } else {
                button.setColorFilter(null)
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun onLoop(button: ImageButton){
//        when (MediaManager.looping){
//            0 -> button.setColorFilter(R.color.black)
//            1 -> button.setColorFilter(R.color.banana)
//            2 -> button.setColorFilter(R.color.teal_200)
//            else -> button.setColorFilter(null)
//        }
        button.setOnClickListener {
            MediaManager.looping = (MediaManager.looping + 1) % 3
            try{
                con.repeatMode = (con.repeatMode + 1) % 3
            } catch (e: UninitializedPropertyAccessException) {
                Log.d("EXCEPTION", "Tried to loop without a media instance")
            }
            when (MediaManager.looping){
                0 -> button.setImageResource(R.drawable.fluent_arrow_repeat_all_20_regular32)
                1 -> button.setImageResource(R.drawable.fluent_arrow_repeat_1_20_filled32)
                2 -> button.setImageResource(R.drawable.fluent_arrow_repeat_all_20_filled32)
                else -> button.setColorFilter(null)
            }
        }
    }

    @Override
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onResume() { // think this is when the activity regains focus
        super.onResume()
        updateSongInfoUI()
        Log.d("resum", "resumed")
    }

    override fun onPause() { // think this means when the app is running but the activity is not in focus
        super.onPause()
//        mediaPlayer?.pause()
//        songPosition = mediaPlayer?.currentPosition
        Log.d("paws", "pawsed")
//        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
//        MediaManager.kill()
        unregisterReceiver(receiver)
//        Intent(this, MusicService::class.java).also{it.action = MusicService.Actions.STOP.toString();startService(it)}
    }

}

