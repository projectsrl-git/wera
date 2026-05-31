
package net.project.mess;

import net.project.errors.AppCrash;

public class EPSMsgFactory extends MsgFactory_base {

    public EPSMsgFactory() {

        super();
    }

    @Override
    public MsgWriter_itf MakeMsgWriter(String type) throws AppCrash {

        MsgWriter_itf result;

        // Se il tipo di messaggio inizia con:
        // 0 significa che e' un messaggio ISO8583: 0100, 0420, etc.
        // f significa che è un messaggio con campi di dimensione fissa
        if (type.startsWith("0")) {
            result = createWriterInstance("net.project.mess.iso8583.ISO8583ReadWrite", type);
        } else if (type.startsWith("f")) {
            result = createWriterInstance("net.project.mess.fixlen.FixReadWrite", type); // type = fNomeMessaggio (4
                                                                                         // char)
        } else if (type.startsWith("l")) {
            result = createWriterInstance("net.project.mess.label.LabelReadWrite", type);
        } else if (type.startsWith("x")) {
            result = createWriterInstance("net.project.mess.xml.XmlReadWrite", type);
        } else {
            result = createWriterInstance("net.project.mess.atdat.ATDATURLReadWrite", type);
        }
        return result;
    }

    @Override
    public MsgWriter_itf MakeMsgWriter(byte[] message) throws AppCrash {

        AppCrash e = new AppCrash();
        e.logContext("EPSMsgFactory", "Metodo non implementato");
        throw e;

    }

    /**
     * @roseuid 3763738300F4
     */
    @Override
    public MsgReader_itf MakeMsgReader(byte[] message) throws AppCrash {

        byte elleASCII = 108; // rappresentazione ASCII del carattere "l"
        MsgReader_itf result = null;
        byte zero = 48; // Rappresentazione ASCII del carattere "0"
        byte effe = 102; // Rappresentazione ASCII del carattere "f"
        // Se il primo byte del messaggio e' 0 significa che e' un messaggio
        // ISO8583: 0100, 0420, etc.
        if (message[0] == zero) {
            result = createReaderInstance("net.project.mess.iso8583.ISO8583Read", message);
        } else if (message[0] == effe) {
            result = createReaderInstance("net.project.mess.fixlen.FixRead", message);
        } else if (message[0] == elleASCII) {
            result = createReaderInstance("net.project.mess.label.LabelRead", message);
        } else if (isXmlMessage(message)) {
            result = createReaderInstance("net.project.mess.xml.XmlRead", message);
        } else {
            result = createReaderInstance("net.project.mess.atdat.ATDATURLRead", message);
        }
        return result;

    }

    private boolean isXmlMessage(byte[] message) {

        int position = 0;

        // supera tutti gli eventuali blank
        // in testa al byte[] message
        while (message[position] == 32) {
            position++;
            if (position > message.length) {
                return false;
            }
        }

        return (message[position] == 60); // rappresentazione ASCII del carattere '<'

    }

}
