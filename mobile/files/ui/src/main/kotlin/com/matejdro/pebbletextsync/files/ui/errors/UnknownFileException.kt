package com.matejdro.pebbletextsync.files.ui.errors

import si.inova.kotlinova.core.outcome.CauseException

class UnknownFileException : CauseException(isProgrammersFault = false)
