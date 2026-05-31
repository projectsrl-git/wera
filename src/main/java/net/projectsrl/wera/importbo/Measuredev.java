package net.projectsrl.wera.importbo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "measuredev")
@XmlAccessorType (XmlAccessType.FIELD)
public class Measuredev 
{
	@XmlElement(name = "datapoint")
	private List<Datapoint> measuredev = null;

	public List<Datapoint> getEmployees() {
		return measuredev;
	}

	public void setEmployees(List<Datapoint> employees) {
		this.measuredev = employees;
	}
	
	
	
	@XmlElement(name = "fixeddataheader")
	private List<Fixeddataheader> fix = null;
	
	public List<Fixeddataheader> getEmployees2() {
		return fix;
	}

	public void setEmployees2(List<Fixeddataheader> fix) {
		this.fix = fix;
	}

	
	
	
	
	
	private String fabnr;
	private String model;
	private String paramset;
	private String hourson;
	private String errorflags;
	private String errordate;
	private String date;
	private String time;
	private String subnet;
	private String receiver;
	private String powerstatus;
	
	public String getFabnr() {
		return fabnr;
	}
	public void setFabnr(String fabnr) {
		this.fabnr = fabnr;
	}
	
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}

	public String getParamset() {
		return paramset;
	}
	public void setParamset(String paramset) {
		this.paramset = paramset;
	}

	public String getHourson() {
		return hourson;
	}
	public void setHourson(String hourson) {
		this.hourson = hourson;
	}
	
	public String getErrorflags() {
		return errorflags;
	}
	public void setErrorflags(String errorflags) {
		this.errorflags = errorflags;
	}
	
	public String getErrordate() {
		return errordate;
	}
	public void setErrordate(String errordate) {
		this.errordate = errordate;
	}
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	public String getSubnet() {
		return subnet;
	}
	public void setSubnet(String subnet) {
		this.subnet = subnet;
	}
	
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	
	public String getPowerstatus() {
		return powerstatus;
	}
	public void setPowerstatus(String powerstatus) {
		this.receiver = powerstatus;
	}

}
