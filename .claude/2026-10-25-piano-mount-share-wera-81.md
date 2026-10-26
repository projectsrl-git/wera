# 2026-10-25 — Share documenti di wera da 192.168.0.81 su .106 (APPLICATO, opzione B)

> **Esito (2026-10-25 11:21-11:23, approvato dall'operatore con opzione B).** Motivazione
> dell'operatore: gestit su .106 e un ambiente di TEST, wera2026 e la produzione pilota, quindi
> meglio che Tomcat non parta piuttosto che wera2026 scriva sulla share sbagliata.
> - Passo 0: `vers=3.0` funziona; contenuto identico a quello visto da /wera su .70.
> - Passi 1-4: backup `*.bak-20261025-112129` (fstab, hosts; override in `/root/`), hosts
>   `192.168.0.81 fileserver_wera_docs`, riga fstab con `vers=3.0`, mount attivo, etckeeper `b630b24`.
> - Passo 5: nella sandbox di Tomcat il mount di .81 e in cima ed e scrivibile come `tomcat`
>   (file di prova creato e cancellato): **nessun riavvio di tomcat9**.
> - Passo 6 (B): `RequiresMountsFor=... /usr/share/webapps/wera`, daemon-reload, etckeeper
>   `f6a2800`; tomcat9 ora ha `Requires=usr-share-webapps-wera.mount` (effetto al prossimo avvio).
> - Passo 7: gestit 200, wera2026 200 (locale e https://weraweb.it), nessun nuovo crash ne SEVERE.
>   Da fare con login: file manager e archivio file importati.

Obiettivo: /wera2026 su .106 deve leggere e scrivere le STESSE cartelle di /wera su .70
(`directory.external_files=/usr/share/webapps/wera/`, `cartella.scarico.ACS`, `xmlconfig.ACS`),
che stanno sulla share `//192.168.0.81/storage/webapps/wera`. Oggi .106 vede la cartella `wera/`
della share di .186 (43 file vecchi di prova, ultimo 2025-06).

## Stato rilevato (sola lettura, 2026-10-25)
| | .70 (/wera) | .106 (oggi) |
|---|---|---|
| mount | `//fileserver_docs/storage/webapps` -> `/usr/share/webapps` | idem, ma `fileserver_docs` = .186 |
| `fileserver_docs` | 192.168.0.81 | 192.168.0.186 (lo usa anche gestit: NON va cambiato) |
| SMB | `vers=1.0` (default del kernel 3.16), password in chiaro in fstab | `vers=3.0`, `credentials=/etc/cifs-credentials/fileserver` |
| credenziali | utente `root`, dominio `FILESERVER` | utente `root`: **stessa password** (hash identico) |
| contenuto `wera/` | `ACS26V3`, `ANLCONFIG`, `import`, `upload`, `doc`, 262 MB | `import`, `upload` |

Da .106: .81 raggiungibile su 445 e 139; `mount.cifs` presente; kernel 5.10.
Override attuale di tomcat9: `RequiresMountsFor=/usr/share/logs /usr/share/webapps`,
`ReadWritePaths=-/usr/share/logs/wera2026 -/usr/share/webapps/wera` (il secondo copre gia il nuovo mount).

## Soluzione proposta
Un mount CIFS **annidato** solo per la sottocartella: `//192.168.0.81/storage/webapps/wera` ->
`/usr/share/webapps/wera`, sopra la share di .186. gestit (`/usr/share/webapps/gestit` su .186) e
il nome `fileserver_docs` restano come sono.

### Passo 0 — prova senza impatto (mount temporaneo in sola lettura)
```sh
mkdir -p /mnt/prova81
for v in 3.0 2.1 2.0 1.0; do
  mount -t cifs //192.168.0.81/storage/webapps/wera /mnt/prova81 \
    -o credentials=/etc/cifs-credentials/fileserver,ro,vers=$v && { echo "OK vers=$v"; break; }
done
ls /mnt/prova81            # attesi ACS26V3 ANLCONFIG import upload doc
umount /mnt/prova81; rmdir /mnt/prova81
```
Si tiene la versione piu alta che funziona. Se va solo `1.0` (SMB1, come oggi su .70): il kernel
5.10 la supporta, ma e un protocollo insicuro; lo segnalo e decidi tu.

### Passo 1 — backup
```sh
TS=$(date +%Y%m%d-%H%M%S); cp -p /etc/fstab /etc/fstab.bak-$TS; cp -p /etc/hosts /etc/hosts.bak-$TS
cp -p /etc/systemd/system/tomcat9.service.d/override.conf /root/override.conf.bak-$TS
```

### Passo 2 — nome host dedicato (non tocca `fileserver_docs`)
```
192.168.0.81 fileserver_wera_docs
```

### Passo 3 — riga in `/etc/fstab` (dopo quella di `/usr/share/webapps`)
```
# WERA wera2026 (2026-10-25): cartelle di /wera dalla share di .81 (come /wera su .70)
//fileserver_wera_docs/storage/webapps/wera  /usr/share/webapps/wera  cifs  credentials=/etc/cifs-credentials/fileserver,uid=tomcat,gid=tomcat,file_mode=0664,dir_mode=0775,vers=<V>,iocharset=utf8,_netdev,nofail,x-systemd.mount-timeout=30  0  0
```
systemd ordina da solo il mount annidato dopo `usr-share-webapps.mount`.

### Passo 4 — attivazione (nessun riavvio)
```sh
systemctl daemon-reload
mount /usr/share/webapps/wera
findmnt /usr/share/webapps/wera; ls /usr/share/webapps/wera
cd /etc && etckeeper commit -m "wera2026: cartelle di wera dalla share di .81"
```

### Passo 5 — Tomcat vede il nuovo mount ed e scrivibile?
Tomcat gira in una sandbox (`ProtectSystem=strict`) creata all'avvio. I mount nuovi del sistema
dovrebbero propagarsi dentro; si verifica senza riavviare, con un file di prova creato e
cancellato subito:
```sh
PID=$(systemctl show -p MainPID --value tomcat9)
nsenter -t $PID -m -- findmnt /usr/share/webapps/wera
nsenter -t $PID -m -- runuser -u tomcat -- sh -c 'f=/usr/share/webapps/wera/upload/.prova-wera2026; touch $f && rm $f && echo SCRIVIBILE'
```
Se il mount non si vede o non e scrivibile: **riavvio di tomcat9**, che ferma anche gestit per
circa 30 s. Lo faccio solo dopo il tuo ok e nell'orario che scegli tu.

### Passo 6 — dipendenza di Tomcat dal mount: DECIDI TU
- **A (consigliata per non toccare gestit) — nessuna dipendenza nuova.** Se .81 non risponde
  all'avvio del server, Tomcat parte lo stesso e gestit funziona. Rischio: in quel caso
  /wera2026 scrive nella `wera/` di .186 senza accorgersene, finche qualcuno non rimonta.
  Contromisura: dopo ogni riavvio di .106 controllare `findmnt /usr/share/webapps/wera`
  (lo aggiungo alla procedura in CLAUDE.md).
- **B — `RequiresMountsFor=/usr/share/webapps/wera` nell'override.** Senza .81 Tomcat non parte:
  dati sempre al posto giusto, ma **gestit dipende anche da .81**. Si attiva al prossimo avvio di
  tomcat9 (non serve riavviarlo ora).

### Passo 7 — verifiche
- File manager e archivio file importati di /wera2026: si vedono i file di /wera (anteprime).
- Nessun nuovo crash in `/usr/share/logs/wera2026/crash`; gestit 200.

## Impatto su gestit
Nessuno con l'opzione A e senza riavvio: il suo mount e la sua cartella su .186 non cambiano.
Con B dipende anche da .81; se al passo 5 serve un riavvio, gestit si ferma per circa 30 s.

## Effetti su /wera da sapere prima
Dopo il mount le cartelle sono **davvero** condivise con la produzione:
- "Scarico dati manuale" da /wera2026 riscrive `ANLCONFIG/AnlConfig.xml`, lo stesso file che usa
  l'import automatico di /wera su .70;
- i file caricati da /wera2026 (file manager, upload) finiscono nelle cartelle di /wera;
- `ACS26V3/` e la cartella da cui il job di /wera importa: /wera2026 non la tocca (Quartz spento),
  ma non va usata a mano.

## Rollback
ATTENZIONE all'ordine: con l'opzione B tomcat9 ha `Requires=` sul mount, quindi fermare il mount
PRIMA di togliere la dipendenza fermerebbe anche Tomcat (wera2026 e gestit).
```sh
TS=20261025-112129
cp -p /root/override.conf.bak-$TS /etc/systemd/system/tomcat9.service.d/override.conf
cp -p /etc/fstab.bak-$TS /etc/fstab; cp -p /etc/hosts.bak-$TS /etc/hosts
systemctl daemon-reload            # prima tolgo la dipendenza...
umount /usr/share/webapps/wera     # ...poi smonto
cd /etc && etckeeper commit -m "rollback mount wera da .81"
```
Dopo il rollback conviene riavviare tomcat9 (fuori orario per gestit) per ricreare la sandbox:
nel suo namespace il mount di .81 compariva in piu copie (propagazione attraverso il bind di
`ReadWritePaths`) e non e garantito che lo smontaggio le tolga tutte.
I file scritti nel frattempo restano su .81, cioe dove /wera li vede. Dopo l'umount riappare la
vecchia `wera/` di .186, intatta.
