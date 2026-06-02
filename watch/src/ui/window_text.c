#include "window_text.h"

#include "commons/connection/bucket_sync.h"
#include "commons/structures/vec.h"
#include "layers/status_bar.h"
#include "commons/math.h"

// 20 slots (max) with approx max bucket size
#define MAX_TEXT_LENGTH (20 * PERSIST_DATA_MAX_LENGTH)
const int32_t SINGLE_SCROLL_HEIGHT = 48;

static ScrollLayer* scroll_layer;
static TextLayer* text_layer;
static CustomStatusBarLayer* status_bar_layer;
static uint8_t initial_bucket_id;
static char text[MAX_TEXT_LENGTH];
uint8_t* relevant_extra_buckets;

static void load_text();

static bool is_bucket_relevant(const uint8_t bucket_id)
{
    for (size_t i = 0; i < vector_size(relevant_extra_buckets); i++)
    {
        if (relevant_extra_buckets[i] == bucket_id)
        {
            return true;
        }
    }
    return false;
}

static void on_bucket_changed(BucketMetadata bucket, void* context)
{
    if (bucket.id == initial_bucket_id || is_bucket_relevant(bucket.id))
    {
        load_text();
    }
}

// ReSharper disable once CppParameterMayBeConst
static void button_up_single(ClickRecognizerRef recognizer, void* context)
{
    const int16_t current_position = scroll_layer_get_content_offset(scroll_layer).y;

    scroll_layer_set_content_offset(scroll_layer, GPoint(0, MIN(0, current_position + SINGLE_SCROLL_HEIGHT)), true);
}

// ReSharper disable once CppParameterMayBeConst
static void button_down_single(ClickRecognizerRef recognizer, void* context)
{
    const int16_t current_position = scroll_layer_get_content_offset(scroll_layer).y;
    const int16_t max_scroll = scroll_layer_get_content_size(scroll_layer).h -
        layer_get_bounds(scroll_layer_get_layer(scroll_layer)).size.h;

    scroll_layer_set_content_offset(scroll_layer, GPoint(0, MAX(-max_scroll, current_position - SINGLE_SCROLL_HEIGHT)), true);
}

static void button_up_repeating(const ClickRecognizerRef recognizer, void* context)
{
    if (click_recognizer_is_repeating(recognizer))
    {
        button_up_single(recognizer, context);
    }
}

static void button_down_repeating(const ClickRecognizerRef recognizer, void* context)
{
    if (click_recognizer_is_repeating(recognizer))
    {
        button_down_single(recognizer, context);
    }
}

static void button_up_double(const ClickRecognizerRef recognizer, void* context)
{
    scroll_layer_set_content_offset(scroll_layer, GPoint(0, 0), true);
}

static void button_down_double(const ClickRecognizerRef recognizer, void* context)
{
    const int16_t max_scroll = scroll_layer_get_content_size(scroll_layer).h -
        layer_get_bounds(scroll_layer_get_layer(scroll_layer)).size.h;

    scroll_layer_set_content_offset(scroll_layer, GPoint(0, -max_scroll), true);
}

static void window_text_buttons_config()
{
    window_multi_click_subscribe(BUTTON_ID_UP, 2, 2, 150, false, button_up_double);
    window_multi_click_subscribe(BUTTON_ID_DOWN, 2, 2, 150, false, button_down_double);

    window_single_repeating_click_subscribe(BUTTON_ID_UP, 100, button_up_repeating);
    window_single_repeating_click_subscribe(BUTTON_ID_DOWN, 100, button_down_repeating);

    // regular button single click action is delayed since the watch is waiting to determine whether we hold the button
    // or not. This is not necessary in our case, so we can just use "button down" event instead
    window_raw_click_subscribe(BUTTON_ID_UP, button_up_single, NULL, NULL);
    window_raw_click_subscribe(BUTTON_ID_DOWN, button_down_single, NULL, NULL);
}


// ReSharper disable once CppParameterMayBeConstPtrOrRef
static void window_load(Window* window)
{
    Layer* window_layer = window_get_root_layer(window);
    const GRect screen_bounds = layer_get_bounds(window_layer);
    status_bar_layer = custom_status_bar_layer_create(screen_bounds);
    const GRect statusbar_layer_bounds = layer_get_bounds(status_bar_layer->layer);

    scroll_layer = scroll_layer_create(
        GRect(
            0,
            statusbar_layer_bounds.size.h,
            screen_bounds.size.w,
            screen_bounds.size.h - statusbar_layer_bounds.size.h
        )
    );

    window_set_click_config_provider(window, window_text_buttons_config);

    text_layer = text_layer_create(GRect(3, 0, screen_bounds.size.w - 3, 10000));
    text_layer_set_font(text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_18));
    scroll_layer_add_child(scroll_layer, text_layer_get_layer(text_layer));

    layer_add_child(window_layer, status_bar_layer->layer);
    layer_add_child(window_layer, scroll_layer_get_layer(scroll_layer));

    relevant_extra_buckets = vector_create();
}

static void window_unload(Window* window)
{
    scroll_layer_destroy(scroll_layer);
    custom_status_bar_layer_destroy(status_bar_layer);
    text_layer_destroy(text_layer);
    window_destroy(window);
    vector_free(relevant_extra_buckets);
}

static void window_show(Window* window)
{
    bucket_sync_set_bucket_data_change_callback(on_bucket_changed, NULL);
    custom_status_bar_set_active(status_bar_layer, true);
    load_text();
}

static void window_hide(Window* window)
{
    bucket_sync_clear_bucket_data_change_callback(on_bucket_changed, NULL);
    custom_status_bar_set_active(status_bar_layer, false);
}

void window_text_show(const uint8_t id)
{
    Window* window = window_create();

    initial_bucket_id = id;

    window_set_window_handlers(window, (WindowHandlers)
                               {
                                   .load = window_load,
                                   .unload = window_unload,
                                   .appear = window_show,
                                   .disappear = window_hide
                               }
    );
    const bool animated = true;
    window_stack_push(window, animated);
}

static void load_text()
{
    vector_clear(relevant_extra_buckets);

    uint8_t bucket_data[PERSIST_DATA_MAX_LENGTH];
    if (!bucket_sync_load_bucket(initial_bucket_id, bucket_data))
    {
        window_stack_pop(true);
        return;
    }

    const uint8_t first_bucket_size = bucket_sync_get_bucket_size(initial_bucket_id);

    const size_t title_length = strlen((const char*)bucket_data);

    const size_t first_bucket_body_length = first_bucket_size - title_length - 1;
    uint8_t next_text_bucket = bucket_data[title_length + 1];

    size_t text_position = first_bucket_body_length;
    strncpy(text, (const char*)&bucket_data[title_length + 2], first_bucket_body_length);

    while (next_text_bucket != 0)
    {
        if (!bucket_sync_load_bucket(next_text_bucket, bucket_data))
        {
            break;
        }

        vector_add(&relevant_extra_buckets, uint8_t, next_text_bucket);
        const uint8_t bucket_size = bucket_sync_get_bucket_size(next_text_bucket);
        next_text_bucket = bucket_data[0];
        const uint8_t body_length = bucket_size - 1;

        strncpy(&text[text_position], (const char*)&bucket_data[1], body_length);
        text_position += body_length;
    }
    text[text_position] = '\0';

    text_layer_set_text(text_layer, text);
    GSize text_size = text_layer_get_content_size(text_layer);
    text_size.h += 30; // Add extra padding at the bottom to account for rounded corners

    scroll_layer_set_content_size(scroll_layer, text_size);
}
