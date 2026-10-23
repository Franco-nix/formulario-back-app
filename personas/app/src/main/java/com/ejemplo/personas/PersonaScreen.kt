package com.ejemplo.personas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaScreen(vm: PersonaViewModel = viewModel(), onVerLista: () -> Unit) {
    val s = vm.state
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(s.mensaje) {
        s.mensaje?.let { snackbar.showSnackbar(it); vm.limpiarMensaje() }
    }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        vm.onFotoResultado(ok)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de personas") },
                actions = { TextButton(onClick = onVerLista) { Text("Ver lista") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- Foto ---
            Box(
                Modifier.size(150.dp).align(Alignment.CenterHorizontally)
                    .clip(CircleShape).background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (s.foto != null) {
                    AsyncImage(
                        model = s.foto,
                        contentDescription = "Foto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else Text("Sin foto")
            }
            OutlinedButton(
                onClick = {
                    val archivo = File(context.filesDir, "fotos/foto_${System.currentTimeMillis()}.jpg")
                        .also { it.parentFile?.mkdirs() }
                    vm.prepararFoto(archivo.absolutePath)
                    val uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.fileprovider", archivo
                    )
                    camara.launch(uri)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text(if (s.foto == null) "Tomar foto" else "Cambiar foto") }

            // --- DNI + Buscar ---
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = s.dni,
                    onValueChange = { v -> if (v.length <= 8 && v.all(Char::isDigit)) vm.editar { it.copy(dni = v) } },
                    label = { Text("DNI") },
                    singleLine = true,
                    enabled = !s.existente,   // la PK no se edita
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = vm::buscar, enabled = !s.existente) { Text("Buscar") }
            }

            OutlinedTextField(s.nombres, { v -> vm.editar { it.copy(nombres = v) } },
                label = { Text("Nombres") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(s.apellidos, { v -> vm.editar { it.copy(apellidos = v) } },
                label = { Text("Apellidos") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(s.direccion, { v -> vm.editar { it.copy(direccion = v) } },
                label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())

            ProfesionDropdown(s.profesion) { p -> vm.editar { it.copy(profesion = p) } }

            OutlinedTextField(
                s.celular,
                { v -> if (v.length <= 9 && v.all(Char::isDigit)) vm.editar { it.copy(celular = v) } },
                label = { Text("Celular") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                s.correo, { v -> vm.editar { it.copy(correo = v) } },
                label = { Text("Correo electrónico") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            // --- Acciones ---
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = vm::nuevo, modifier = Modifier.weight(1f)) { Text("Nuevo") }
                Button(onClick = vm::guardar, modifier = Modifier.weight(1f)) {
                    Text(if (s.existente) "Actualizar" else "Guardar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionDropdown(seleccion: String, onSelect: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { abierto = it }) {
        OutlinedTextField(
            value = seleccion,
            onValueChange = {},
            readOnly = true,
            label = { Text("Profesión") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            PROFESIONES.forEach { p ->
                DropdownMenuItem(text = { Text(p) }, onClick = { onSelect(p); abierto = false })
            }
        }
    }
}