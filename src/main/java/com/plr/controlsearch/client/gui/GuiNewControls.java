package com.plr.controlsearch.client.gui;

import com.plr.controlsearch.Control;
import com.plr.controlsearch.util.Utilities;
import committee.nova.mkb.api.IKeyBinding;
import committee.nova.mkb.keybinding.KeyModifier;
import cpw.mods.fml.client.config.GuiCheckBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class GuiNewControls extends GuiControls {

    private static final GameSettings.Options[] OPTIONS_ARR = new GameSettings.Options[]{GameSettings.Options.INVERT_MOUSE, GameSettings.Options.SENSITIVITY, GameSettings.Options.TOUCHSCREEN};
    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */
    private final GuiScreen parentScreen;
    /**
     * Reference to the GameSettings object.
     */
    private final GameSettings options;
    /**
     * The ID of the button that has been pressed.
     */
    private GuiButton buttonReset;

    private GuiTextField search;
    private String lastFilterText = "";

    private boolean conflicts = false;
    private boolean none = false;

    private EnumSortingType sortingType = EnumSortingType.DEFAULT;

    public GuiButton buttonConflict;
    public GuiButton buttonNone;
    public GuiButton buttonSorting;
    public GuiButton buttonPatreon;
    private String name;

    public GuiCheckBox boxSearchCategory;
    public GuiCheckBox boxSearchKey;


    public GuiNewControls(GuiScreen screen, GameSettings settings) {
        super(screen, settings);
        this.parentScreen = screen;
        this.options = settings;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        this.conflicts = false;
        this.none = false;
        this.sortingType = EnumSortingType.DEFAULT;
        this.keyBindingList = new GuiNewKeyBindingList(this, this.mc);
        this.buttonList.add(new GuiButton(200, this.width / 2 - 155, this.height - 29, 150, 20, translate("gui.done")));

        this.buttonReset = new GuiButton(201, this.width / 2 - 155 + 160, this.height - 29, 155, 20, translate("controls.resetAll"));
        this.buttonList.add(this.buttonReset);
        this.field_146495_a = translate("controls.title");
        int i = 0;

        for (GameSettings.Options gamesettings$options : OPTIONS_ARR) {
            if (gamesettings$options.getEnumFloat()) {
                this.buttonList.add(new GuiOptionSlider(gamesettings$options.returnEnumOrdinal(), this.width / 2 - 155 + i % 2 * 160, 18 + 24 * (i >> 1), gamesettings$options));
            } else {
                this.buttonList.add(new GuiOptionButton(gamesettings$options.returnEnumOrdinal(), this.width / 2 - 155 + i % 2 * 160, 18 + 24 * (i >> 1), gamesettings$options, this.options.getKeyBinding(gamesettings$options)));
            }
            ++i;
        }

        search = new GuiTextField(mc.fontRenderer, this.width / 2 - 154, this.height - 29 - 23, 148, 18);
        search.setCanLoseFocus(true);
        buttonConflict = new GuiButton(2906, this.width / 2 - 155 + 160, this.height - 29 - 24, 150 / 2, 20, translate("options.showConflicts"));
        buttonNone = new GuiButton(2907, this.width / 2 - 155 + 160 + 80, this.height - 29 - 24, 150 / 2, 20, translate("options.showNone"));
        buttonSorting = new GuiButton(2908, this.width / 2 - 155 + 160 + 80, this.height - 29 - 24 - 24, 150 / 2, 20, translate("options.sort") + ": " + sortingType.getName());
        boxSearchCategory = new GuiCheckBox(2909, this.width / 2 - (155 / 2) + 20, this.height - 29 - 37, translate("options.category"), false);
        boxSearchKey = new GuiCheckBox(2910, this.width / 2 - (155 / 2) + 20, this.height - 29 - 50, translate("options.key"), false);
        buttonPatreon = new GuiButton(2911, this.width / 2 - 155 + 160, this.height - 29 - 24 - 24, 150 / 2, 20, "Patreon");
        this.buttonList.add(buttonConflict);
        this.buttonList.add(buttonNone);
        this.buttonList.add(buttonSorting);
        this.buttonList.add(boxSearchCategory);
        this.buttonList.add(boxSearchKey);
        this.buttonList.add(buttonPatreon);
        name = Control.PATRON_LIST.stream().skip(Control.PATRON_LIST.isEmpty() ? 0 : new Random().nextInt(Control.PATRON_LIST.size())).findFirst().orElse("");
    }

    @Override
    public void updateScreen() {
        search.updateCursorCounter();
        if (!search.getText().equals(lastFilterText)) {
            refreshKeys();
        }
    }

    public void refreshKeys() {
        LinkedList<GuiListExtended.IGuiListEntry> workingList = getAllEntries();
        LinkedList<GuiListExtended.IGuiListEntry> newList = getEmptyList();
        if (none) {
            LinkedList<GuiListExtended.IGuiListEntry> unbound = new LinkedList<>();
            for (GuiListExtended.IGuiListEntry entry : workingList) {
                if (entry instanceof GuiNewKeyBindingList.KeyEntry) {
                    GuiNewKeyBindingList.KeyEntry ent = (GuiNewKeyBindingList.KeyEntry) entry;
                    if (ent.getKeybinding().getKeyCode() == 0) {
                        unbound.add(ent);
                    }
                }
            }
            workingList = unbound;
        } else if (conflicts) {
            LinkedList<GuiListExtended.IGuiListEntry> conflicts = new LinkedList<>();
            for (GuiListExtended.IGuiListEntry entry : workingList) {
                if (entry instanceof GuiNewKeyBindingList.KeyEntry) {
                    GuiNewKeyBindingList.KeyEntry ent = (GuiNewKeyBindingList.KeyEntry) entry;
                    if (ent.getKeybinding().getKeyCode() == 0) {
                        continue;
                    }
                    for (GuiListExtended.IGuiListEntry entry1 : ((GuiNewKeyBindingList) keyBindingList).getListEntriesAll()) {
                        if (!entry.equals(entry1))
                            if (entry1 instanceof GuiNewKeyBindingList.KeyEntry) {
                                GuiNewKeyBindingList.KeyEntry ent1 = (GuiNewKeyBindingList.KeyEntry) entry1;
                                if (ent1.getKeybinding().getKeyCode() == 0) {
                                    continue;
                                }
                                if (((IKeyBinding) ent.getKeybinding()).conflicts(ent1.getKeybinding())) {
                                    if (!conflicts.contains(ent))
                                        conflicts.add(ent);
                                    if (!conflicts.contains(ent1))
                                        conflicts.add(ent1);
                                }
                            }
                    }

                }
            }
            workingList = conflicts;
        }
        boolean searched = false;
        if (!search.getText().isEmpty()) {

            for (GuiListExtended.IGuiListEntry entry : workingList) {
                if (!isKeyEntry(entry)) {
                    continue;
                }
                GuiNewKeyBindingList.KeyEntry ent = (GuiNewKeyBindingList.KeyEntry) entry;
                String compareStr = translate(ent.getKeybinding().getKeyDescription()).toLowerCase();
                if (boxSearchCategory.isChecked()) {
                    compareStr = translate(ent.getKeybinding().getKeyCategory()).toLowerCase();
                }
                if (boxSearchKey.isChecked()) {
                    compareStr = translate(((IKeyBinding) ent.getKeybinding()).getDisplayName()).toLowerCase();
                }

                if (compareStr.contains(search.getText().toLowerCase())) {
                    newList.add(entry);
                }
            }
            searched = true;
        }
        lastFilterText = search.getText();
        if (!searched) {
            newList = workingList;
        }
        newList = sort(newList, sortingType);
        setEntries(newList);

    }


    public LinkedList<GuiListExtended.IGuiListEntry> sort(LinkedList<GuiListExtended.IGuiListEntry> list, EnumSortingType type) {
        if (sortingType != EnumSortingType.DEFAULT) {
            LinkedList<GuiListExtended.IGuiListEntry> filteredList = getEmptyList();
            for (GuiListExtended.IGuiListEntry entry : list) {
                if (isKeyEntry(entry)) {
                    filteredList.add(entry);
                }
            }
            filteredList.sort((o1, o2) -> {
                if (o1 instanceof GuiNewKeyBindingList.KeyEntry && o2 instanceof GuiNewKeyBindingList.KeyEntry) {
                    GuiNewKeyBindingList.KeyEntry ent1 = (GuiNewKeyBindingList.KeyEntry) o1;
                    GuiNewKeyBindingList.KeyEntry ent2 = (GuiNewKeyBindingList.KeyEntry) o2;
                    if (type == EnumSortingType.AZ) {
                        return translate(ent1.getKeybinding().getKeyDescription()).compareTo(translate(ent2.getKeybinding().getKeyDescription()));
                    } else if (type == EnumSortingType.ZA) {
                        return translate(ent2.getKeybinding().getKeyDescription()).compareTo(translate(ent1.getKeybinding().getKeyDescription()));
                    }

                }
                return -1;
            });
            return filteredList;
        }
        return list;
    }


    public LinkedList<GuiListExtended.IGuiListEntry> getAllEntries() {
        return ((GuiNewKeyBindingList) keyBindingList).getListEntriesAll();
    }

    public LinkedList<GuiListExtended.IGuiListEntry> getEmptyList() {
        return new LinkedList<>();
    }

    public void setEntries(LinkedList<GuiListExtended.IGuiListEntry> entries) {
        ((GuiNewKeyBindingList) keyBindingList).setListEntries(entries);
    }

    public boolean isKeyEntry(GuiListExtended.IGuiListEntry entry) {
        return entry instanceof GuiNewKeyBindingList.KeyEntry;// || entry instanceof GuiKeyBindingList.KeyEntry; Vanilla class doesn't have the methods we need and reflection / AT would be tedious
    }


    public String translate(String text) {
        return I18n.format(text);
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() {
        superSuperHandleMouseInput();
        Utilities.handleMouseInput(this.keyBindingList);
    }

    /**
     * Handles mouse input.
     */
    public void superSuperHandleMouseInput() {
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int k = Mouse.getEventButton();

        if (Mouse.getEventButtonState()) {
            if (this.mc.gameSettings.touchscreen && this.field_146298_h++ > 0) {
                return;
            }

            this.eventButton = k;
            this.lastMouseEvent = Minecraft.getSystemTime();
            this.mouseClicked(i, j, this.eventButton);
        } else if (k != -1) {
            if (this.mc.gameSettings.touchscreen && --this.field_146298_h > 0) {
                return;
            }

            this.eventButton = -1;
            this.mouseReleased(i, j, k);
        } else if (this.eventButton != -1 && this.lastMouseEvent > 0L) {
            long l = Minecraft.getSystemTime() - this.lastMouseEvent;
            this.mouseClickMove(i, j, this.eventButton, l);
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) {
        if (button.id == 200) {
            this.mc.displayGuiScreen(this.parentScreen);
        } else if (button.id == 201) {
            for (KeyBinding keybinding : this.mc.gameSettings.keyBindings) {
                ((IKeyBinding) keybinding).setToDefault();
            }
            KeyBinding.resetKeyBindingArrayAndHash();
        } else if (button.id < 100 && button instanceof GuiOptionButton) {
            this.options.setOptionValue(((GuiOptionButton) button).returnEnumOptions(), 1);
            button.displayString = this.options.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));
        } else if (button.id == 2906) {
            none = false;
            buttonNone.displayString = translate("options.showNone");
            if (!conflicts) {
                conflicts = true;
                buttonConflict.displayString = translate("options.showAll");
                refreshKeys();
            } else {
                conflicts = false;
                buttonConflict.displayString = translate("options.showConflicts");
                refreshKeys();
            }
        } else if (button.id == 2907) {
            conflicts = false;
            buttonConflict.displayString = translate("options.showConflicts");
            if (!none) {
                none = true;
                buttonNone.displayString = translate("options.showAll");
                refreshKeys();
            } else {
                none = false;
                buttonNone.displayString = translate("options.showNone");
                refreshKeys();
            }
        } else if (button.id == 2908) {
            sortingType = sortingType.cycle();
            buttonSorting.displayString = translate("options.sort") + ": " + sortingType.getName();
            refreshKeys();
        } else if (button.id == 2909) {
            boxSearchKey.setIsChecked(false);
            refreshKeys();
        } else if (button.id == 2910) {
            boxSearchCategory.setIsChecked(false);
            refreshKeys();
        } else if (button.id == 2911) {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(new URI("https://patreon.com/jaredlll08?s=controllingmod"));
                } catch (IOException | URISyntaxException ignored) {
                }
            } else {
                System.out.println("Desktop not supported");
            }
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.buttonId != null) {
            ((IKeyBinding) this.buttonId).setKeyModifierAndCode(KeyModifier.getActiveModifier(), -100 + mouseButton);
            this.options.setOptionKeyBinding(this.buttonId, -100 + mouseButton);
            this.buttonId = null;
            KeyBinding.resetKeyBindingArrayAndHash();
        } else if (mouseButton != 0 || !this.keyBindingList.func_148179_a(mouseX, mouseY, mouseButton)) {
            superSuperMouseClicked(mouseX, mouseY, mouseButton);
        }
        search.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 1 && mouseX >= search.xPosition && mouseX < search.xPosition + search.width && mouseY >= search.yPosition && mouseY < search.yPosition + search.height) {
            search.setText("");
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void superSuperMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (int i = 0; i < this.buttonList.size(); ++i) {
                GuiButton guibutton = (GuiButton) this.buttonList.get(i);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.buttonList);
                    if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
                        break;
                    guibutton = event.button;
                    this.selectedButton = guibutton;
                    guibutton.func_146113_a(this.mc.getSoundHandler());
                    this.actionPerformed(guibutton);
                    if (this.equals(this.mc.currentScreen))
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, event.button, this.buttonList));
                }
            }
        }
    }

    /**
     * Called when a mouse button is released.
     */
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state != 0 || !this.keyBindingList.func_148181_b(mouseX, mouseY, state)) {
            superSuperMouseReleased(mouseX, mouseY, state);
        }
    }

    protected void superSuperMouseReleased(int mouseX, int mouseY, int state) {
        if (this.selectedButton != null && state == 0) {
            this.selectedButton.mouseReleased(mouseX, mouseY);
            this.selectedButton = null;
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.buttonId != null) {
            final IKeyBinding mixined = (IKeyBinding) this.buttonId;
            if (keyCode == 1) {
                mixined.setKeyModifierAndCode(KeyModifier.NONE, 0);
                this.options.setOptionKeyBinding(this.buttonId, 0);
            } else if (keyCode != 0) {
                mixined.setKeyModifierAndCode(KeyModifier.getActiveModifier(), keyCode);
                this.options.setOptionKeyBinding(this.buttonId, keyCode);
            } else if (typedChar > 0) {
                mixined.setKeyModifierAndCode(KeyModifier.getActiveModifier(), typedChar + 256);
                this.options.setOptionKeyBinding(this.buttonId, typedChar + 256);
            }
            if (!KeyModifier.isKeyCodeModifier(keyCode)) {
                this.buttonId = null;
            }
            this.field_152177_g = Minecraft.getSystemTime();
            KeyBinding.resetKeyBindingArrayAndHash();
        } else {
            if (search.isFocused())
                search.textboxKeyTyped(typedChar, keyCode);
            else {
                superSuperKeyTyped(typedChar, keyCode);
            }
        }
    }

    protected void superSuperKeyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);

            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.keyBindingList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.field_146495_a, this.width / 2, 8, 16777215);
        this.drawCenteredString(this.fontRendererObj, translate("options.search"), this.width / 2 - (155 / 2), this.height - 29 - 39, 16777215);
        boolean flag = false;

        for (KeyBinding keybinding : this.options.keyBindings) {
            if (!((IKeyBinding) keybinding).isSetToDefaultValue()) {
                flag = true;
                break;
            }
        }

        this.buttonReset.enabled = flag;
        superSuperDrawScreen(mouseX, mouseY, partialTicks);
        search.drawTextBox();


        if (buttonPatreon.func_146115_a()) {
            String str = "Join " + name + " and other patrons!";
            drawHoveringText(Collections.singletonList(str), mouseX, mouseY, this.fontRendererObj);
        }

    }

    public void superSuperDrawScreen(int mouseX, int mouseY, float partialTicks) {
        for (Object aButtonList : this.buttonList) {
            ((GuiButton) aButtonList).drawButton(this.mc, mouseX, mouseY);
        }

        for (Object aLabelList : this.labelList) {
            ((GuiLabel) aLabelList).func_146159_a(this.mc, mouseX, mouseY);
        }
    }

}
