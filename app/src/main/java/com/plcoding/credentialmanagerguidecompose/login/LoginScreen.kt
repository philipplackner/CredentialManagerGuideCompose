package com.plcoding.credentialmanagerguidecompose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(state: LoginState, onAction: (LoginAction) -> Unit, onLoggedIn: (user: String) -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val accountManager = remember {
        AccountManager(context)
    }

    Column {
        TextField(value = state.username, onValueChange = {
            onAction(LoginAction.OnUsernameChange(it))
        }, label = {
            Text("Username")
        })
//        TextField(value = state.password, onValueChange = {
//            onAction(LoginAction.OnPasswordChanged(it))
//        }, label = {
//            Text("Password")
//        })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Login/Register", modifier = Modifier.padding(end = 16.dp))
            Switch(checked = state.isRegister, onCheckedChange = {
                onAction(LoginAction.OnToggleIsRegister)
            })
        }
        if (state.errorMessage != null) {
            Text(text = state.errorMessage, color = MaterialTheme.colorScheme.error)
        }
        Button(onClick = {
            scope.launch {
                if (!state.isRegister) {
                    val loginResult = accountManager.signIn(state.username)
                    onAction(LoginAction.OnSignIn(loginResult, onLoggedIn))
                } else {
                    val signupResult = accountManager.signUp(state.username)
                    onAction(LoginAction.OnSignUp(signupResult))
                }
            }
        }) {
            Text(text = if(state.isRegister) "Register with signkey" else "Login with signkey")
        }
    }
}