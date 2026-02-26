/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.Date;

public class DbDriver {
	
	public DbDriver () {
		
	}

	public DbDriver ( int driver_id, int type_id, String dname, String dclass, String turl, String bturl, boolean loaded,Date ts) {
		db_driver_id = driver_id;
		datasource_type_id = type_id;
		name = dname;
		driver_class = dclass;
		url_template = turl;
		base_url_template = bturl;
		driver_stored = loaded;
		timestamp = ts;
	}
	
	/**
	 * The value for the db_driver_id field
	 */
	private int db_driver_id;

	/**
	 * The value for the datasource_type_id field
	 */
	private int datasource_type_id;

	/**
	 * The value for the name field
	 */
	private String name;

	/**
	 * The value for the driver_class field
	 */
	private String driver_class;

	/**
	 * The value for the url_template field
	 */
	private String url_template;

	/**
	 * The value for the base_url_template field
	 */
	private String base_url_template;

	private boolean driver_stored;
	
	public boolean isDriverStored() {
		return driver_stored;
	}

	public void setDriverStored(boolean stored_flag) {
		this.driver_stored = stored_flag;
	}

	/**
	 * The value for the timestamp field
	 */
	private Date timestamp;

	// get and set

	public int getDbDriverId() {
		return db_driver_id;
	}

	public void setDbDriverId(int db_driver_id) {
		this.db_driver_id = db_driver_id;
	}

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

	public String getDriverClass() {
		return driver_class;
	}

	public void setDriverClass(String driver_class) {
		this.driver_class = driver_class;
	}

	public String getUrlTemplate() {
		return url_template;
	}

	public void setUrlTemplate(String url_template) {
		this.url_template = url_template;
	}

	public String getBaseUrlTemplate() {
		return base_url_template;
	}

	public void setBaseUrlTemplate(String base_url_template) {
		this.base_url_template = base_url_template;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public void dump () {
		System.out.println("db driver id: " + this.getDbDriverId());
		System.out.println("db type id: " + this.getDatasourceTypeId());
		System.out.println("db driver name: " + this.getName());
		System.out.println("db driver class: " + this.getDriverClass());
		System.out.println("db driver url template: " + this.getUrlTemplate());
		System.out.println("db driver base url template: " + this.getBaseUrlTemplate());
		System.out.println("db driver stored: " + this.isDriverStored());
		System.out.println("db driver timestamp: " + this.getTimestamp());
	}
/*
mysql> desc DB_DRIVER;

+--------------------+--------------+------+-----+-------------------+-----------------------------+
| Field              | Type         | Null | Key | Default           | Extra                       |
+--------------------+--------------+------+-----+-------------------+-----------------------------+
| DB_DRIVER_ID       | int(11)      | NO   | PRI | 0                 |                             |
| DATASOURCE_TYPE_ID | int(11)      | NO   |     | 0                 |                             |
| NAME               | varchar(50)  | NO   |     |                   |                             |
| DRIVER_CLASS       | varchar(255) | NO   |     |                   |                             |
| URL_TEMPLATE       | varchar(255) | NO   |     |                   |                             |
| BASE_URL_TEMPLATE  | varchar(255) | NO   |     |                   |                             |
| TIMESTAMP          | timestamp    | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
+--------------------+--------------+------+-----+-------------------+-----------------------------+
7 rows in set (0.00 sec)
*/

}
