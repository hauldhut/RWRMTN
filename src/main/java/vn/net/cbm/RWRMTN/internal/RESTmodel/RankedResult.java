package vn.net.cbm.RWRMTN.internal.RESTmodel;

public class RankedResult {
	String name;
	double score;
	int rank;
	String type;
	boolean known;
	
	
	public RankedResult() {
		super();
	}


	public RankedResult(String name, double score, int rank, String type, boolean known) {
		super();
		this.name = name;
		this.score = score;
		this.rank = rank;
		this.type = type;
		this.known = known;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public double getScore() {
		return score;
	}


	public void setScore(double score) {
		this.score = score;
	}


	public int getRank() {
		return rank;
	}


	public void setRank(int rank) {
		this.rank = rank;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public boolean isKnown() {
		return known;
	}


	public void setKnown(boolean known) {
		this.known = known;
	}
	
	
}
