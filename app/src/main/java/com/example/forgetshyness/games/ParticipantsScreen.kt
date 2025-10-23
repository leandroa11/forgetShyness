package com.example.forgetshyness.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.R
import com.example.forgetshyness.data.Player
import androidx.compose.ui.res.stringResource
@Composable
fun ParticipantsScreen(
    userName: String,
    existingPlayers: List<Player>,
    onAdd: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit,
    userId: String,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFB3405F),
                        Color(0xFFDD6D47)
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(id = com.example.forgetshyness.R.drawable.fondo_burbujas_2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = com.example.forgetshyness.R.drawable.flecha_izquierda),
                contentDescription = stringResource(R.string.content_description_back),
                tint = Color.Yellow,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = stringResource(R.string.participants_title),
                    fontSize = 26.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Icon(
                    painter = painterResource(id = com.example.forgetshyness.R.drawable.usuario_editar),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.participants_empty_message),
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Jugador principal (host)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFD9D9D9))
                        .padding(vertical = 12.dp, horizontal = 30.dp)
                ) {
                    Text(
                        text = userName,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lista de jugadores adicionales
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(existingPlayers, key = { it.id }) { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 32.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = player.name, color = Color.White)
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = LocalContext.current.getString(R.string.content_description_delete),
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onDelete(player.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón abrir diálogo para añadir jugador
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(46.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD24C)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.dialog_add_button),
                        tint = Color(0xFF6C3905)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.button_add_player),
                        color = Color(0xFF6C3905),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Botón principal al final
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD24C)
                )
            ) {
                Text(
                    text = stringResource(R.string.button_start_fun),
                    color = Color(0xFF6C3905),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Dialogo para ingresar nombre del nuevo jugador
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        val name = inputName.trim()
                        if (name.isNotEmpty()) {
                            onAdd(name)
                            inputName = ""
                            showAddDialog = false
                        }
                    }) {
                        Text(stringResource(R.string.dialog_add_button_))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(stringResource(R.string.dialog_cancel_button))
                    }
                },
                title = { Text(stringResource(R.string.dialog_new_player_title)) },
                text = {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        placeholder = { Text(stringResource(R.string.dialog_player_name_)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    }
}
