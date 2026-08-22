package com.songsetlist.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NeonGreen = Color(0xFF9DFF00)
private val DarkGrey = Color(0xFF777777)
private val CardGrey = Color(0xFF111111)

data class Song(
    val name: String,
    val bpm: Int
)

data class Setlist(
    val name: String,
    val songs: MutableList<Song> = mutableStateListOf()
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SongSetlistApp()
        }
    }
}

@Composable
fun SongSetlistApp() {

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var songs by remember {
        mutableStateOf(
            listOf(
                Song("Panaginip", 112),
                Song("Ikaw Lang", 98),
                Song("Through the Years", 105),
                Song("Perfect", 95),
                Song("A Thousand Years", 85)
            )
        )
    }

    var setlists by remember {
        mutableStateOf(
            listOf(
                Setlist(
                    "1st Set",
                    mutableStateListOf(
                        Song("Panaginip", 112),
                        Song("Ikaw Lang", 98),
                        Song("Through the Years", 105)
                    )
                ),
                Setlist("2nd Set"),
                Setlist("3rd Set"),
                Setlist("4th Set")
            )
        )
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color.Black,
            surface = CardGrey,
            primary = NeonGreen,
            onPrimary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {

            /*
             * IMPORTANT:
             * Content and bottom navigation are now arranged
             * vertically instead of being drawn on top of each other.
             */
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {

                    if (selectedTab == 0) {

                        LibraryScreen(
                            songs = songs,
                            setlists = setlists,

                            onAdd = { name, bpm ->
                                songs = songs + Song(name, bpm)
                            },

                            onRemove = { song ->
                                songs = songs.filterNot {
                                    it == song
                                }

                                setlists.forEach {
                                    it.songs.remove(song)
                                }
                            },

                            onAddToSet = { song, set ->
                                if (song !in set.songs) {
                                    set.songs.add(song)
                                }
                            }
                        )

                    } else {

                        SetlistsScreen(
                            setlists = setlists
                        )
                    }
                }

                BottomNavigationBar(
                    selected = selectedTab,
                    onSelect = {
                        selectedTab = it
                    }
                )
            }
        }
    }
}

/* ---------------------------------------------------------
   LIBRARY
--------------------------------------------------------- */

@Composable
fun LibraryScreen(
    songs: List<Song>,
    setlists: List<Setlist>,
    onAdd: (String, Int) -> Unit,
    onRemove: (Song) -> Unit,
    onAddToSet: (Song, Setlist) -> Unit
) {

    var query by remember {
        mutableStateOf("")
    }

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var selectedSong by remember {
        mutableStateOf<Song?>(null)
    }

    var showAddToSet by remember {
        mutableStateOf<Song?>(null)
    }

    val filteredSongs = songs.filter {
        it.name.contains(
            query,
            ignoreCase = true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 18.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Library",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    showAddDialog = true
                }
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add song",
                    tint = NeonGreen
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            singleLine = true,

            placeholder = {
                Text(
                    "Search songs",
                    color = DarkGrey
                )
            },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = DarkGrey
                )
            },

            shape = RoundedCornerShape(18.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = Color(0xFF333333),
                cursorColor = NeonGreen
            )
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(
                bottom = 16.dp
            )
        ) {

            items(
                items = filteredSongs,
                key = {
                    it.name
                }
            ) { song ->

                SongRow(
                    song = song,
                    onMenu = {
                        selectedSong = song
                    }
                )
            }
        }
    }

    if (showAddDialog) {

        AddSongDialog(
            onDismiss = {
                showAddDialog = false
            },
            onSave = { name, bpm ->

                onAdd(name, bpm)

                showAddDialog = false
            }
        )
    }

    selectedSong?.let { song ->

        SongMenu(
            song = song,

            onDismiss = {
                selectedSong = null
            },

            onEdit = {
                selectedSong = null
            },

            onAddToSet = {

                showAddToSet = song
                selectedSong = null
            },

            onRemove = {

                onRemove(song)
                selectedSong = null
            }
        )
    }

    showAddToSet?.let { song ->

        AddToSetDialog(
            song = song,
            setlists = setlists,

            onDismiss = {
                showAddToSet = null
            },

            onAdd = { selectedSongToAdd, set ->

                onAddToSet(
                    selectedSongToAdd,
                    set
                )

                showAddToSet = null
            }
        )
    }
}

/* ---------------------------------------------------------
   SONG ROW
--------------------------------------------------------- */

