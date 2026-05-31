
package net.projectsrl.webapp.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.webapp.security.SessionTokenDAO;
import project.misc.Utils;

public class WebAppUtils {

    private static final String WHERECONDITION_TEMPLATE_KEY               = "WHERECONDITION";
    private static final String SQL_LIKE_OPERATOR_CASE_INSENSITIVE        = "LIKE";
    private static final String POSTGRESQL_DRIVER_KEY                     = "postgres";
    private static final String POSTGRESQL_LIKE_OPERATOR_CASE_INSENSITIVE = "ILIKE";

    /**
     * Costruttore privato in quanto la classe contiene solo metodi static e non deve percio' essere istanziata.
     */
    private WebAppUtils() {

        super();
    }

    public static final String WHERECONDITION_FIELD_PREFIX = "WC_";

    public static void composeWhereCondition(SsbServletRequest req, Map<String, Object> templateData) {

        String whereCondition = "";
        // legge tutti i parametri della request

        Enumeration<String> param = req.getParameterNames();

        while (param.hasMoreElements()) {
            String name = param.nextElement();
            String value = req.getField(name);

            if (name.startsWith(WHERECONDITION_FIELD_PREFIX)) {
                String fieldName = name.replace(WHERECONDITION_FIELD_PREFIX, "");

                if (!value.trim().equals("")) {
                    if (!whereCondition.equals("")) {
                        whereCondition += " AND ";
                    }

                    whereCondition += fieldName + " " + getLikeCaseInsensitiveOperator() + " '%" + value + "%' ";
                }

            }

        }

        if (!whereCondition.equals("")) {
            whereCondition = " WHERE " + whereCondition;
        }

        templateData.put(WHERECONDITION_TEMPLATE_KEY, whereCondition);
    }

    private static String getLikeCaseInsensitiveOperator() {

        if (Config.GetInstance().getProperty("DB.JDBCDriver", "").contains(POSTGRESQL_DRIVER_KEY)) {
            return POSTGRESQL_LIKE_OPERATOR_CASE_INSENSITIVE;
        }

        return SQL_LIKE_OPERATOR_CASE_INSENSITIVE;
    }

    public static void composeWhereConditionJSON(SsbServletRequest req, Map<String, Object> templateData,
            String fieldName, String value, String whereCond) {

        String whereCondition = "";

        if (!fieldName.equals("")) {
            whereCondition += " CAST(" + fieldName + " AS TEXT) " + getLikeCaseInsensitiveOperator() + " '%" + value
                    + "%' ";
        }

        if (!whereCond.equals("")) {
            whereCondition += whereCond;
        }

        if (!whereCondition.equals("")) {
            if (whereCondition.startsWith(" AND ")) {
                whereCondition = " " + whereCondition.substring(4);
            }
            whereCondition = " WHERE " + whereCondition;
        }

        templateData.put(WHERECONDITION_TEMPLATE_KEY, whereCondition);
    }

    public static void normalizeWhereCondition(Map<String, Object> templateData, String whereCond) {

        String whereCondition = "";

        if (Util.IsNotEmpty(whereCond)) {
            whereCondition += whereCond;
            
            if (whereCondition.startsWith(" AND ")) {
                whereCondition = " " + whereCondition.substring(4);
            }
            
            whereCondition = " WHERE " + whereCondition;
        }

        templateData.put(WHERECONDITION_TEMPLATE_KEY, whereCondition);
    }

    public static boolean exceptionIsInstanceOf(Throwable error, Class<?> classType) {

        while (error != null) {

            if (error.getClass() == classType) {
                return true;
            }

            if (error instanceof AppCrash) {
                error = ((AppCrash) error).getOriginalException();
                continue;
            }

            error = error.getCause();

        }

        return false;
    }

