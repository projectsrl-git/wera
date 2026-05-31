
package net.projectsrl.webapp.listener;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class HttpSessionCollector implements HttpSessionListener {

    private static final Map<String, HttpSession> _sessions = new HashMap<String, HttpSession>();
    private static final Map<String, Long> _sessionsLastAliveCheck = new HashMap<String, Long>();

    @Override
    public void sessionCreated(HttpSessionEvent event) {

        HttpSession session = event.getSession();
        _sessions.put(session.getId(), session);
        _sessionsLastAliveCheck.put(session.getId(), System.currentTimeMillis());

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {

        _sessions.remove(event.getSession().getId());
        _sessionsLastAliveCheck.remove(event.getSession().getId());
    }

    public static Map<String, HttpSession> getSessions() {

        return _sessions;
    }
    
    public static Map<String, Long> getSessionsLastAliveCheck() {
    
        return _sessionsLastAliveCheck;
    }


}