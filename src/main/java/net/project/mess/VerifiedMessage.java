
package net.project.mess;

/**
 * Questa classe serve per contenere un MsgReader_itf ed il risultato della sua verifica eseguita dal verifier. Viene
 * utilizzata per inserire i MsgReader_itf e le relative diagnosi in Logger.Info
 * 
 * @author Simone
 */
public class VerifiedMessage {

    private MsgReader_itf _msg;
    private String        _diagnosi;

    /**
     * Creates a new VerifiedMessage object.
     *
     * @param msgReader il messaggio verificato
     * @param diagnosi il risultato della verifica
     */
    public VerifiedMessage(MsgReader_itf msgReader, String diagnosi) {

        _msg = msgReader;
        _diagnosi = diagnosi;
    }

    /**
     * Returns the diagnosi.
     * 
     * @return String
     */
    public String getDiagnosi() {

        return _diagnosi;
    }

    /**
     * Returns the msg.
     * 
     * @return MsgReader_itf
     */
    public MsgReader_itf getMsg() {

        return _msg;
    }

    /**
     * Sets the diagnosi.
     * 
     * @param diagnosi The diagnosi to set
     */
    public void setDiagnosi(String diagnosi) {

        _diagnosi = diagnosi;
    }

    /**
     * Sets the msg.
     * 
     * @param msg The msg to set
     */
    public void setMsg(MsgReader_itf msg) {

        _msg = msg;
    }
}