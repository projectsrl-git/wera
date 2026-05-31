package net.projectsrl.wera.importbo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "siemeca")
@XmlAccessorType (XmlAccessType.FIELD)
public class Siemeca 
{
	@XmlElement(name = "network")
	private List<Network> network = null;
	
	public List<Network> getNetwork() {
		return network;
	}

	public void setNetwork(List<Network> employees) {
		this.network = employees;
	}

}
