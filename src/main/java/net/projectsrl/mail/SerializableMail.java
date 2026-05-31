
package net.projectsrl.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;

public class SerializableMail implements Serializable {

    /**
     * SerializableMail
     */
    private static final long serialVersionUID                = 1L;
    private static final int  MAX_RETRY_ATTEMPTS_BEFORE_PAUSE = 10;
    private static final int  MAX_RETRY_ATTEMPTS              = 30;
    private static final int  MIN_ATTEMPT_WAIT_MILLIS         = 30000;
    private static final int  MAX_ATTEMPT_WAIT_MILLIS         = 60000;

    private String            _username                       = null;
    private UUID              _idEmailLog                     = null;
    private int               _retryAttempts                  = 0;
    private int               _maxRetryAttempts               = 0;
    private int               _maxRetryAttemptsBeforePause    = 0;
    private int               _minAttemptsWaitMillis          = 0;
    private int               _maxAttemptsWaitMillis          = 0;
    private long              _lastAttemp                     = 0;
    private String            _subject                        = "";
    private String            _host                           = "";
    private String            _port                           = "";
    private String            _from                           = "";
    private String            _to                             = "";
    private String            _bcc                            = "";
    private String            _cc                             = "";
    private String            _body                           = "";
    private String            _signFileName                   = "";
    private String            _footer                         = "";
    private List<String>      _attachmentList;

    public SerializableMail() {

        _idEmailLog = UUID.randomUUID();

        _attachmentList = new ArrayList<String>();

        setHost(Config.GetInstance().getProperty("mail.SMTPHost"));

        setPort(Config.GetInstance().getProperty("mail.SMTPHost.port", "25"));

        setMinAttemptsWaitMillis(getDefaultPropertyValue(MIN_ATTEMPT_WAIT_MILLIS, "mail.minAttemptsWaitMillisString"));
        setMaxAttemptsWaitMillis(getDefaultPropertyValue(MAX_ATTEMPT_WAIT_MILLIS, "mail.maxAttemptsWaitMillisString"));
        setMaxRetryAttempts(getDefaultPropertyValue(MAX_RETRY_ATTEMPTS, "mail.maxRetryAttempts"));
        setMaxRetryAttemptsBeforePause(
                getDefaultPropertyValue(MAX_RETRY_ATTEMPTS_BEFORE_PAUSE, "mail.maxRetryAttemptsBeforePause"));

    }

    private int getDefaultPropertyValue(int defaultValue, String defaultConfigProperty) {

        int defaultPropertyValue = defaultValue;

        String defaultConfigPropertyValue = Config.GetInstance().getProperty(defaultConfigProperty);

        if (Util.IsNotEmpty(defaultConfigPropertyValue)) {
            try {
                defaultPropertyValue = Integer.parseInt(defaultConfigPropertyValue);
            } catch (Throwable th) {
                AppCrash ac = new AppCrash(th);
                ac.logContext(this.getClass().getName(), "error parsing " + defaultConfigProperty + ":"
                        + defaultConfigPropertyValue + " - setting defaultValue=" + defaultValue);
            }
        }
        return defaultPropertyValue;
    }

