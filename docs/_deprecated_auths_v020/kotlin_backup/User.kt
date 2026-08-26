package com.liuflow.app.auths.domain

/**
 * 登录用户。对应 CloudBase user 记录（由 CloudBase Auth 内置管理，开发者无需自建表）。
 */
data class User(
    val uid: String,
    val email: String,
    val createdAt: Long?
)
