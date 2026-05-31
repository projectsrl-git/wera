/*
  MenuItem.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 09/10/2000

  Autore: Andrea R.

  Note:

  Modifiche:

 */

package net.project.servlet.security;

import java.util.Enumeration;

/**
 * Questa classe rappresenta un'item in un menu
 */
public class MenuItem {

    private String _menuId     = null;
    private String _label      = null;
    private String _idFunction = null;
    private String _image      = null;
    private String _url        = null;
    private String _itemLevel  = null;
    private String _cssClass   = null;
    private String _cssID      = null;
    private String _tipo       = null;
    private String _xfnz       = null;

    /**
     * @return Ritorna il campo tipo.
     */
    public String getTipo() {

        return _tipo;
    }

    /**
     * @param tipo il tipo da impostare.
     */
    public void setTipo(String tipo) {

        _tipo = tipo;
    }

    /**
     * @return Ritorna il campo idFunction.
     */
    public String getIdFunction() {

        return _idFunction;
    }

    /**
     * @param idFunction il idFunction da impostare.
     */
    public void setIdFunction(String idFunction) {

        _idFunction = idFunction;
    }

    /**
     * @return Ritorna il campo itemLevel.
     */
    public String getItemLevel() {

        return _itemLevel;
    }

    /**
     * @param itemLevel il itemLevel da impostare.
     */
    public void setItemLevel(String itemLevel) {

        _itemLevel = itemLevel;
    }

    /**
     * Costruttore di default
     */
    public MenuItem() {

        super();
    }

    /**
     * Costruttore con inizializzazione attributi
     *
     * @param caption java.lang.String Nome del servizio .
     * @param functionId java.lang.String Identificatore da assegnare al menù item.
     * @param image java.lang.String URL dell'immagine da assegnare al menu item.
     * @param url java.lang.String URL da assegnare al menu item.
     */
    public MenuItem(String menuId, String caption, String idFun) {

        _menuId = menuId;
        _label = caption;
        _idFunction = idFun;
        // Il livello dell'item e' data dalla lunghezza dell'ID-1 perche' gli ID sono del tipo
        // 01 Anagrafica
        // 011 Anag. clienti
        // 012 Anag. fornitori
        _itemLevel = Integer.toString(menuId.length() - 1);

    }

    /**
     * Restituisce il nome del servizio fornito dal menù item.
     *
     * @return java.lang.String Nome assegnato al servizio fornito da questo menù item.
     */
    public String getLabel() {

        return (_label);
    }

    /**
     * Restituisce l'URL dell'immagine assegnata al menù item
     *
     * @return java.lang.String Url dell'immagine assegnata al menù item.
     */
    public String getImage() {

        return (_image);
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
     * Restituisce l'identificatore del servizio (alias del menù item)
     *
     * @return java.lang.String Identificatore del menù item
     */
    public String getFunctionId() {

        return (_idFunction);
    }

    /**
     * Imposta il nome del servizio assegnato al menù item.
     *
     * @param caption java.lang.String Nome del servizio
     */
    protected void setLabel(String label) {

        _label = label;
    }

    /**
     * Imposta l'URL dell'immagine da assegnare al menù item
     *
     * @param image java.lang.String URL dell'immagine da assegnare al menu item.
     */
    protected void setImage(String image) {

        _image = image;
    }

    /**
     * Imposta l'URL da assegnare al menù item
     *
     * @param url java.lang.String URL da assegnare al menu item.
     */
    protected void setURL(String url) {

        _url = url;
    }

    /**
     * Imposta l'identificatore del servizio (alias del menù item)
     *
     * @param functionId java.lang.String Identificatore da assegnare al menù item
     */
    protected void setFunctionId(String functionId) {

        _idFunction = functionId;
    }

    /**
     * Dato che questa classe rappresenta un MenuItem e non un Menu' questo metodo ritorna un null
     *
     * @return Enumeration null
     */
    public Enumeration getMenuItems() {

        return null;
    }

    /**
     * @return Ritorna il campo menuId.
     */
    public String getMenuId() {

        return _menuId;
    }

    /**
     * @param menuId il menuId da impostare.
     */
    public void setMenuId(String menuId) {

        _menuId = menuId;
    }

    /**
     * @return Ritorna il campo class.
     */
    public String getCssClass() {

        return _cssClass;
    }

    /**
     * @param class1 il class da impostare.
     */
    public void setCssClass(String class1) {

        _cssClass = class1;
    }

    /**
     * Ritorna una stringa con tutti i campi della classe
     * 
     * @return String
     */
    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("MenuItem[");
        buffer.append("_caption = ").append(_label);
        buffer.append(" _cssClass = ").append(_cssClass);
        buffer.append(" _idFunction = ").append(_idFunction);
        buffer.append(" _image = ").append(_image);
        buffer.append(" _itemLevel = ").append(_itemLevel);
        buffer.append(" _menuId = ").append(_menuId);
        buffer.append(" _url = ").append(_url);
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * @return Ritorna il campo cssID.
     */
    public String getCssID() {

        return _cssID;
    }

    /**
     * @param cssID il cssID da impostare.
     */
    public void setCssID(String cssID) {

        _cssID = cssID;
    }

    /**
     * @return Ritorna il campo xfnz.
     */
    public String getXfnz() {

        return _xfnz;
    }

    /**
     * @param xfnz il xfnz da impostare.
     */
    public void setXfnz(String xfnz) {

        _xfnz = xfnz;
    }

}
