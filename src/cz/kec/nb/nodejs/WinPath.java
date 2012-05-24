package cz.kec.nb.nodejs;

import java.io.File;

public class WinPath {
    public static String winfixPath(String path){
        String fixed =(new File(path)).getAbsolutePath();
        fixed = fixed.replaceAll("\\%20"," ");
        return "\""+fixed+"\"";
//       String fixed = path;
//       if(path.startsWith("/"))fixed = fixed.replaceFirst("/","");
//       fixed = fixed.replaceAll("/", "\\\\");
//       return fixed;         
    }
}
