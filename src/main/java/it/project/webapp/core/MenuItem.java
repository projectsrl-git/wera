/*
 * Created on 6-mag-2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

package it.project.webapp.core;

@Deprecated
public class MenuItem implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String            _menuId          = null;
    private String            _caption         = null;
    private String            _functionId      = null;
    private String            _itemLevel       = null;
    private String            _url             = null;
    private String            _colore          = null;
    private String            _dimensione      = null;
    private String            _immagine        = null;

    /**
     * Costruttore di default
     */
    public MenuItem() {

        super();
    }

    /**
     * Costruttore con inizializzazione attributi
     * 
     * @param menuId java.lang.String ID della voce di menu.
     * @param caption java.lang.String Descrizione della voce di menu.
     * @param functionId java.lang.String ID funzione associata alla voce di menu.
     * @param itemLevel java.lang.String Livello gerarchico della voce di menu.
     * @param url java.lang.String URL da assegnare alla voce di menu.
     */
    public MenuItem(String menuId, String caption, String functionId, String url, String colore, String dimensione,
            String immagine) {

        _menuId = menuId;
        _caption = caption;
        _functionId = functionId;
        _itemLevel = Integer.toString(menuId.length());
        _url = url;
        _colore = colore;
        _dimensione = dimensione;
        _immagine = immagine;
    }

    /**
     * Costruttore con inizializzazione attributi
     * 
     * @param menuId java.lang.String ID della voce di menu.
     * @param caption java.lang.String Descrizione della voce di menu.
     * @param functionId java.lang.String ID funzione associata alla voce di menu.
     * @param itemLevel java.lang.String Livello gerarchico della voce di menu.
     * @param url java.lang.String URL da assegnare alla voce di menu.
     */
    public MenuItem(String menuId, String caption, String functionId, String url) {

        _menuId = menuId;
        _caption = caption;
        _functionId = functionId;
        _itemLevel = Integer.toString(menuId.length());
        _url = url;
    }

    public void setItemLevel(String level) {

        _itemLevel = level;
    }

    /**
     * Restituisce il nome del servizio fornito dal menù item.
     * 
     * @return java.lang.String Nome assegnato al servizio fornito da questo menù item.
     */
    public String getCaption() {

        return (_caption);
    }

    /**
     * Restituisce l'URL assegnata al menù item
     * 
     * @return java.lang.String Url assegnata al menù item.
     */
    public String getURL() {

        return (_url);
    }

    /**
     * Restituisce l'identificatore della funzione assegnata alla voce di menu (alias del menu item)
     * 
     * @return java.lang.String Identificatore del menù item
     */
    public String getFunctionId() {

        return (_functionId);
    }

    /**
     * Imposta il nome del servizio assegnato al menù item.
     * 
     * @param caption java.lang.String Nome del servizio
     */
    public void setCaption(String caption) {

        _caption = caption;
    }

    /**
     * Imposta l'URL da assegnare al menù item
     * 
     * @param url java.lang.String URL da assegnare al menu item.
     */
    public void setURL(String url) {

        _url = url;
    }

    /**
     * Imposta l'identificatore del servizio (alias del menù item)
     * 
     * @param functionId java.lang.String Identificatore da assegnare al menù item
     */
    public void setFunctionId(String functionId) {

        _functionId = functionId;
    }

    /**
     * Imposta il COLORE da assegnare al menù item
     * 
     * @param functionId java.lang.String Identificatore da assegnare al menù item
     */
    public void setCOLORE(String colore) {

        _colore = colore;
    }

    /**
     * Imposta la DIMENSIONE da assegnare al menù item
     * 
     * @param functionId java.lang.String Identificatore da assegnare al menù item
     */
    public void setDIMENSIONE(String dimensione) {

        _dimensione = dimensione;
    }

    /**
     * Imposta l'IMMAGINE da assegnare al menù item
     * 
     * @param functionId java.lang.String Identificatore da assegnare al menù item
     */
    public void setIMMAGINE(String immagine) {

        _immagine = immagine;
    }

    /**
     * Restituisce una descrizione di formato stringa dell'oggetto
     * 
     * @return java.lang.String Descrizione del componente
     */
    @Override
    public String toString() {

        String rc = "";
        rc = rc + _menuId + "-";
        rc = rc + _caption + "-";
        rc = rc + _functionId + "-";
        rc = rc + _itemLevel + "-";
        rc = rc + _url + "-";
        rc = rc + _colore + "-";
        rc = rc + _dimensione + "-";
        rc = rc + _immagine + "-";

        return (rc);
    }

    /**
     * @return
     */
    public String getItemLevel() {

        return _itemLevel;
    }

    /**
     * @return
     */
    public String getMenuId() {

        return _menuId;
    }

    /**
     * @return
     */
    public String getColore() {

        return _colore;
    }

    /**
     * @return
     */
    public String getDimensione() {

        return _dimensione;
    }

    /**
     * @return
     */
    public String getImmagine() {

        return _immagine;
    }

    /**
     * @param string
     */
    public void setMenuId(String string) {

        _menuId = string;
        _itemLevel = Integer.toString(_menuId.length());
    }

    /**
     * @return
     */
    public String getParentLevel() {

        return _itemLevel.substring(0, 1);
    }

}
