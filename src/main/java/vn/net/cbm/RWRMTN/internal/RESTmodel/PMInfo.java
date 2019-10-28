package vn.net.cbm.RWRMTN.internal.RESTmodel;

import java.util.ArrayList;

public class PMInfo {
	private String pubdate;
	private ArrayList<String> authors;
	private String title;
	private String pages;
	public PMInfo() {
		super();
		authors=new ArrayList<>();
	}
	public PMInfo(String pubdate, ArrayList<String> authors, String title, String pages) {
		super();
		this.pubdate = pubdate;
		this.authors = authors;
		this.title = title;
		this.pages = pages;
	}
	public String getPubdate() {
		return pubdate;
	}
	public void setPubdate(String pubdate) {
		this.pubdate = pubdate;
	}
	public ArrayList<String> getAuthors() {
		return authors;
	}
	public void setAuthors(ArrayList<String> authors) {
		this.authors = authors;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPages() {
		return pages;
	}
	public void setPages(String pages) {
		this.pages = pages;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str=authors.toString();
		str=str.substring(1, str.length()-2);
		return  "\nAuthors: "+str
				+ "\n\nTitle: "+title
				+ "\n\nPages: "+pages
				+ "\n\nPubdate: "+pubdate;
	}
	
}
