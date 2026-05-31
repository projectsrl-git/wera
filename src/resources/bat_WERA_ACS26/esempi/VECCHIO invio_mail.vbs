Set objMessage = CreateObject("CDO.Message")
objMessage.Subject = "Checklist agenzie"
' gli indirizzi possono essere inseriti semplici oppure con "nome mittente"
' versione "normale": destinatario@dominio.xxx
' versione "avanzata": ""Nome Cognome"" < destinatatio@dominio.xxx>
' da notare la ripetizione dei doppi apici (")
objMessage.From = "luca.remiddi@projectsrl.net"
objMessage.To = "davide.rossi@airliquide.com;andrea.disnan@airliquide.com;paolo.varalta@airliquide.com"
 objMessage.Cc = "fabiano.moda@projectsrl.net"
' objMessage.Bcc = "luca.remiddi@projectsrl.net"
objMessage.AddAttachment "D:\bat_mail_checklist_alibow\situazione_checklist_agenzie.xls"

objMessage.TextBody = "Aggiornamento odierno"



objMessage.Configuration.Fields.Item ("http://schemas.microsoft.com/cdo/configuration/sendusing") = 2
' server posta in uscita (SMTP)
objMessage.Configuration.Fields.Item ("http://schemas.microsoft.com/cdo/configuration/smtpserver") = "192.168.0.2"
objMessage.Configuration.Fields.Item ("http://schemas.microsoft.com/cdo/configuration/smtpserverport") = 25
objMessage.Configuration.Fields.Update
objMessage.Send
' msgbox "il messaggio e stato inviato!"
