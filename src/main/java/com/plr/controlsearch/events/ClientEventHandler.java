package com.plr.controlsearch.events;

import com.plr.controlsearch.client.gui.GuiNewControls;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraftforge.client.event.GuiOpenEvent;

/**
 * Created by Jared on 8/28/2016.
 */
public class ClientEventHandler {

    @SubscribeEvent
    public void guiInit(GuiOpenEvent e) {
        if (e.gui instanceof GuiControls && !(e.gui instanceof GuiNewControls)) {
            e.gui = new GuiNewControls(Minecraft.getMinecraft().currentScreen, Minecraft.getMinecraft().gameSettings);
        }
    }


}
