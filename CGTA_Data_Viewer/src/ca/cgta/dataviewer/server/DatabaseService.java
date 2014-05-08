package ca.cgta.dataviewer.server;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.*;
//import oracle.jdbc.driver.OracleDriver;

/**
 * <p>
 * DatabaseService performs all database queries for the app.
 * </p>
 */
public class DatabaseService {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	//static final String DB_URL = "jdbc:oracle:thin:@10.7.9.50:1521:uat";	// 'uat' is SERVICE name
	static final String DB_URL = "jdbc:oracle:thin:@10.7.9.41:1521:HTBDEV";	// 'uat' is SERVICE name
	//static final String DB_URL = "jdbc:oracle:thin:@192.168.105.1:1521:uat";	// 'uat' is SERVICE name
	
	//jdbc:oracle:thin:/@(DESCRIPTION=(LOAD_BALANCE=YES)(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=10.62.33.14)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=10.62.33.15)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=10.62.33.16)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=HTBSTAGE.cgta-or.connectinggta.staging)))
	
	// UHN Host: 10.7.9.50:1521 (default JDBC port)
	
	//  Database credentials
	//static final String USER = "lgoswami";
	//static final String PASS = "NulDeAfB";
	
	static final String USER = "apps";
	static final String PASS = "apps";
	
	
	
	public static String main(String oid, String id) {	// String[] args
		   Connection conn = null;
		   Statement stmt = null;
		   Clob outClob = null;
		   CallableStatement cstmt = null;
		   
		   String outV3String = null;
		   
		   try{
		      //STEP 2: Register JDBC driver
		      //	Class.forName("com.mysql.jdbc.Driver");
		      Class.forName("oracle.jdbc.driver.OracleDriver");

		      //STEP 3: Open a connection
		      System.out.println("Connecting to database...");
		      conn = DriverManager.getConnection(DB_URL,USER,PASS);

		      //STEP 4: Execute a query
		      System.out.println("Creating statement...");
		      stmt = conn.createStatement();
		      String sql;
		      //sql = "SELECT id, first, last, age FROM Employees";
		      //sql = "select error_code, senderid, message_id from EHIPUSER.error_queue where message_id='Q134735687T124139201'";
		      sql = "select receiver_extension_id, control_act_author_name from ctb.ctb_ms_submission_units where control_act_author_name = 'L7_38f4a622-aeb8-473d-b773-d0659b05c16b'";
		      ResultSet rs = stmt.executeQuery(sql);

		      //STEP 5: Extract data from result set
		      while(rs.next()){
		         //Retrieve by column name
		         //int id  = rs.getInt("id");
		         //int age = rs.getInt("age");
		         //String first = rs.getString("first");
		         //String last = rs.getString("last");
		         
		    	 //receiver_extension_id, control_act_author_name 
		    	  String receiver_extension_id = rs.getString("receiver_extension_id");
		    	  String control_act_author_name = rs.getString("control_act_author_name");
		         //String error_code = rs.getString("error_code");
		         //String senderid = rs.getString("senderid");
		         //String message_id = rs.getString("message_id");

		         //Display values
		    	  System.out.print("receiver_extension_id: " + receiver_extension_id);
		    	  System.out.print("control_act_author_name: " + control_act_author_name);
		         //System.out.print("message_id: " + message_id);
		         //System.out.print(", senderid: " + senderid);
		         //System.out.print(", error_code: " + error_code);
		         
		      }
		      //STEP 6: Clean-up environment
		      rs.close();
		      stmt.close();
		      
		      // Creating OutClob
		      
		      outClob = conn.createClob();
		      //getoruxml_v2('2.16.840.1.113883.3.239.22.1.88.23.5.101.3','5809%','Y',outClob);
		      
		      //cstmt = conn.prepareCall("call getoruxml_v2('2.16.840.1.113883.3.239.22.1.88.23.5.101.3','5809%','Y',outClob)");
		      cstmt = conn.prepareCall("{ call getoruxml_v2(?,?,?,?) }");
		      //cstmt.setString(1, "2.16.840.1.113883.3.239.22.1.88.23.5.101.3");
		      //cstmt.setString(2, "5809%");
		      cstmt.setString(1, oid);
		      cstmt.setString(2, id+"%");
		      cstmt.setString(3, "Y");
		      cstmt.registerOutParameter(4, Types.CLOB);
		      cstmt.setClob(4, outClob);
		      
		      Boolean flag = cstmt.execute();
		      
		      System.out.print("Value of execute flag : "+ flag);
		      
		      outClob = cstmt.getClob(4);
		      System.out.print("Value of output clob: "+ outClob.toString());
		      
		      //display clob to string
		      
		      StringBuilder sb = new StringBuilder();
		      Reader reader = outClob.getCharacterStream();
		      BufferedReader br = new BufferedReader(reader);
		      String line;
		        while(null != (line = br.readLine())) {
		            sb.append(line);
		        }
		        br.close();
		        outV3String = sb.toString();
		     
		        System.out.println("Value of output V3 String: "+ outV3String);
		        
		      //outClob = cstmt.getClob(outClob);
		     // System.out.print("Value of output clob: "+ outputClob.toString());
		      
		      cstmt.close();
		      
		      conn.close();
		   }catch(SQLException se){
		      //Handle errors for JDBC
		      se.printStackTrace();
		   }catch(Exception e){
		      //Handle errors for Class.forName
		      e.printStackTrace();
		   }finally{
		      //finally block used to close resources
		      try{
		         if(stmt!=null)
		            stmt.close();
		      }catch(SQLException se2){
		      }// nothing we can do
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }//end finally try
		   }//end try
		   		   
		   System.out.println("Goodbye!");
		   
		   return outV3String;
		   
		}//end main
	
	
	/**
	 * Save a line item to the audit configuration specified in audit_log_settings.xml
	 * 
	 * @param the string to log
	 * @return true if log to audit file was successful, false if not
	 */
	public static void testDatabase() {
		
		
		
	}
	
}
