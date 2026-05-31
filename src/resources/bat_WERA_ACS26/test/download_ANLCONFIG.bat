d:
cd winSCP
winscp.com /ini=nul /command ^
    "open sftp://root:Project1;fingerprint=ssh-ed25519-dfhY9OYeY%2BVYq2oTgZPooPvONhR%2F14Qgg8GZIFP9tRw=@192.168.0.62/""" ^
    "get /usr/share/webapps/wera/ANLCONFIG/AnlConfig.xml C:\ProgramData\ACS26V3\" ^
    "exit"






