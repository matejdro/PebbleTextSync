package com.matejdro.pebbletextsync.home

import com.matejdro.pebbletextsync.navigation.keys.base.BaseScreenKey
import com.matejdro.pebbletextsync.navigation.keys.base.TabContainerKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeScreenKey : BaseScreenKey(), TabContainerKey
