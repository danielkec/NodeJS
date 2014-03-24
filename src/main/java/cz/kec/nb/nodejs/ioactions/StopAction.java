/*
 * Copyright 2014 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.kec.nb.nodejs.ioactions;

import cz.kec.nb.nodejs.MultiOSCmd;
import cz.kec.nb.nodejs.RunNode;
import cz.kec.nb.nodejs.WinPath;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.util.Exceptions;
import org.openide.windows.IOColorLines;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Daniel Kec <daniel at kecovi.cz>
 */
public class StopAction implements Action {

    private HashMap<String, Object> hashMap = new HashMap<>();
    private boolean enabled;
    private InputOutput io;
    private OutputWriter out;

    public void setIo(InputOutput io) {
        this.io = io;
        this.out = io.getOut();
    }

    public StopAction() {
        hashMap.put(Action.SMALL_ICON, new ImageIcon(RunNode.class.getResource("stop-icon.png")));
        hashMap.put(Action.NAME,"Kill all node.js processes");
        hashMap.put(Action.SHORT_DESCRIPTION, "Kill all node.js processes");
    }

    @Override
    public Object getValue(String key) {
        return hashMap.get(key);
    }

    @Override
    public void putValue(String key, Object value) {
        hashMap.put(key, value);
    }

    @Override
    public void setEnabled(boolean b) {
        this.enabled = b;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            String[] cmd = MultiOSCmd.createKillCmd();
            if (cmd == null) {
                return;
            }
            
            IOColorLines.println(this.io, WinPath.concatStringArray(cmd), Color.lightGray);
            final Process proc = Runtime.getRuntime().exec(cmd);
            
            final BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            final BufferedReader readerr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            // thx to Andrew Skiba http://andskiba.blogspot.com/2011/09/nodejs-plugin-for-netbeans-and-daemons.html
            final AtomicBoolean done = new AtomicBoolean(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!done.get()) {
                            Thread.sleep(50);
                            while (read.ready()) {
                                out.println(read.readLine());
                            }
                            while (readerr.ready()) {
                                //printErrLine(readerr.readLine(), io);
                                 IOColorLines.println(io, readerr.readLine(), Color.RED);
                            }
                            
                        }
                        
                        read.close();
                        readerr.close();
                        
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        proc.waitFor();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        done.set(true);
                    }
                }
            }).start();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
}
