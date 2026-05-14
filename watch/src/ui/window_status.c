#include "window_status.h"
#include "pebble.h"
#include "layers/status_bar.h"

static TextLayer* main_text;
static TextLayer* app_name_text;
static CustomStatusBarLayer* status_bar;
static const char* status_text;

// ReSharper disable once CppParameterMayBeConstPtrOrRef
static void window_load(Window* window)
{
    Layer* window_layer = window_get_root_layer(window);
    const GRect screen_bounds = layer_get_bounds(window_layer);
    status_bar = custom_status_bar_layer_create(screen_bounds);
    const GRect status_bar_bounds = layer_get_bounds(status_bar->layer);

    main_text = text_layer_create(
        GRect(
            0,
            status_bar_bounds.size.h,
            screen_bounds.size.w,
            screen_bounds.size.h - status_bar_bounds.size.h
        )
    );
    text_layer_set_text_alignment(main_text, GTextAlignmentCenter);
    text_layer_set_text_color(main_text, GColorBlack);
    text_layer_set_background_color(main_text, GColorClear);
    text_layer_set_font(main_text, fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text(main_text, status_text);

    GRect app_name_bounds = custom_status_bar_get_left_space(status_bar);

    app_name_text = text_layer_create(app_name_bounds);
    text_layer_set_text_alignment(app_name_text, GTextAlignmentLeft);
    text_layer_set_text_color(app_name_text, GColorWhite);
    text_layer_set_background_color(app_name_text, GColorClear);
    text_layer_set_font(app_name_text, fonts_get_system_font(FONT_KEY_GOTHIC_14));
    text_layer_set_text(app_name_text, "Text Sync");

    layer_add_child(window_layer, text_layer_get_layer(main_text));
    layer_add_child(window_layer, status_bar->layer);
    layer_add_child(window_layer, text_layer_get_layer(app_name_text));
}

static void window_show(Window* window)
{
    custom_status_bar_set_active(status_bar, true);
}

static void window_hide(Window* window)
{
    custom_status_bar_set_active(status_bar, false);
}

static void window_unload(Window* window)
{
    text_layer_destroy(main_text);
    text_layer_destroy(app_name_text);
    window_destroy(window);
}

static void window_status_show(const char* text)
{
    status_text = text;

    Window* window = window_create();
    window_set_window_handlers(
        window,
        (WindowHandlers)
    {
        .
        load = window_load,
        .
        unload = window_unload,
        .
        appear = window_show,
        .
        disappear = window_hide
    }
    )
    ;
    window_stack_pop_all(false);
    window_stack_push(window, false);
}

void window_status_show_error(const char* text)
{
    window_status_show(text);
}

void bluetooth_show_error(const char* text)
{
    window_status_show_error(text);
}
