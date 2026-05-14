#include "window_text.h"

#include "commons/connection/bucket_sync.h"
#include "layers/status_bar.h"

// 20 slots (max) with approx max bucket size
#define MAX_TEXT_LENGTH (20 * PERSIST_DATA_MAX_LENGTH)

static ScrollLayer* scroll_layer;
static TextLayer* text_layer;
static CustomStatusBarLayer* status_bar_layer;
static uint8_t initial_bucket_id;
static char text[MAX_TEXT_LENGTH];


static void load_text();

static void on_bucket_changed(BucketMetadata bucket, void* context)
{
    load_text();
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

    scroll_layer_set_click_config_onto_window(scroll_layer, window);

    text_layer = text_layer_create(GRect(3, 0, screen_bounds.size.w - 3, 10000));
    text_layer_set_font(text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_18));
    scroll_layer_add_child(scroll_layer, text_layer_get_layer(text_layer));

    layer_add_child(window_layer, status_bar_layer->layer);
    layer_add_child(window_layer, scroll_layer_get_layer(scroll_layer));
}

static void window_unload(Window* window)
{
    scroll_layer_destroy(scroll_layer);
    custom_status_bar_layer_destroy(status_bar_layer);
    text_layer_destroy(text_layer);
    window_destroy(window);
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
    uint8_t bucket_data[PERSIST_DATA_MAX_LENGTH];
    if (!bucket_sync_load_bucket(initial_bucket_id, bucket_data))
    {
        window_stack_pop(true);
        return;
    }

    const uint8_t bucket_size = bucket_sync_get_bucket_size(initial_bucket_id);

    const size_t title_length = strlen((const char*)bucket_data);

    const size_t body_length = bucket_size - title_length - 1;
    strncpy(text, (const char*)&bucket_data[title_length + 1], body_length);

    text_layer_set_text(text_layer, text);
    GSize text_size = text_layer_get_content_size(text_layer);
    text_size.h += 30; // Add extra padding at the bottom to account for rounded corners

    scroll_layer_set_content_size(scroll_layer, text_size);
}
