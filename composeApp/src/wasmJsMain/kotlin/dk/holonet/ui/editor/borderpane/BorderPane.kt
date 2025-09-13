package dk.holonet.ui.editor.borderpane

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dk.holonet.core.Position

@Composable
fun BorderPane(
    onPositionClick: (Position) -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
                .padding(16.dp)
                .background(Color.Black)
        ) {
            // Top bar (light gray), pinned to the top
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.05f)
                    .background(Color.LightGray)
                    .clickable {
                        onPositionClick(Position.TOP_BAR)
                    }
            )

            // Bottom bar (light gray), pinned to the bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.05f)
                    .background(Color.LightGray)
                    .clickable {
                        onPositionClick(Position.BOTTOM_BAR)
                    }
            )

            // Main content column, taking the remaining space between top/bottom bars
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // ----------------------
                // Top row with 3 blocks
                // ----------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                        .padding(0.dp, 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Top-left
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.Red)
                            .clickable {
                                onPositionClick(Position.TOP_LEFT)
                            }
                    )

                    // Top-center
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.Blue)
                            .clickable {
                                onPositionClick(Position.TOP_CENTER)
                            }
                    )

                    // Top-right
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.Green)
                            .clickable {
                                onPositionClick(Position.TOP_RIGHT)
                            }
                    )
                }

                // ---------------------------------------
                // Middle stack:
                // upper_third,
                // middle_center,
                // lower_third
                // ---------------------------------------
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                        .padding(0.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Upper third
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Yellow)
                            .clickable {
                                onPositionClick(Position.UPPER_THIRD)
                            }
                    )

                    // Middle center
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Cyan)
                            .clickable {
                                onPositionClick(Position.MIDDLE_THIRD)
                            }
                    )

                    // Lower third
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Magenta)
                            .clickable {
                                onPositionClick(Position.LOWER_THIRD)
                            }
                    )
                }

                // -------------------------
                // Bottom row with 3 blocks
                // -------------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                        .padding(0.dp, 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bottom-left
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.Red)
                            .clickable {
                                onPositionClick(Position.BOTTOM_LEFT)
                            }
                    )

                    // Bottom-center
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.Blue)
                            .clickable {
                                onPositionClick(Position.BOTTOM_CENTER)
                            }
                    )

                    // Bottom-right
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.Green)
                            .clickable {
                                onPositionClick(Position.BOTTOM_RIGHT)
                            }
                    )
                }
            }
        }
    }
}