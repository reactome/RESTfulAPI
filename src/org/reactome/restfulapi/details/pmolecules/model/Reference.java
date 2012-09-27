package org.reactome.restfulapi.details.pmolecules.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="reference")
@XmlAccessorType(XmlAccessType.FIELD)
public class Reference {

	@XmlElement
	private String id;
	@XmlElement
	private String name;
	@XmlElement
	private String url;
	@XmlElement
	private String geneName = null;
	
	public Reference(){
		
	}
	
	public Reference(String id, String name, String url) {
		super();
		this.id = id;
		this.name = name;
		this.url = url;
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getGeneName() {
		return geneName;
	}

	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}

}
