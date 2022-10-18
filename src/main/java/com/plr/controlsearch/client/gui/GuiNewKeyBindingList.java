package com.plr.controlsearch.client.gui;

import committee.nova.mkb.api.IKeyBinding;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.LinkedList;

@SideOnly(Side.CLIENT)
public class GuiNewKeyBindingList extends GuiKeyBindingList {
    private final GuiNewControls controlsScreen;
    private final Minecraft mc;
    private LinkedList<IGuiListEntry> listEntries;
    private LinkedList<IGuiListEntry> listEntriesAll;

    private int maxListLabelWidth;

    public GuiNewKeyBindingList(GuiNewControls controls, Minecraft mcIn) {
        super(controls, mcIn);
        this.controlsScreen = controls;
        this.mc = mcIn;
        this.width = controls.width + 45;
        this.height = controls.height + 80;
        this.top = 63;
        this.bottom = controls.height - 80;
        KeyBinding[] akeybinding = ArrayUtils.clone(mcIn.gameSettings.keyBindings);
        listEntries = new LinkedList<>();
        listEntriesAll = new LinkedList<>();

        Arrays.sort(akeybinding);
        int i = 0;
        String s = null;

        for (KeyBinding keybinding : akeybinding) {
            String s1 = keybinding.getKeyCategory();

            if (!s1.equals(s)) {
                s = s1;
                if (!s1.endsWith(".hidden")) {
                    this.listEntries.add(new GuiNewKeyBindingList.CategoryEntry(s1));
                    this.listEntriesAll.add(new GuiNewKeyBindingList.CategoryEntry(s1));
                }
            }

            int j = mcIn.fontRenderer.getStringWidth(I18n.format(keybinding.getKeyDescription()));

            if (j > this.maxListLabelWidth) {
                this.maxListLabelWidth = j;
            }
            if (!s1.endsWith(".hidden")) {
                this.listEntries.add(new GuiNewKeyBindingList.KeyEntry(keybinding));
                this.listEntriesAll.add(new GuiNewKeyBindingList.KeyEntry(keybinding));
            }
        }
    }

    protected int getSize() {
        return this.listEntries.size();
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    public GuiListExtended.IGuiListEntry getListEntry(int index) {
        return this.listEntries.get(index);
    }

    protected int getScrollBarX() {
        return super.getScrollBarX() + 35;
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth() {
        return super.getListWidth() + 32;
    }

    @SideOnly(Side.CLIENT)
    public class CategoryEntry implements GuiListExtended.IGuiListEntry {

        public final String labelText;
        private final int labelWidth;

        public CategoryEntry(String name) {
            this.labelText = I18n.format(name);
            this.labelWidth = GuiNewKeyBindingList.this.mc.fontRenderer.getStringWidth(this.labelText);
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator t, int mouseX, int mouseY, boolean isSelected) {
            GuiNewKeyBindingList.this.mc.fontRenderer.drawString(this.labelText, GuiNewKeyBindingList.this.mc.currentScreen.width / 2 - this.labelWidth / 2, y + slotHeight - GuiNewKeyBindingList.this.mc.fontRenderer.FONT_HEIGHT - 1, 16777215);
        }

        /**
         * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
         * clicked and the list should not be dragged.
         */
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            return false;
        }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        }

    }

    @SideOnly(Side.CLIENT)
    public class KeyEntry implements GuiListExtended.IGuiListEntry {
        /**
         * The keybinding specified for this KeyEntry
         */
        private final KeyBinding keybinding;
        /**
         * The localized key description for this KeyEntry
         */
        private final String keyDesc;
        private final GuiButton btnChangeKeyBinding;
        private final GuiButton btnReset;

