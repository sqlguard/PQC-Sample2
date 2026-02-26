/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QueryConditionOperator {
	
	/** Enumeration of Query Condition Operators */
	public enum QueryOperator {
		/** In Group operator */
		IN_GROUP(1),
		/** Is Null operator */
		IS_NULL(2),
		/** Is Not Null operator */
		IS_NOT_NULL(3),
		/** < operator */
		LESS_THAN(4),
		/** <= operator */
		LESS_THAN_OR_EQUAL(5),
		/** > operator */
		GREATER_THAN(6),
		/** >= operator */
		GREATER_THAN_OR_EQUAL(7),
		/** = operator */
		EQUAL(8),
		/** <> operator */
		NOT_EQUAL(9),
		/** Like (Wildcard) operator */
		LIKE(10),
		/** Regular Expression operator */
		REGEXP(11),
		/** Not In Group operator */
		NOT_IN_GROUP(12),
		/** In Period operator */
		IN_PERIOD(13),
		/** Not In Period operator */
		NOT_IN_PERIOD(14),
		/** Not Like (Wildcard) operator */
		NOT_LIKE(15),
		/** Classified As operator */
		CLASSIFIED_AS(16),
		/** Categorized As operator */
		CATEGORIZED_AS(17),
		/** Like Group operator */
		LIKE_GROUP(18),
		/** In Dynamic Group operator */
		IN_DYNAMIC_GROUP(19),
		/** Not In Dynamic Group operator */
		NOT_IN_DYNAMIC_GROUP(20),
		/** NOT Regular Expression operator */
		NOT_REGEXP(21),
		/** In Dynamic Group operator */
		IN_ALIASES_GROUP(22),
		/** Not In Dynamic Group operator */
		NOT_IN_ALIASES_GROUP(23),
		/** In Dynamic Group operator */
		IN_DYNAMIC_ALIASES_GROUP(24),
		/** Not In Dynamic Group operator */
		NOT_IN_DYNAMIC_ALIASES_GROUP(25);
		
		private final int id;
		private QueryConditionOperator operator = null;
		private QueryOperator(int id) { this.id = id; }
		
		/** @return the id of the query operator */
		public int getId() { return this.id; }
		
		/**
		 * @param id
		 * @return The Query Operator for the id.
		 */
		public static QueryOperator findById(int id) {
			for ( QueryOperator op : QueryOperator.values() ) {
				if (op.id == id) { return op; }
			}
			return null;
		}
		
		/**
		 * @return The database record for this operator.
		 */
		public QueryConditionOperator getQueryConditionOperator() {
			if (this.operator == null){
				this.operator = this.getQueryConditionOperator().getOperatorById(this.id);

			}
			return this.operator;
		}
		
		@Override
		public String toString() {
			String result = null;
			if ( this.getQueryConditionOperator() != null) {
				result = this.getQueryConditionOperator().getOperator();
			} else {
				result = super.toString();
			}
			return result;
		}
	}
	

	public QueryConditionOperator(int operatorId, String operator) {
		super();
		this.operatorId = operatorId;
		this.operator = operator;
	}
	
	private int operatorId;
	private String operator;
	
	
	public int getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(int operatorId) {
		this.operatorId = operatorId;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}

	private List <QueryConditionOperator> dtlist = new ArrayList <QueryConditionOperator> ();
	
	public List<QueryConditionOperator> getDrlist() {
		return dtlist;
	}

	public void setDrlist(List<QueryConditionOperator> tlist) {
		this.dtlist = tlist;
	}

	public void initMap () {
		      
		// Put elements to the list
		Date date = new Date();

		QueryConditionOperator t = new QueryConditionOperator (1, "IN GROUP");
		dtlist.add(t);
	
		t = new QueryConditionOperator (2, "IS NUL");
		dtlist.add(t);
		t = new QueryConditionOperator (3, "IS NOT NUL");
		dtlist.add(t);
		t = new QueryConditionOperator (4, "<");
		dtlist.add(t);
		t = new QueryConditionOperator (5, "<=");
		dtlist.add(t);
		t = new QueryConditionOperator (6, ">");
		dtlist.add(t);
		t = new QueryConditionOperator (7, ">=");
		dtlist.add(t);
		t = new QueryConditionOperator (8, "=");
		dtlist.add(t);
		t = new QueryConditionOperator (9, "<>");
		dtlist.add(t);
		t = new QueryConditionOperator (10, "LIKE");
		dtlist.add(t);
		t = new QueryConditionOperator (11, "REGEXP");
		dtlist.add(t);
		t = new QueryConditionOperator (12, "NOT IN GROUP");
		dtlist.add(t); 
		t = new QueryConditionOperator (13, "IN PERIOD");
		dtlist.add(t);
		t = new QueryConditionOperator (14, "NOT IN PERIOD");
		dtlist.add(t);
		t = new QueryConditionOperator (15, "NOT LIKE");
		dtlist.add(t);
		t = new QueryConditionOperator (16, "CLASSIFIED AS");
		dtlist.add(t);
		t = new QueryConditionOperator (17, "CATEGORIZED AS");
		dtlist.add(t);
		t = new QueryConditionOperator (18, "LIKE GROUP");
		dtlist.add(t);
		t = new QueryConditionOperator (19, "IN DYNAMIC GROUP");
		dtlist.add(t);
		t = new QueryConditionOperator (20, "NOT IN DYNAMIC GROUP");
		dtlist.add(t);
		t = new QueryConditionOperator (21, "NOT REGEXP");
		dtlist.add(t);
		t = new QueryConditionOperator (22, "IN ALIASES GROUP");
		dtlist.add(t);
		t = new QueryConditionOperator (23, "NOT IN ALIASES GROUP");
		dtlist.add(t);
		t = new QueryConditionOperator (24, "IN DYNAMIC ALIASES GROUP");
		dtlist.add(t);
		t = new QueryConditionOperator (25, "NOT IN DYNAMIC ALIASES GROUP");
		dtlist.add(t);
		t = new QueryConditionOperator (26, "NOT LIKE GROUP");
		dtlist.add(t);	
	}
	
	public QueryConditionOperator getOperatorById (int id) {
		QueryConditionOperator str = null;
		initMap();
		
		for (QueryConditionOperator s : dtlist) {
			if (s.getOperatorId() == id) {
				return s;
			}
	    }
		return str;
	}
	
/*	
	mysql> desc QUERY_CONDITION_OPERATOR;
	+-------------+-------------+------+-----+---------+----------------+
	| Field       | Type        | Null | Key | Default | Extra          |
	+-------------+-------------+------+-----+---------+----------------+
	| OPERATOR_ID | int(11)     | NO   | PRI | NULL    | auto_increment |
	| OPERATOR    | varchar(30) | NO   |     |         |                |
	+-------------+-------------+------+-----+---------+----------------+
	2 rows in set (0.00 sec)

	mysql> select * from QUERY_CONDITION_OPERATOR;
	+-------------+------------------------------+
	| OPERATOR_ID | OPERATOR                     |
	+-------------+------------------------------+
	|           1 | IN GROUP                     |
	|           2 | IS NULL                      |
	|           3 | IS NOT NULL                  |
	|           4 | <                            |
	|           5 | <=                           |
	|           6 | >                            |
	|           7 | >=                           |
	|           8 | =                            |
	|           9 | <>                           |
	|          10 | LIKE                         |
	|          11 | REGEXP                       |
	|          12 | NOT IN GROUP                 |
	|          13 | IN PERIOD                    |
	|          14 | NOT IN PERIOD                |
	|          15 | NOT LIKE                     |
	|          16 | CLASSIFIED AS                |
	|          17 | CATEGORIZED AS               |
	|          18 | LIKE GROUP                   |
	|          19 | IN DYNAMIC GROUP             |
	|          20 | NOT IN DYNAMIC GROUP         |
	|          21 | NOT REGEXP                   |
	|          22 | IN ALIASES GROUP             |
	|          23 | NOT IN ALIASES GROUP         |
	|          24 | IN DYNAMIC ALIASES GROUP     |
	|          25 | NOT IN DYNAMIC ALIASES GROUP |
	|          26 | NOT LIKE GROUP               |
	+-------------+------------------------------+
	26 rows in set (0.00 sec)
*/

}