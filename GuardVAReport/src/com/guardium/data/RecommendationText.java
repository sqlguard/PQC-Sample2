/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.Date;

public class RecommendationText {

	public RecommendationText(int recommendationId, int testId,
			double fromScore, double toScore, String text) {
		super();
		this.recommendationId = recommendationId;
		this.testId = testId;
		this.fromScore = fromScore;
		this.toScore = toScore;
		this.text = text;
	}
	
	private int recommendationId;
    private int testId;
    private double fromScore;
    private double toScore;
    String text;
 
	

    public int getRecommendationId() {
		return recommendationId;
	}
	public void setRecommendationId(int recommendationId) {
		this.recommendationId = recommendationId;
	}
	public int getTestId() {
		return testId;
	}
	public void setTestId(int testId) {
		this.testId = testId;
	}
	public double getFromScore() {
		return fromScore;
	}
	public void setFromScore(double fromScore) {
		this.fromScore = fromScore;
	}
	public double getToScore() {
		return toScore;
	}
	public void setToScore(double toScore) {
		this.toScore = toScore;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

    public void dump () {
        System.out.println("recommendation id: " + getRecommendationId());
        System.out.println("recommendation test id: " + getTestId());
        System.out.println("recommendation from score: " + getFromScore());
        System.out.println("recommendation to score: " + getToScore());
        System.out.println("recommendation text: " + getText());
    }
    	

/*
mysql> desc RECOMMENDATION_TEXT;
+-------------------+------------+------+-----+---------+----------------+
| Field             | Type       | Null | Key | Default | Extra          |
+-------------------+------------+------+-----+---------+----------------+
| RECOMMENDATION_ID | int(11)    | NO   | PRI | NULL    | auto_increment |
| TEST_ID           | int(11)    | NO   |     | 0       |                |
| FROM_SCORE        | float      | NO   |     | 0       |                |
| TO_SCORE          | float      | NO   |     | 0       |                |
| TEXT              | mediumtext | NO   |     | NULL    |                |
+-------------------+------------+------+-----+---------+----------------+
5 rows in set (0.00 sec)

 */
  
  
	
}
