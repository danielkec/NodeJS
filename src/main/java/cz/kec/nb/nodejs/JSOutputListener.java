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

import java.util.Scanner;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * JSOutputListener
 *
 * @author Daniel Kec <daniel.kec at syntea.cz>
 * @since 23.2.2011
 * @version 1.0
 * @class cz.kec.nb.nodejs.JSOutputListener
 */
public class JSOutputListener implements OutputListener{
private final EditorCookie ec;
    private final FileObject fo;
    private final int line;
    public JSOutputListener(FileObject fo,int line) throws DataObjectNotFoundException {
        this.fo = fo;
        this.ec = DataObject.find(fo).getLookup().lookup(EditorCookie.class);
        this.line = line;
    }

@Override
    public void outputLineSelected(OutputEvent oe) {

    }

@Override
    public void outputLineAction(OutputEvent oe) {
                ec.open();
		if (EditorRegistry.lastFocusedComponent() != null) {
			JTextComponent target = EditorRegistry.lastFocusedComponent();
			String text = target.getText();
			selectError(text, target);
		}
    }

@Override
    public void outputLineCleared(OutputEvent oe) {

    }

    	private void selectError(String text, JTextComponent target) {

		Scanner scanner = new Scanner(text);
		int lineCount = 0;
		int charCount = 0;
		int lineStart = 0;
		while(scanner.hasNextLine()){
			lineCount++;
			lineStart = charCount;
			charCount = charCount + (scanner.nextLine().length())+("\n".length());
			if(lineCount==line){
				target.select(lineStart, charCount - ("\n".length())); // Select line with error
				break;
			}

		}

	}
}
