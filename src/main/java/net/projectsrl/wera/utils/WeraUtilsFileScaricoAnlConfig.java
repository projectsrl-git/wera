
package net.projectsrl.wera.utils;

import java.io.FileWriter;
import java.io.IOException;

public class WeraUtilsFileScaricoAnlConfig {

	
	
	
	public static void creaFileScarico(String condominio,String sim,String citta,String via,String cap,String azienda, int progressivo, String cartellaCondominio, String pianificazione, String dataDomani, FileWriter fw) throws IOException{
	      
		  String progressivoRiga=String.valueOf(progressivo);
  	  String riga3="<Anlagen>\n";
		  String riga4="<id_anl>"+progressivoRiga+"</id_anl>\n";
		  String riga5="<Bezeich>"+condominio+"</Bezeich>\n";
		  String riga6="<Ort>"+citta+"</Ort>\n";
		  String riga7="<PLZ>"+cap+"</PLZ>\n";
		  String riga8="<Strasse>"+via+"</Strasse>\n";
		  String riga9="<Typ>2</Typ>\n";
		  String riga10="<Adresse>"+sim+"</Adresse>\n";
		  String riga11="<Password />\n";
		  String riga12="<Ordner>"+cartellaCondominio+"</Ordner>\n";
		  String riga13="<Periode>"+pianificazione+"</Periode>\n";
		  String riga14="<Next>"+dataDomani+"T01:10:00+00:00</Next>\n";
		  String riga15="<PW_default>true</PW_default>\n";
		  String riga16="<sFTP>false</sFTP>\n";
		  String riga17="<Error>0</Error>\n";
		  String riga18="<Proxy>true</Proxy>\n";
		  String riga19="<PWSlf />\n";
		  String riga20="<PWSlf_def>true</PWSlf_def>\n";
		  String riga21="<V8>false</V8>\n";
		  String riga22="</Anlagen>\n";
		  
		  fw.write(riga3);
		  fw.write(riga4);
		  fw.write(riga5);
		  fw.write(riga6);
		  fw.write(riga7);
		  fw.write(riga8);
		  fw.write(riga9);
		  fw.write(riga10);
		  fw.write(riga11);
		  fw.write(riga12);
		  fw.write(riga13);
		  fw.write(riga14);
		  fw.write(riga15);
		  fw.write(riga16);
		  fw.write(riga17);
		  fw.write(riga18);
		  fw.write(riga19);
		  fw.write(riga20);
		  fw.write(riga21);
		  fw.write(riga22);
  }
	
	
	
	
}
