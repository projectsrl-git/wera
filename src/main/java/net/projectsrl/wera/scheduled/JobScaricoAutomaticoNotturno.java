package net.projectsrl.wera.scheduled;

import java.io.IOException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.project.errors.AppCrash;
import net.projectsrl.wera.utils.WeraUtilsCondominiDaScaricare;

public class JobScaricoAutomaticoNotturno implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		String idCondominio="";
		Boolean scaricoManuale=false;
		try {
			WeraUtilsCondominiDaScaricare.condomini(idCondominio,scaricoManuale);
		} catch (AppCrash e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}