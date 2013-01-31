package org.glassfish.openesb.pojose.api.annotation;

public @interface ConsumerEndpoint {

	public String  serviceQN();
	public String  operationQN();
	public String name();	
	public String inMessageTypeQN();
	public String interfaceQN();

}
