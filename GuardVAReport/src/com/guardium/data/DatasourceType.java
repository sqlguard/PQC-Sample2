/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.Date;

public class DatasourceType {
	public static final String PORT_SYMBOL = "<PORT>";
	public static final String HOST_NAME_SYMBOL = "<HOST>";
	public static final String DB_NAME_SYMBOL = "<DB_NAME>";
	public static final String SERVICE_NAME_SYMBOL = "<SERVICE_NAME>";
	public static final String DBSERVER_NAME_SYMBOL = "<DBSERVER_NAME>";
	public static final String LOCALE_SYMBOL = "<LOCALE>";
	public static final String USR_SYMBOL = "<USER>";
	public static final String PASSWD_SYMBOL = "<PASSWD>";

	public DatasourceType () {
		
	}
	
	public DatasourceType ( int type_id, String dname, int dport, int ddriver_id, Date ts) {
		datasource_type_id = type_id;
		name = dname;
		default_port = dport;
		default_driver_id = ddriver_id;
		timestamp = ts;
	}		
	
	/**
     * The value for the datasource_type_id field
     */
    private int datasource_type_id;
          
    /**
     * The value for the name field
     */
    private String name;
          
    /**
     * The value for the default_port field
     */
    private int default_port;
                                                                        
    /**
     * The value for the default_driver_id field
     */
    private int default_driver_id = -1;
          
    /**
     * The value for the timestamp field
     */
    private Date timestamp;
	
    public int getDatasourceTypeId() {
		return datasource_type_id;
	}

	public void setDatasourceTypeId(int datasource_type_id) {
		this.datasource_type_id = datasource_type_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDefaultPort() {
		return default_port;
	}

	public void setDefaultPort(int default_port) {
		this.default_port = default_port;
	}

	public int getDefaultDriverId() {
		return default_driver_id;
	}

	public void setDefaultDriverId(int default_driver_id) {
		this.default_driver_id = default_driver_id;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}


	public void dump() {
        System.out.println("datasource type id: " + getDatasourceTypeId());
        System.out.println("datasource type name: " + getName());
        System.out.println("datasource default driver id: " + getDefaultDriverId());
        System.out.println("datasource default port: " + getDefaultPort());
	}
  
	
}
