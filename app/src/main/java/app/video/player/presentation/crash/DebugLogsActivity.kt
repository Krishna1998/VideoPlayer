package app.video.player.presentation.crash

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import app.video.player.ui.theme.VideoPlayerTheme

class DebugLogsActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      VideoPlayerTheme {
        DebugLogsScreen(onNavigateBack = ::finish)
      }
    }
  }
}