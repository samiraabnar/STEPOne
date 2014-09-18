package org.iis.ut.STEPOne.stemmer;
import java.util.ArrayList;
import java.util.List;


public class Word {
	String word;
	List<Morphology> morphologies;
	String  mostFrequentRoot;
	
	
	
	public void setMostFrequentRoot(String mostFrequentStem) {
		this.mostFrequentRoot = mostFrequentStem;
	}

	public Word(String word) {
		super();
		this.word = word;
	
		morphologies = new ArrayList<Morphology>();
		mostFrequentRoot = null;
	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	
	public List<Morphology> getMorphologies() {
		return morphologies;
	}
	public void setMorphologies(List<Morphology> morphologies) {
		this.morphologies = morphologies;
	}
	


	public String getMostFrequentRoot() {
		if(mostFrequentRoot == null)
		{
			int frequency = -1;
	
			for(Morphology morph: morphologies)
			{
				if(morph.getRootFrequency() > frequency)
				{
					mostFrequentRoot = morph.getRoot();
					frequency = morph.getRootFrequency();
				}
			}
		}
		return mostFrequentRoot;
	}	
}
