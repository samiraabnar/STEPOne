package org.iis.ut.STEPOne.stemmer;
import java.util.Arrays;


public class Morphology {
	private String root;
	private String type;
	private String[] tokens;
	private Integer rootFrequency;
	private String pronounciation;
	
	

	public Morphology()
	{
	}
	
	public Morphology(String root, String type, String[] tokens,
			String rootFrequency, String pronounciation) {
		super();
		this.root = root;
		this.type = type;
		this.tokens = tokens;
		this.rootFrequency = Integer.parseInt(rootFrequency);
		this.pronounciation = pronounciation;
	}
	
	
	public String getRoot() {
		return root;
	}
	public void setRoot(String root) {
		this.root = root;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String[] getTokens() {
		return tokens;
	}
	public void setTokens(String[] tokens) {
		this.tokens = tokens;
	}
	public Integer getRootFrequency() {
		return rootFrequency;
	}
	public void setRootFrequency(Integer rootFrequency) {
		this.rootFrequency = rootFrequency;
	}
	public void setPronounciation(String pronounciation) {
		this.pronounciation = pronounciation;
	}
	public String getPronounciation() {
		return pronounciation;
	}

	public Integer getRootIndexInStructure()
	{
		int indx = Arrays.binarySearch(tokens,root);
		
		return indx;
	}
	
	public String getRootType()
	{
		
		String[] splitedType = type.split("\\+");
		if(splitedType.length <= 1)
			return splitedType[0];
		if(getRootIndexInStructure() >= 0)
			return splitedType[getRootIndexInStructure()].trim();
		
		return type;
	}
	
	public String getConvertedRootType()
	{
		String rootType = getRootType();
		if(rootType.equals("اسم"))
		{
			return "Noun";
		}
		
		if(rootType.equals("قید"))
		{
			return "Adverb";
		}
		
		if(rootType.equals("ضمیر مشترک/متقابل"))
		{
			return "Pronoun";
		}
		
		if(rootType.equals("حرف ربط"))
		{
			return "CON";
		}
		if(rootType.equals("اول شخص مفرد"))
		{
			 return "Verb";
		}
		
		if(rootType.equals("اول شخص جمع"))
		{
			return "Verb";
		}
		
		if(rootType.equals("دوم شخص مفرد"))
		{
			return "Verb";
		}
		
		if(rootType.equals("دوم شخص جمع"))
		{
			return "Verb";
		}
		
		if(rootType.equals("سوم شخص مفرد"))
		{
			return "Verb";
		}
		
		if(rootType.equals("سوم شخص جمع"))
		{
			return "Verb";
		}
	
		
		return rootType;
	}

	public String createSynonym(String newStem) {
		String[] newWordTokens = tokens.clone();
		
		newWordTokens[getRootIndexInStructure()] = newStem;
		
		String newWord = "";
		for(String toks: newWordTokens)
		{
			newWord += toks;
		}
		
		return newWord;
	}
	
}
