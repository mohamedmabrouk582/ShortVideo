package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.repository.VideoRepository
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.HomeFeedScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.UploadScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShortVideoViewModel

enum class NavigationTab {
    HOME, DISCOVER, CREATE, PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize Room Database, Repository and ViewModel cleanly
                val context = LocalContext.current
                val database = remember {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "short_video_db"
                    ).fallbackToDestructiveMigration().build()
                }

                val repository = remember {
                    VideoRepository(database.videoDao())
                }

                val videoViewModel: ShortVideoViewModel = viewModel(
                    factory = ShortVideoViewModel.Factory(repository)
                )

                var currentTab by remember { mutableStateOf(NavigationTab.HOME) }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    bottomBar = {
                        Column {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
                            NavigationBar(
                                containerColor = Color(0xFF1C1B1F),
                                contentColor = Color(0xFFC9C5D0),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("app_navigation_bar"),
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.HOME,
                                    onClick = { currentTab = NavigationTab.HOME },
                                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home HomeFeed") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF21005D),
                                        selectedTextColor = Color.White,
                                        unselectedIconColor = Color(0xFFC9C5D0),
                                        unselectedTextColor = Color(0xFFC9C5D0),
                                        indicatorColor = Color(0xFFEADDFF)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_home")
                                )

                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.DISCOVER,
                                    onClick = { currentTab = NavigationTab.DISCOVER },
                                    label = { Text("Discover", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    icon = { Icon(Icons.Default.Search, contentDescription = "Discover trends") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF21005D),
                                        selectedTextColor = Color.White,
                                        unselectedIconColor = Color(0xFFC9C5D0),
                                        unselectedTextColor = Color(0xFFC9C5D0),
                                        indicatorColor = Color(0xFFEADDFF)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_discover")
                                )

                                // Stylized "Create" tab to feel like premium social apps
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.CREATE,
                                    onClick = { currentTab = NavigationTab.CREATE },
                                    label = { Text("Create", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    icon = {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = Color.White,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .width(44.dp)
                                                .height(30.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Upload Content",
                                                tint = Color.Black,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        unselectedIconColor = Color(0xFFC9C5D0),
                                        unselectedTextColor = Color(0xFFC9C5D0),
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_tab_create")
                                )

                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.PROFILE,
                                    onClick = { currentTab = NavigationTab.PROFILE },
                                    label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Active Profile") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF21005D),
                                        selectedTextColor = Color.White,
                                        unselectedIconColor = Color(0xFFC9C5D0),
                                        unselectedTextColor = Color(0xFFC9C5D0),
                                        indicatorColor = Color(0xFFEADDFF)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_profile")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF121212))
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        when (currentTab) {
                            NavigationTab.HOME -> {
                                HomeFeedScreen(
                                    viewModel = videoViewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            NavigationTab.DISCOVER -> {
                                DiscoverScreen(
                                    viewModel = videoViewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            NavigationTab.CREATE -> {
                                UploadScreen(
                                    viewModel = videoViewModel,
                                    onUploadSuccess = { currentTab = NavigationTab.HOME },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            NavigationTab.PROFILE -> {
                                ProfileScreen(
                                    viewModel = videoViewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
