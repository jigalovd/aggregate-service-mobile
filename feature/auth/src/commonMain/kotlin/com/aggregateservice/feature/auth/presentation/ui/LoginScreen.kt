package com.aggregateservice.feature.auth.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.aggregateservice.core.i18n.AuthStrings
import com.aggregateservice.core.theme.Spacing

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: AuthViewModel = getScreenModel()
        val state by viewModel.loginState.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.LG),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = AuthStrings.login,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(Spacing.XL))

            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text(AuthStrings.email) },
                isError = state.emailError != null,
                supportingText = state.emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Spacing.MD))

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(AuthStrings.password) },
                isError = state.passwordError != null,
                supportingText = state.passwordError?.let { { Text(it) } },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Spacing.LG))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = viewModel::login,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(AuthStrings.login)
                }
            }

            state.error?.let { error ->
                Spacer(modifier = Modifier.height(Spacing.SM))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
