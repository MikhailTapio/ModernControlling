package com.plr.controlsearch.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiSlot;
import org.lwjgl.input.Mouse;

public class Utilities {
    public static void handleMouseInput(GuiKeyBindingList gui) {
        if (isMouseYWithinSlotBounds(gui)) {
            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && gui.mouseY >= gui.top && gui.mouseY <= gui.bottom) {
                int i = (gui.width - gui.getListWidth()) / 2;
                int j = (gui.width + gui.getListWidth()) / 2;
                int k = gui.mouseY - gui.top - gui.headerPadding + (int) gui.amountScrolled - 4;
                int l = k / gui.slotHeight;

                if (l < gui.field_148190_m.length && gui.mouseX >= i && gui.mouseX <= j && l >= 0 && k >= 0) {
                    gui.selectedElement = l;
                }
            }

            if (Mouse.isButtonDown(0) && gui.func_148125_i()) {
                if (gui.initialClickY == -1) {
                    boolean flag1 = true;

                    if (gui.mouseY >= gui.top && gui.mouseY <= gui.bottom) {
                        int j2 = (gui.width - gui.getListWidth()) / 2;
                        int k2 = (gui.width + gui.getListWidth()) / 2;
                        int l2 = gui.mouseY - gui.top - gui.headerPadding + (int) gui.amountScrolled - 4;
                        int i1 = l2 / gui.slotHeight;

                        if (i1 < gui.field_148190_m.length && gui.mouseX >= j2 && gui.mouseX <= k2 && i1 >= 0 && l2 >= 0) {
                            boolean flag = i1 == gui.selectedElement && Minecraft.getSystemTime() - gui.lastClicked < 250L;
                            gui.selectedElement = i1;
                            gui.lastClicked = Minecraft.getSystemTime();
                        } else if (gui.mouseX >= j2 && gui.mouseX <= k2 && l2 < 0) {
                            flag1 = false;
                        }

                        int i3 = gui.width / 2 + 139;
                        int j1 = i3 + 6;

                        if (gui.mouseX >= i3 && gui.mouseX <= j1) {
                            gui.scrollMultiplier = -1.0F;
                            int k1 = gui.func_148135_f();

                            if (k1 < 1) {
                                k1 = 1;
                            }

                            int l1 = (int) ((float) ((gui.bottom - gui.top) * (gui.bottom - gui.top)) / (float) (gui.field_148190_m.length * gui.slotHeight + gui.headerPadding));
                            l1 = clamp(l1, 32, gui.bottom - gui.top - 8);
                            gui.scrollMultiplier /= (float) (gui.bottom - gui.top - l1) / (float) k1;
                        } else {
                            gui.scrollMultiplier = 1.0F;
                        }

                        if (flag1) {
                            gui.initialClickY = gui.mouseY;
                        } else {
                            gui.initialClickY = -2;
                        }
                    } else {
                        gui.initialClickY = -2;
                    }
                } else if (gui.initialClickY >= 0) {
                    gui.amountScrolled -= (float) (gui.mouseY - gui.initialClickY) * gui.scrollMultiplier;
                    gui.initialClickY = gui.mouseY;
                }
            } else {
                gui.initialClickY = -1;
            }

            int i2 = Mouse.getEventDWheel();

            if (i2 != 0) {
                if (i2 > 0) {
                    i2 = -1;
                } else if (i2 < 0) {
                    i2 = 1;
                }

                gui.amountScrolled += (float) (i2 * gui.slotHeight / 2);
            }
        }
    }

    public static int clamp(int num, int min, int max) {
        return (num < min) ? min : Math.min(num, max);
    }

    public static boolean isMouseYWithinSlotBounds(GuiSlot slot) {
        return slot.mouseY >= slot.top && slot.mouseY <= slot.bottom && slot.mouseX >= slot.left && slot.mouseX <= slot.right;
    }
}
