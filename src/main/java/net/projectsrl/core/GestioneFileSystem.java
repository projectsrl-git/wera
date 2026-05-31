package net.projectsrl.core;


import java.io.File;
import java.util.Hashtable;
import java.util.Vector;


/**
 * La classe
 *
 * @version $Revision: 1.1 $
 * @author $author$
 */
public class GestioneFileSystem {
    /**
     * Questo metodo
     *
     * @return DOCUMENT ME!
     */
    public File[] getRoot() {
        return File.listRoots();
    }



    /**
     * Questo metodo
     *
     * @return DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
	public Vector directoryRootLevel() {
        Vector    Hfile = new Vector();
        Hashtable hf = new Hashtable();

        for (int j = 0; j < getRoot().length; j++) {
            hf = new Hashtable();
            hf.put("nome", (getRoot()[j].toString()).replaceFirst("\\\\", "/"));
            hf.put("percorso", (getRoot()[j].toString()).replaceFirst("\\\\", "/"));
            hf.put("parent", "");
            hf.put("isDirectory", "true");
            hf.put("isTopLevel", "true");

            Hfile.add(hf);
        }

        return Hfile;
    }



    /**
     * Questo metodo
     *
     * @param file DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
	public Vector directoryLevel(File file) {
        Vector    Hfile = new Vector();
        Hashtable hf = new Hashtable();

        for (int i = 0; i < file.listFiles().length; i++) {
            hf = new Hashtable();
            hf.put("nome", file.list()[i]);
            hf.put("percorso", file.listFiles()[i].getAbsolutePath().replaceAll("\\\\", "/"));
            hf.put("parent", file.listFiles()[0].getParentFile().getAbsolutePath().replaceAll("\\\\", "/"));

            if (file.listFiles()[i].isDirectory()) {
                hf.put("isDirectory", "true");
            } else {
                hf.put("isDirectory", "false");
            }

            hf.put("isTopLevel", "false");
            Hfile.add(hf);
        }

        return Hfile;
    }
}
