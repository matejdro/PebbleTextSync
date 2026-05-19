#include "window_file_list.h"
#include "pebble.h"
#include "window_status.h"
#include "window_text.h"
#include "commons/connection/bucket_sync.h"
#include "../connection/packets.h"
#include "commons/bytes.h"
#include "commons/connection/bluetooth.h"
#include "layers/status_bar.h"

typedef struct
{
    uint8_t bucket_id;
    char title[21];
} File;

static uint8_t file_count = 0;
static File files[MAX_BUCKETS];
static MenuLayer* menu;
static TextLayer* empty_text;
static CustomStatusBarLayer* status_bar;

static void load_menu();
static void configure_buttons(void* context);

static void on_bucket_changed(BucketMetadata bucket, void* context)
{
    load_menu();
}

static uint16_t menu_get_num_rows_callback(MenuLayer* me, uint16_t section_index, void* data)
{
    return file_count;
}

// ReSharper disable once CppParameterMayBeConstPtrOrRef
static int16_t menu_get_cell_height_callback(MenuLayer* me, MenuIndex* cell_index, void* data)
{
    return 40;
}

// ReSharper disable once CppParameterMayBeConstPtrOrRef
static void menu_draw_row_callback(GContext* ctx, const Layer* cell_layer, MenuIndex* cell_index, void* data)
{
    const int16_t row = cell_index->row;

    menu_cell_basic_draw(ctx, cell_layer, files[row].title, NULL, NULL);
}

// ReSharper disable once CppParameterMayBeConstPtrOrRef
static void window_load(Window* window)
{
    Layer* window_layer = window_get_root_layer(window);
    const GRect screen_bounds = layer_get_bounds(window_layer);
    status_bar = custom_status_bar_layer_create(screen_bounds);
    const GRect status_bar_bounds = layer_get_bounds(status_bar->layer);

    menu = menu_layer_create(
        GRect(
            0,
            status_bar_bounds.size.h,
            screen_bounds.size.w,
            screen_bounds.size.h - status_bar_bounds.size.h
        )
    );

    menu_layer_set_callbacks(menu, NULL, (MenuLayerCallbacks){
                                 .get_num_rows = menu_get_num_rows_callback,
                                 .draw_row = menu_draw_row_callback,
                                 .get_cell_height = menu_get_cell_height_callback
                             }
    );

    empty_text = text_layer_create(
        GRect(
            0,
            status_bar_bounds.size.h,
            screen_bounds.size.w,
            screen_bounds.size.h - status_bar_bounds.size.h
        )
    );
    text_layer_set_text_alignment(empty_text, GTextAlignmentCenter);
    text_layer_set_text_color(empty_text, GColorBlack);
    text_layer_set_background_color(empty_text, GColorClear);
    text_layer_set_font(empty_text, fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text(empty_text, "No texts yet. \n\n Add some in the phone.");
    layer_set_hidden(text_layer_get_layer(empty_text), true);

    layer_add_child(window_layer, menu_layer_get_layer(menu));
    layer_add_child(window_layer, status_bar->layer);
    layer_add_child(window_layer, text_layer_get_layer(empty_text));

    window_set_click_config_provider_with_context(window, configure_buttons, NULL);

    load_menu();
}

static void window_unload(Window* window)
{
    menu_layer_destroy(menu);
    text_layer_destroy(empty_text);
    window_destroy(window);
}

static void window_show(Window* window)
{
    bucket_sync_set_bucket_list_change_callback(load_menu);
    bucket_sync_set_bucket_data_change_callback(on_bucket_changed, NULL);
    custom_status_bar_set_active(status_bar, true);
    load_menu();
}

static void window_hide(Window* window)
{
    custom_status_bar_set_active(status_bar, false);
    bucket_sync_clear_bucket_list_change_callback(load_menu);
    bucket_sync_clear_bucket_data_change_callback(on_bucket_changed, NULL);
}

static void on_button_up_pressed(ClickRecognizerRef recognizer, void* context)
{
    const MenuIndex index = menu_layer_get_selected_index(menu);

    if (index.row == 0)
    {
        const uint8_t last_index = file_count - 1;
        menu_layer_set_selected_index(
            menu,
            (MenuIndex){
                .row = last_index,
                .section = 0
            },
            MenuRowAlignCenter,
            true
        );
    }
    else
    {
        menu_layer_set_selected_index(
            menu,
            (MenuIndex){
                .row = index.row - 1,
                .section = 0
            },
            MenuRowAlignCenter,
            true
        );
    }
}

static void on_button_down_pressed(ClickRecognizerRef recognizer, void* context)
{
    const MenuIndex index = menu_layer_get_selected_index(menu);
    const uint8_t last_index = file_count - 1;

    if (index.row == last_index)
    {
        menu_layer_set_selected_index(
            menu,
            (MenuIndex){
                .row = 0,
                .section = 0
            },
            MenuRowAlignCenter,
            true
        );
    }
    else
    {
        menu_layer_set_selected_index(
            menu, (MenuIndex)
            {
                .row = index.row + 1,
                .section = 0
            },
            MenuRowAlignCenter,
            true
        );
    }
}


static void on_button_select_pressed(ClickRecognizerRef recognizer, void* context)
{
    const MenuIndex index = menu_layer_get_selected_index(menu);
    window_text_show(files[index.row].bucket_id);
}

static void configure_buttons(void* context)
{
    window_single_repeating_click_subscribe(BUTTON_ID_UP, 100, on_button_up_pressed);
    window_single_repeating_click_subscribe(BUTTON_ID_DOWN, 100, on_button_down_pressed);
    window_single_click_subscribe(BUTTON_ID_SELECT, on_button_select_pressed);
}

void window_file_list_show()
{
    Window* window = window_create();

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

static void load_menu()
{
    BucketList* buckets = bucket_sync_get_bucket_list();

    uint8_t count = 0;
    for (uint8_t i = 0; i < buckets->count; i++)
    {
        const BucketMetadata bucket = buckets->data[i];

        if ((bucket.flags & 0x01) != 0)
        {
            // Bucket is extra text bucket
            continue;
        }

        uint8_t tmp_data[20];
        if (!bucket_sync_load_bucket_limited(bucket.id, tmp_data, 20))
        {
            continue;
        }

        File* target = &files[count];
        target->bucket_id = bucket.id;
        strcpy(target->title, (const char*)tmp_data);
        count++;
    }

    file_count = count;
    menu_layer_reload_data(menu);
    layer_set_hidden(text_layer_get_layer(empty_text), count > 0);
}
