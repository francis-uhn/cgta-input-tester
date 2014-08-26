package ca.cgta.dataviewer.client;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("security")
public interface SecurityService extends RemoteService {
	String userAccountServer() throws IllegalArgumentException;
	String loginServer(String username, String password);
	String logoutServer(); //  throws IllegalArgumentException;
}
