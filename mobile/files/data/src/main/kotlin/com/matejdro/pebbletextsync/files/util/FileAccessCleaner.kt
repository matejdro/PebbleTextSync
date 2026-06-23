package com.matejdro.pebbletextsync.files.util

fun interface FileAccessCleaner {
   fun onFileDeleted(uri: String)
}
