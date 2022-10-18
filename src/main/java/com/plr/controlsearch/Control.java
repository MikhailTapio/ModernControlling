package com.plr.controlsearch;

import com.plr.controlsearch.events.ClientEventHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.plr.controlsearch.Control.*;

/**
 * Created by Jared on 8/28/2016.
 */
@Mod(modid = MODID, name = NAME, version = VERSION, acceptableRemoteVersions = "*")
public class Control {
    public static Set<String> PATRON_LIST = new HashSet<>();
    public static final String MODID = "controlling";
    public static final String NAME = "Controlling";
    public static final String VERSION = "3.0.10";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        if (e.getSide() != Side.CLIENT) return;
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        new Thread(() -> {
            try {
                URL url = new URL("https://blamejared.com/patrons.txt");
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);
                urlConnection.setRequestProperty("User-Agent", "Controlling|1.12.2");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    PATRON_LIST = reader.lines().filter(s -> !s.isEmpty()).collect(Collectors.toSet());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
