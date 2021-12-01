// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import gov.nasa.worldwind.BasicModel
import gov.nasa.worldwind.awt.WorldWindowGLCanvas
import kotlinx.coroutines.delay
import javax.swing.BoxLayout
import javax.swing.JPanel

// Purpose of this sample is to show the trick how to draw and control(click responses) Compose elements over Heavyweight Swing/AWT components like Maps, VideoPlayers etc...
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose for Desktop",
        state = rememberWindowState(width = 800.dp, height = 500.dp)
    ) {
        MaterialTheme {
            RootUI()
        }
    }
}

@Composable
fun RootUI() {
    SwingPanel( // <--- Swing panel as root for hierarchy where drawing over heavyweight components needed (Swing/Compose switching trick START)
        modifier = Modifier.fillMaxSize().background(Color.Yellow),
        factory = {
            ComposePanel().apply { // <--- Switch back to Compose world for layout management purposes (Swing/Compose switching trick END)
                setContent {
                    val selectedMode = remember { mutableStateOf(Mode.Control) }
                    Box { // <--- Here we use Box to make layering of SwingPanels

                        // Bottom MAP layer
                        SwingPanel( // <--- Here we use Swing panel to show WorldWindGLCanvas from Swing world (proper use of SwingPanel)
                            modifier = Modifier.fillMaxSize().background(Color.Black),
                            factory = { createJPanelWithWorldWindMap() }
                        )

                        // Map OVERLAY layer
                        SwingPanel( // <--- Here we use Swing panel to make a trick wrapping Compose layout (Swing/Compose switching trick START). If we do not wrap Compose with SwingPanel here, you will not see Compose content over WorldWindGLCanvas(Swing)
                            modifier = Modifier.width(60.dp).fillMaxHeight().align(Alignment.CenterStart),
                            factory = {
                                ComposePanel().apply { // <-- Swing/Compose switching trick END
                                    setContent {
                                        ComposeOverlay(selectedMode = selectedMode.value, onModeChanged = { selectedMode.value = it }) // <--- This is our Compose components we want to draw over WorldWindGLCanvas(Swing)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ComposeOverlay(selectedMode: Mode, onModeChanged: (Mode) -> Unit) {
    Box(
        Modifier.fillMaxHeight(),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.background(
                shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp),
                color = Color.DarkGray.copy(alpha = 0.5f)
            )
        ) {
            ModeTab(
                modifier = Modifier.clickable { onModeChanged(Mode.Control) }
                    .padding(top = 16.dp, bottom = 8.dp),
                "Control",
                selected = selectedMode == Mode.Control
            )
            ModeTab(
                modifier = Modifier.clickable { onModeChanged(Mode.Plan)  }
                    .padding(top = 8.dp, bottom = 8.dp),
                "Plan",
                selected = selectedMode == Mode.Plan
            )
        }
    }
}

private fun createJPanelWithWorldWindMap(): JPanel {
    return JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        this.background = java.awt.Color.RED
        add(WorldWindowGLCanvas().apply {
            model = BasicModel()
        })
    }
}

enum class Mode {
    Control,
    Plan
}

@Composable
fun ModeTab(
    modifier: Modifier = Modifier,
    text: String = "",
    icon: ImageVector = Icons.Default.AccountBox,
    selected: Boolean = false
) {
    Column(
        modifier.width(72.dp).alpha(if (selected) 1.0f else 0.5f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(modifier = Modifier.size(32.dp), imageVector = icon, contentDescription = null, tint = Color.White)
        Text(text = text, color = Color.White, fontSize = 10.sp)
    }
}