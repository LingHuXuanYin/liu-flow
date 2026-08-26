package com.liuflow.app.auths.domain

sealed class SignUpResult {
    data class Success(val session: AuthSession) : SignUpResult()
    data class Failure(val error: AuthError) : SignUpResult()
}
