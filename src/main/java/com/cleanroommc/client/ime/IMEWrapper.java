package com.cleanroommc.client.ime;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjglx.input.KeyCodes;
import org.lwjglx.input.Keyboard;
import org.lwjglx.opengl.Display;

import javax.swing.*;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * General IMEWrapper class
 */
@SideOnly(Side.CLIENT)
public final class IMEWrapper extends JDialog{
    private static final IMEWrapper instance = new IMEWrapper();
    public static void setWrapperLocation(int x, int y) {
        instance.setLocation(x, y);
    }

    public static void setWrapperVisible(boolean v) {
        instance.setVisible(v);
    }
    public static boolean isWrapperVisible() {
        return instance.isVisible();
    }

    private final JTextField textField;
    public IMEWrapper() {
        // Allow lwjglx to detect key pressing in awt
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEvent -> {
            synchronized (IMEHelper.class) {
                switch (keyEvent.getID()) {
                    case KeyEvent.KEY_PRESSED -> IMEHelper.pressingMap[IMEHelper.translateFromAWT(keyEvent.getKeyCode())] = true;
                    case KeyEvent.KEY_RELEASED -> IMEHelper.pressingMap[IMEHelper.translateFromAWT(keyEvent.getKeyCode())] = false;
                }
                return false;
            }
        });

        this.setVisible(false);
        //this.setSize(100, 20);
        this.setAlwaysOnTop(true);
        this.setUndecorated(true); //Minimize to one pixel
        this.setOpacity(1.0F); //Try to make it invisible
        this.setType(Type.POPUP);
        this.setLocation(Display.getX(), Display.getY() + Display.getHeight()); //Default location
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        textField = new JTextField();
        textField.setFocusTraversalKeysEnabled(false); // Allow tab key to be detected
        textField.setTransferHandler(null); // Should disable clip board and drag & drop
        this.add(textField);

        textField.setNavigationFilter(new NavigationFilter() { // Disable cursor moving
            @Override
            public void setDot(FilterBypass fb, int dot, Position.Bias bias) {}
            @Override
            public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {}
        });

        textField.addKeyListener(new KeyListener() { // Key event dispatching
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                int lwjglCode = IMEHelper.translateFromAWT(keyEvent.getKeyCode());
                GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                char c = Keyboard.getCharFromCode(lwjglCode);
                if (screen != null) {
                    if (c == 0 || keyEvent.getKeyChar() == '\uFFFF') { // Control keys with no char
                        screen.invokeKeyTyped('\0', lwjglCode);
                    }
                }
                insertAndClear(keyEvent);
                //FMLLog.log.info("TYPED: " + KeyEvent.getKeyText(keyEvent.getKeyCode()) + keyEvent.getKeyCode());
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                int lwjglCode = IMEHelper.translateFromAWT(keyEvent.getKeyCode());
                GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                char c = Keyboard.getCharFromCode(lwjglCode);
                if (screen != null) {
                    if (c == 0 || keyEvent.getKeyChar() == '\uFFFF') {
                        screen.invokeKeyTyped('\0', lwjglCode);
                    } else if (IMEHelper.pressingMap[Keyboard.KEY_LCONTROL] || IMEHelper.pressingMap[Keyboard.KEY_RCONTROL]) { // Handle ctrl + c, v, a, x
                        textField.setText(textField.getText() + c);
                    }
                }
                insertAndClear(keyEvent);
                //FMLLog.log.info("PRESSED: " + KeyEvent.getKeyText(keyEvent.getKeyCode()) + keyEvent.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                int lwjglCode = IMEHelper.translateFromAWT(keyEvent.getKeyCode());
                GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                if (screen != null) {
                    switch (lwjglCode) {
                        // For unknown reason tab key only has release event
                        case Keyboard.KEY_TAB -> screen.invokeKeyTyped('\0', lwjglCode);
                    }
                }
                insertAndClear(keyEvent);
                //jjjjFMLLog.log.info("RELEASED: " + KeyEvent.getKeyText(keyEvent.getKeyCode()) + keyEvent.getKeyCode());
            }

            /**
             * type every char in test field to screen and clear them all
             * @param event key event from swing
             */
            private void insertAndClear(KeyEvent event) {
                GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                if(screen != null) {
                    for (char c : textField.getText().toCharArray()) {
                        if (KeyCodes.toLwjglKey(c) != Keyboard.KEY_NONE) {
                            screen.invokeKeyTyped(c, KeyCodes.toLwjglKey(c));

                        } else {
                            screen.invokeKeyTyped(c, Keyboard.KEY_SPACE);
                        }
                    }
                }
                if (event.getComponent() instanceof JTextField) {
                    ((JTextField) event.getComponent()).setText("");
                }
            }
        });
    }
}
