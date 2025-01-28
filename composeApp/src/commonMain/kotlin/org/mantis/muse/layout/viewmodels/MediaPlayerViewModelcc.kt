package org.mantis.muse.layout.viewmodels

import androidx.lifecycle.ViewModel
//import org.mantis.muse.util.Playlist



//class MediaPlayerViewModelcc(

//): ViewModel() {
//    var currentSongIndex: Int = 0
//    var currentPlaylist: Playlist? = null

//    var playing: Boolean = false
//    var loopState: LoopState = LoopState.None
//    var shuffling: Boolean = false
//
//    fun play(){
//        this.playing = true
//
//    }
//
//    fun pause(){
//        this.playing = false
//    }
//
//    fun togglePlayPauseState(){
//        this.playing = !this.playing
//    }
//
////    fun skipNext(){
////        if (currentPlaylist == null) return
////        currentSongIndex = min(currentSongIndex+1, currentPlaylist!!.size)
////    }
////
////    fun skipLast(){
////        if (currentPlaylist == null) return
////        currentSongIndex = max(currentSongIndex-1, 0)
////    }
//
//    fun toggleShuffle(){
//        this.shuffling = !this.shuffling
//    }
//
//    fun setLoopState(state: LoopState){
//        this.loopState = state
//    }
//}