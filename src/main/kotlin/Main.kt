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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import gov.nasa.worldwind.BasicModel
import gov.nasa.worldwind.awt.WorldWindowGLCanvas
import org.jetbrains.skiko.GraphicsApi
import java.awt.Dimension
import java.util.*
import javax.swing.BoxLayout
import javax.swing.JPanel

// MAC
// Comment: clipping works only for regular swing content but not for heavyweight or compose
// Comment: on Mac - no chance to work over heavyweight if graphics.on = true (not displaying at all)
// Comment: on Mac - swing panel over heavyweight works but with positioning problems

// WINDOWS
// Without flags overlay renders but in another order - you cant turn off and then on overlay - you should off/on map to see overlay
// Blending on: WW doesn't shown at all

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
    Column(Modifier.background(Color.LightGray)) {
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
            Box(Modifier.weight(1f)) { // <--- Here we use Box to make layering of SwingPanels
                if (mapShown) {
                    // Bottom MAP layer
                    SwingPanel( // <--- Here we use Swing panel to show WorldWindGLCanvas from Swing world (proper use of SwingPanel)
                        modifier = Modifier.fillMaxSize().background(Color.Black).clip(RoundedCornerShape(12.dp)),
                        factory = { createJPanelWithWorldWindMap() }
                    )
                }
                Popup(alignment = Alignment.Center) {
                    Box(
                        Modifier.size(200.dp, 100.dp).background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Popup")
                    }
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
//            Box(Modifier.weight(1f).fillMaxWidth().background(Color.DarkGray, RoundedCornerShape(12.dp))) {
                SwingPanel( // <--- Here we use Swing panel to make a trick wrapping Compose layout (Swing/Compose switching trick START). If we do not wrap Compose with SwingPanel here, you will not see Compose content over WorldWindGLCanvas(Swing)
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).weight(1f).fillMaxWidth().clip(RoundedCornerShape(6.dp)),
                    factory = {
                        ComposePanel().apply { // <-- Swing/Compose switching trick END
                            background = java.awt.Color.PINK
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
//            }
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

private fun setupSkikoRenderAPI() {
    //System.setProperty("compose.swing.render.on.graphics", "true")
    //System.setProperty("compose.interop.blending", "true")
    //System.setProperty("compose.layers.type", "true") // OnSameCanvas, Dialog, Popup


    when (getOS()) {
        OS.LINUX -> System.setProperty("skiko.renderApi", "OPENGL")
        OS.WINDOWS -> System.setProperty("skiko.renderApi", "DIRECT3D")
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
