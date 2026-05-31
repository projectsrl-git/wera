
package net.projectsrl.dafne.security;

import net.project.misc.Util;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.authentication.MenuItem;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;

public class DafneUserSecurityInfo extends WebAppUserSecurityInfo<Integer> {

    public DafneUserSecurityInfo() {

        super();
    }

    
    @SuppressWarnings("unchecked")
    @Override
    public void copy(UserSecurityInfo userInfo) {

        super.copy(userInfo);

        getSessionMap().putAll(((WebAppUserSecurityInfo<Integer>) userInfo).getSessionMap());
        
        Integer userId=((WebAppUserSecurityInfo<Integer>) userInfo).getIdUtente();
        
        setUserUniqueIdentifier(userId);

    }
    
    @Override    
    public String getSelectedPathTag(MenuItem currentMenuItem) {

        String path = "";
        if (Util.IsNotEmpty(currentMenuItem.getPath())) {
            path = currentMenuItem.getPath();
        }
        return path;
    }    
    
}
