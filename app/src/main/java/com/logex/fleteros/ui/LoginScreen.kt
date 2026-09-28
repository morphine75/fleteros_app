package com.logex.fleteros.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.logex.fleteros.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(estado.loginExitoso) {
        if (estado.loginExitoso) {
            onLoginExitoso()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_logex),
                contentDescription = "Logo Grupo Logex",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .width(250.dp)          // ancho fijo
                    .height(100.dp)          // alto fijo (opcional)
                    .padding(bottom = 24.dp)
            )
            Text(text = "Reparto Movil Logex", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = estado.usuario,
                onValueChange = viewModel::onUsuarioChange,
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = estado.clave,
                onValueChange = viewModel::onClaveChange,
                label = { Text("Clave") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            val mensajeError = estado.error
            if (mensajeError != null) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = viewModel::login,
                enabled = !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Ingresar")
                }
            }
        }
    }
}