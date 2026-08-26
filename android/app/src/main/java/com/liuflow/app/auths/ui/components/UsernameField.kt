package com.liuflow.app.auths.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * 登录用户名输入框。
 * username 是注册时绑定的字段（不是 email），用于 CloudBase /signin 登录。
 * 规则：5-24 位，字母/数字/_/-
 */
@Composable
fun UsernameField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("用户名") },
        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        singleLine = true,
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    )
}
