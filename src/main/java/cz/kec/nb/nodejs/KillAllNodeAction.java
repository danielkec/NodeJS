 /* Copyright 2013 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.kec.nb.nodejs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

@ActionID(
        category = "NodeJS",
        id = "cz.kec.nb.nodejs.KillAllNodeAction"
)
@ActionRegistration(
        iconBase = "cz/kec/nb/nodejs/stop-icon.png",
        displayName = "Stop All NodeJS instances"
)
@ActionReferences(value = {
@ActionReference(path = "Menu/BuildProject", position = 1201),
@ActionReference(path = "Editors/text/javascript/Popup", position = 401)})
public final class KillAllNodeAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            String[] cmd = MultiOSCmd.createKillCmd();
            if (cmd == null) {
                return;
            }
            
            
            final InputOutput io = IOProvider.getDefault().getIO("Killing all Node.js instances", false);
            final OutputWriter out = io.getOut();
            OutputWriter erout = io.getErr();
            io.select();
            io.setOutputVisible(true);
            out.reset();
            
            IOColorLines.println(io, WinPath.concatStringArray(cmd), Color.lightGray);

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
                                printErrLine(readerr.readLine(), io);
                                //erout.println(readerr.readLine());  
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

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /**
     * Prints the err line with hyperlinks to the source files
     * @throws IOException 
     */
    private void printErrLine(String line, InputOutput io) throws IOException {
       
            IOColorLines.println(io, line, Color.RED);

    }
}
