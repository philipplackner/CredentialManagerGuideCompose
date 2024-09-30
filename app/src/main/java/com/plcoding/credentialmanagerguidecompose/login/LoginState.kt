package com.plcoding.credentialmanagerguidecompose

data class LoginState (
    val loggedInUser: String? = null,
    val username: String = "bob",
    val password: String = "",
    val errorMessage: String? = null,
    val isRegister: Boolean = false
)
