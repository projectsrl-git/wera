package net.projectsrl.wera.importbo;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.projectsrl.wera.importazioni.db.ScaricoDAO;

public class TestEmployeeMarshing 
{
	static Siemeca employees = new Siemeca();
	
	public static void main(String[] args) throws JAXBException, AppCrash 
	{
		System.out.println("************************************************");
		unMarshalingExample();
	}

	private static void unMarshalingExample() throws JAXBException, AppCrash {
		JAXBContext jaxbContext = JAXBContext.newInstance(Siemeca.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Siemeca emps = (Siemeca) jaxbUnmarshaller.unmarshal( new File("d:/test/201507281442.xml") );
		
		DBTransaction dbTransaction = new DBTransaction();
		ScaricoDAO scarico = new ScaricoDAO(dbTransaction);
		
		for (int x=0;x < emps.getNetwork().size();x++)
		{
		    if (!(emps.getNetwork().get(0).getColectordev()==null)){
    		    for (int y=0;y < emps.getNetwork().get(0).getColectordev().size();y++)
    	        {
    		        String fabnr       = emps.getNetwork().get(x).getColectordev().get(y).getFabnr();
                    String model       = emps.getNetwork().get(x).getColectordev().get(y).getModel();
                    String paramset    = emps.getNetwork().get(x).getColectordev().get(y).getParamset();
                    String hourson     = emps.getNetwork().get(x).getColectordev().get(y).getHourson();
                    String errorflags  = emps.getNetwork().get(x).getColectordev().get(y).getErrorflags();
                    String errordate   = emps.getNetwork().get(x).getColectordev().get(y).getErrordate();
                    String date        = emps.getNetwork().get(x).getColectordev().get(y).getDate();
                    String time        = emps.getNetwork().get(x).getColectordev().get(y).getTime();
                    String subnet      = emps.getNetwork().get(x).getColectordev().get(y).getSubnet();
                    String receiver    = emps.getNetwork().get(x).getColectordev().get(y).getReceiver();
                    String powerstatus = emps.getNetwork().get(x).getColectordev().get(y).getPowerstatus();
                    
                    String identnr   = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0).getIdentnr();
                    String manufact  = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0).getManufact();
                    String version   = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0).getVersion();
                    String devtype   = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0).getDevtype();
                    String accessnr  = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0).getAccessnr();
                    String status    = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0).getStatus();
                    String signature = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0).getSignature();
                    
                    System.out.println("COLECTORDEV - fabnr:       "+fabnr      );
                    System.out.println("COLECTORDEV - model:       "+model      );
                    System.out.println("COLECTORDEV - paramset:    "+paramset   );
                    System.out.println("COLECTORDEV - hourson:     "+hourson    );
                    System.out.println("COLECTORDEV - errorflags:  "+errorflags );
                    System.out.println("COLECTORDEV - errordate:   "+errordate  );
                    System.out.println("COLECTORDEV - date:        "+date       );
                    System.out.println("COLECTORDEV - time:        "+time       );
                    System.out.println("COLECTORDEV - subnet:      "+subnet     );
                    System.out.println("COLECTORDEV - receiver:    "+receiver   );
                    System.out.println("COLECTORDEV - powerstatus: "+powerstatus);
                    System.out.println("COLECTORDEV - identnr:     "+identnr    );
                    System.out.println("COLECTORDEV - manufact:    "+manufact   );
                    System.out.println("COLECTORDEV - version:     "+version    );
                    System.out.println("COLECTORDEV - devtype:     "+devtype    );
                    System.out.println("COLECTORDEV - accessnr:    "+accessnr   );
                    System.out.println("COLECTORDEV - status:      "+status     );
                    System.out.println("COLECTORDEV - signature:   "+signature  );
    	        }
		    }
		    if (!(emps.getNetwork().get(0).getMeasuredev()==null)){
    		    for (int z=0;z < emps.getNetwork().get(0).getMeasuredev().size();z++)
                {
    		        String fabnr       = emps.getNetwork().get(x).getMeasuredev().get(z).getFabnr();
                    String model       = emps.getNetwork().get(x).getMeasuredev().get(z).getModel();
                    String paramset    = emps.getNetwork().get(x).getMeasuredev().get(z).getParamset();
                    String hourson     = emps.getNetwork().get(x).getMeasuredev().get(z).getHourson();
                    String errorflags  = emps.getNetwork().get(x).getMeasuredev().get(z).getErrorflags();
                    String errordate   = emps.getNetwork().get(x).getMeasuredev().get(z).getErrordate();
                    String date        = emps.getNetwork().get(x).getMeasuredev().get(z).getDate();
                    String time        = emps.getNetwork().get(x).getMeasuredev().get(z).getTime();
                    String subnet      = emps.getNetwork().get(x).getMeasuredev().get(z).getSubnet();
                    String receiver    = emps.getNetwork().get(x).getMeasuredev().get(z).getReceiver();
                    String powerstatus = emps.getNetwork().get(x).getMeasuredev().get(z).getPowerstatus();
                    
                    String identnr   = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0).getIdentnr();
                    String manufact  = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0).getManufact();
                    String version   = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0).getVersion();
                    String devtype   = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0).getDevtype();
                    String accessnr  = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0).getAccessnr();
                    String status    = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0).getStatus();
                    String signature = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0).getSignature();
                    
                    System.out.println("MEASUREDEV - fabnr:       "+fabnr      );
                    System.out.println("MEASUREDEV - model:       "+model      );
                    System.out.println("MEASUREDEV - paramset:    "+paramset   );
                    System.out.println("MEASUREDEV - hourson:     "+hourson    );
                    System.out.println("MEASUREDEV - errorflags:  "+errorflags );
                    System.out.println("MEASUREDEV - errordate:   "+errordate  );
                    System.out.println("MEASUREDEV - date:        "+date       );
                    System.out.println("MEASUREDEV - time:        "+time       );
                    System.out.println("MEASUREDEV - subnet:      "+subnet     );
                    System.out.println("MEASUREDEV - receiver:    "+receiver   );
                    System.out.println("MEASUREDEV - powerstatus: "+powerstatus);
                    System.out.println("MEASUREDEV - identnr:     "+identnr    );
                    System.out.println("MEASUREDEV - manufact:    "+manufact   );
                    System.out.println("MEASUREDEV - version:     "+version    );
                    System.out.println("MEASUREDEV - devtype:     "+devtype    );
                    System.out.println("MEASUREDEV - accessnr:    "+accessnr   );
                    System.out.println("MEASUREDEV - status:      "+status     );
                    System.out.println("MEASUREDEV - signature:   "+signature  );
                    
                    scarico.setAttribute(ScaricoDAO.ANTENNA_DI_RIFERIMENTO, receiver);
                    scarico.setAttribute(ScaricoDAO.DATA_DI_SCARICO, date);
                    scarico.setAttribute(ScaricoDAO.ORA_DI_LETTURA, time);
                    scarico.setAttribute(ScaricoDAO.N_FABBRICA_DISPOSITIVO, identnr);
                    scarico.setAttribute(ScaricoDAO.CODICE_DI_PRODUZIONE, manufact);
                    scarico.setAttribute(ScaricoDAO.VERSIONE_SOFTWARE, version);
                    scarico.setAttribute(ScaricoDAO.FATTORE_ENERGIA, devtype);
                    if (errorflags.equals("0")) {
                        errorflags = "";
                    }
                    scarico.setAttribute(ScaricoDAO.ANOMALIA, errorflags);
                    scarico.setAttribute(ScaricoDAO.DATA_ANOMALIA, errordate);
                    String letturaAttuale="";
                    if (!powerstatus.equals("")){
                        letturaAttuale=powerstatus;
                    }
                    scarico.setAttribute(ScaricoDAO.LETTURA_ATTUALE, letturaAttuale);
                    
                    
                    
                    if (!(emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees()==null)){
                        for (int w=0;w < emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().size();w++)
                        {
                            String storagenr = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().get(w).getStoragenr();
                            String value = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().get(w).getValue();
                            String dimension = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().get(w).getDimension();
                            String tariff = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().get(w).getTariff();
                            String subunit = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().get(w).getSubunit();
                            
                            System.out.println("MEASUREDEV "+identnr+" - storagenr:     "+storagenr    );
                            System.out.println("MEASUREDEV "+identnr+" - value:     "+value    );
                            System.out.println("MEASUREDEV "+identnr+" - dimension:    "+dimension   );
                            System.out.println("MEASUREDEV "+identnr+" - tariff:      "+tariff     );
                            System.out.println("MEASUREDEV "+identnr+" - subunit:   "+subunit  );
                            
                            if(w==1){
                                scarico.setAttribute(ScaricoDAO.LETTURA_ATTUALE, value);
                                scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA, dimension);
                            }
                            if(w==2){
                                scarico.setAttribute(ScaricoDAO.DATA_DI_LETTURA, value);
                            }
                            if(w==3){
                                scarico.setAttribute(ScaricoDAO.LETTURA_A_DATA_DI_SCARICO, value);
                                scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA2, dimension);
                            }
                            if(w==5){
                                scarico.setAttribute(ScaricoDAO.DATA_INIZIO_STATISTICA, value);
                            }
                            if(w==7){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE1, value);
                                scarico.setAttribute(ScaricoDAO.UNIT_OF_STAT, dimension);
                            }
                            if(w==8){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE2, value);
                            }
                            if(w==9){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE3, value);
                            }
                            if(w==10){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE4, value);
                            }
                            if(w==11){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE5, value);
                            }
                            if(w==12){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE6, value);
                            }
                            if(w==13){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE7, value);
                            }
                            if(w==14){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE8, value);
                            }
                            if(w==15){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE9, value);
                            }
                            if(w==16){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE10, value);
                            }
                            if(w==17){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE11, value);
                            }
                            if(w==18){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE12, value);
                            }
                            if(w==19){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE13, value);
                            }
                            if(w==20){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE14, value);
                            }
                            if(w==21){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE15, value);
                            }
                            if(w==22){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE16, value);
                            }
                            if(w==23){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE17, value);
                            }
                            if(w==24){
                                scarico.setAttribute(ScaricoDAO.STAT_VALUE18, value);
                            }
                        }
                    }
                }
		    }
		}
		dbTransaction.commit();
		
		System.out.println("---");
	}

}
