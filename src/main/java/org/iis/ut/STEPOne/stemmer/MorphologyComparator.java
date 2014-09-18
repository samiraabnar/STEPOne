package org.iis.ut.STEPOne.stemmer;
import java.util.Comparator;


public class MorphologyComparator implements Comparator<Morphology> {

	public int compare(Morphology arg0, Morphology arg1) {
		
		return arg0.getRootFrequency().compareTo(arg1.getRootFrequency());
	}

}
