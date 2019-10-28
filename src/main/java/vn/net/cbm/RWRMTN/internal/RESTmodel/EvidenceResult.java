package vn.net.cbm.RWRMTN.internal.RESTmodel;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class EvidenceResult {
	String miRnaName;
	Set<String> PubMedIds;
	Map<String, PMInfo> info;
	
	public EvidenceResult() {
		super();
		info=new TreeMap<>();
	}

	public EvidenceResult(String rnaName, Set<String> pubMedIds) {
		super();
		this.miRnaName = rnaName;
		PubMedIds = pubMedIds;
	}

	public String getRnaName() {
		return miRnaName;
	}

	public void setRnaName(String rnaName) {
		this.miRnaName = rnaName;
	}

	public Set<String> getPubMedIds() {
		return PubMedIds;
	}

	public void setPubMedIds(Set<String> pubMedIds) {
		PubMedIds = pubMedIds;
	}

	public Map<String, PMInfo> getInfo() {
		return info;
	}

	public void setInfo(Map<String, PMInfo> info) {
		this.info = info;
	}

}
