package ca.cgta.input.listener;

import java.net.InetSocketAddress;
import java.net.Socket;

import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.hl7v2.llp.HL7Reader;
import ca.uhn.hl7v2.llp.HL7Writer;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;

public class ListenerTest {

	private static Listener myListener;

	@BeforeClass
	public static void setupClass() throws ServletException, JAXBException {
		myListener = new Listener(true);
		myListener.init(null);
	}
	
	@AfterClass
	public static void cleanupClass() {
		myListener.destroy();
	}
	
	@Test
	public void testMessageWithReturnsInBadPlaces() throws Exception {
		
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress("localhost", 13999), 1000);
		
		LowerLayerProtocol llp = LowerLayerProtocol.makeLLP();
		HL7Reader is = llp.getReader(socket.getInputStream());
		HL7Writer os = llp.getWriter(socket.getOutputStream());
		
		String msg = "MSH|^~\\&|2.16.840.1.113883.3.239.23.5^2.16.840.1.113883.3.239.23.5.101.2|SMH^2.16.840.1.113883.3.239.23.5.100.1|ConnectingGTA|ConnectingGTA|20120711161111-0400|1238973875fff|ORU^O01^ORU_O01||T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
				"PID|1||4003313^^^2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.2^MR^^^||CGTA-SMH^ER^^^^^L||19781116-0400|M|||||||^^HL70296|||||||||||||||N\r" + 
				"PV1|1|X||EM|||56835^^^^^^^^2.16.840.1.113883.4.347&2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.2|||EMA|||||||||00850009663^^^2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.2^AN|||||||||||||||||||||||||2012062600-0400\r" + 
				"ORC|||628622^2.16.840.1.113883.3.239.23.5^2.16.840.1.113883.3.239.23.5.101.2\r" + 
				"OBR|1|628622^2.16.840.1.113883.3.239.23.5^2.16.840.1.113883.3.239.23.5.101.2||EDRECID^ED Documents^2.16.840.1.113883.3.239.23.5.102.3|||||||||||||||||||||F\r" + 
				"OBX|1|ED|EDRECID^ED Documents^2.16.840.1.113883.3.239.23.5.102.4||Text^TIFF^Base64^SUkqAGScAAAmoFsFMjg1VOOQd8geLFD5B6EJakDwjkMgGQcgcgXggxAroWQe0yDwmQ1XCZA2g3cg\r" + 
				"vaqQfCC2DU5DUdeIME0GQYHIY4JkHHUgwOpA4gKpDFkNWFVSGHIZAKw5BZHIYgJkMWj4IdGeB/EW\r" + 
				"CDIimmEyC+EGHIPhBiCC9qQxBDGJkMWQXcgvBBhyGQCsOQIghpWQyQIdNUyD8FIMOQxZA8GpyGpZ\r" + 
				"DVdV0GQxBBjCB4K1gmQxxQ5UEk5SwVtRxESfI8GQhBIVCYTwmmqhNBpkMYQfCC9kHwhiyGm5BiE1\r" + 
				"IYhSGByBDkF/IPxBPIXyCeQ5glIKJcXWRkEEmQhyH4hdCOYXU1aZvCE6MnBxERER8RERERERERER\r" + 
				"EhmGQ+BBhMJhSEZS0mbrehERERH///////////////JKZgZW84/CaIITNBeEV3mmfEO6L54zwaJD\r" + 
				"a6NDadbLmcjhE5mzOCggZcQ2y4pGP0mnfemkE/Wz5adHe00jO0eLND5BMP7+xHH/a+9/Sf0g6T7b\r" + 
				"U8GerXt9rqPoER/HXfpJvH+qe/4f/X/vCx73v8cV/YfaBEf+NJuy+o31W313BEfyyld4N8p/fsm/\r" + 
				"WPYPBP4PX0rzqZhnxmCMRtFxnyI4LlxkcMHhCORgZszaOBDBkjPZczBnVGMxl4oGfRrRFERiMI0z\r" + 
				"2fRozeez2dRmM3mMoRvOZtkfM2YM9mZnNMufv9pfoE6HPHYcfhh4S6/01Rh33eycOqaTtNPCdrpm";
		os.writeMessage(msg);

		String response = is.getMessage();
		Assert.assertTrue(response, response.contains("|AE|"));
		Assert.assertTrue(response, response.contains("Message appears to have CR characters"));
		
	}
	
}
