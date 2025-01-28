package com.example.muse

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class PlaylistPosition(val pos: Int)

class PlaylistSongSelection : ComponentActivity() {
    val context = this
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.playlist_song_list)
        val bigContainer = findViewById<LinearLayout>(R.id.SongList)

        loadNav()

        if (!MediaManager.doSongsExist()){val nothing = TextView(this);nothing.text = "No Playlist is loaded"; nothing.textSize = 30f; nothing.textAlignment = TextView.TEXT_ALIGNMENT_CENTER; nothing.setTextColor(resources.getColor(R.color.black)); bigContainer.addView(nothing);return}

        val songList = MediaManager.songs

        val scroll = ScrollView(this)
        scroll.isVerticalScrollBarEnabled = false
        val offsetPx = resources.getDimensionPixelOffset(R.dimen.loaded_playlist_bottom_offset)
        scroll.setPadding(0,0,0,offsetPx)
        bigContainer.addView(scroll)
        val host = LinearLayout(this)
        host.orientation = LinearLayout.VERTICAL
        scroll.addView(host)

        GlobalScope.launch(Dispatchers.IO){
            for (i in 0 until songList.size) {
                val song = songList[i]
                val name = song.name
                val artist = song.artists
                val img = song.getAlbumCover()
                val container = LinearLayout(context)
                container.orientation = LinearLayout.HORIZONTAL
                if (MediaManager.playlistPosition == i) {
                    container.setBackgroundColor(resources.getColor(R.color.pink_light))
                    container.setOnClickListener{
                        Intent(context, MainActivity::class.java).also { startActivity(it) }
                        overridePendingTransition(R.anim.enter_from_right,R.anim.exit_to_left)
                    }
                } else {
                    container.setOnClickListener {
                        Log.d("some", "$i")
                        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
                        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
                        controllerFuture.addListener(
                            {
                                val con = controllerFuture.get()
                                MediaManager.loadTrack(i)
                                con.seekTo(i, 0)
                            },
                            MoreExecutors.directExecutor())
                        Intent(context, MainActivity::class.java).also { startActivity(it) }
                        overridePendingTransition(R.anim.enter_from_right,R.anim.exit_to_left)
                    }
                }

                val titleAuthorContainer = LinearLayout(context)
                titleAuthorContainer.orientation = LinearLayout.VERTICAL
                titleAuthorContainer.gravity = Gravity.CENTER

                val titleView = TextView(context)
                titleAuthorContainer.addView(titleView)
                titleView.text = name

                val artistView = TextView(context)
                titleAuthorContainer.addView(artistView)
                artistView.text = artist

                val thumbnailView = ImageView(context)
                thumbnailView.setImageBitmap(img)
                val thumbnailViewWindowLayout = LinearLayout.LayoutParams(
                    200, 200
                )
                thumbnailView.layoutParams = thumbnailViewWindowLayout


                container.addView(thumbnailView)
                container.addView(titleAuthorContainer)

                withContext(Dispatchers.Main){
                    host.addView(container)
                }

            }
        }
    }

    private fun loadNav(){
        // I cbf writing a separate function per page so Im copy and pasting a template in every file.
        val playlistItemsButton = findViewById<ImageButton>(R.id.SongListButtonPLP)
        val musicControlsButton = findViewById<ImageButton>(R.id.mediaControlsButtonPLP)
        val playlistSelectionButton = findViewById<ImageButton>(R.id.SongSelectionButtonPLP)

//        playlistItemsButton.setOnClickListener {
//
//        }

        musicControlsButton.setOnClickListener {
            val i = Intent(this@PlaylistSongSelection, MainActivity::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.enter_from_right,R.anim.exit_to_left)
        }

        playlistSelectionButton.setOnClickListener {
            val i = Intent(this@PlaylistSongSelection, SongSelection::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.enter_from_right,R.anim.exit_to_left)
        }
    }
}