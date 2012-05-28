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

import java.io.File;

public class WinPath {
    /**
     * Fix file path so it can be used on win with Runtime.execute
     */
    public static String winfixPath(String path){
        String fixed =(new File(path)).getAbsolutePath();
        fixed = fixed.replaceAll("\\%20"," ");
        return "\""+fixed+"\"";     
    }
}
