
package net.projectsrl.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.mail.smtp.SMTPAddressFailedException;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Base64;

public class DeferredMailSender {

    private static final String                    DATASET_MAIL_NON_INVIATE = "DSMailNonInviate";
    private static DeferredMailSender              _instance                = null;
    private static BlockingQueue<SerializableMail> _queue                   = null;
    private static ThreadConsumer                  _thread                  = null;

    private class ThreadConsumer extends Thread {

        private Consumer _target;

        public ThreadConsumer(Consumer target) {

            super(target);
            _target = target;
        }

        public void exit() {

            _target.exit();
        }

    }

    private class Consumer implements Runnable {

        private boolean _isExit = false;

        public void run() {

            while (!_isExit) {
                SerializableMail serializableMail = null;

                try {
                    serializableMail = _queue.take();

                    if (serializableMail == null) {
                        continue;
                    }

                    if (!serializableMail.canRetryOnceMore()) {
                        Logger.GetInstance().log0(
                                "SerializableMail object null or retry attemps exhausted - item:" + serializableMail);
                        continue;
                    }

                    if (!serializableMail.isPauseEnded()) {
                        _queue.offer(serializableMail);
                        continue;
                    }

                    if (!serializableMail.isTimeToRetry()) {
                        _queue.offer(serializableMail);
                        continue;
                    }

                    SendSMTPMail email = new SendSMTPMail(serializableMail);
                    serializableMail.incRetryAttempts();
                    email.sendMail();

                    if (serializableMail.getRetryAttempts() == 1) {
                        insertEmailLog(serializableMail, true, false);
                    } else {
                        updateEmailLog(serializableMail, true, false);
                    }

                } catch (Throwable e) {
                    AppCrash ac = new AppCrash(e);
                    ac.logContext(this.getClass().getName(),
                            "Error occurred but doesn't stop process - serializableMail:" + serializableMail);

                    boolean blockSend = false;
                    if (retryCatchAndStopSend(serializableMail, e)) {
                        blockSend = true;
                    }

                    if (serializableMail.getRetryAttempts() == 1) {
                        insertEmailLog(serializableMail, false, blockSend);
                    } else {
                        updateEmailLog(serializableMail, false, blockSend);
                    }

                    if (!blockSend) {
                        _queue.offer(serializableMail);
                    }

                }
            }
        }

        public void exit() {

            _isExit = true;
        }
    }

    private DeferredMailSender() {

        _queue = new LinkedBlockingQueue<SerializableMail>();
        Consumer consumer = new Consumer();
        _thread = new ThreadConsumer(consumer);
        _thread.setDaemon(true);
        _thread.setName(this.getClass().getName());

        restoreNotSentEmails();

    }

