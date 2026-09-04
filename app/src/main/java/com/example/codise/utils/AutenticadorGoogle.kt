package com.example.codise.utils

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.codise.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class AutenticadorGoogle(private val contexto: Context) {
    private val credentialManager = CredentialManager.create(contexto)
    private val webClientId = contexto.getString(R.string.google_web_client_id)

    suspend fun iniciarSesion(): ResultadoGoogleAuth {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val peticion = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val resultado = credentialManager.getCredential(
                request = peticion,
                context = contexto
            )

            val credencial = resultado.credential
            if (credencial is CustomCredential && credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credencial.data)
                ResultadoGoogleAuth.Exito(
                    idToken = googleIdTokenCredential.idToken,
                    correo = googleIdTokenCredential.id,
                    nombre = googleIdTokenCredential.displayName,
                    fotoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                )
            } else {
                ResultadoGoogleAuth.Error("Tipo de credencial no reconocido")
            }
        } catch (_: GetCredentialCancellationException) {
            ResultadoGoogleAuth.Cancelado
        } catch (e: Exception) {
            ResultadoGoogleAuth.Error(e.localizedMessage ?: "Error al autenticar con Google")
        }
    }
}

sealed class ResultadoGoogleAuth {
    data class Exito(
        val idToken: String,
        val correo: String,
        val nombre: String?,
        val fotoUrl: String?
    ) : ResultadoGoogleAuth()
    data class Error(val mensaje: String) : ResultadoGoogleAuth()
    object Cancelado : ResultadoGoogleAuth()
}
