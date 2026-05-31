package net.projectsrl.wera.utils;

import java.util.HashMap;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.projectsrl.alibow.core.StatiRichiesta;
import net.projectsrl.wera.importazioni.db.DatiRilevatoriDAO;


public class UtilsDatiRilevatori {
	
	private static final String DATASET_RILEVATORI_ESISTENZA = "DataSetRilevatoriEsistenza";
	 
    
    // REGOLA GENERALE
    public static void doValoreMensile(Integer idScarico, String dataScarico, String energiaCumul, String volumeCumul, String umEnergiaCumul, String dataImport,String oraImport,String dataLettura,String oraLettura,String anomalia,String dataAnomalia,String oraAnomalia,String unitStat, String nomeFileBil, String nFabbricaDisp, String idRilevatore,String lettura, String meseStat, String anno, String annoDaSalvare, Float floatStat1, Float floatStat2, Float floatStat3, Float floatStat4, Float floatStat5, Float floatStat6, Float floatStat7, Float floatStat8, Float floatStat9, Float floatStat10, Float floatStat11, Float floatStat12, Float floatStat13, Float floatStat14, Float floatStat15, Float floatStat16, Float floatStat17, Float floatStat18, int intMeseStat, int azienda, String fattEnergia, String[][] arrayCondominio) throws AppCrash {
        for(int i=0; i<18; i++){
            
            int identificativoAnno=intMeseStat-i;
            String meseDaSalvare="";
           
            if (identificativoAnno>0){
                annoDaSalvare=anno;
                meseDaSalvare=Integer.toString(intMeseStat-i);
            }else{
                if (identificativoAnno<=0 && identificativoAnno>=-11){
                    annoDaSalvare=String.valueOf(Integer.parseInt(anno)-1);
                    meseDaSalvare=String.valueOf(Math.abs(identificativoAnno+12));
                }
                if (identificativoAnno<=-12){
                    annoDaSalvare=String.valueOf(Integer.parseInt(anno)-2);
                    meseDaSalvare=String.valueOf(Math.abs(identificativoAnno+24));
                }
            }
            
            
            
			if (inserisciDatiRilevatori(nFabbricaDisp, meseDaSalvare, annoDaSalvare, arrayCondominio) == true){
            	 switch(i){
                 case 0:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat1,floatStat2));
                     break;
                 case 1:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat2,floatStat3));
                     break;
                 case 2:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat3,floatStat4));
                     break;
                 case 3:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat4,floatStat5));
                     break;
                 case 4:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat5,floatStat6));
                     break;
                 case 5:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat6,floatStat7));
                     break;
                 case 6:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat7,floatStat8));
                     break;
                 case 7:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat8,floatStat9));
                     break;
                 case 8:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat9,floatStat10));
                     break;
                 case 9:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat10,floatStat11));
                     break;
                 case 10:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat11,floatStat12));
                     break;
                 case 11:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat12,floatStat13));
                     break;
                 case 12:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat13,floatStat14));
                     break;
                 case 13:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat14,floatStat15));
                     break;
                 case 14:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat15,floatStat16));
                     break;
                 case 15:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat16,floatStat17));
                     break;
                 case 16:
                         lettura=String.valueOf(WeraUtils.doDifferenza(floatStat17,floatStat18));
                     break;
                 case 17:
                         lettura=String.valueOf(floatStat18);
                     break;
                 }
   
            	 scriviDatiRilevatori(idScarico, dataScarico,energiaCumul, volumeCumul, umEnergiaCumul, dataImport, oraImport, dataLettura, oraLettura, anomalia, dataAnomalia, oraAnomalia, unitStat,lettura,nomeFileBil,nFabbricaDisp,meseDaSalvare,annoDaSalvare,azienda,fattEnergia);
            }
            
           
        }
    }
    
    
    

    
    
    
    
    private static void scriviDatiRilevatori(Integer idScarico, String dataScarico,String energiaCumul, String volumeCumul,String umEnergiaCumul,String dataImport,String oraImport,String dataLettura,String oraLettura,String anomalia,String dataAnomalia,String oraAnomalia,String unitStat, String lettura,String nomeFileBil,String nFabbricaDisp,String meseFinale,String anno, int azienda, String fattEnergia) throws AppCrash {
	 	
    	if (dataLettura.length()>10){
    		dataLettura=dataLettura.substring(0, 10);
		}
    	if (dataAnomalia.length()>10){
    		dataAnomalia=dataAnomalia.substring(0, 10);
		}
    	if (dataScarico.length()>10){
    		dataScarico=dataScarico.substring(0, 10);
		}
    	
    	
    	
    	DatiRilevatoriDAO dri = new DatiRilevatoriDAO();
		dri.setAttribute(DatiRilevatoriDAO.RILEVATORE,nFabbricaDisp);
		dri.setAttribute(DatiRilevatoriDAO.MESE,meseFinale);
		dri.setAttribute(DatiRilevatoriDAO.ANNO,anno);
		
		if (unitStat.toUpperCase().trim().replace(" ", "").equals("M3")){
		    dri.setAttribute(DatiRilevatoriDAO.LETTURA_ACS,lettura);
		}else{
		    dri.setAttribute(DatiRilevatoriDAO.LETTURA,lettura);
		}
		
		dri.setAttribute(DatiRilevatoriDAO.DATA_IMPORT,dataImport);
		dri.setAttribute(DatiRilevatoriDAO.ORA_IMPORT,oraImport);
		dri.setAttribute(DatiRilevatoriDAO.DATA_DI_LETTURA,dataLettura);
		dri.setAttribute(DatiRilevatoriDAO.ORA_DI_LETTURA,oraLettura);
		dri.setAttribute(DatiRilevatoriDAO.ANOMALIA,anomalia);
		dri.setAttribute(DatiRilevatoriDAO.DATA_ANOMALIA,dataAnomalia);
		dri.setAttribute(DatiRilevatoriDAO.ORA_ANOMALIA,oraAnomalia);
		dri.setAttribute(DatiRilevatoriDAO.LETTURA_ATTUALE,energiaCumul);
		dri.setAttribute(DatiRilevatoriDAO.VOLUME_ATTUALE,volumeCumul);
		dri.setAttribute(DatiRilevatoriDAO.UNITA_DI_MISURA,umEnergiaCumul.replace(".", ""));
		dri.setAttribute(DatiRilevatoriDAO.DATA_LETTURA_ATTUALE,dataScarico);
		dri.setAttribute(DatiRilevatoriDAO.UNIT_OF_STAT,unitStat.replace(".", ""));
		dri.setAttribute(DatiRilevatoriDAO.NOME_FILE_DR,nomeFileBil);
		dri.setAttribute(DatiRilevatoriDAO.ID_CONDOMINIO,idScarico);
		dri.setAttribute(DatiRilevatoriDAO.ID_UTENTE_INS,0);
		dri.setAttribute(DatiRilevatoriDAO.STATO,StatiRichiesta.DRAFT.getCode());
		dri.setAttribute(DatiRilevatoriDAO.DT_MODULO,project.misc.Utils.getStringDataOggi());
		dri.setAttribute(DatiRilevatoriDAO.ID_AZIENDA,azienda);
		dri.setAttribute(DatiRilevatoriDAO.FATTORE_ENERGIA,fattEnergia);
		
		dri.insert();
		
	}
    
    
    
 public static void scriviDatiRilevatoriCSV(Integer idScarico, String dataScarico,String energiaCumul, String volumeCumul,String umEnergiaCumul,String dataImport,String oraImport,String dataLettura,String oraLettura,String anomalia,String dataAnomalia,String oraAnomalia,String unitStat, String lettura,String nomeFileBil,String nFabbricaDisp,String meseFinale,String anno, int azienda, String fattEnergia, String volume1) throws AppCrash {
	 	
    	if (dataLettura.length()>10){
    		dataLettura=dataLettura.substring(0, 10);
		}
    	if (dataAnomalia.length()>10){
    		dataAnomalia=dataAnomalia.substring(0, 10);
		}
    	if (dataScarico.length()>10){
    		dataScarico=dataScarico.substring(0, 10);
		}
    	
    	
    	
    	DatiRilevatoriDAO dri = new DatiRilevatoriDAO();
		dri.setAttribute(DatiRilevatoriDAO.RILEVATORE,nFabbricaDisp);
		dri.setAttribute(DatiRilevatoriDAO.MESE,meseFinale);
		dri.setAttribute(DatiRilevatoriDAO.ANNO,anno);
		
		if (fattEnergia.contains("06") || fattEnergia.contains("07")){
			if (fattEnergia.contains("06")){
				dri.setAttribute(DatiRilevatoriDAO.LETTURA_ACS,volume1);
			}
			if (fattEnergia.contains("07")){
				dri.setAttribute(DatiRilevatoriDAO.LETTURA_AFS,volume1);
			}
		}else{
		    dri.setAttribute(DatiRilevatoriDAO.LETTURA,lettura);
		}
		
		dri.setAttribute(DatiRilevatoriDAO.DATA_IMPORT,dataImport);
		dri.setAttribute(DatiRilevatoriDAO.ORA_IMPORT,oraImport);
		dri.setAttribute(DatiRilevatoriDAO.DATA_DI_LETTURA,dataLettura);
		dri.setAttribute(DatiRilevatoriDAO.ORA_DI_LETTURA,oraLettura);
		dri.setAttribute(DatiRilevatoriDAO.ANOMALIA,anomalia);
		dri.setAttribute(DatiRilevatoriDAO.DATA_ANOMALIA,dataAnomalia);
		dri.setAttribute(DatiRilevatoriDAO.ORA_ANOMALIA,oraAnomalia);
		dri.setAttribute(DatiRilevatoriDAO.LETTURA_ATTUALE,energiaCumul);
		dri.setAttribute(DatiRilevatoriDAO.VOLUME_ATTUALE,volumeCumul);
		dri.setAttribute(DatiRilevatoriDAO.UNITA_DI_MISURA,umEnergiaCumul.replace(".", ""));
		dri.setAttribute(DatiRilevatoriDAO.DATA_LETTURA_ATTUALE,dataScarico);
		dri.setAttribute(DatiRilevatoriDAO.UNIT_OF_STAT,unitStat.replace(".", ""));
		dri.setAttribute(DatiRilevatoriDAO.NOME_FILE_DR,nomeFileBil);
		dri.setAttribute(DatiRilevatoriDAO.ID_CONDOMINIO,idScarico);
		dri.setAttribute(DatiRilevatoriDAO.ID_UTENTE_INS,0);
		dri.setAttribute(DatiRilevatoriDAO.STATO,StatiRichiesta.DRAFT.getCode());
		dri.setAttribute(DatiRilevatoriDAO.DT_MODULO,project.misc.Utils.getStringDataOggi());
		dri.setAttribute(DatiRilevatoriDAO.ID_AZIENDA,azienda);
		dri.setAttribute(DatiRilevatoriDAO.FATTORE_ENERGIA,fattEnergia);
		
		dri.insert();
		
	}
    
    /*private static boolean aggiornaRilevatori(String nFabbricaDisp, String meseFinale, String anno) throws AppCrash {

        boolean esisteRilevatore = true;

        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "DataSetRilevatoriEsistenza");
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("CONDIZIONE", " RILEVATORE='"+nFabbricaDisp+"' AND MESE ='"+meseFinale+"' AND ANNO='"+anno+"'");
            dataSet.setParam(params);
            dataSet.open();
            if (dataSet.hasMoreElements()) {
            	esisteRilevatore = false;
            }
            dataSet.close();
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
        return esisteRilevatore;
    }*/
    
    
    private static boolean inserisciDatiRilevatori(String nFabbricaDisp, String meseFinale, String anno, String[][] arrayCondominio) throws AppCrash {

        boolean eseguireInserimento = true;
        
        for (int w=0;w<arrayCondominio.length;w++){
       
        	 if (nFabbricaDisp.equals(arrayCondominio[w][0]) && arrayCondominio[w][1].contains(meseFinale+"/"+anno)){
             	eseguireInserimento = false;
             }
        }
        
        return eseguireInserimento;
    }
    
    
    
    

	public static String setUnitaDiMisura(String fattEnergia) {
		DataSet_itf dataSet = null;
		String um="";
		
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "DSUnitaMisuraCodiciUtenze");
            HashMap<String, String> params = new HashMap<String, String>();
            if(fattEnergia.length()>3){
            	fattEnergia=fattEnergia.substring(0,3);
            }
            params.put("CODICE", fattEnergia.replace("|", "").trim());
            dataSet.setParam(params);
            dataSet.open();
            if (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	um = (String) dbRow.getField("DESCRIZIONE");
            }
            dataSet.close();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    //ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
        return um;
	}








	public static void pulisciDoppi(Integer idCondominio, String nFabbricaDisp, String meseDaSalvare,
			String annoDaSalvare) {
		
		DataSet_itf dataSet = null;
		Boolean esiste=false;
		
		try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_RILEVATORI_ESISTENZA);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("CONDIZIONE", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE='"+meseDaSalvare+"' AND ANNO='"+annoDaSalvare+"'");
            dataSet.setParam(params);
            dataSet.open();
            if (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	esiste = true;
            }
            dataSet.close();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    //ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
        //return esiste;
		
	}







	public static Float calcolaDifferenzaStessoMese(Integer idCondominio, String nFabbricaDisp, String meseDaSalvare,
			String annoDaSalvare, String lettura, String dataStorico, String udrStorico) {

		if(lettura.isEmpty()){
			lettura="0";
		}
		
		DataSet_itf dataSet = null;
		Float consumo=(float) 0.00;
		String idModulo="";
		
		try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "DataSetRilevatoriDifferenza");
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("CONDIZIONE", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE='"+meseDaSalvare+"' AND ANNO='"+annoDaSalvare+"'");
            dataSet.setParam(params);
            dataSet.open();
            if (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	//consumo = Float.parseFloat(lettura)-Float.parseFloat(dbRow.getField("LETTURA").toString());
            	//if (consumo<0){
            	idModulo = (dbRow.getField("ID_MODULO").toString());
            		consumo=Float.parseFloat(lettura);
            		DatiRilevatoriDAO dri = new DatiRilevatoriDAO();
            		dri.setAttribute(DatiRilevatoriDAO.ID_MODULO,idModulo);
            		dri.delete();
            	//}
            }else{
            	consumo=Float.parseFloat(lettura);
            }
            dataSet.close();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    //ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
		return consumo;
	}
	
	
	
	
	
	public static Float calcolaDifferenzaTraMesi(Integer idCondominio, String nFabbricaDisp, String meseDaSalvare,
			String annoDaSalvare, String lettura, String dataStorico, String udrStorico) {

		if(lettura.isEmpty()){
			lettura="0";
		}
		
		DataSet_itf dataSet = null;
		Float consumo=(float) 0.00;
		String idModulo="";
		
		try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "DataSetRilevatoriDifferenzaCSV");
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("CONDIZIONE_1", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '1' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '1' month)::char(4)");
            params.put("CONDIZIONE_2", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '2' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '2' month)::char(4)");
            params.put("CONDIZIONE_3", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '3' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '3' month)::char(4)");
            params.put("CONDIZIONE_4", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '4' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '4' month)::char(4)");
            params.put("CONDIZIONE_5", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '5' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '5' month)::char(4)");
            params.put("CONDIZIONE_6", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '6' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '6' month)::char(4)");
            params.put("CONDIZIONE_7", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '7' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '7' month)::char(4)");
            params.put("CONDIZIONE_8", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '8' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '8' month)::char(4)");
            params.put("CONDIZIONE_9", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '9' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '9' month)::char(4)");
            params.put("CONDIZIONE_10", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '10' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '10' month)::char(4)");
            params.put("CONDIZIONE_11", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '11' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '11' month)::char(4)");
            params.put("CONDIZIONE_12", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '12' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '12' month)::char(4)");
            	
            dataSet.setParam(params);
            dataSet.open();
            if (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	idModulo = (dbRow.getField("ID_MODULO").toString());
            	consumo = Float.parseFloat(lettura)-Float.parseFloat(dbRow.getField("LETTURA").toString());
            	if (consumo<0){
            		consumo=Float.parseFloat(lettura);
            	}
            }else{
            	consumo=Float.parseFloat(lettura);
            }
            dataSet.close();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    //ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
		return consumo;
	}
	
	
	
	
	
	
	
	public static Float calcolaDifferenzaStessoMeseAcqua(Integer idCondominio, String nFabbricaDisp, String meseDaSalvare,
			String annoDaSalvare, String volume1, String tipoContatore) {

		if(volume1.isEmpty()){
			volume1="0";
		}
		
		DataSet_itf dataSet = null;
		Float consumo=(float) 0.00;
		String idModulo="";
		
		try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "DataSetRilevatoriDifferenzaAcqua");
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("CONDIZIONE", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE='"+meseDaSalvare+"' AND ANNO='"+annoDaSalvare+"'");
            dataSet.setParam(params);
            dataSet.open();
            if (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	//consumo = Float.parseFloat(lettura)-Float.parseFloat(dbRow.getField("LETTURA").toString());
            	//if (consumo<0){
            	idModulo = (dbRow.getField("ID_MODULO").toString());
            		consumo=Float.parseFloat(volume1);
            		DatiRilevatoriDAO dri = new DatiRilevatoriDAO();
            		dri.setAttribute(DatiRilevatoriDAO.ID_MODULO,idModulo);
            		dri.delete();
            	//}
            }else{
            	consumo=Float.parseFloat(volume1);
            }
            dataSet.close();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    //ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
		return consumo;
	}
	
	
	
	
	
	public static Float calcolaDifferenzaTraMesiAcqua(Integer idCondominio, String nFabbricaDisp, String meseDaSalvare,
			String annoDaSalvare, String volume1, String fattoreEnergia) {

		if(volume1.isEmpty()){
			volume1="0";
		}
		
		DataSet_itf dataSet = null;
		Float consumo=(float) 0.00;
		String idModulo="";
		
		try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "DataSetRilevatoriDifferenzaCSVAcqua");
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("CONDIZIONE_1", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '1' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '1' month)::char(4)");
            params.put("CONDIZIONE_2", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '2' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '2' month)::char(4)");
            params.put("CONDIZIONE_3", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '3' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '3' month)::char(4)");
            params.put("CONDIZIONE_4", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '4' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '4' month)::char(4)");
            params.put("CONDIZIONE_5", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '5' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '5' month)::char(4)");
            params.put("CONDIZIONE_6", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '6' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '6' month)::char(4)");
            params.put("CONDIZIONE_7", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '7' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '7' month)::char(4)");
            params.put("CONDIZIONE_8", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '8' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '8' month)::char(4)");
            params.put("CONDIZIONE_9", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '9' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '9' month)::char(4)");
            params.put("CONDIZIONE_10", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '10' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '10' month)::char(4)");
            params.put("CONDIZIONE_11", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '11' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '11' month)::char(4)");
            params.put("CONDIZIONE_12", " RILEVATORE='"+nFabbricaDisp+"' AND ID_CONDOMINIO="+idCondominio+" AND MESE=lpad(extract(month from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '12' month)::char(2),2,'0')::char(2) AND ANNO=extract(year from '01/"+meseDaSalvare+"/"+annoDaSalvare+"'::date - interval '12' month)::char(4)");
            	
            dataSet.setParam(params);
            dataSet.open();
            if (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	idModulo = (dbRow.getField("ID_MODULO").toString());
            	if (nFabbricaDisp.equals("51518978") || nFabbricaDisp.equals("51535346")){
            		String aaa="";
            	}
            	if (fattoreEnergia.contains("06")) {
            		consumo = Float.parseFloat(volume1)-Float.parseFloat(dbRow.getField("LETTURA_ACS").toString());
            	}else{
            		consumo = Float.parseFloat(volume1)-Float.parseFloat(dbRow.getField("LETTURA_AFS").toString());
            	}
            	
            	if (consumo<0){
            		consumo=Float.parseFloat(volume1);
            	}
            }else{
            	consumo=Float.parseFloat(volume1);
            }
            dataSet.close();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    //ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
		return consumo;
	}
	
	
	
	
	
	public static Float calcolaDifferenzaTraMesiOttimizzato(Integer idCondominio, String nFabbricaDisp,
			String meseDaSalvare, String annoDaSalvare, String lettura) {
		if (lettura == null || lettura.isEmpty()) {
			lettura = "0";
		}

		DataSet_itf dataSet = null;
		Float consumo = 0f;

		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", "DataSetRilevatoriDifferenzaCSV");

			// Calcolo date in formato 'yyyy-MM-dd'
			String dataFine = annoDaSalvare + "-" + String.format("%02d", Integer.parseInt(meseDaSalvare)) + "-01";

			// Calcolo dataInizio = dataFine - 12 mesi (qui serve calcolo in
			// Java)
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.set(Integer.parseInt(annoDaSalvare), Integer.parseInt(meseDaSalvare) - 1, 1);
			cal.add(java.util.Calendar.MONTH, -12);
			String dataInizio = sdf.format(cal.getTime());

			// Imposto i parametri per la query (devi definire la query con i
			// parametri in DataSetRilevatoriDifferenzaCSV)
			HashMap<String, String> params = new HashMap<>();
			params.put("RILEVATORE", nFabbricaDisp);
			params.put("ID_CONDOMINIO", idCondominio.toString());
			params.put("DATA_INIZIO", dataInizio);
			params.put("DATA_FINE", dataFine);

			dataSet.setParam(params);
			dataSet.open();

			if (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				Float letturaStorica = Float.parseFloat(dbRow.getField("lettura_attuale").toString());
				consumo = Float.parseFloat(lettura) - letturaStorica;
				if (consumo < 0) {
					consumo = Float.parseFloat(lettura); // reset se negativo
				}
			} else {
				consumo = Float.parseFloat(lettura);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (Throwable e) {
					// gestione errore chiusura
				}
			}
		}

		return consumo;
	}
	
	
	
	
	public static Float calcolaDifferenzaTraMesiAcquaOttimizzato(Integer idCondominio, String nFabbricaDisp,
			String meseDaSalvare, String annoDaSalvare, String lettura, String fattoreEnergia) {
		if (lettura == null || lettura.isEmpty()) {
			lettura = "0";
		}

		DataSet_itf dataSet = null;
		Float consumo = 0f;

		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", "DataSetRilevatoriDifferenzaCSVAcqua");

			// Calcolo date in formato 'yyyy-MM-dd'
			String dataFine = annoDaSalvare + "-" + String.format("%02d", Integer.parseInt(meseDaSalvare)) + "-01";

			// Calcolo dataInizio = dataFine - 12 mesi (qui serve calcolo in
			// Java)
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.set(Integer.parseInt(annoDaSalvare), Integer.parseInt(meseDaSalvare) - 1, 1);
			cal.add(java.util.Calendar.MONTH, -12);
			String dataInizio = sdf.format(cal.getTime());

			// Imposto i parametri per la query (devi definire la query con i
			// parametri in DataSetRilevatoriDifferenzaCSV)
			HashMap<String, String> params = new HashMap<>();
			params.put("RILEVATORE", nFabbricaDisp);
			params.put("ID_CONDOMINIO", idCondominio.toString());
			params.put("DATA_INIZIO", dataInizio);
			params.put("DATA_FINE", dataFine);

			dataSet.setParam(params);
			dataSet.open();

			
			
			if (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				if (fattoreEnergia.contains("06")) {
					Float letturaStorica = Float.parseFloat(dbRow.getField("lettura_attuale_acs").toString());
					consumo = Float.parseFloat(lettura) - letturaStorica;
	        	}else{
	        		Float letturaStorica = Float.parseFloat(dbRow.getField("lettura_attuale_afs").toString());
					consumo = Float.parseFloat(lettura) - letturaStorica;
	        	}
				
				if (consumo < 0) {
					consumo = Float.parseFloat(lettura); // reset se negativo
				}
			} else {
				consumo = Float.parseFloat(lettura);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (Throwable e) {
					// gestione errore chiusura
				}
			}
		}

		return consumo;
	}
	
	
	
	
	




	
      
}
