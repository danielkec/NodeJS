/*
 * Copyright 2010 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.kec.nb.nodejs;

import cz.kec.nb.nodejs.ioactions.StopAction;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.CookieAction;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
/**
 * @author Daniel Kec,Andrew Skiba
 */
@ActionID(id = "cz.kec.nb.nodejs.RunNode", category = "NodeJS")
@ActionRegistration(displayName = "#CTL_RunNode", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Editors/text/javascript/Popup", position = 400),
    @ActionReference(path = "Loaders/text/javascript/Actions", position = 150)})
public final class RunNode extends CookieAction {

    /**
     * Load the win icon for the win settings bubble 
     * @return 
     */
    public static Icon loadIcon(){
        return new ImageIcon(RunNode.class.getResource("win.png"));
    }
    
    private static final long serialVersionUID = -3853106497059314882L;
    private FileObject fo;
    private EditorCookie editorCookie;
    /**
     * Happen when Run Node.js is selected
     * @param activatedNodes
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        try {
            DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
            editorCookie = activatedNodes[0].getLookup().lookup(EditorCookie.class);
            editorCookie.saveDocument();
            
            fo = dataObject.getPrimaryFile();
            fo.toURL();
            fo.getParent().toURL();
            StopAction sopka = new StopAction();
            final InputOutput io = IOProvider.getDefault().getIO("Node.js " + fo.getName(),false, new Action[]{sopka}, null);
            sopka.setIo(io);
            final OutputWriter out = io.getOut();
            OutputWriter erout = io.getErr();
            io.select();
            io.setOutputVisible(true);
            out.reset();
            // multi OS thing
            String[] cmd = MultiOSCmd.createRunCmd(fo);
            if(cmd==null)return;
            
            
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
                    }  
                    finally {  
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
        String file = null;
        int lineNum = 0;
        int colNum = 0;
        Pattern mask = Pattern.compile("\\((.*\\.js):(\\d*)\\:(\\d*)\\)");
        Matcher m = mask.matcher(line);
        while (m.find()) {
            file = m.group(1);
            lineNum = Integer.parseInt(m.group(2));
            colNum = Integer.parseInt(m.group(3));
        }
        if (file == null) {
            IOColorLines.println(io, line, Color.RED);
            return;
        } else {
            IOColorLines.println(io, line, new JSOutputListener(this.fo,lineNum), true, Color.BLUE);
        }
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RunNode.class, "CTL_RunNode");
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[]{DataObject.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