    public static String encryptSHA1(String plainText) throws AppCrash {

        String encrypted = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");

            byte[] passBytes = plainText.getBytes();
            md.reset();
            byte[] digested = md.digest(passBytes);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digested.length; i++) {
                sb.append(Integer.toHexString(0xff & digested[i]));
            }
            encrypted = sb.toString();
        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(WebAppUtils.class.getName(), "error encrypting plain text " + plainText);
        }

        return encrypted;

    }

    public static String getRemoteAddress(SsbServletRequest req) {

        String ipAddress = req.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = req.getRemoteAddr();
        }

        return ipAddress;
    }

    public static String createSessionToken(SsbServletRequest req) {

        String tokenString = null;
        try {
            Map<String, String> dataMap = new HashMap<String, String>();
            Enumeration<String> param = req.getParameterNames();

            while (param.hasMoreElements()) {
                String name = param.nextElement();
                String value = req.getField(name);
                dataMap.put(name, value);
            }

            // Convert Map to byte array
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(dataMap);

            SessionTokenDAO token = new SessionTokenDAO();
            String ipAddr = WebAppUtils.getRemoteAddress(req);
            UUID tokenID = UUID.randomUUID();
            tokenString = tokenID.toString();
            token.setAttribute(SessionTokenDAO.ID_TOKEN, tokenID);
            token.setAttribute(SessionTokenDAO.IPADDRESS, ipAddr);
            token.setAttribute(SessionTokenDAO.DATAMAP, byteOut.toByteArray());
            token.insert();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(WebAppUtils.class.getName(), "errore nella creazione del token di sessione ");
        }
        return tokenString;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getSessionTokenMap(String tokenString) {

        Map<String, String> dataMap = null;

        try {

            SessionTokenDAO token = new SessionTokenDAO();
            token.setAttribute(SessionTokenDAO.ID_TOKEN, tokenString);
            ErrDetector.GetInstance().preCond(token.retrieve(), "token non trovato");

            byte[] storedDataMap = (byte[]) token.getAttribute(SessionTokenDAO.DATAMAP);

            ErrDetector.GetInstance().preCond(storedDataMap != null, "storedDataMap is null");

            ByteArrayInputStream byteIn = new ByteArrayInputStream(storedDataMap);
            ObjectInputStream in = new ObjectInputStream(byteIn);
            dataMap = (Map<String, String>) in.readObject();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(WebAppUtils.class.getName(), "errore nella creazione del token di sessione ");
        }
        return dataMap;
    }

    public static void executeQuery(String sqlQuery) throws AppCrash {

        DBTransaction dbtransaction = null;

        try {
            dbtransaction = new DBTransaction();
            executeQuery(dbtransaction, sqlQuery);
            dbtransaction.commit();

        } catch (Throwable th) {

            if (dbtransaction != null) {
                try {
                    dbtransaction.rollBack();
                } catch (Throwable th1) {
                    AppCrash ac = new AppCrash(th1);
                    ac.logContext(WebAppUtils.class.getName(),
                            "error in dbtransaction.rollBack() - sqlQuery:" + sqlQuery);
                    throw ac;
                }
            }

            AppCrash ac = new AppCrash(th);
            ac.logContext(WebAppUtils.class.getName(), "error in executeQuery() - sqlQuery:" + sqlQuery);
            throw ac;
        }

    }

    public static void executeQuery(DBTransaction dbtransaction, String sqlQuery) throws AppCrash {

        ErrDetector.GetInstance().param(Utils.IsNotEmpty(sqlQuery), "sqlQuery empty");

        PreparedStatement ps = null;

        try {

            ps = dbtransaction.prepareStatement(sqlQuery);
            ps.execute();

            Logger.GetInstance().log0("Executed query: " + sqlQuery);

        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(WebAppUtils.class.getName(), "error executing  PreparedStatement ps - sqlQuery:" + sqlQuery);
            throw ac;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Throwable th1) {
                    AppCrash ac = new AppCrash(th1);
                    ac.logContext(WebAppUtils.class.getName(),
                            "error closing PreparedStatement - sqlQuery:" + sqlQuery);
                    throw ac;
                }
            }
        }
    }

}
