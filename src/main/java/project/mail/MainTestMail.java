
package project.mail;

public class MainTestMail {

    public static void main(String a[]) {

        inviaMail();

    }

    private static void inviaMail() {

        String from = "assistenza@chevroletiride.com";
        String to = "fabiano.moda@gmail.com;fabiano@orquestra.it";

        InvioMail invioMail = new InvioMail();

        invioMail.setFrom(from);
        invioMail.setServer("mail.project-online.it");
        invioMail.setSubject("prova mail");

        invioMail.setBody("prova mail a carlo senza auth");

        invioMail.setFooter("\n\n");

        boolean hasAttachment = false;

        invioMail.setSignFileName("");

        invioMail.setTo(to);

        try {
            String user = "fmoda@project-online.local";
            MyAuthenticator auth = null;
            if (!user.equals("")) {
                auth = new MyAuthenticator();
            }

            invioMail.invioMail(auth, hasAttachment);

            System.out.println("Ok");

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

}