    public int getRetryAttempts() {

        return _retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {

        _retryAttempts = retryAttempts;
    }

    public int getMaxRetryAttempts() {

        return _maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {

        _maxRetryAttempts = maxRetryAttempts;
    }

    public int getMinAttemptsWaitMillis() {

        return _minAttemptsWaitMillis;
    }

    public void setMinAttemptsWaitMillis(int minAttemptsWaitMillis) {

        _minAttemptsWaitMillis = minAttemptsWaitMillis;
    }

    public int getMaxAttemptsWaitMillis() {

        return _maxAttemptsWaitMillis;
    }

    public void setMaxAttemptsWaitMillis(int maxAttemptsWaitMillis) {

        _maxAttemptsWaitMillis = maxAttemptsWaitMillis;
    }

    public int getMaxRetryAttemptsBeforePause() {

        return _maxRetryAttemptsBeforePause;
    }

    public void setMaxRetryAttemptsBeforePause(int maxRetryAttemptsBeforePause) {

        _maxRetryAttemptsBeforePause = maxRetryAttemptsBeforePause;
    }

    public long getLastAttemp() {

        return _lastAttemp;
    }

    public void setLastAttemp(long lastAttemp) {

        _lastAttemp = lastAttemp;
    }

    public String getSubject() {

        return _subject;
    }

    public void setSubject(String subject) {

        _subject = subject;
    }

    public String getHost() {

        return _host;
    }

    public void setHost(String host) {

        _host = host;
    }

    public String getPort() {

        return _port;
    }

    public void setPort(String port) {

        _port = port;
    }

    public String getFrom() {

        return _from;
    }

    public void setFrom(String from) {

        _from = from;
    }

    public String getTo() {

        return _to;
    }

    public void setTo(String to) {

        _to = to;
    }

    public String getBcc() {

        return _bcc;
    }

    public void setBcc(String bcc) {

        _bcc = bcc;
    }

    public String getCc() {

        return _cc;
    }

    public void setCc(String cc) {

        _cc = cc;
    }

    public String getBody() {

        return _body;
    }

    public void setBody(String body) {

        _body = body;
    }

    public String getSignFileName() {

        return _signFileName;
    }

    public void setSignFileName(String signFileName) {

        _signFileName = signFileName;
    }

    public String getFooter() {

        return _footer;
    }

    public void setFooter(String footer) {

        _footer = footer;
    }

    public List<String> getAttachmentList() {

        return _attachmentList;
    }

    public void addAttachment(String attachmentPath) {

        _attachmentList.add(attachmentPath);
    }

    public boolean isTimeToRetry() {

        if (getRetryAttempts() == 0) {
            return true;
        }

        if ((getRetryAttempts() % getMaxRetryAttemptsBeforePause()) == 0) {
            // in pause
            if (System.currentTimeMillis() - getLastAttemp() >= getMaxAttemptsWaitMillis()) {
                return true;
            }

        } else {
            // retrying cycle
            if (System.currentTimeMillis() - getLastAttemp() >= getMinAttemptsWaitMillis()) {
                return true;
            }

        }

        return false;
    }

    public boolean isPauseEnded() {

        if (getRetryAttempts() == 0) {
            return true;
        }

        if (System.currentTimeMillis() - getLastAttemp() >= getMinAttemptsWaitMillis()) {
            return true;
        }

        return false;
    }

    public void incRetryAttempts() {

        setRetryAttempts(getRetryAttempts() + 1);
    }

    public boolean canRetryOnceMore() {

        if (getRetryAttempts() >= getMaxRetryAttempts()) {
            return false;
        }

        return true;
    }

    public UUID getIdEmailLog() {

        return _idEmailLog;
    }

    public void setIdEmailLog(UUID idEmailLog) {

        _idEmailLog = idEmailLog;
    }

    public String getUsername() {

        return _username;
    }

    public void setUsername(String username) {

        this._username = username;
    }

    public String getAttachmentString() {

        List<String> list = _attachmentList;
        StringBuilder commaSeparatedList = new StringBuilder();

        for (String s : list) {
            if (Util.IsNotEmpty(commaSeparatedList)) {
                commaSeparatedList.append(",");
            }

            int p = s.lastIndexOf("/");
            commaSeparatedList.append(s.substring(p + 1));
        }

        String result = commaSeparatedList.toString();
        return result;
    }

    @Override
    public String toString() {

        StringBuilder attachmentList = new StringBuilder();
        for (String attachment : getAttachmentList()) {
            attachmentList.append(attachment);
            attachmentList.append(" - ");
        }

        String logMail = " _idEmailLog:" + getIdEmailLog() + " _retryAttempts:" + getRetryAttempts()
                + " _maxRetryAttemptsBeforePause:" + getMaxRetryAttemptsBeforePause() + " _maxRetryAttempts:"
                + getMaxRetryAttempts() + " - _lastAttemp:" + getLastAttemp() + " _from:" + getFrom() + " _to:"
                + getTo() + " _cc:" + getCc() + " _bcc:" + getBcc() + " _subject:" + getSubject() + " _body:"
                + getBody() + " _footer:" + getFooter() + " _server:" + getHost() + " _attachmentList:" + attachmentList
                + " _idUtente:" + _username;

        return logMail;

    }

}