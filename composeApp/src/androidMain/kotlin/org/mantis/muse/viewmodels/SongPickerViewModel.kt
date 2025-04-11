package org.mantis.muse.viewmodels

import androidx.lifecycle.ViewModel
import org.mantis.muse.repositories.SongRepository
import org.mantis.muse.util.AndroidMediaPlayer

class SongPickerViewModel(
    val songRepository: SongRepository,
    val player: AndroidMediaPlayer
): ViewModel() {

}