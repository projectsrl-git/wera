
package net.projectsrl.wera.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.projectsrl.wm.utils.Utils;

public class WeraUtilsCondominiDaScaricare {

	
	
	
	public static void condomini(String idCondominio, Boolean scaricoManuale) throws AppCrash, IOException{
        DataSet_itf dataSet = null;
        String sim="";
        String condominio="";
      	String citta="";
      	String via="";
      	String cap="";
      	String azienda = "";
      	String scarico = "";
      	String dataScarico = "";
      	Boolean gen = false;
      	Boolean feb = false;
      	Boolean mar = false;
      	Boolean apr = false;
      	Boolean mag = false;
      	Boolean giu = false;
      	Boolean lug = false;
        Boolean ago = false;
      	Boolean set = false;
      	Boolean ott = false;
      	Boolean nov = false;
      	Boolean dic = false;
      	int oreScarico=0;
      	String dataToday=Utils.getStringDataOggiRibaltata();
      	
      	
      	String path = Config.GetInstance().getProperty("xmlconfig.ACS", "C:\\ProgramData\\ACS26V3\\AnlConfig.xml");
      	File file = new File(path);
      	FileWriter fw = new FileWriter(file);
      	String cartella= Config.GetInstance().getProperty("cartella.scarico.ACS.webapp", "C:\\ACS26V3\\");
  		String cartellaCondominio=cartella;
  		String pianificazione="4"; //giornaliera
  		int progressivo=0;
  		String dataDomani= WeraUtils.getStringDataDomaniScaricoACS();
  		String dataOggiACS = WeraUtils.getStringDataOggiScaricoACS();
  		new File(cartella).mkdir();
  		new File(cartellaCondominio).mkdir();
  		String riga1="<?xml version=\"1.0\" standalone=\"yes\"?>\n";
  		String riga2="<AnlDat xmlns=\"http://tempuri.org/AnlDat.xsd\">\n";
  		String riga23="</AnlDat>\n";
  		fw.write(riga1);
  		fw.write(riga2);

  		
        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "DataSetCondominiXML");
            HashMap<String, String> param = new HashMap<String, String>();
            if (idCondominio.isEmpty()){
            	param.put("WHERECONDITION", " ");
            }else{
	            param.put("WHERECONDITION", " AND ID_MODULO= "+idCondominio);
            }
            dataSet.setParam(param);
            dataSet.open();
            

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                condominio = dbRow.getField("DENOMINAZIONE").toString().trim().toUpperCase();
                sim = dbRow.getField("SIM").toString().trim().toUpperCase().replace("\\", "").replace("/", "").replace("-", "");
                citta = dbRow.getField("LOCALITA").toString().trim().toUpperCase();
                via = dbRow.getField("INDIRIZZO").toString().trim().toUpperCase();
                cap = dbRow.getField("CAP").toString().trim().toUpperCase();
                azienda = dbRow.getField("ID_AZIENDA").toString().trim().toUpperCase();
                scarico = dbRow.getField("SCARICO").toString().trim().toUpperCase();
                oreScarico = Integer.parseInt(scarico)*24;
                dataScarico = dbRow.getField("DATA_PRIMO_SCARICO").toString().trim().toUpperCase();
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ITALY);
                Date dataPrimoScarico = sdf.parse(Utils.ribaltaData(dataScarico));
                
                SimpleDateFormat dto = new SimpleDateFormat("yyyy/MM/dd", Locale.ITALY);
                Date dataOggi = dto.parse(dataToday); 
                
                
                gen = (Boolean) dbRow.getField("GEN");
                feb = (Boolean) dbRow.getField("FEB");
                mar = (Boolean) dbRow.getField("MAR");
                apr = (Boolean) dbRow.getField("APR");
                mag = (Boolean) dbRow.getField("MAG");
                giu = (Boolean) dbRow.getField("GIU");
                lug = (Boolean) dbRow.getField("LUG");
                ago = (Boolean) dbRow.getField("AGO");
                set = (Boolean) dbRow.getField("SETT");
                ott = (Boolean) dbRow.getField("OTT");
                nov = (Boolean) dbRow.getField("NOV");
                dic = (Boolean) dbRow.getField("DIC");
                
                progressivo=progressivo+1;
                
                
                // lo scarico deve essere programmato come giornaliero alle 0.10 della mattina
                if (!sim.equals("") && !scarico.equals("") && !dataPrimoScarico.equals("")){

                	if (scaricoManuale){
                		WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataOggiACS,fw);
                	}else{
                		while (dataPrimoScarico.before(dataOggi)){
                            dataPrimoScarico.setHours(dataPrimoScarico.getHours()+oreScarico);
                        }
                        
                        if (dataPrimoScarico.equals(dataOggi)){
                            int meseScarico=dataPrimoScarico.getMonth()+1;
                            
                            switch (meseScarico) {
                                case 1:  if(!gen){
                                			WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                case 2:  if(!feb){
                                            WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                case 3:  if(!mar){
                                            WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                case 4:  if(!apr){
                                            WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                case 5:  if(!mag){
                                            WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                case 6:  if(!giu){
                                            WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                case 7:  if(!lug){
                                            WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                case 8:  if(!ago){
                                            WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                case 9:  if(!set){
                                            WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                case 10: if(!ott){
                                            WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                case 11: if(!nov){
                                            WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                case 12: if(!dic){
                                            WeraUtilsFileScaricoAnlConfig.creaFileScarico(condominio,sim,citta,via,cap,azienda, progressivo, cartellaCondominio, pianificazione, dataDomani,fw);
                                         }
                                         break;
                                default: break;
                            }

                        }
                	}
                    
                }
                
                
            }

            dataSet.close();
            fw.write(riga23);
            fw.flush();
	  		fw.close();

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    //ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
	}
	
	
	
	
}
