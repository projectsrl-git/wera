
package net.projectsrl.webapp.authentication;

import java.util.Arrays;

/**
 * Voce di menu
 */
public class MenuItem {

    private String             _menuId       = null;
    private String             _menuIdSup    = null;
    private String             _isoLanguage  = null;
    private String             _label        = null;
    private String             _function     = null;
    private String             _link         = null;
    private int                _itemLevel    = 0;
    private String             _path         = null;
    private String             _pathDescri   = null;
    private String[]           _linkChain;
    private String[]           _pathDescriChain;
    private boolean            _readOnly     = false;
    private String             _icon         = null;
    private int                _nrOfChildren = 0;

    public static final String ID_MENU       = "ID_MENU";
    public static final String ID_MENU_SUP   = "ID_MENU_SUP";
    public static final String ISO_LANGUAGE  = "ISO_LANGUAGE";
    public static final String LABEL         = "LABEL";
    public static final String ALIAS         = "ALIAS";
    public static final String LINK          = "LINK";
    public static final String PATH          = "PATH";
    public static final String PATH_DESCRI   = "PATH_DESCRI";
    public static final String LINK_CHAIN    = "LINK_CHAIN";
    public static final String READONLY      = "READONLY";
    public static final String ITEM_LEVEL    = "ITEM_LEVEL";
    public static final String NR_CHILDREN   = "NR_CHILDREN";
    public static final String ICON          = "ICON";


    public MenuItem(String menuId, String menuIdSup, String languageISO, String label, String function, String link,
            int itemLevel, String path, String pathDescri, String linkChain, boolean readOnly, int nrOfChildren,
            String icon) {

        super();
        _menuId = menuId;
        _menuIdSup = menuIdSup;
        _isoLanguage = languageISO;
        _label = label;
        _function = function;
        _link = link;
        _itemLevel = itemLevel;
        _path = path;
        _pathDescri = pathDescri;
        _readOnly = readOnly;
        _nrOfChildren = nrOfChildren;
        _linkChain = linkChain.split(";");
        _pathDescriChain = pathDescri.split("/");
        _icon = icon;

    }



    public String getMenuId() {

        return _menuId;
    }

    public String getMenuIdSup() {

        return _menuIdSup;
    }

    public String getIsoLanguage() {

        return _isoLanguage;
    }

    public String getLabel() {

        return _label;
    }

    public String getFunction() {

        return _function;
    }

    public String getPathDescri() {

        return _pathDescri;
    }

    public String getLink() {

        return _link;
    }

    public String[] getLinkChain() {

        return _linkChain;
    }

    public String[] getPathDescriChain() {

        return _pathDescriChain;
    }

    public int getItemLevel() {

        return _itemLevel;
    }

    public String getPath() {

        return _path;
    }

    public boolean isReadOnly() {

        return _readOnly;
    }

    public boolean hasChildren() {

        return _nrOfChildren > 0;
    }

    public String getIcon() {

        return _icon;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((_function == null) ? 0 : _function.hashCode());
        result = prime * result + ((_isoLanguage == null) ? 0 : _isoLanguage.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MenuItem other = (MenuItem) obj;

        if (_function == null) {
            if (other._function != null) return false;
        } else if (!_function.equals(other._function)) return false;

        if (_isoLanguage == null) {
            if (other._isoLanguage != null) return false;
        } else if (!_isoLanguage.equals(other._isoLanguage)) return false;

        return true;
    }



    @Override
    public String toString() {

        return "MenuItem [_menuId=" + _menuId + ", _menuIdSup=" + _menuIdSup + ", _isoLanguage=" + _isoLanguage
                + ", _label=" + _label + ", _function=" + _function + ", _link=" + _link + ", _itemLevel=" + _itemLevel
                + ", _path=" + _path + ", _pathDescri=" + _pathDescri + ", _linkChain=" + Arrays.toString(_linkChain)
                + ", _pathDescriChain=" + Arrays.toString(_pathDescriChain) + ", _readOnly=" + _readOnly + ", _icon="
                + _icon + ", _nrOfChildren=" + _nrOfChildren + "]";
    }

}
