#include "config.h"

static const char* fonts[] = {
    FONT_KEY_GOTHIC_14,
    FONT_KEY_GOTHIC_14_BOLD,
    FONT_KEY_GOTHIC_18,
    FONT_KEY_GOTHIC_18_BOLD,
    FONT_KEY_GOTHIC_24,
    FONT_KEY_GOTHIC_24_BOLD,
    FONT_KEY_GOTHIC_28,
    FONT_KEY_GOTHIC_28_BOLD,
    FONT_KEY_BITHAM_30_BLACK,
    FONT_KEY_BITHAM_42_BOLD,
    FONT_KEY_BITHAM_42_LIGHT,
    FONT_KEY_BITHAM_42_MEDIUM_NUMBERS,
    FONT_KEY_BITHAM_34_MEDIUM_NUMBERS,
    FONT_KEY_BITHAM_34_LIGHT_SUBSET,
    FONT_KEY_BITHAM_18_LIGHT_SUBSET,
    FONT_KEY_ROBOTO_CONDENSED_21,
    FONT_KEY_ROBOTO_BOLD_SUBSET_49,
    FONT_KEY_DROID_SERIF_28_BOLD
};

static const int32_t DEFAULT_FONT = 2;
static const uint32_t FONT_PERSIST_KEY = 3000;

GFont config_get_text_font()
{
    int32_t value;
    if (persist_exists(FONT_PERSIST_KEY))
    {
        value = persist_read_int(FONT_PERSIST_KEY);
    }
    else
    {
        value = DEFAULT_FONT;
    }

    return fonts_get_system_font(fonts[value]);
}

void config_update_text_font(uint8_t font)
{
    persist_write_int(FONT_PERSIST_KEY, font);
}
