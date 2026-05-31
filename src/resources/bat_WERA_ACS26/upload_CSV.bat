d:
cd winSCP
winscp.com /ini=nul /command ^
    "open sftp://root:Project1@192.168.0.61/ -hostkey=""ssh-ed25519 256 dfhY9OYeY+VYq2oTgZPooPvONhR/14Qgg8GZIFP9tRw=""" ^
    "put -delete \\192.168.0.149\Project\Wera\*.csv /usr/share/webapps/wera/ACS26V3/" ^
    "exit"


