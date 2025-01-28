package org.mantis.muse.layouts.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import org.mantis.muse.R
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun ShuffleButton(
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.shuffle_icon),
            contentDescription = "",
            tint = tint,
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }

}