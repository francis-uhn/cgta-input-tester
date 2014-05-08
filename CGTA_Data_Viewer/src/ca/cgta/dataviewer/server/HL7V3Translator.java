package ca.cgta.dataviewer.server;

import java.io.File;
import java.io.IOException;

import java.io.StringReader;
import java.io.StringWriter;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import oracle.jdbc.driver.OracleDriver;

/**
 * <p>
 * HL7V3Translator performs xslt translation of database output of HL7 V3 XML structure.
 * </p>
 */
public class HL7V3Translator {
		
	
	//public static void main() {	
	public static String v3Translator(String argString){
		
		/*//test file read starts
				String fileResult =null;
				
				
				 File outdatafile = new File("C:/Workplace/StockWatcher/data/test.txt");
				 FileInputStream fis;
				try {
					fis = new FileInputStream(outdatafile);
					StringBuilder sbuilder = new StringBuilder();
			         int ch;
			         while((ch = fis.read()) != -1){
			             sbuilder.append((char)ch);
			         }
			        
			        
			         
			         fis.close();
			         
			        // FileReader fr = new FileReader(outdatafile); 
			        // char [] a = new char[50];
			        // fr.read(a); // reads the content to the array
			        // for(char c : a)
			        	// sbuilder.append(c);
			             //System.out.print(c); //prints the characters one by one
			        // fr.close();
			         //fileResult=outdatafile.getName();
			        // fileResult=sbuilder.toString();
			         
			         
			         //System.out.println(sbuilder.toString());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		         
				
				//test file read ends
				
				*/
		
				//Split arguments
				System.out.println("argString: " + argString);
				//String arg1 = 
				String[] argSplit = argString.split("\\|");
				System.out.println("argSplit[0]: " + argSplit[0]+"  , argSplit[1]:  "+ argSplit[1]);
				
				//Pull data from database
				String v3String = DatabaseService.main(argSplit[0],argSplit[1]);
		
				
				
				//Call hl7 translate 
				
				String hl7Result = hl7Translate(v3String);
			
				
				return hl7Result;

		
				}//end main
	
	

	
	private static String hl7Translate(String outV3String) {
		// TODO Auto-generated method stub
		
		Document document; 
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	        //factory.setNamespaceAware(true);
	        //factory.setValidating(true);
	        try {
	            

	            //File stylesheet = new File("C:/Workplace/Test/data/result_and_document_transform_v4.1.xsl");
	            	            
	        	
	        	File f = new File(HL7V3Translator.class.getClassLoader().getResource("").getPath());
	    		String path = f.getParent();
	    		String fileName = path.substring(path.lastIndexOf("\\")+1,path.length()) + File.separator + "result_and_document_transform_v4.xsl";
	    		 
	    		File stylesheet = new File(fileName);
	    		
	  
	            //File datafile = new File("C:/Workplace/Test/data/ecg_14.xml");
	            

	          
	            InputSource is = new InputSource(new StringReader(outV3String));
	            DocumentBuilder builder = factory.newDocumentBuilder();
	            
	            //document = builder.parse(datafile);
	            document = builder.parse(is);
	            
	            
	            // Use a Transformer for output
	            TransformerFactory tFactory = TransformerFactory.newInstance();
	            StreamSource stylesource = new StreamSource(stylesheet);
	            Transformer transformer = tFactory.newTransformer(stylesource);

	            DOMSource source = new DOMSource(document);
	            //StreamResult result = new StreamResult(System.out);
	            //StreamResult result = new StreamResult(outdatafile);
	            
	            StringWriter outWriter = new StringWriter();
	            StreamResult result = new StreamResult( outWriter );
	            transformer.transform(source, result);
	            
	            
	            StringBuffer sbuf = outWriter.getBuffer(); 
	            String outhtmlstream = sbuf.toString();
	            
	            return outhtmlstream;
	           
	           
	            
	            /*
	           // result.getOutputStream();
	            FileInputStream fis = new FileInputStream(outdatafile);
	            StringBuilder sbuilder = new StringBuilder();
	            int ch;
	            while((ch = fis.read()) != -1){
	                sbuilder.append((char)ch);
	            }
	            fis.close();*/
	            //System.out.println(sbuilder.toString());
	           
	            //return sbuilder.toString();
	            
	            //String stringhtml = result.toString();
	            //System.out.print(outhtmlstream);
	        } catch (TransformerConfigurationException tce) {
	            // Error generated by the parser
	            System.out.println("\n** Transformer Factory error");
	            System.out.println("   " + tce.getMessage());

	            // Use the contained exception, if any
	            Throwable x = tce;

	            if (tce.getException() != null) {
	                x = tce.getException();
	            }

	            x.printStackTrace();
	        } catch (TransformerException te) {
	            // Error generated by the parser
	            System.out.println("\n** Transformation error");
	            System.out.println("   " + te.getMessage());

	            // Use the contained exception, if any
	            Throwable x = te;

	            if (te.getException() != null) {
	                x = te.getException();
	            }

	            x.printStackTrace();
	        } catch (SAXException sxe) {
	            // Error generated by this application
	            // (or a parser-initialization error)
	            Exception x = sxe;

	            if (sxe.getException() != null) {
	                x = sxe.getException();
	            }

	            x.printStackTrace();
	        } catch (ParserConfigurationException pce) {
	            // Parser with specified options can't be built
	            pce.printStackTrace();
	        } catch (IOException ioe) {
	            // I/O error
	            ioe.printStackTrace();
	        }
		
		return null;
	}
	
}