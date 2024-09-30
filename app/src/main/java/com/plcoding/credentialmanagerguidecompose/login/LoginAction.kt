package com.plcoding.credentialmanagerguidecompose

sealed interface LoginAction {
    data class OnSignIn(val result: SignInResult, val onLoggedIn: (String) -> Unit) : LoginAction
    data class OnSignUp(val result: SignUpResult): LoginAction
    data class OnUsernameChange(val username: String): LoginAction
    data class OnPasswordChange(val password: String): LoginAction
    data object OnToggleIsRegister: LoginAction
}