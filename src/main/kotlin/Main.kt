// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.background
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
import java.awt.Dimension
import java.util.*
import javax.swing.BoxLayout
import javax.swing.JPanel

// Purpose of this sample is to show the trick how to draw and control(click responses) Compose elements over Heavyweight Swing/AWT components like Maps, VideoPlayers etc...
fun main() = application {
    setupSkikoRenderAPI()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose for Desktop",
        state = rememberWindowState(width = 1000.dp, height = 600.dp)
    ) {
        MaterialTheme {
            RootUI()
        }
    }
}

@Composable
fun RootUI() {
    Column {
        var leftPanelShown by remember { mutableStateOf(false) }
        var rightPanelShown by remember { mutableStateOf(false) }
        var bottomPanelShown by remember { mutableStateOf(false) }
        var swingComposeShown by remember { mutableStateOf(true) }
        var composeShown by remember { mutableStateOf(true) }
        var mapShown by remember { mutableStateOf(true) }
        var overlaysLeft by remember { mutableStateOf(true) }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(onClick = { leftPanelShown = !leftPanelShown }) {
                Text(if (leftPanelShown) "Hide Left" else "Show Left")
            }
            Button(onClick = { bottomPanelShown = !bottomPanelShown }) {
                Text(if (bottomPanelShown) "Hide Bottom" else "Show Bottom")
            }
            Button(onClick = { rightPanelShown = !rightPanelShown }) {
                Text(if (rightPanelShown) "Hide Right" else "Show Right")
            }
            Button(onClick = { mapShown = !mapShown }) {
                Text(if (mapShown) "Hide Map" else "Show Map")
            }
            Button(onClick = { swingComposeShown = !swingComposeShown }) {
                Text(if (swingComposeShown) "Hide Swing Compose" else "Show Swing Compose")
            }
            Button(onClick = { composeShown = !composeShown }) {
                Text(if (composeShown) "Hide Compose" else "Show Compose")
            }
            Button(onClick = { overlaysLeft = !overlaysLeft }) {
                Text(if (overlaysLeft) "Move Right" else "Move Left")
            }
        }
        Row(Modifier.weight(1f)) {
            if (leftPanelShown) {
                Box(
                    Modifier.width(200.dp).fillMaxHeight().background(Color.Blue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Left Panel")
                }
            }
            val selectedMode = remember { mutableStateOf(Mode.Control) }
            Box(Modifier.weight(1f)) { // <--- Here we use Box to make layering of SwingPanels
                if (mapShown) {
                    // Bottom MAP layer
                    SwingPanel( // <--- Here we use Swing panel to show WorldWindGLCanvas from Swing world (proper use of SwingPanel)
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        factory = { createJPanelWithWorldWindMap() }
                    )
                }
                Row {
                    if (!overlaysLeft) {
                        Spacer(Modifier.weight(1f))
                    }
                    OverlaysUI(Modifier.padding(8.dp).width(100.dp), swingComposeShown, composeShown)
                    if (overlaysLeft) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
            if (rightPanelShown) {
                Box(
                    Modifier.width(200.dp).fillMaxHeight().background(Color.Magenta),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Right Panel")
                }
            }
        }
        if (bottomPanelShown) {
            Box(Modifier.fillMaxWidth().height(100.dp).background(Color.Cyan), contentAlignment = Alignment.Center) {
                Text("Bottom Panel")
            }
        }
    }
}

@Composable
private fun OverlaysUI(modifier: Modifier, swingComposeShown: Boolean, composeShown: Boolean) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (swingComposeShown) {
            SwingPanel( // <--- Here we use Swing panel to make a trick wrapping Compose layout (Swing/Compose switching trick START). If we do not wrap Compose with SwingPanel here, you will not see Compose content over WorldWindGLCanvas(Swing)
                modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Yellow, RoundedCornerShape(12.dp)),
                factory = {
                    ComposePanel().apply { // <-- Swing/Compose switching trick END
                        setContent {
                            Box(
                                Modifier.fillMaxSize().background(Color.Green, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Swing Compose")
                            }
                        }
                    }
                }
            )
        }
        if (composeShown) {
            Box(
                Modifier.fillMaxWidth().weight(1f).background(Color.Green, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Compose")
            }
        }
    }
}


private fun createJPanelWithWorldWindMap(): JPanel {
    return JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        this.background = java.awt.Color.RED
        add(WorldWindowGLCanvas().apply {
            preferredSize = Dimension(10, 10)
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

private fun setupSkikoRenderAPI() {
    when (getOS()) {
        OS.LINUX, OS.WINDOWS -> System.setProperty("skiko.renderApi", "OPENGL")
        OS.MAC -> System.setProperty("skiko.renderApi", "METAL")
        else -> {}
    }
}

fun getOS(): OS? {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    return when {
        os.contains("win") -> {
            OS.WINDOWS
        }

        os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
            OS.LINUX
        }

        os.contains("mac") -> {
            OS.MAC
        }

        os.contains("sunos") -> {
            OS.SOLARIS
        }

        else -> null
    }
}

enum class OS {
    WINDOWS, LINUX, MAC, SOLARIS, ANDROID
}
