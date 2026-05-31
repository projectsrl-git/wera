/*
  DataSetMenu.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 21/05/2001

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.servlet.security;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;

/**
 * Questa classe serve per leggere dalla base dati i menu' corrispondenti ai vari ruoli degli utenti. Il metodo ReadMenu
 * crea una List contenente tanti MenuItem quante sono le voci dei menu'.
 * <p>
 * Per leggere i menu' la classe utilizza un dataset di nome 'MenuList' che deve essere percio' presente nel file di
 * configurazione. Il dataset deve prevedere la presenza obbligatoria delle colonne:
 * <p>
 * <ul>
 * <li>id_menu</li>
 * <li>id_funz</li>
 * <li>caption</li>
 * </ul>
 * Le colonne opzionali sono:
 * <p>
 * <ul>
 * <li>url_image</li>
 * <li>url</li>
 * <li>css_class</li>
 * <li>id_app</li>
 * </ul>
 * <p>
 * Il dataset <b>DEVE ESSERE</b> ordinato per id_menu.
 * <p>
 * Al dataset vengono passati i parametri 'RUOLO' ed 'IDAPP' dove IDAPP e' quello letto dalla proprieta' di
 * configurazione <code>servlet.SecurityProvider.applicationId</code> dalla ServletApplication_base La colonna id_menu
 * ha un duplice scopo: identificare una voce ed organizzare la gerarchia del menu; il contenuto deve essere nella forma
 * illustrata nell'esempio seguente:
 * <p>
 * <p>
 * 01
 * <p>
 * 011
 * <p>
 * 012
 * <p>
 * 013
 * <p>
 * 0131
 * <p>
 * 0132
 * <p>
 * 02
 * <p>
 * 03
 * <p>
 * 031
 * <p>
 * 0311
 * <p>
 * 0312
 * <p>
 * 032
 * <p>
 * <p>
 */
class DataSetMenu {

    // definizione colonne del dataset
    private static final String ID_FUNZ   = "C_FNZ";
    private static final String LABEL     = "label";
    private static final String URL_IMAGE = "url_image";
    private static final String URL       = "url";
    private static final String CSS_CLASS = "css_class";
    private static final String CSS_ID    = "css_id";
    private static final String IDMENU    = "C_LVL_GRCH";
    private static final String TIPO      = "C_TP_FNZ";
    private static final String XFNZ      = "X_FNZ";

    private String              _idRole;
    private String              _idApp;

    /**
     * Costruttore.
     *
     * @param java.lang.String Identificatore del ruolo.
     */
    public DataSetMenu(String idRole, String idApp) {

        _idRole = idRole;
        _idApp = idApp;

    }

    /**
     * Metodo per la lettura dei componenti del menu passato.
     *
     * @param menu net.project.servlet.menu.Menu Il menu da leggere.
     * @exception net.project.errors.AppCrash.
     */
    public List readMenu() throws AppCrash {

        DataSet_itf dsMenu = null;

        List menu = new LinkedList();

        DataSetFactory dsFactory = DataSetFactory.getInstance();
        dsMenu = dsFactory.makeDataSet("", "MenuList");
        Map whereConditionParam = new HashMap();
        whereConditionParam.put("RUOLO", _idRole);
        whereConditionParam.put("IDAPP", _idApp);

        dsMenu.setParam(whereConditionParam);

        try {
            dsMenu.open();

            // Leggo tutto il contenuto del menu dal dataset ed aggiungo
            // alla lista menu tanti oggetti MenuItem
            MenuItem newElement = null;
            while (dsMenu.hasMoreElements()) {
                Row_itf item = (Row_itf) dsMenu.nextElement();

                String id_menu = item.getField(IDMENU).toString();
                String nome = item.getField(LABEL).toString();
                String idFunz = item.getField(ID_FUNZ).toString();

                newElement = new MenuItem(id_menu, nome, idFunz);

                if (item.getField(URL_IMAGE) != null) {
                    newElement.setImage(item.getField(URL_IMAGE).toString());
                }
                if (item.getField(URL) != null) {
                    newElement.setURL(item.getField(URL).toString());
                }
                if (item.getField(CSS_CLASS) != null) {
                    newElement.setCssClass(item.getField(CSS_CLASS).toString());
                }
                if (item.getField(TIPO) != null) {
                    newElement.setTipo(item.getField(TIPO).toString());
                }
                if (item.getField(CSS_ID) != null) {
                    newElement.setCssID(item.getField(CSS_ID).toString());
                }
                if (item.getField(XFNZ) != null) {
                    newElement.setCssID(item.getField(XFNZ).toString());
                }

                menu.add(newElement);
            }
            return menu;
        } finally {
            dsMenu.close();
        }

    }
}
