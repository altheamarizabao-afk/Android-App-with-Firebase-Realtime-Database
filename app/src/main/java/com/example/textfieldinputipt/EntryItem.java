package com.example.textfieldinputipt;

import java.util.HashMap;

public class EntryItem extends HashMap<String, String> {
    public EntryItem(String first, String second) {
        put("firstInput", first);
        put("secondInput", second);
    }
}