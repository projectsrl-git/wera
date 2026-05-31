
package it.project.webapp.core;

import net.project.servlet.security.UserSecurityInfo;

class SessionData {

    private String           _sessionId       = "";
    private UserSecurityInfo _userInfo;
    private long             _startTimeMillis = 0;
    private long             _lastTimeMillis  = 0;
    private String           _lastActivity    = "";

    public SessionData(String id, UserSecurityInfo info) {

        super();
        _sessionId = id;
        _userInfo = info;
        _startTimeMillis = System.currentTimeMillis();
        _lastTimeMillis = _startTimeMillis;
    }

    public void setUserId(String userId) {

        if (userId != null) {
            _userInfo.setUserId(userId);
        }

    }

    public String getSessionId() {

        return _sessionId;
    }

    public String getUserId() {

        if (_userInfo == null) {
            return "";
        }

        return _userInfo.getUserId();
    }

    public long getStartTimeMillis() {

        return _startTimeMillis;
    }

    public long getLastTimeMillis() {

        return _lastTimeMillis;
    }

    public String getLastActivity() {

        return _lastActivity;
    }

    public void updateLastActivity(String activity) {

        _lastTimeMillis = System.currentTimeMillis();
        _lastActivity = activity;
    }

}