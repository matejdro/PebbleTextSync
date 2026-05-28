#include <pebble.h>
#include "commons/connection/bluetooth.h"
#include "commons/connection/bucket_sync.h"
#include "connection/packets.h"
#include "ui/window_file_list.h"

const uint16_t PROTOCOL_VERSION = 3;

int main(void)
{
    packets_init();
    bluetooth_init();
    bucket_sync_init();

    send_watch_welcome();

    window_file_list_show();

    app_event_loop();
}
