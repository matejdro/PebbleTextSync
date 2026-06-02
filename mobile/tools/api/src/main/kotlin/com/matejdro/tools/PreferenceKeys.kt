package com.matejdro.tools

import androidx.datastore.preferences.core.stringPreferencesKey

enum class PebbleFont {
   GOTHIC_14,
   GOTHIC_14_BOLD,
   GOTHIC_18,
   GOTHIC_18_BOLD,
   GOTHIC_24,
   GOTHIC_24_BOLD,
   GOTHIC_28,
   GOTHIC_28_BOLD,
   BITHAM_30_BLACK,
   BITHAM_42_BOLD,
   BITHAM_42_LIGHT,
   BITHAM_42_MEDIUM_NUMBERS,
   BITHAM_34_MEDIUM_NUMBERS,
   BITHAM_34_LIGHT_SUBSET,
   BITHAM_18_LIGHT_SUBSET,
   ROBOTO_21_CONDENSED,
   ROBOTO_49_SUBSET,
   DROID_SERIF_28_BOLD,
}

object PreferenceKeys {
   val TEXT_FONT = stringPreferencesKey("text_font")
   val TEXT_FONT_DEFAULT = PebbleFont.GOTHIC_18
}
