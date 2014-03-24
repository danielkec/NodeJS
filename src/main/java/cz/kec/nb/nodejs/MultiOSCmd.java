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

import static cz.kec.nb.nodejs.RunNode.loadIcon;
import java.util.regex.Matcher;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbPreferences;

/**
 * @author Daniel Kec <daniel at kecovi.cz>
 */


public class MultiOSCmd {
    public static String[] createRunCmd(FileObject fo){
        String[] cmd = null;
        String command = NbPreferences.forModule(NodeJSOptionsPanel.class).get("COMMAND", "cd ${workingdir};\nnode ${selectedfile};");
        if(System.getProperty("os.name").toLowerCase().contains("windows")){
                String wdir = WinPath.winfixPath(fo.getParent().toURL().getPath());
                System.out.println("Working dir "+wdir);
                command = command.replaceAll("\\$\\{selectedfile\\}", fo.getNameExt());
                command = command.replaceAll("\\$\\{workingdir\\}", Matcher.quoteReplacement(wdir));
                if(command.contains(";")){
                    // win settings bubble
                    NotificationDisplayer.getDefault().notify("NodeJS",loadIcon(), "Windows with Unix settings detected reseting cmd options. Please run again.", null);
                    NbPreferences.forModule(NodeJSOptionsPanel.class).put("COMMAND", "cd ${workingdir} && node ${selectedfile}");
                    return cmd;
                }
                cmd = new String[]{"cmd", "/c",command};
            }else{
                command = command.replaceAll("\\$\\{selectedfile\\}", fo.getNameExt());
                command = command.replaceAll("\\$\\{workingdir\\}", fo.getParent().toURL().getPath());
                cmd = new String[]{"/bin/sh", "-c", command};
            }
        return cmd;
    }
    public static String[] createKillCmd(){
        String[] cmd = null;
        String command = NbPreferences.forModule(NodeJSOptionsPanel.class).get("KILL_COMMAND", "cd ${workingdir};\nnode ${selectedfile};");
        if(System.getProperty("os.name").toLowerCase().contains("windows")){
                if(command.contains(";")){
                    // win settings bubble
                    NotificationDisplayer.getDefault().notify("NodeJS",loadIcon(), "Windows with Unix settings detected reseting cmd options. Please run again.", null);
                    NbPreferences.forModule(NodeJSOptionsPanel.class).put("KILL_COMMAND", "taskkill /F /IM node.exe");
                    return cmd;
                }
                cmd = new String[]{"cmd", "/c",command};
            }else{
                cmd = new String[]{"/bin/sh", "-c", command};
            }
        return cmd;
    }
    
}
