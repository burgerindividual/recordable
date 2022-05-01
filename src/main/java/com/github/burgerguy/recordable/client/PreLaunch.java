package com.github.burgerguy.recordable.client;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class PreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        System.loadLibrary("renderdoc");
    }
}
