package net.projectsrl.wera.importbo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "fixeddataheader")
@XmlAccessorType (XmlAccessType.FIELD)
public class Fixeddataheader 
{
	private String identnr;
	private String manufact;
	private String version;
	private String devtype;
	private String accessnr;
	private String status;
	private String signature;
	
	public String getIdentnr() {
		return identnr;
	}
	public void setIdentnr(String identnr) {
		this.identnr = identnr;
	}
	
	public String getManufact() {
		return manufact;
	}
	public void setManufact(String manufact) {
		this.manufact = manufact;
	}

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public String getDevtype() {
		return devtype;
	}
	public void setDevtype(String devtype) {
		this.devtype = devtype;
	}
	
	public String getAccessnr() {
		return accessnr;
	}
	public void setAccessnr(String accessnr) {
		this.accessnr = accessnr;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
}
