package net.projectsrl.wera.importbo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "datapoint")
@XmlAccessorType (XmlAccessType.FIELD)
public class Datapoint 
{
	private String storagenr;
	private String value;
	private String dimension;
	private String tariff;
	private String subunit;
	
	public String getStoragenr() {
		return storagenr;
	}
	public void setStoragenr(String storagenr) {
		this.storagenr = storagenr;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getDimension() {
		return dimension;
	}
	public void setDimension(String dimension) {
		this.dimension = dimension;
	}
	public String getTariff() {
		return tariff;
	}
	public void setTariff(String tariff) {
		this.tariff = tariff;
	}
	
	public String getSubunit() {
		return subunit;
	}
	public void setSubunit(String subunit) {
		this.subunit = subunit;
	}
}
