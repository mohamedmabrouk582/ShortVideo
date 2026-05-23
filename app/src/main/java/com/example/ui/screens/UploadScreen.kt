package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.VideoPlayerComponent
import com.example.ui.viewmodel.ShortVideoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UploadScreen(
    viewModel: ShortVideoViewModel,
    onUploadSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var creatorName by remember { mutableStateOf("guest_creator") }
    var description by remember { mutableStateOf("") }
    var musicTrack by remember { mutableStateOf("Neon Pulse") }
    var hashtags by remember { mutableStateOf("#vibe #edited") }

    // Stocks/Preloaded templates to choose from
    val videoTemplates = listOf("Skaters", "Gaming", "Cooking", "Forest Stream")
    var selectedTemplate by remember { mutableStateOf("Forest Stream") }

    // Visual Filter Options
    val filtersList = listOf(
        "None", "Sepia", "Black and White", "Vintage", "Glitch",
        "Warm Tone", "Cool Breeze", "Sunset Glow", "Cine Gray", "Neon Cyan"
    )
    var selectedFilter by remember { mutableStateOf("None") }

    // Mood-based Royalty-Free Music Tracks
    val moodTrackCategories = mapOf(
        "Upbeat" to listOf(
            Pair("Neon Pulse", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
            Pair("Techno Rush", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3")
        ),
        "Calm" to listOf(
            Pair("Golden Hour Piano", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"),
            Pair("Morning Dew", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3")
        ),
        "Dramatic" to listOf(
            Pair("Shadow Forest", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"),
            Pair("Thunderous Horizon", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3")
        )
    )
    var selectedMoodTab by remember { mutableStateOf("Upbeat") }
    var selectedBgMusicName by remember { mutableStateOf("None") }
    var activeMusicUrl by remember { mutableStateOf("None") }
    var isCustomMusicSelected by remember { mutableStateOf(false) }

    // Audio mixing studio volumes
    var bgVolume by remember { mutableFloatStateOf(0.5f) }
    var originalVolume by remember { mutableFloatStateOf(0.8f) }

    // Upload simulation progress flows
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var uploadComplete by remember { mutableStateOf(false) }

    // Map template name to MP4 stream URL for live preview
    val previewVideoUrl = remember(selectedTemplate) {
        when (selectedTemplate) {
            "Skaters" -> "https://assets.mixkit.co/videos/preview/mixkit-skater-doing-a-trick-in-a-skatepark-42045-large.mp4"
            "Gaming" -> "https://assets.mixkit.co/videos/preview/mixkit-hands-of-a-gamer-playing-with-a-controller-41618-large.mp4"
            "Cooking" -> "https://assets.mixkit.co/videos/preview/mixkit-chef-plating-a-gourmet-dish-42510-large.mp4"
            else -> "https://assets.mixkit.co/videos/preview/mixkit-forest-stream-in-the-sunlight-529-large.mp4"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App header title
        Text(
            text = "Video Editing Studio",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // Live Real-Time Studio Monitor
        Text(
            text = "Studio Live Monitor (Real-time Preview)",
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                VideoPlayerComponent(
                    videoUrl = previewVideoUrl,
                    isCurrentlyActive = !isUploading, // Pause playback when overlay simulation is active
                    filterName = selectedFilter,
                    bgMusicUrl = activeMusicUrl,
                    bgMusicVolume = bgVolume,
                    originalVideoVolume = originalVolume,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Real-time Badge specifications overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "FX: $selectedFilter",
                            color = Color(0xFFD0BCFF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Music: ${if (activeMusicUrl == "None") "None (Original)" else selectedBgMusicName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 1. Selector for Stock Video Template
        Text(
            text = "Step 1: Choose Video Visual Theme",
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            videoTemplates.forEach { template ->
                val isSelected = selectedTemplate == template
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFFD0BCFF) else Color.White.copy(alpha = 0.08f))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFFD0BCFF) else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTemplate = template }
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                        .testTag("video_template_selector_$template"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = template,
                            tint = if (isSelected) Color(0xFF381E72) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = template,
                            color = if (isSelected) Color(0xFF381E72) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Visual filter selections
        Text(
            text = "Step 2: Select Visual Filter Effect",
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filtersList.size) { index ->
                val filt = filtersList[index]
                val isFiltSelected = selectedFilter == filt
                
                // Color gradient preview reflecting the filters mood
                val previewGradientColors = when (filt) {
                    "Sepia" -> listOf(Color(0xFF704213), Color(0xFF4E2C0A))
                    "Black and White" -> listOf(Color(0xFF555555), Color(0xFF111111))
                    "Vintage" -> listOf(Color(0xFFD84315), Color(0xFF3E2723))
                    "Glitch" -> listOf(Color(0xFF00E5FF), Color(0xFFD500F9))
                    "Warm Tone" -> listOf(Color(0xFFFFB300), Color(0xFFFF6F00))
                    "Cool Breeze" -> listOf(Color(0xFF00B0FF), Color(0xFF0D47A1))
                    "Sunset Glow" -> listOf(Color(0xFFFF4081), Color(0xFF6A1B9A))
                    "Cine Gray" -> listOf(Color(0xFF2C3E50), Color(0xFF0F172A))
                    "Neon Cyan" -> listOf(Color(0xFF00F5D4), Color(0xFF007A78))
                    else -> listOf(Color(0xFF222222), Color(0xFF111111))
                }

                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brush = Brush.verticalGradient(previewGradientColors))
                        .border(
                            2.dp,
                            if (isFiltSelected) Color(0xFFD0BCFF) else Color.White.copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedFilter = filt }
                        .padding(8.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("FX", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            if (isFiltSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Chosen",
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = filt,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Select background music categorize by mood
        Text(
            text = "Step 3: Choose Background Music (By Mood)",
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        TabRow(
            selectedTabIndex = when (selectedMoodTab) {
                "Upbeat" -> 0
                "Calm" -> 1
                else -> 2
            },
            containerColor = Color.Transparent,
            contentColor = Color(0xFFD0BCFF)
        ) {
            listOf("Upbeat", "Calm", "Dramatic").forEach { mood ->
                Tab(
                    selected = selectedMoodTab == mood,
                    onClick = { selectedMoodTab = mood },
                    text = { Text(mood, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                )
            }
        }

        // List selected mood tracks
        val tracksList = moodTrackCategories[selectedMoodTab] ?: emptyList()
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // "None" option to clear background sound
            val isNoneSelected = !isCustomMusicSelected && activeMusicUrl == "None"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isNoneSelected) Color(0xFFD0BCFF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                    .border(
                        1.dp,
                        if (isNoneSelected) Color(0xFFD0BCFF) else Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        isCustomMusicSelected = false
                        selectedBgMusicName = "None"
                        activeMusicUrl = "None"
                        musicTrack = "Original Audio"
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "No Music",
                        tint = if (isNoneSelected) Color(0xFFD0BCFF) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text("No Background Music", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Plays original audio track only", color = Color.Gray, fontSize = 11.sp)
                    }
                }
                if (isNoneSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            tracksList.forEach { track ->
                val isTrackSelected = !isCustomMusicSelected && selectedBgMusicName == track.first && activeMusicUrl == track.second
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isTrackSelected) Color(0xFFD0BCFF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                        .border(
                            1.dp,
                            if (isTrackSelected) Color(0xFFD0BCFF) else Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            isCustomMusicSelected = false
                            selectedBgMusicName = track.first
                            activeMusicUrl = track.second
                            musicTrack = track.first
                        }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music Track",
                            tint = if (isTrackSelected) Color(0xFFD0BCFF) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(track.first, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Mood: $selectedMoodTab • Royalty-Free Loop", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    if (isTrackSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 4. Upload own device files simulation
        Text(
            text = "Step 4: Or Import Custom Music File From Device",
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.02f)
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                var customMusicNameInput by remember { mutableStateOf("My Custom Electro Beat") }
                var filePickedName by remember { mutableStateOf("") }
                var isPickingFile by remember { mutableStateOf(false) }

                Text("Label your track and tap simulate device upload:", color = Color.LightGray, fontSize = 12.sp)

                OutlinedTextField(
                    value = customMusicNameInput,
                    onValueChange = { customMusicNameInput = it },
                    label = { Text("Soundtrack Name", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        isPickingFile = true
                        coroutineScope.launch {
                            delay(1200) // Simulate interactive local disk access picker latencies
                            filePickedName = "${customMusicNameInput.lowercase().replace(" ", "_")}.mp3"
                            isPickingFile = false
                            
                            // Apply custom attributes and mock audio source so they can hear it play in real-time Preview!
                            isCustomMusicSelected = true
                            selectedBgMusicName = customMusicNameInput
                            activeMusicUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
                            musicTrack = customMusicNameInput
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCustomMusicSelected) Color(0xFF381E72) else Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, if (isCustomMusicSelected) Color(0xFFD0BCFF) else Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPickingFile) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text("Accessing local Android file stream...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Simulate Device Picker", modifier = Modifier.size(16.dp))
                            Text(if (isCustomMusicSelected) "Custom Device Soundtrack Applied!" else "Simulate File Selection", fontSize = 12.sp)
                        }
                    }
                }

                if (filePickedName.isNotBlank() && isCustomMusicSelected) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                        Text(
                            text = "Import Success: \"$filePickedName\" linked cleanly (Simulated file).",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 5. Sound balance mixers
        Text(
            text = "Step 5: Mix Master Volumes",
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Sound Desk", tint = Color(0xFFD0BCFF))
                    Text("Audio Studio Mixer Panel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Slider original voice volume
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Original Video Sound Ratio", color = Color.LightGray, fontSize = 12.sp)
                        Text("${(originalVolume * 100).toInt()}%", color = Color(0xFFD0BCFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = originalVolume,
                        onValueChange = { originalVolume = it },
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFFD0BCFF),
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                            thumbColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.testTag("mix_slider_original")
                    )
                }

                // Slider bgMusic volume
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Added Background Music Audio Ratio", color = Color.LightGray, fontSize = 12.sp)
                        Text("${(bgVolume * 100).toInt()}%", color = Color(0xFFD0BCFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = bgVolume,
                        onValueChange = { bgVolume = it },
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFFD0BCFF),
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                            thumbColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.testTag("mix_slider_background")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 6. Creator Details
        Text(
            text = "Step 6: Feed Publishers Metadata",
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        // Creator Handle TextField
        OutlinedTextField(
            value = creatorName,
            onValueChange = { creatorName = it },
            label = { Text("Your handle", color = Color.Gray) },
            leadingIcon = { Text("@", color = Color.LightGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFD0BCFF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_creator_input")
        )

        // Caption description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Write a descriptive caption...", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFD0BCFF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_desc_input")
        )

        // Hashtags input field
        OutlinedTextField(
            value = hashtags,
            onValueChange = { hashtags = it },
            label = { Text("Hashtags (separated by spaces)", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFD0BCFF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_hashtags_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // simulated actions triggers
        Button(
            onClick = {
                if (description.isNotBlank()) {
                    isUploading = true
                    uploadProgress = 0f
                    uploadComplete = false
                    coroutineScope.launch {
                        // Simulate network latency incremental uploading speeds
                        while (uploadProgress < 1f) {
                            delay(150)
                            uploadProgress += 0.2f
                        }
                        uploadProgress = 1f
                        delay(200)
                        
                        // Submit to database repository
                        viewModel.uploadVideo(
                            creator = creatorName,
                            description = description,
                            musicTrack = if (activeMusicUrl == "None") "Original Video Audio" else selectedBgMusicName,
                            hashtags = hashtags,
                            templateType = selectedTemplate,
                            filter = selectedFilter,
                            bgMusicUrl = activeMusicUrl,
                            bgMusicVolume = bgVolume,
                            originalVideoVolume = originalVolume
                        )
                        
                        uploadComplete = true
                        delay(1200)
                        
                        // Reset forms fields
                        isUploading = false
                        description = ""
                        hashtags = "#vibe #edited"
                        selectedFilter = "None"
                        activeMusicUrl = "None"
                        selectedBgMusicName = "None"
                        isCustomMusicSelected = false
                        uploadProgress = 0f
                        uploadComplete = false
                        onUploadSuccess()
                    }
                }
            },
            enabled = description.isNotBlank() && !isUploading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF381E72),
                disabledContainerColor = Color.White.copy(alpha = 0.05f),
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("publish_video_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = "publishing")
                Text("Publish Studio Composition", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }

    // Modal popup showing uploading progress animations
    if (isUploading) {
        Dialog(
            onDismissRequest = {},
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1C24).copy(alpha = 0.94f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                modifier = Modifier.width(280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!uploadComplete) {
                        CircularProgressIndicator(
                            progress = { uploadProgress },
                            color = Color(0xFFD0BCFF),
                            trackColor = Color.DarkGray,
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = "Processing video... ${(uploadProgress * 100).toInt()}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Applying sound mixers & filters...",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = "Studio Video Live!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Render completed successfully",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
