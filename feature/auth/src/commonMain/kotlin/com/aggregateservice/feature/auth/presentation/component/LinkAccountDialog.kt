package com.aggregateservice.feature.auth.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aggregateservice.core.theme.Spacing
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey

/**
 * Dialog for linking Firebase account to existing user with password.
 *
 * Shown when verifyFirebaseToken returns link_required error.
 *
 * @param i18nProvider I18n provider for localized strings
 * @param email Email of existing account
 * @param authProvider Firebase auth provider trying to link (Google/Apple/Phone)
 * @param onLink Callback with password when user submits
 * @param onDismiss Callback when user cancels
 * @param modifier Modifier for the dialog
 */
@Composable
fun LinkAccountDialog(
    i18nProvider: I18nProvider,
    email: String,
    authProvider: String,
    onLink: (password: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var password by remember { mutableStateOf("") }

    val providerDisplayName = when (authProvider) {
        "google.com" -> i18nProvider[StringKey.Auth.PROVIDER_GOOGLE]
        "apple.com" -> i18nProvider[StringKey.Auth.PROVIDER_APPLE]
        "phone" -> i18nProvider[StringKey.Auth.PROVIDER_PHONE]
        else -> authProvider
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(i18nProvider[StringKey.Auth.LINK_ACCOUNT_TITLE]) },
        text = {
            Column {
                Text(
                    text = i18nProvider[StringKey.Auth.LINK_ACCOUNT_MESSAGE],
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(Spacing.SM))

                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(Spacing.XS))

                Text(
                    text = "${i18nProvider[StringKey.Auth.SIGN_IN_WITH]} $providerDisplayName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(Spacing.MD))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(i18nProvider[StringKey.Auth.PASSWORD]) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onLink(password) },
                enabled = password.isNotBlank(),
            ) {
                Text(i18nProvider[StringKey.Auth.LINK_ACCOUNT])
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(i18nProvider[StringKey.CANCEL])
            }
        },
    )
}