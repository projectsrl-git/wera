package net.projectsrl.wera.scheduled;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.wera.importazioni.core.FunctionFileUploadDatiRilevatori;
import net.projectsrl.wm.utils.Utils;

public class JobImportAutomatico implements Job {

    private static final int MAX_THREAD = 5;

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {

        File cartellaTemp = new File(Config.GetInstance().getProperty("cartella.scarico.ACS", "C:\\ACS26V3\\"));
        File[] filesTemp = cartellaTemp.listFiles();

        if (filesTemp == null || filesTemp.length == 0) {
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD);

        for (File inFile : filesTemp) {
            if (!inFile.getName().endsWith("_A") && !inFile.getName().contains(".lock") && !isLocked(inFile)) {
                executor.execute(new FileProcessor(inFile));
            }
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean isLocked(File file) {
        File lock = new File(file.getAbsolutePath() + ".lock");
        return lock.exists();
    }

    class FileProcessor implements Runnable {
        private File file;

        public FileProcessor(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            String nomeFile = file.getName();
            String nomeFileOriginale = nomeFile;
            String nomeFileCompleto = file.getAbsolutePath();
            String nomeFileCompletoReading = nomeFileCompleto + "_A";
            Integer azienda = 1;
            boolean xml = nomeFileCompleto.endsWith(".xml");

            File lockFile = new File(nomeFileCompleto + ".lock");
            try {
                if (!lockFile.createNewFile()) {
                    return;
                }
                FileWriter fw = new FileWriter(lockFile);
                fw.write("locked");
                fw.close();

                File renamed = new File(nomeFileCompletoReading);
                if (!file.renameTo(renamed)) {
                    return;
                }

                String dataImport = Utils.getStringDataOggiRibaltata();
                String oraImport = Utils.getOrario();
                SsbServletRequest req = null;

                if (xml) {
                    FunctionFileUploadDatiRilevatori.importazioneFileXML(azienda, nomeFile + "_A", renamed.getAbsolutePath(), req, dataImport, oraImport);
                    FunctionFileUploadDatiRilevatori.scriviInElencoFileImportati(azienda, nomeFile, dataImport, oraImport);
                } else {
                    FunctionFileUploadDatiRilevatori.importazioneFileCSV(req, azienda, renamed.getAbsolutePath(), dataImport, oraImport);
                    FunctionFileUploadDatiRilevatori.scriviInElencoFileImportati(azienda, nomeFileOriginale, dataImport, oraImport);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JAXBException e) {
                e.printStackTrace();
            } catch (AppCrash e) {
                e.printStackTrace();
            } finally {
                lockFile.delete();
            }
        }
    }
}