@echo off

D:

cd WinSCP

WinSCP.exe /ini=nul /script="D:\bat_mail_checklist_alibow\FTP-UPS.txt"
