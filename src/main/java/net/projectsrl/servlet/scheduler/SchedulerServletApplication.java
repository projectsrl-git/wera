package net.projectsrl.servlet.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.ParamCrash;
import net.project.misc.Config;
import net.projectsrl.bow.core.BOWServletApplication;

public class SchedulerServletApplication extends BOWServletApplication {

	private static final long serialVersionUID = 1L;

    public static final String WEB_INF                = "WEB-INF";
    
    public static final String DB_PROPERTIES_FILENAME = "db_connection.properties";
    
    private Scheduler _scheduler;
	

	public SchedulerServletApplication() {
		super();
	}


	@Override
	/**
	 * ridefinisco la init per gestire la schedulazione
	 */
	public void init(ServletConfig config) throws UnavailableException, ServletException {
		super.init(config);
		
	    String taskListProp=Config.GetInstance().getProperty("Quartz-scheduler.tasklist","");
		if (taskListProp.equals("")) {
			return;
		}

		makeScheduler(taskListProp);
	    
	}


	@SuppressWarnings("deprecation")
	/**
	 * Crea una schedulazione dei task descritti nella taskListProp
	 * @param String taskListProp
	 */
	private void makeScheduler(String taskListProp) {
		
		//Create instance of factory
	    SchedulerFactory schedulerFactory=null;
	    
	    try {

			readDBConfiguration();
			
	    	schedulerFactory = new StdSchedulerFactory();
			_scheduler= schedulerFactory.getScheduler();
			
			String[] taskList= taskListProp.split(",");
			
			for (int i = 0; i < taskList.length; i++) {
				
				String jobClass=Config.GetInstance().getProperty("Quartz-scheduler.job."+taskList[i]+".class","");
				
				ErrDetector.GetInstance().preCond(!jobClass.equals(""),"in config.cfg manca la proprieta' Quartz-scheduler.job."+taskList[i]+".class");
				
				//crea l'oggetto con JobDetail da eseguire
				String jobAlias="job_"+i;
				JobDetail jobDetail=new JobDetailImpl(taskList[i],jobAlias,(Class<? extends Job>) Class.forName(jobClass) );

				String cronScheduling=Config.GetInstance().getProperty("Quartz-scheduler.job."+taskList[i]+".cronScheduling","");
				ErrDetector.GetInstance().preCond(!cronScheduling.equals(""),"in config.cfg manca la proprieta' Quartz-scheduler.job."+taskList[i]+".cronScheduling");
						
				//Associa all'oggetto un trigger di scedulazione di tipo Crontab
				CronTrigger trigger=new CronTriggerImpl("cronTrigger",jobAlias,cronScheduling);

				//aggiunge allo scheduler
				_scheduler.scheduleJob(jobDetail,trigger);
				
			}
			

			//avvia lo scheduler
			_scheduler.start();
		} catch (Throwable e) {

			AppCrash ac = new AppCrash();
			ac.logContext(this.getClass().getName(), "errore nella creazione della schedulazione per i task "+taskListProp);
		}
	}


	private void readDBConfiguration() throws ParamCrash {
		
        String dbUrl = Config.GetInstance().getProperty("DB.ConnectionURL.generic","");
        ErrDetector.GetInstance().param(!dbUrl.equals(""),"in config.cfg non presente proprieta' DB.ConnectionURL.generic");

        String nomeDbHostName = readDBHostName();
        dbUrl = dbUrl.replaceAll("#DB_HOST_NAME#", nomeDbHostName);
          
        Config.GetInstance().setProperty("DB.ConnectionURL", dbUrl);
        Config.GetInstance().setProperty("DBEntity.NomeDB", nomeDbHostName.substring(nomeDbHostName.indexOf("/") + 1));
	}


	@Override
	public void destroy() {
		super.destroy();

		//ferma lo scheduler
		try {
			_scheduler.shutdown();
		} catch (SchedulerException e) {
			AppCrash ac = new AppCrash();
			ac.logContext(this.getClass().getName(), "errore nello shutdown della schedulazione");
		}		
	}
	

    protected String readDBHostName() {

        String applicationPath = getRoot() + WEB_INF;
        File file = new File(applicationPath + File.separator + DB_PROPERTIES_FILENAME);

        Properties properties = new Properties();
        String dbHostName = "";
        try {
            properties.load(new FileInputStream(file));
            //dbHostName = properties.getProperty("host_name");
        } catch (IOException e) {
            //dbHostName = "---proprieta' non trovata---";
        }

        return dbHostName;
    }	
	

}