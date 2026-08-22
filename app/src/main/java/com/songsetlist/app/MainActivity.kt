package com.songsetlist.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NeonGreen = Color(0xFF9DFF00)
private val DarkGrey = Color(0xFF555555)
private val CardGrey = Color(0xFF111111)

data class Song(val name: String, val bpm: Int)
data class Setlist(val name: String, val songs: MutableList<Song> = mutableStateListOf())

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SongSetlistApp() }
    }
}

@Composable
fun SongSetlistApp() {
    var tab by remember { mutableIntStateOf(0) }
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
                Setlist("1st Set", songs.take(3).toMutableStateList()),
                Setlist("2nd Set", mutableStateListOf()),
                Setlist("3rd Set", mutableStateListOf()),
                Setlist("4th Set", mutableStateListOf())
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
        Surface(Modifier.fillMaxSize(), color = Color.Black) {
            if (tab == 0) {
                LibraryScreen(
                    songs = songs,
                    onAdd = { name, bpm -> songs = songs + Song(name, bpm) },
                    onRemove = { song ->
                        songs = songs.filterNot { it == song }
                        setlists.forEach { it.songs.remove(song) }
                    },
                    setlists = setlists,
                    onAddToSet = { song, set ->
                        if (song !in set.songs) set.songs.add(song)
                    }
                )
            } else {
                SetlistsScreen(setlists = setlists)
            }
            BottomNavigationBar(tab) { tab = it }
        }
    }
}

@Composable
fun LibraryScreen(
    songs: List<Song>,
    onAdd: (String, Int) -> Unit,
    onRemove: (Song) -> Unit,
    setlists: List<Setlist>,
    onAddToSet: (Song, Setlist) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var menuSong by remember { mutableStateOf<Song?>(null) }
    var addToSetSong by remember { mutableStateOf<Song?>(null) }
    val filtered = songs.filter { it.name.contains(query, ignoreCase = true) }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 18.dp, bottom = 76.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Library", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, "Add song", tint = NeonGreen) }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            singleLine = true, placeholder = { Text("Search songs", color = DarkGrey) },
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = DarkGrey) },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = Color(0xFF333333),
                cursorColor = NeonGreen
            )
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            itemsIndexed(filtered, key = { _, s -> s.name }) { _, song ->
                SongRow(song, onMenu = { menuSong = song })
            }
        }
    }

    if (showAdd) AddEditDialog("Add Song", null, onDismiss = { showAdd = false }) { n, b ->
        onAdd(n, b); showAdd = false
    }
    menuSong?.let { song ->
        SongMenu(song, onDismiss = { menuSong = null }, onEdit = { menuSong = null },
            onAddToSet = { addToSetSong = song; menuSong = null },
            onRemove = { onRemove(song); menuSong = null })
    }
    addToSetSong?.let { song ->
        AddToSetDialog(song, setlists, { addToSetSong = null }, onAddToSet)
    }
}

@Composable
fun SongRow(song: Song, onMenu: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(58.dp).background(CardGrey, RoundedCornerShape(14.dp))
            .padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Text(song.name, Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 16.sp)
        Text("${song.bpm}", color = Color.White, fontSize = 15.sp)
        IconButton(onClick = onMenu) { Icon(Icons.Default.MoreVert, "Song options", tint = Color.LightGray) }
    }
}

@Composable
fun SongMenu(song: Song, onDismiss: () -> Unit, onEdit: () -> Unit, onAddToSet: () -> Unit, onRemove: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(song.name) },
        text = {
            Column {
                Text("Edit", Modifier.fillMaxWidth().clickable { onEdit() }.padding(14.dp))
                Text("Add to Setlist", Modifier.fillMaxWidth().clickable { onAddToSet() }.padding(14.dp))
                Text("Remove", Modifier.fillMaxWidth().clickable { onRemove() }.padding(14.dp))
            }
        }, confirmButton = {})
}

@Composable
fun AddToSetDialog(song: Song, setlists: List<Setlist>, onDismiss: () -> Unit, onAdd: (Song, Setlist) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add to Setlist") },
        text = {
            Column {
                setlists.forEach { set ->
                    Column(Modifier.fillMaxWidth().clickable {
                        onAdd(song, set); onDismiss()
                    }.padding(vertical = 10.dp)) {
                        Text(set.name, fontWeight = FontWeight.Medium)
                        Text("${set.songs.size} songs", fontSize = 9.sp, color = DarkGrey)
                    }
                }
            }
        }, confirmButton = {})
}

@Composable
fun AddEditDialog(title: String, initial: Song?, onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var bpm by remember { mutableStateOf(initial?.bpm?.toString() ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Song Name") }, singleLine = true)
                OutlinedTextField(bpm, { bpm = it.filter(Char::isDigit) }, label = { Text("BPM") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank() && bpm.isNotBlank(),
                onClick = { onSave(name.trim(), bpm.toInt()) }) {
                Text("Save", color = NeonGreen)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun SetlistsScreen(setlists: List<Setlist>) {
    var selected by remember { mutableStateOf<Setlist?>(null) }
    var showNew by remember { mutableStateOf(false) }
    if (selected != null) {
        SetlistDetailScreen(selected!!) { selected = null }
        return
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 18.dp, bottom = 76.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Setlists", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { showNew = true }) { Icon(Icons.Default.Add, "New setlist", tint = NeonGreen) }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            itemsIndexed(setlists) { _, set ->
                Row(Modifier.fillMaxWidth().background(CardGrey, RoundedCornerShape(14.dp))
                    .clickable { selected = set }.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(set.name, fontWeight = FontWeight.Bold)
                        Text("${set.songs.size} songs", fontSize = 9.sp, color = DarkGrey)
                    }
                    Icon(Icons.Default.MoreVert, "Setlist options", tint = Color.LightGray)
                }
            }
        }
    }
    if (showNew) AddNewSetlistDialog({ showNew = false }) { showNew = false }
}

@Composable
fun AddNewSetlistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New Setlist") },
        text = { OutlinedTextField(name, { name = it }, placeholder = { Text("Setlist name...") }, singleLine = true) },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onCreate(name.trim()) }) {
                Text("Create", color = NeonGreen)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
fun SetlistDetailScreen(setlist: Setlist, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 18.dp, bottom = 76.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", fontSize = 30.sp, modifier = Modifier.clickable { onBack() })
            Text(setlist.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            itemsIndexed(setlist.songs) { _, song ->
                SongRow(song, onMenu = {})
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(68.dp).background(Color.Black)
            .padding(horizontal = 36.dp), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NavItem(Icons.Default.LibraryMusic, "Library", selected == 0) { onSelect(0) }
        NavItem(Icons.Default.QueueMusic, "Setlists", selected == 1) { onSelect(1) }
    }
}

@Composable
fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable { onClick() }.padding(horizontal = 20.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, label, tint = if (active) NeonGreen else DarkGrey)
        Text(label, fontSize = 11.sp, color = if (active) NeonGreen else DarkGrey)
    }
}
