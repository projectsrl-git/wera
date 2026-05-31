set objMessage = createobject("cdo.message") 
set objConfig = createobject("cdo.configuration") 
Set Flds = objConfig.Fields 

Flds.Item("http://schemas.microsoft.com/cdo/configuration/sendusing") = 2 
Flds.Item("http://schemas.microsoft.com/cdo/configuration/smtpserver") ="smtp.projectsrl.net" 

' ' Passing SMTP authentication 
Flds.Item ("http://schemas.microsoft.com/cdo/configuration/smtpauthenticate") = 1 'basic (clear-text) authentication 
Flds.Item ("http://schemas.microsoft.com/cdo/configuration/sendusername") ="luca.remiddi@projectsrl.net" 
Flds.Item ("http://schemas.microsoft.com/cdo/configuration/sendpassword") ="Project2012" 

Flds.update 
Set objMessage.Configuration = objConfig 
objMessage.From = "luca.remiddi@projectsrl.net" 

objMessage.To = "davide.rossi@airliquide.com;andrea.disnan@airliquide.com;paolo.varalta@airliquide.com" 
objMessage.Cc = "fabiano.moda@projectsrl.net"
objMessage.Bcc = "luca.remiddi@projectsrl.net"


objMessage.Subject = "Checklist agenzie" 
objMessage.TextBody = "Aggiornamento odierno"
objMessage.AddAttachment "D:\bat_mail_checklist_alibow\situazione_checklist_agenzie.xls"

objMessage.fields.update 
objMessage.HTMLBody = "Aggiornamento odierno" 
objMessage.Send 