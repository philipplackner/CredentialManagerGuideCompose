package com.plcoding.credentialmanagerguidecompose.login

import com.plcoding.credentialmanagerguidecompose.SignInResult
import com.plcoding.credentialmanagerguidecompose.SignUpResult

sealed interface LoginAction {
    data class OnSignIn(val result: SignInResult): LoginAction
    data class OnSignUp(val result: SignUpResult): LoginAction
    data class OnUsernameChange(val username: String): LoginAction
    data class OnPasswordChange(val password: String): LoginAction
    data object OnToggleIsRegister: LoginAction
}