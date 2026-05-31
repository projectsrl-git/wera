package net.projectsrl.wera.importbo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "network")
@XmlAccessorType (XmlAccessType.FIELD)
public class Network 
{
	@XmlElement(name = "measuredev")
	private List<Measuredev> measuredev = null;
	
	public List<Measuredev> getMeasuredev() {
		return measuredev;
	}

	public void setMeasuredev(List<Measuredev> employees) {
		this.measuredev = employees;
	}
	
	
	
	@XmlElement(name = "colectordev")
	private List<Colectordev> colectordev = null;
	
	public List<Colectordev> getColectordev() {
		return colectordev;
	}

	public void setColectordev(List<Colectordev> employees2) {
		this.colectordev = employees2;
	}

}
