package com.ejemplo.personas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonasListaScreen(vm: PersonaViewModel, onVolver: () -> Unit) {
    val lista = vm.lista

    LaunchedEffect(Unit) { vm.cargarLista() }
    BackHandler(onBack = onVolver)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personas guardadas") },
                navigationIcon = { TextButton(onClick = onVolver) { Text("Volver") } },
                actions = { TextButton(onClick = vm::cargarLista) { Text("Recargar") } }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                lista == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                vm.listaError != null -> Text(vm.listaError!!, Modifier.align(Alignment.Center))
                lista.isEmpty() -> Text("No hay personas guardadas", Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista, key = { it.dni }) { p ->
                        PersonaCard(p) { vm.seleccionar(p); onVolver() }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonaCard(p: Persona, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (p.fotoRuta != null) {
                    AsyncImage(
                        model = ApiClient.urlFoto(p.fotoRuta),
                        contentDescription = "Foto de ${p.nombres}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else Text(p.nombres.take(1).uppercase(), style = MaterialTheme.typography.titleLarge)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "${p.nombres} ${p.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (p.profesion.isNotEmpty()) Text(p.profesion, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "DNI ${p.dni} · ${p.celular}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
