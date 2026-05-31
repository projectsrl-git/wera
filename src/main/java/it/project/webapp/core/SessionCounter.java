
package it.project.webapp.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import net.project.db.DBConnection;
import net.project.servlet.security.UserSecurityInfo;

public class SessionCounter implements ServletContextListener, HttpSessionListener {

    private ServletContext                      _context;
    private static HashMap<String, SessionData> _sessionData     = null;
    private static Map<String, HttpSession>     _sessionObjects  = new HashMap<String, HttpSession>();
    private static int[]                        _dbConnectionNum = new int[] { 0, 0 };

    public static int[] getDbConnectionNum() {

        return _dbConnectionNum;
    }

    public static void setDbConnectionNum(int busy, int available) {

        _dbConnectionNum[0] = busy;
        _dbConnectionNum[1] = available;
    }

    private static HashSet<DBConnection> _dbConnections = new HashSet<DBConnection>();

    public static HashSet<DBConnection> getConnections() {

        return _dbConnections;
    }

    public static void addConnection(DBConnection connection) {

        _dbConnections.add(connection);
    }

    public static void removeConnection(int index) {

        _dbConnections.remove(index);
    }

    public static HashMap<String, String> getSessionIds() {

        HashMap<String, String> sessionIds = new HashMap<String, String>();
        Iterator<Map.Entry<String, SessionData>> iterator = _sessionData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SessionData> pairs = iterator.next();

            sessionIds.put(pairs.getKey(), (pairs.getValue()).getUserId());
        }
        return sessionIds;
    }

    public static HashMap<String, String> getSessionLastActivity() {

        HashMap<String, String> sessionIds = new HashMap<String, String>();
        Iterator<Map.Entry<String, SessionData>> iterator = _sessionData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SessionData> pairs = iterator.next();

            sessionIds.put(pairs.getKey(), (pairs.getValue()).getLastActivity());
        }
        return sessionIds;
    }

    public static HashMap<String, Long> getSessionStartTimeMillis() {

        HashMap<String, Long> sessionIds = new HashMap<String, Long>();
        Iterator<Map.Entry<String, SessionData>> iterator = _sessionData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SessionData> pairs = iterator.next();

            sessionIds.put(pairs.getKey(), new Long((pairs.getValue()).getStartTimeMillis()));
        }
        return sessionIds;
    }

    public static HashMap<String, Long> getSessionLastTimeMillis() {

        HashMap<String, Long> sessionIds = new HashMap<String, Long>();
        Iterator<Map.Entry<String, SessionData>> iterator = _sessionData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SessionData> pairs = iterator.next();

            sessionIds.put(pairs.getKey(), new Long((pairs.getValue()).getLastTimeMillis()));
        }
        return sessionIds;
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {

        this.increment(event.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {

        this.decrement(event.getSession());

    }

    public synchronized static void setSessionProperties(HttpSession session) {

        if (session == null) {
            return;
        }

        if (_sessionData == null) {
            _sessionData = new HashMap<String, SessionData>();

        }

        SessionData thisSessionData = _sessionData.get(session.getId());

        if (thisSessionData == null) {
            UserSecurityInfo userInfo = new UserSecurityInfo();
            if ((String) session.getAttribute("USER") != null) {
                userInfo.setUserId((String) session.getAttribute("USER"));
                thisSessionData = new SessionData(session.getId(), userInfo);
                _sessionData.put(thisSessionData.getSessionId(), thisSessionData);
            }

        }

        System.out.println("=========================");
        Iterator<Map.Entry<String, SessionData>> iterator = _sessionData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SessionData> pairs = iterator.next();
            System.out.println(pairs.getKey() + " = " + (pairs.getValue()).getUserId());
        }
        System.out.println("=========================");

    }

    synchronized static void updateLastActivity(HttpSession session, String activity) {

        if (session == null) {
            return;
        }

        if (_sessionData != null) {
            if (_sessionData.get(session.getId()) != null) {
                _sessionData.get(session.getId()).updateLastActivity(activity);
            }
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        _context = sce.getServletContext();
        _context.setAttribute("session_counter", new Integer(0));

    }

    synchronized private void increment(HttpSession session) {

        _sessionObjects.put(session.getId(), session);

        setSessionProperties(session);

        Integer counter = (Integer) _context.getAttribute("session_counter") + 1;
        _context.setAttribute("session_counter", counter);

        systemLog();

    }

    synchronized private void decrement(HttpSession session) {

        Integer counter = (Integer) _context.getAttribute("session_counter") - 1;
        _context.setAttribute("session_counter", counter);

        _sessionData.remove(session.getId());
        _sessionObjects.remove(session.getId());
        systemLog();
    }

    /**
     * Questo metodo
     * 
     */
    private void systemLog() {

        System.out.println("=========================");
        Iterator<Map.Entry<String, SessionData>> iterator = _sessionData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SessionData> pairs = iterator.next();
            System.out.println(pairs.getKey() + " = " + (pairs.getValue()).getUserId());
        }
        System.out.println("=========================");

    }

    public static void destroySession(String sessionID) {

        _sessionObjects.get(sessionID).invalidate();
    }

}