@Composable
fun SongRow(
    song: Song,
    onMenu: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                CardGrey,
                RoundedCornerShape(14.dp)
            )
            .padding(start = 16.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = song.name,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = song.bpm.toString(),
            fontSize = 15.sp,
            color = Color.White
        )

        IconButton(
            onClick = onMenu
        ) {

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Song options",
                tint = Color.LightGray
            )
        }
    }
}

/* ---------------------------------------------------------
   SONG MENU
--------------------------------------------------------- */

@Composable
fun SongMenu(
    song: Song,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onAddToSet: () -> Unit,
    onRemove: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text(song.name)
        },

        text = {

            Column {

                Text(
                    text = "Edit",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onEdit()
                        }
                        .padding(14.dp)
                )

                Text(
                    text = "Add to Setlist",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAddToSet()
                        }
                        .padding(14.dp)
                )

                Text(
                    text = "Remove",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onRemove()
                        }
                        .padding(14.dp)
                )
            }
        },

        confirmButton = {}
    )
}

/* ---------------------------------------------------------
   ADD SONG
--------------------------------------------------------- */

@Composable
fun AddSongDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var bpm by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("Add Song")
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text("Song Name")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = bpm,
                    onValueChange = {
                        bpm = it.filter(Char::isDigit)
                    },
                    label = {
                        Text("BPM")
                    },
                    singleLine = true
                )
            }
        },

        confirmButton = {

            TextButton(
                enabled = name.isNotBlank() &&
                        bpm.isNotBlank(),

                onClick = {

                    onSave(
                        name.trim(),
                        bpm.toInt()
                    )
                }
            ) {

                Text(
                    "Save",
                    color = NeonGreen
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Cancel")
            }
        }
    )
}

/* ---------------------------------------------------------
   ADD TO SETLIST
--------------------------------------------------------- */

@Composable
fun AddToSetDialog(
    song: Song,
    setlists: List<Setlist>,
    onDismiss: () -> Unit,
    onAdd: (Song, Setlist) -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("Add to Setlist")
        },

        text = {

            Column {

                setlists.forEach { set ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {

                                onAdd(
                                    song,
                                    set
                                )
                            }
                            .padding(
                                vertical = 10.dp
                            )
                    ) {

                        Text(
                            text = set.name,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "${set.songs.size} songs",
                            fontSize = 9.sp,
                            color = DarkGrey
                        )
                    }
                }
            }
        },

        confirmButton = {}
    )
}

/* ---------------------------------------------------------
   SETLISTS
--------------------------------------------------------- */

@Composable
fun SetlistsScreen(
    setlists: List<Setlist>
) {

    var selectedSetlist by remember {
        mutableStateOf<Setlist?>(null)
    }

    if (selectedSetlist != null) {

        SetlistDetailScreen(
            setlist = selectedSetlist!!,
            onBack = {
                selectedSetlist = null
            }
        )

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 18.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Setlists",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {

            items(setlists) { set ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            CardGrey,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            selectedSetlist = set
                        }
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = set.name,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${set.songs.size} songs",
                            fontSize = 9.sp,
                            color = DarkGrey
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Setlist options",
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}

/* ---------------------------------------------------------
   SETLIST DETAIL
--------------------------------------------------------- */

@Composable
fun SetlistDetailScreen(
    setlist: Setlist,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 18.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "‹",
                fontSize = 32.sp,
                modifier = Modifier.clickable {
                    onBack()
                }
            )

            Text(
                text = setlist.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    start = 10.dp
                )
            )
        }

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            items(setlist.songs) { song ->

                SongRow(
                    song = song,
                    onMenu = {}
                )
            }
        }
    }
}

/* ---------------------------------------------------------
   BOTTOM NAVIGATION
--------------------------------------------------------- */

@Composable
fun BottomNavigationBar(
    selected: Int,
    onSelect: (Int) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color.Black)
            .padding(
                horizontal = 36.dp
            ),

        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        NavItem(
            icon = Icons.Default.LibraryMusic,
            label = "Library",
            active = selected == 0
        ) {
            onSelect(0)
        }

        NavItem(
            icon = Icons.Default.QueueMusic,
            label = "Setlists",
            active = selected == 1
        ) {
            onSelect(1)
        }
    }
}

@Composable
fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 20.dp,
                vertical = 6.dp
            ),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) {
                NeonGreen
            } else {
                DarkGrey
            }
        )

        Text(
            text = label,
            fontSize = 11.sp,
            color = if (active) {
                NeonGreen
            } else {
                DarkGrey
            }
        )
    }
}