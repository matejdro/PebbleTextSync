package com.matejdro.pebbletextsync.files.ui

import com.matejdro.pebbletextsync.navigation.keys.base.BaseScreenKey
import com.matejdro.pebbletextsync.navigation.keys.base.DetailKey
import kotlinx.serialization.Serializable

@Serializable
data class FileDetailsScreenKey(val id: Int) : BaseScreenKey(), DetailKey
