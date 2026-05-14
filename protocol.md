# Buckets

Watch can store up to 15 of them, up to 255 bytes each.    
Every bucket is stored in the `2001` - `2015` storage keys.

## First file packet

Bucket data:

* File name (string, up to 20 bytes + null terminator)
* text (string, up to 253 bytes, depending on how much space was already taken by the title). No null terminator (end of bucket functions as the end of string)
