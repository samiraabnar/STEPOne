package org.iis.ut.STEPOne.stemmer;

/* 
 *
 * Created by Mostafa and Samira on 4/27/13.
 * NOTE: STeP1 Parser is open-source and licensed under the MIT license.
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.iis.persiannormalizer.PersianNormalizerScheme;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.xml.sax.SAXException;

public class Stemmer {

	private Map<String, Word> stemmedWords;
	private ArrayList<String> notStemmedWords;
	private final String currentFile = new String("offlineSTeP1.xml");

	public Stemmer() throws SAXException, IOException,
			ParserConfigurationException {
		stemmedWords = new HashMap<String, Word>();
		XMLHandler.XMLReader(currentFile, getStemmedWords());

	}

	public Map<String, Word> getStemmedWords() {
		return stemmedWords;
	}

	public void setStemmedWords(Map<String, Word> stemmedWords) {
		this.stemmedWords = stemmedWords;
	}

	public void saveStemmedWords() throws TransformerConfigurationException,
			ParserConfigurationException, SAXException, IOException,
			TransformerException {

		/*
		 * File file = new File(currentFile); if(file.exists())
		 * file.renameTo(new File("offlineSTeP1"+(new
		 * Date()).toString()+".xml"));
		 */
		XMLHandler.XMLWriter(currentFile, getStemmedWords());
	}

	public static void mergeDatasets() throws SAXException, IOException,
			ParserConfigurationException, TransformerConfigurationException,
			TransformerException {
		String srcDir = ".";
		File inDir = new File(srcDir);
		if (!inDir.exists()) {
			System.out
					.println("Error: The specified source directory could not be found...");
			return;
		}

		for (File file : inDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml") && !name.contains("stemmed");
			}
		})) {
			try {
				HashMap<String, Word> stemmedWordsList = new HashMap<String, Word>();
				XMLHandler.XMLReader(file.getPath(), stemmedWordsList);
				XMLHandler.MySQLWriter("", stemmedWordsList);
				System.out.println(file.getPath());

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(file.getPath());
			}
		}
	}

	public static void main(String[] argv) throws IOException, SAXException,
			ParserConfigurationException, TransformerConfigurationException,
			TransformerException, SQLException {
		Stemmer stemmer = new Stemmer();
		// stemmer.getStem("خرید", "Verb");

		String srcDir = argv[0];
		File inDir = new File(srcDir);
		if (!inDir.exists()) {
			System.out
					.println("Error: The specified source directory could not be found...");
			return;
		}

		for (File file : inDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".txt") && !name.contains("stemmed");
			}
		})) {
			try {
				stemmer.DBHelpedStemming(file.getPath());
				file.delete();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("Error with file: " + file.getName());
				e.printStackTrace();
			}
			// stemmer.saveStemmedWords();

		}
		/*
		 * try { XMLHandler.createTables(); } catch (SQLException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		// Stemmer.mergeDatasets();
	}

	public Morphology getStem(String word, String pos) throws SQLException {
		if ((XMLHandler.dbContainsWord(word) <= 0)) {
			Word stemmingword = new Word(word);
			try {
				stemWord(stemmingword);
			} catch (Exception e) {
				XMLHandler.insertNotStemmedIntoDB(word);
				return new Morphology(word, pos, new String[] { word }, "0", "");
			}
		}

		String stem = word;
		ArrayList<Morphology> roots = XMLHandler.getWordRoots(word);
		ArrayList<Morphology> selectedRoots = new ArrayList<Morphology>();
		for (Morphology root : roots) {
			System.out.println(root.getConvertedRootType());
			if (pos.toLowerCase().equals(
					root.getConvertedRootType().toLowerCase())) {
				selectedRoots.add(root);
			}
		}

		Collections.sort(selectedRoots, new MorphologyComparator());

		return selectedRoots.size() > 0 ? selectedRoots.get(selectedRoots
				.size() - 1) : new Morphology(word, pos, new String[] { word },
				"0", "");
	}

	public void DBHelpedStemming(String fileName) throws IOException,
			SAXException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException,
			SQLException {
		BufferedReader inputFile = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), "utf-8"));
		stemmedWords.clear();
		String line = null;
		while ((line = inputFile.readLine()) != null) {
			line = PersianNormalizerScheme.PersianStringNormalizer(line);
			for (String token : line.split("\\s+")) {
				token = token.replace(" ", "");
				if (!token.isEmpty() || (token.length() <= 1))
					System.out.print(token + " -> ");
				if ((!getStemmedWords().containsKey(token))
						&& (XMLHandler.dbContainsWord(token) <= 0)) {
					Word word = new Word(token);
					try {
						stemWord(word);
					} catch (Exception e) {
						XMLHandler.insertNotStemmedIntoDB(token);
						continue;
					}

					stemmedWords.put(token, word);
					System.out.println(token + ": "
							+ word.getMostFrequentRoot() + " count morphemes: "
							+ word.getMorphologies().size());
				}
			}
		}
		inputFile.close();
		try {
			XMLHandler.MySQLWriter("", stemmedWords);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void stemFile(String fileName) throws IOException, SAXException,
			ParserConfigurationException, TransformerConfigurationException,
			TransformerException {
		BufferedReader inputFile = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), "utf-8"));

		String line = null;
		StringBuilder stemmedFile = new StringBuilder();
		StringBuilder notStemmesWords = new StringBuilder();
		int counter = 0;
		while ((line = inputFile.readLine()) != null) {
			line = PersianNormalizerScheme.PersianStringNormalizer(line);
			for (String token : line.split("\\s+")) {
				token = token.replace(" ", "");
				if (!token.isEmpty() || (token.length() <= 1))
					System.out.print(token + " -> ");
				if (!getStemmedWords().containsKey(token)) {
					Word word = new Word(token);
					try {
						stemWord(word);
					} catch (Exception e) {
						notStemmesWords.append(token + "\n");
						stemmedFile.append(" " + token);
						continue;
					}

					stemmedWords.put(token, word);
					System.out.println(token + ": "
							+ word.getMostFrequentRoot());
					counter++;
					if (counter > 500) {
						saveStemmedWords();
						counter = 0;
					}
				}
				try {
					stemmedFile.append(" "
							+ getStemmedWords().get(token)
									.getMostFrequentRoot());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			stemmedFile.append('\n');
		}
		inputFile.close();
		BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName + ".stemmed.txt"), "utf-8"));

		writeFile.write(stemmedFile.toString());
		writeFile.close();

		BufferedWriter notStemmesWordsFile = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("notStemmed"
						+ ".stemmed.txt", true), "utf-8"));

		notStemmesWordsFile.write(notStemmesWords.toString());
		notStemmesWordsFile.close();
	}

	public void stemFileToDB(String fileName) throws IOException, SAXException,
			ParserConfigurationException, TransformerConfigurationException,
			TransformerException {
		BufferedReader inputFile = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), "utf-8"));

		String line = null;
		StringBuilder stemmedFile = new StringBuilder();
		StringBuilder notStemmesWords = new StringBuilder();
		int counter = 0;
		while ((line = inputFile.readLine()) != null) {
			line = PersianNormalizerScheme.PersianStringNormalizer(line);
			for (String token : line.split("\\s+")) {
				token = token.replace(" ", "");
				if (!token.isEmpty() || (token.length() <= 1))
					System.out.print(token + " -> ");
				if (!getStemmedWords().containsKey(token)) {
					Word word = new Word(token);
					try {
						stemWord(word);
					} catch (Exception e) {
						notStemmesWords.append(token + "\n");
						stemmedFile.append(" " + token);
						continue;
					}

					stemmedWords.put(token, word);
					System.out.println(token + ": "
							+ word.getMostFrequentRoot());
					counter++;
					if (counter > 500) {
						saveStemmedWords();
						counter = 0;
					}
				}
				try {
					stemmedFile.append(" "
							+ getStemmedWords().get(token)
									.getMostFrequentRoot());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			stemmedFile.append('\n');
		}
		inputFile.close();
		BufferedWriter writeFile = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName + ".stemmed.txt"), "utf-8"));

		writeFile.write(stemmedFile.toString());
		writeFile.close();

		BufferedWriter notStemmesWordsFile = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("notStemmed"
						+ ".stemmed.txt", true), "utf-8"));

		notStemmesWordsFile.write(notStemmesWords.toString());
		notStemmesWordsFile.close();
	}

	public void stemWord(Word word) throws Exception {
		String url = "http://step1.nlplab.sbu.ac.ir/stemmer/Stemmer.aspx?__LASTFOCUS=&__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwULLTE1MDczOTE2OTJkZCbg%2FMT%2BR4RuvJ%2BxNILXZsLglsgq&__EVENTVALIDATION=%2FwEWBQKtzsmMDQLSkcC3AwKJ16nKAQKJnYlsAvax%2B9cHjf2Hnb6sYWJ%2B%2FIEQlQEUqO%2F8F%2Bc%3D&ctl00%24ContentPlaceHolder1%24wordTextBox="
				+ word.getWord()
				+ "&ctl00%24ContentPlaceHolder1%24BtnStems=%D8%B1%DB%8C%D8%B4%D9%87+%DB%8C%D8%A7%D8%A8%DB%8C&ctl00%24ContentPlaceHolder1%24stemsTextBox=&ctl00%24ContentPlaceHolder1%24morphologyTextBox=";
		Boolean requestNotComplete = true;
		Document doc = null;
		while (requestNotComplete) {
			requestNotComplete = false;
			try {
				Thread.sleep(500);
				doc = Jsoup
						.connect(url)
						.header("User-Agent",
								" Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.91 Safari/537.11")
						.header("Accept", "*/*")
						.header("X-Chrome-Variations",
								"CNi1yQEIlLbJAQiZtskBCKO2yQEIp7bJAQiqtskBCL22yQEIt4PKAQ==")
						.header("Referer",
								"http://step1.nlplab.sbu.ac.ir/stemmer/")
						.header("Accept-Encoding", "gzip,deflate,sdch")
						.header("Accept-Language", "en-US,en;q=0.8")
						.header("Accept-Charset",
								"ISO-8859-1,utf-8;q=0.7,*;q=0.3").get();
			} catch (Exception e) {
				e.printStackTrace();
				requestNotComplete = true;
				saveStemmedWords();

			}
		}

		if (doc != null) {

			Element stemsNode = doc
					.getElementById("ctl00_ContentPlaceHolder1_stemsTextBox");
			List<Node> stemText = stemsNode.childNodes();
			if (stemText.get(0).toString().equals("ریشه‌ای یافت نشد")) {
				throw new Exception();
			}
			String[] stems = PersianNormalizerScheme.PersianStringNormalizer(
					stemText.get(0).toString()).split("\\s+");

			Element morphInfo = doc
					.getElementById("ctl00_ContentPlaceHolder1_morphologyTextBox");
			List<Node> morphText = morphInfo.childNodes();
			String[] morphologies = morphText.get(0).toString().split("\\*+");
			String type = "نوع:";
			String rootPronounciationForm = "صورت واجی ریشه:";
			String rootFrequency = "بسامد ریشه:";
			for (int i = 0; i < morphologies.length; i++) {
				if (morphologies[i].trim().length() != 0) {
					String typeValue = morphologies[i].substring(
							morphologies[i].indexOf(type) + type.length(),
							morphologies[i].indexOf(rootPronounciationForm));
					String rootPronounciationFormValue = morphologies[i]
							.substring(
									morphologies[i]
											.indexOf(rootPronounciationForm)
											+ rootPronounciationForm.length(),
									morphologies[i].indexOf(rootFrequency));
					String rootFrequencyValueAndTokens = morphologies[i]
							.substring(morphologies[i].indexOf(rootFrequency)
									+ rootFrequency.length());
					rootFrequencyValueAndTokens = rootFrequencyValueAndTokens
							.replaceAll("\\s", "");
					Pattern pattern = Pattern.compile("\\D");
					Matcher matcher = pattern
							.matcher(rootFrequencyValueAndTokens);
					String rootFrequencyValue = rootFrequencyValueAndTokens
							.substring(0, matcher.find() ? matcher.start() : -1);
					String notSplitedTokens = PersianNormalizerScheme
							.PersianStringNormalizer(rootFrequencyValueAndTokens
									.substring(rootFrequencyValueAndTokens
											.indexOf(rootFrequencyValue)
											+ rootFrequencyValue.length()));
					String[] tokens = notSplitedTokens.split("\\+");

					for (String stem : stems) {
						if (notSplitedTokens.indexOf(stem.trim()) > -1) {
							word.getMorphologies()
									.add(new Morphology(
											PersianNormalizerScheme
													.PersianStringNormalizer(stem),
											PersianNormalizerScheme
													.PersianStringNormalizer(typeValue),
											tokens, rootFrequencyValue,
											rootPronounciationFormValue));
							break;
						}
					}
				}
			}
		}
	}

}