    private void restoreNotSentEmails() {

        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_MAIL_NON_INVIATE);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                String stringToDeserialize = (String) dbRow.getField(EmailLogDAO.EMAIL_DATA);
                Integer attempts = (Integer) dbRow.getField(EmailLogDAO.ATTEMPTS);
                SerializableMail notSent = deserializeObject(stringToDeserialize);
                notSent.setRetryAttempts(attempts);
                _queue.put(notSent);

            }
            dataSet.close();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Error loading not sent emails");
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
    }

    public static DeferredMailSender getInstance() {

        if (_instance == null) {
            _instance = new DeferredMailSender();
        }

        return _instance;

    }

    public void start() {

        if (_thread != null && !_thread.isAlive()) {
            _thread.start();
        }
    }

    public void exit() {

        if (_thread != null && _thread.isAlive()) {
            _thread.exit();
        }
    }

    public BlockingQueue<SerializableMail> getQueue() {

        return _queue;
    }

    public boolean offer(SendSMTPMail email) {

        SerializableMail serializableMail = email.getMailMessage();

        return _queue.offer(serializableMail);
    }

    public boolean offer(SerializableMail serializableMail) {

        return _queue.offer(serializableMail);
    }

    public void insertEmailLog(SerializableMail serializableMail, boolean sent, boolean stopSend) {

        try {
            ErrDetector.GetInstance().param(serializableMail != null);

            EmailLogDAO emailLogDAO = new EmailLogDAO();
            emailLogDAO.setAttribute(EmailLogDAO.ID_EMAIL_LOG, serializableMail.getIdEmailLog());
            emailLogDAO.setAttribute(EmailLogDAO.USERNAME, serializableMail.getUsername());
            emailLogDAO.setAttribute(EmailLogDAO.SENDER, serializableMail.getFrom());
            emailLogDAO.setAttribute(EmailLogDAO.RECIPIENTS_TO, serializableMail.getTo());
            emailLogDAO.setAttribute(EmailLogDAO.RECIPIENTS_CC, serializableMail.getCc());
            emailLogDAO.setAttribute(EmailLogDAO.RECIPIENTS_BCC, serializableMail.getBcc());
            emailLogDAO.setAttribute(EmailLogDAO.SUBJECT, serializableMail.getSubject());
            emailLogDAO.setAttribute(EmailLogDAO.BODY, serializableMail.getBody());
            emailLogDAO.setAttribute(EmailLogDAO.ATTACHMENT_LIST, serializableMail.getAttachmentString());

            emailLogDAO.setAttribute(EmailLogDAO.ATTEMPTS, serializableMail.getRetryAttempts());
            Timestamp tsLastAttemp = new Timestamp(serializableMail.getLastAttemp());
            emailLogDAO.setAttribute(EmailLogDAO.TS_SEND, tsLastAttemp);
            emailLogDAO.setAttribute(EmailLogDAO.FL_SENT, sent);
            emailLogDAO.setAttribute(EmailLogDAO.FL_STOP_SEND, stopSend);

            String serialized = serializeObject(serializableMail);
            emailLogDAO.setAttribute(EmailLogDAO.EMAIL_DATA, serialized);
            emailLogDAO.insert();

        } catch (AppCrash e) {
            e.logContext(this.getClass().getName(), "error insert SerializableMail - " + serializableMail);
        }
    }

    private void updateEmailLog(SerializableMail serializableMail, boolean sent, boolean stopSend) {

        try {
            EmailLogDAO emailLogDAO = new EmailLogDAO();
            emailLogDAO.setAttribute(EmailLogDAO.ID_EMAIL_LOG, serializableMail.getIdEmailLog());
            emailLogDAO.setAttribute(EmailLogDAO.ATTEMPTS, serializableMail.getRetryAttempts());
            Timestamp tsLastAttemp = new Timestamp(serializableMail.getLastAttemp());
            emailLogDAO.setAttribute(EmailLogDAO.TS_SEND, tsLastAttemp);
            emailLogDAO.setAttribute(EmailLogDAO.FL_SENT, sent);
            emailLogDAO.setAttribute(EmailLogDAO.FL_STOP_SEND, stopSend);
            emailLogDAO.update();
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(), "Error updating EMAIL_LOG - serializableMail:" + serializableMail);
        }

    }

    /** Write the object to a Base64 string. */
    private String serializeObject(Serializable objectToSerialize) throws AppCrash {

        String serialized = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(objectToSerialize);
            oos.close();
            serialized = Base64.encode(baos.toByteArray());
        } catch (Throwable th) {
            throw new AppCrash(th);
        }

        return serialized;
    }

    /** Read the object from Base64 string. */
    private SerializableMail deserializeObject(String stringToDeserialize) throws AppCrash {

        SerializableMail deserialized = null;

        try {
            byte[] data = Base64.decode(stringToDeserialize);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            deserialized = (SerializableMail) ois.readObject();
            ois.close();
        } catch (Throwable th) {
            throw new AppCrash(th);
        }

        return deserialized;

    }

    private boolean retryCatchAndStopSend(SerializableMail serializableMail, Throwable exception) {

        try {
            if (exception != null) {
                throw exception;
            }

            return false;
        } catch (SMTPAddressFailedException e) {
            // AppCrash ac = new AppCrash(e);
            // ac.logContext(this.getClass().getName(), "Error sending email - serializableMail:" + serializableMail);
            return true;
        } catch (AppCrash ac) {
            Throwable originalException = ac.getOriginalException();
            return retryCatchAndStopSend(serializableMail, originalException);
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            return retryCatchAndStopSend(serializableMail, cause);
        }
    }

}