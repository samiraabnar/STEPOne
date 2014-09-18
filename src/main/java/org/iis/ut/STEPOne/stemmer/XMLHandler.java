package org.iis.ut.STEPOne.stemmer;
/* 
 *
 * Created by Mostafa and Samira on 4/27/13.
 * NOTE: STeP1 Parser is open-source and licensed under the MIT license.
 * 
 */

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.mysql.jdbc.PreparedStatement;

public class XMLHandler {
	
   public static MySqlConnection mySqlCon = new MySqlConnection();

	public static void XMLWriter(String filePath,
			Map<String, Word> stemmedWordsMap)
			throws ParserConfigurationException, SAXException, IOException,
			TransformerConfigurationException, TransformerException {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		// root elements
		org.w3c.dom.Document doc = dBuilder.newDocument();
		org.w3c.dom.Element wordsElement = doc.createElement("words");

		for (String key : stemmedWordsMap.keySet()) {
			Word word = stemmedWordsMap.get(key);
			org.w3c.dom.Element wordElement = doc.createElement("word");
			org.w3c.dom.Element tokenElement = doc.createElement("token");
			Text token = doc.createTextNode(word.getWord());
			tokenElement.appendChild(token);
			wordElement.appendChild(tokenElement);

			org.w3c.dom.Element most_frequent_rootElement = doc
					.createElement("most_frequent_root");
			Text most_frequent_root = doc.createTextNode(word
					.getMostFrequentRoot());
			most_frequent_rootElement.appendChild(most_frequent_root);
			wordElement.appendChild(most_frequent_rootElement);

			org.w3c.dom.Element morphemesElement = doc
					.createElement("morphemes");

			for (Morphology morpheme : word.getMorphologies()) {
				org.w3c.dom.Element morphemeElement = doc
						.createElement("morpheme");

				org.w3c.dom.Element rootElement = doc.createElement("root");
				Text root = doc.createTextNode(morpheme.getRoot());
				rootElement.appendChild(root);
				morphemeElement.appendChild(rootElement);

				org.w3c.dom.Element root_frequencyElement = doc
						.createElement("root_frequency");
				Text root_frequency = doc.createTextNode(morpheme
						.getRootFrequency().toString());
				root_frequencyElement.appendChild(root_frequency);
				morphemeElement.appendChild(root_frequencyElement);

				org.w3c.dom.Element typeElement = doc.createElement("type");
				Text type = doc.createTextNode(morpheme.getType());
				typeElement.appendChild(type);
				morphemeElement.appendChild(typeElement);

				org.w3c.dom.Element tokensElement = doc.createElement("tokens");
				StringBuilder tokensString = new StringBuilder();
				for(int i=0; i < morpheme.getTokens().length; i++)
				{
					if(i > 0)
						tokensString.append("+");
					tokensString.append(morpheme.getTokens()[i]);
				}
				
				Text tokens = doc.createTextNode(tokensString.toString());
				tokensElement.appendChild(tokens);
				morphemeElement.appendChild(tokensElement);

				org.w3c.dom.Element pronunciationElement = doc
						.createElement("pronunciation");
				Text pronunciation = doc.createTextNode(morpheme
						.getPronounciation());
				pronunciationElement.appendChild(pronunciation);
				morphemeElement.appendChild(pronunciationElement);

				morphemesElement.appendChild(morphemeElement);
			}

			wordElement.appendChild(morphemesElement);
			wordsElement.appendChild(wordElement);

		}
		doc.appendChild(wordsElement);

		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(filePath));
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(source, result);

	}


	public static int dbContainsWord(String word) throws SQLException
	{
	        if((MySqlConnection.Conn == null) || !MySqlConnection.Conn.isValid(0))
        		MySqlConnection.startConnection(DB_Settings.dbName);
	        String query = "SELECT * FROM words where word = '"+word+"'";
	        String[] Fields = {"word"};       
	     //   System.out.println(query);
	        ArrayList<String[]> ids = mySqlCon.DB_reader(query, Fields);
	        
	        return ids.size();
	}
	
	
	public static int dbContainsNotStemmedWord(String word) throws SQLException
	{
	        if((MySqlConnection.Conn == null) || !MySqlConnection.Conn.isValid(0))
        		MySqlConnection.startConnection(DB_Settings.dbName);
	        String query = "SELECT * FROM stemless where word = '"+word+"'";
	        String[] Fields = {"word"};       
	     //   System.out.println(query);
	        ArrayList<String[]> ids = mySqlCon.DB_reader(query, Fields);
	        
	        return ids.size();
	}
	
	
	public static void insertNotStemmedIntoDB(String word) throws SQLException
	{
		if(dbContainsNotStemmedWord(word) <= 0)
		{
			String Query = "INSERT INTO  stemless (word) VALUES(?)";
			 if((MySqlConnection.Conn == null) || !MySqlConnection.Conn.isValid(0))
	        		MySqlConnection.startConnection(DB_Settings.dbName);		        java.sql.PreparedStatement pstmt = null;
		        try {
		            pstmt = MySqlConnection.Conn.prepareStatement(Query);
		            pstmt.setString(1, word);
		            
		            //System.out.print(Query);
		            pstmt.executeUpdate();
		      //      System.out.println(count + "row(s) affected");                  
		        } catch (SQLException ex) {
		            System.out.println("SQLException: " + ex.getMessage());
		            System.out.println("SQLState: " + ex.getSQLState());
		            System.out.println("VendorError: " + ex.getErrorCode());
		        }	
		}
	}
	public static int dbContainsMorpheme(String word,String root,String type, String tokens, String pronounciation) throws SQLException
	{
	        if((MySqlConnection.Conn == null) || !MySqlConnection.Conn.isValid(0))
        		MySqlConnection.startConnection(DB_Settings.dbName);
	        String query = "SELECT count(*) as resultCount FROM morphemes where word= ? and root= ? and type= ? and tokens = ? and pronounciation = ?";
	        java.sql.PreparedStatement pstmt = MySqlConnection.Conn.prepareStatement(query);
            pstmt.setString(1, word);
            pstmt.setString(2, root);
            pstmt.setString(3, type);
            pstmt.setString(4, tokens);
            pstmt.setString(5, pronounciation);
            //System.out.print(query);
            ResultSet result = pstmt.executeQuery();
            int count = 0;
	        if(result.next())
	        	count = result.getInt("resultCount");

	 
	        return count;
	}
	
	public static String getMostFrequentWords(String word) throws SQLException
	{
		if((MySqlConnection.Conn == null) || !MySqlConnection.Conn.isValid(0))
    		MySqlConnection.startConnection(DB_Settings.dbName);        
		String query = "SELECT word FROM words where word='"+word+"'";
        String[] Fields = {"word"};       
        
        //System.out.println(query);
        ArrayList<String[]> words = mySqlCon.DB_reader(query, Fields);
 
        return words.get(0)[0];
	}
	
	public static ArrayList<String[]> getAllRoots() throws SQLException
	{
		if((MySqlConnection.Conn == null) || !MySqlConnection.Conn.isValid(0))
    		MySqlConnection.startConnection(DB_Settings.dbName);       
		String query = "SELECT * FROM morphemes";
        String[] Fields = {"root","word","type","root_frequency","tokens"};       
        //System.out.print(query);
        ArrayList<String[]> roots = mySqlCon.DB_reader(query, Fields);
 
        return roots;	
	}
	
	public static ArrayList<Morphology> getWordRoots(String word) throws SQLException
	{
		if((MySqlConnection.Conn == null) || !MySqlConnection.Conn.isValid(0))
    		MySqlConnection.startConnection(DB_Settings.dbName);       
		String query = "SELECT * FROM morphemes where word = '"+word+"'";
        String[] Fields = {"root","word","type","root_frequency","tokens","pronounciation"};       
        //System.out.print(query);
        ArrayList<String[]> roots = mySqlCon.DB_reader(query, Fields);
 
        ArrayList<Morphology> morphs = new ArrayList<Morphology>();
        
        for(String[] morphItem: roots)
        {
        	Morphology morph = new Morphology(morphItem[0], morphItem[2], morphItem[4].split("\\+"), morphItem[3], morphItem[5]);
        	morphs.add(morph);
        }
        
        return morphs;
	}
	
	public static void MySQLWriter(String filePath,
			Map<String, Word> stemmedWordsMap)
			throws ParserConfigurationException, SAXException, IOException,
			TransformerConfigurationException, TransformerException, SQLException {
		if((MySqlConnection.Conn == null) || !MySqlConnection.Conn.isValid(0))
			MySqlConnection.startConnection(DB_Settings.dbName);
		
		for(Word word: stemmedWordsMap.values())
		{
			if(dbContainsWord(word.getWord()) <= 0)
			{
			 String Query = "INSERT INTO  words (word,most_frequent_root) VALUES(?,?)";
			 if((MySqlConnection.Conn == null) || !MySqlConnection.Conn.isValid(0))
	        		MySqlConnection.startConnection(DB_Settings.dbName);		        java.sql.PreparedStatement pstmt = null;
		        try {
		            pstmt = MySqlConnection.Conn.prepareStatement(Query);
		            pstmt.setString(1, word.getWord());
		            pstmt.setString(2, word.getMostFrequentRoot());
		            
		            
		            //System.out.print(Query);
		            int count = pstmt.executeUpdate();
		      //      System.out.println(count + "row(s) affected");                  
		        } catch (SQLException ex) {
		            System.out.println("SQLException: " + ex.getMessage());
		            System.out.println("SQLState: " + ex.getSQLState());
		            System.out.println("VendorError: " + ex.getErrorCode());
		        }
//		        sqlcon.DB_writer(Query);
			}
			
			for(Morphology morph: word.getMorphologies())
			{
				StringBuilder tokensString = new StringBuilder();
				for(int i=0; i < morph.getTokens().length; i++)
				{
					if(i > 0)
						tokensString.append("+");
					tokensString.append(morph.getTokens()[i]);
				}
				String tokens = tokensString.toString();
				if(dbContainsMorpheme(word.getWord(),morph.getRoot(),morph.getType(),tokens, morph.getPronounciation()) <= 0)
				{
				 String Query = "INSERT INTO  morphemes (id,root,word,type,tokens,root_frequency,pronounciation) VALUES(NULL,?,?,?,?,?,?)";
			        	if((MySqlConnection.Conn == null) || !MySqlConnection.Conn.isValid(0))
			        		MySqlConnection.startConnection(DB_Settings.dbName);

			        java.sql.PreparedStatement pstmt = null;
			        try {
			            pstmt = MySqlConnection.Conn.prepareStatement(Query);
			            pstmt.setString(1, morph.getRoot());
			            pstmt.setString(2, word.getWord());
			            pstmt.setString(3, morph.getType());
			            
						
			            pstmt.setString(4, tokens);
			            pstmt.setDouble(5, morph.getRootFrequency());
			            pstmt.setString(6, morph.getPronounciation());
			            
			            //System.out.print(Query);
			            int count = pstmt.executeUpdate();
			       //     System.out.println(count + "row(s) affected");                  
			        } catch (SQLException ex) {
			            System.out.println("SQLException: " + ex.getMessage());
			            System.out.println("SQLState: " + ex.getSQLState());
			            System.out.println("VendorError: " + ex.getErrorCode());
			        }
//			        sqlcon.DB_writer(Query);
			//        MySqlConnection.Conn.commit();
				}
								
			}
			 MySqlConnection.endConnection();
		}

	}
	
	
	public static void createTables() throws SQLException
	{
//       sqlcon.DB_Creator(DB_Settings.dbName);
       String T1 = "CREATE  TABLE words ("
  +" word LONGTEXT NOT NULL ,"
 + " most_frequent_root LONGTEXT NOT NULL,"
+ "  PRIMARY KEY (word) "
               + ");";// ENGINE = MYISAM CHARACTER SET utf8;";

       MySqlConnection.startConnection(DB_Settings.dbName);
       MySqlConnection.DB_writer(T1);
       MySqlConnection.endConnection();		
	
       String T2 = "CREATE TABLE morphemes("
    		   + "  id INT NOT NULL AUTO_INCREMENT,"
               + "  root LONGTEXT NOT NULL ,"
               + "  word LONGTEXT NOT NULL,"
               + "  type LONGTEXT NOT NULL,"
               + "  tokens LONGTEXT NOT NULL,"
               + "  root_frequency DOUBLE NOT NULL,"
               + "  pronounciation LONGTEXT NOT NULL,"
               + "  PRIMARY KEY  (id)"
               + ");";// ENGINE = MYISAM CHARACTER SET utf8;";

       MySqlConnection.startConnection(DB_Settings.dbName);
       MySqlConnection.DB_writer(T2);
       MySqlConnection.endConnection();		
	
	}
	
	public static void XMLReader(String fileName,
			Map<String, Word> stemmedWordsMap) throws SAXException,
			IOException, ParserConfigurationException {

		File fXmlFile = new File(fileName);
		if (fXmlFile.exists()) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			//.println("Root element :"
			//		+ doc.getDocumentElement().getNodeName());

			org.w3c.dom.NodeList words = doc.getElementsByTagName("word");
			
			for (int temp = 0; temp < words.getLength(); temp++) {
				org.w3c.dom.Node wordNode = words.item(temp);
				wordNode.normalize();
				org.w3c.dom.NodeList children = wordNode.getChildNodes();
				String mostFrequentRoot = "";
				String token = "";
				List<Morphology> morphologies = new ArrayList<Morphology>();

				for(int k=0; k < children.getLength(); k++)
				{
					org.w3c.dom.Node childK = children.item(k);
					String name = childK.getNodeName();
					
					if(name.equals("token"))
					{
						token = childK.getTextContent();
					}
					else if(name.equals("most_frequent_root"))
					{
						mostFrequentRoot = childK.getTextContent();
					}
					else if(name.equals("morphemes"))
					{
						org.w3c.dom.NodeList morphems = childK.getChildNodes();
						for (int m = 0; m < morphems.getLength(); m++) {
							String root = "";
							String root_frequency = "";
							String type = "";
							String[] tokens = null;
							String pronunciation = "";

							
							org.w3c.dom.Node  morph = morphems.item(m);
							if(morph.getNodeName().equals("morpheme"))
							{
						//	System.out.println(morph.getNodeName()+":"+morph.getTextContent());
							org.w3c.dom.NodeList morph_atts = morph.getChildNodes();
							for(int a=0; a < morph_atts.getLength(); a++)
							{
								org.w3c.dom.Node ma = morph_atts.item(a);
								String morph_name = ma.getNodeName();
							//	System.out.println(morph_name);
								if(morph_name.equals("root"))
								{
									root = ma.getTextContent();
								}else if(morph_name.equals("root_frequency"))
								{
									root_frequency = ma.getTextContent();
								}else if(morph_name.equals("type"))
								{
									type = ma.getTextContent();
								}
								else if(morph_name.equals("tokens"))
								{
									tokens = ma.getTextContent().split("\\+");
								}
								else if(morph_name.equals("pronunciation"))
								{
									pronunciation = ma.getTextContent();
								}
							}
							morphologies.add(
									new Morphology(root, type, tokens, root_frequency,
											pronunciation));
							}
						}
					}
					
				}
				
				Word word = new Word(token);
				
				word.setMostFrequentRoot(mostFrequentRoot);
				//System.out.println(mostFrequentRoot);
				word.setMorphologies(morphologies);
				
				if(stemmedWordsMap.containsKey(word.getWord()))
				{
					for(Morphology morph: word.getMorphologies())
					{
						if(!stemmedWordsMap.get(word.getWord()).getMorphologies().contains(morph))
						{
							stemmedWordsMap.get(word.getWord()).getMorphologies().add(morph);
						}
					}
				}
				else
				{
					stemmedWordsMap.put(word.getWord(), word);
				}
			}
		}
	}

	private static String getTagValue(String sTag, org.w3c.dom.Element eElement) {
		if (eElement.getElementsByTagName(sTag).item(0) != null) {
			org.w3c.dom.NodeList nList = eElement.getElementsByTagName(sTag)
					.item(0).getChildNodes();
			org.w3c.dom.Node nValue = (org.w3c.dom.Node) nList.item(0);
			return nValue != null ? nValue.getNodeValue() : null;
		} else
			return null;
	}
}
