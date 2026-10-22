#!/usr/bin/perl
# mailcatcher.pl - SMTP "pozzo" minimale per l'istanza /wera-test.
# Accetta qualunque mail (AUTH PLAIN/LOGIN con credenziali qualsiasi, niente TLS) e la
# salva come file .eml; non inoltra NIENTE a nessuno.
#
# USO (Git Bash o PowerShell, lasciarlo aperto in un tab del Terminal panel):
#   perl deploy/mailcatcher.pl [--host 127.0.0.1] [--port 1025] [--dir D:/usr/share/logs/wera/mailcatcher]
# Il config del WAR di test punta gia a 127.0.0.1:1025 (deploy/make_test_war.bat).
use strict;
use warnings;
use IO::Socket::INET;
use Getopt::Long;
use File::Path qw(make_path);
use POSIX qw(strftime);

my $host = '127.0.0.1';
my $port = 1025;
my $dir  = 'D:/usr/share/logs/wera/mailcatcher';
GetOptions('host=s' => \$host, 'port=i' => \$port, 'dir=s' => \$dir) or die "opzioni non valide\n";

make_path($dir) unless -d $dir;
-d $dir or die "cartella $dir non creabile\n";
$| = 1;

my $srv = IO::Socket::INET->new(LocalAddr => $host, LocalPort => $port, Listen => 5, ReuseAddr => 1, Proto => 'tcp')
    or die "impossibile ascoltare su $host:$port: $!\n";
print "mailcatcher in ascolto su $host:$port, mail salvate in $dir\n";

my $seq = 0;
while (my $c = $srv->accept) {
    eval {
        local $SIG{ALRM} = sub { die "timeout\n" };
        alarm 60;
        session($c);
        alarm 0;
    };
    print "  sessione interrotta: $@" if $@;
    close $c;
}

sub reply { my ($c, $msg) = @_; print $c "$msg\r\n"; }

sub session {
    my ($c) = @_;
    my ($from, @rcpt) = ('');
    reply($c, '220 mailcatcher WERA test');
    while (defined(my $line = <$c>)) {
        $line =~ s/\r?\n\z//;
        my ($cmd, $arg) = $line =~ /^(\S+)\s*(.*)$/ ? (uc $1, $2) : ('', '');
        if ($cmd eq 'EHLO') {
            print $c "250-mailcatcher\r\n250-AUTH PLAIN LOGIN\r\n250-8BITMIME\r\n250 SIZE 52428800\r\n";
        } elsif ($cmd eq 'HELO') {
            reply($c, '250 mailcatcher');
        } elsif ($cmd eq 'AUTH') {
            my ($mech, $ini) = split ' ', $arg;
            $mech = uc($mech // '');
            if ($mech eq 'LOGIN') {
                unless (defined $ini) { reply($c, '334 VXNlcm5hbWU6'); <$c>; }
                reply($c, '334 UGFzc3dvcmQ6'); <$c>;
            } elsif ($mech eq 'PLAIN' && !defined $ini) {
                reply($c, '334'); <$c>;
            }
            reply($c, '235 2.7.0 autenticato (qualsiasi credenziale)');
        } elsif ($cmd eq 'MAIL') {
            ($from) = $arg =~ /<([^>]*)>/; $from //= $arg; @rcpt = ();
            reply($c, '250 OK');
        } elsif ($cmd eq 'RCPT') {
            my ($to) = $arg =~ /<([^>]*)>/; push @rcpt, $to // $arg;
            reply($c, '250 OK');
        } elsif ($cmd eq 'DATA') {
            reply($c, '354 fine con <CRLF>.<CRLF>');
            my $data = '';
            while (defined(my $l = <$c>)) {
                last if $l =~ /^\.\r?\n\z/;
                $l =~ s/^\.\././;
                $data .= $l;
            }
            my $file = save($from, \@rcpt, $data);
            reply($c, "250 OK salvata $file");
            @rcpt = ();
        } elsif ($cmd eq 'RSET') {
            ($from, @rcpt) = (''); reply($c, '250 OK');
        } elsif ($cmd eq 'NOOP') {
            reply($c, '250 OK');
        } elsif ($cmd eq 'QUIT') {
            reply($c, '221 ciao'); return;
        } else {
            reply($c, '502 comando non gestito');
        }
    }
}

sub save {
    my ($from, $rcpt, $data) = @_;
    my $name = strftime('%Y%m%d-%H%M%S', localtime) . sprintf('-%03d.eml', ++$seq % 1000);
    my ($subject) = $data =~ /^Subject:\s*(.*?)\r?$/mi;
    open my $fh, '>:raw', "$dir/$name" or die "scrittura $dir/$name: $!\n";
    print $fh "X-Mailcatcher-From: $from\r\n";
    print $fh "X-Mailcatcher-Rcpt: $_\r\n" for @$rcpt;
    print $fh $data;
    close $fh;
    printf "%s  da %s  a %s  oggetto: %s  -> %s\n", strftime('%H:%M:%S', localtime),
        $from, join(', ', @$rcpt), $subject // '(nessuno)', $name;
    return $name;
}