        private KeyEntry(KeyBinding name) {
            this.keybinding = name;
            this.keyDesc = I18n.format(name.getKeyDescription());
            this.btnChangeKeyBinding = new GuiButton(0, 0, 0, 95, 20, I18n.format(name.getKeyDescription()));
            this.btnReset = new GuiButton(0, 0, 0, 50, 20, I18n.format("controls.reset"));
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator t, int mouseX, int mouseY, boolean isSelected) {
            boolean flag = GuiNewKeyBindingList.this.controlsScreen.buttonId == this.keybinding;
            GuiNewKeyBindingList.this.mc.fontRenderer.drawString(this.keyDesc, x + 90 - GuiNewKeyBindingList.this.maxListLabelWidth, y + slotHeight / 2 - GuiNewKeyBindingList.this.mc.fontRenderer.FONT_HEIGHT / 2, 16777215);
            final IKeyBinding mixined = (IKeyBinding) keybinding;
            btnReset.visible = !mixined.isSetToDefaultValue();
            this.btnReset.xPosition = x + 210;
            this.btnReset.yPosition = y;
            this.btnReset.enabled = !mixined.isSetToDefaultValue();

            this.btnChangeKeyBinding.xPosition = x + 105;
            this.btnChangeKeyBinding.yPosition = y;
            this.btnChangeKeyBinding.displayString = mixined.getDisplayName();

            boolean flag1 = false;
            boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G

            if (this.keybinding.getKeyCode() != 0) {
                for (KeyBinding keybinding : GuiNewKeyBindingList.this.mc.gameSettings.keyBindings) {
                    if (keybinding != this.keybinding && ((IKeyBinding) keybinding).conflicts(this.keybinding)) {
                        flag1 = true;
                        keyCodeModifierConflict &= mixined.hasKeyCodeModifierConflict(this.keybinding);
                    }
                }
            }

            if (flag) {
                this.btnChangeKeyBinding.displayString = EnumChatFormatting.WHITE + "> " + EnumChatFormatting.YELLOW + this.btnChangeKeyBinding.displayString + EnumChatFormatting.WHITE + " <";
            } else if (flag1) {
                this.btnChangeKeyBinding.displayString = (keyCodeModifierConflict ? EnumChatFormatting.GOLD : EnumChatFormatting.RED) + this.btnChangeKeyBinding.displayString;
            }
            this.btnChangeKeyBinding.drawButton(GuiNewKeyBindingList.this.mc, mouseX, mouseY);
            this.btnReset.drawButton(GuiNewKeyBindingList.this.mc, mouseX, mouseY);
            //            if(mouseX >= x + 90 - GuiNewKeyBindingList.this.maxListLabelWidth && mouseX <= x + listWidth) {
            if (mouseY >= y && mouseY <= y + slotHeight) {
                mc.fontRenderer.drawString(I18n.format(keybinding.getKeyCategory()), mouseX + 10, mouseY, 0xFFFFFF);
            }
            //            }


        }

        /**
         * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
         * clicked and the list should not be dragged.
         */
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            if (this.btnChangeKeyBinding.mousePressed(GuiNewKeyBindingList.this.mc, mouseX, mouseY)) {
                GuiNewKeyBindingList.this.controlsScreen.buttonId = this.keybinding;
                return true;
            } else if (this.btnReset.mousePressed(GuiNewKeyBindingList.this.mc, mouseX, mouseY)) {
                ((IKeyBinding) this.keybinding).setToDefault();
                GuiNewKeyBindingList.this.mc.gameSettings.setOptionKeyBinding(this.keybinding, this.keybinding.getKeyCodeDefault());
                KeyBinding.resetKeyBindingArrayAndHash();
                return true;
            } else {
                return false;
            }
        }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            this.btnChangeKeyBinding.mouseReleased(x, y);
            this.btnReset.mouseReleased(x, y);
        }

        public KeyBinding getKeybinding() {
            return keybinding;
        }
    }

    public LinkedList<IGuiListEntry> getListEntries() {
        return listEntries;
    }

    public LinkedList<IGuiListEntry> getListEntriesAll() {
        return listEntriesAll;
    }

    public void setListEntries(LinkedList<IGuiListEntry> listEntries) {
        this.listEntries = listEntries;
    }
}
