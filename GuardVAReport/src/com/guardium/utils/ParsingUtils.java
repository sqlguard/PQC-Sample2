/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.guardium.utils.GuardGeneralException;

//for group csv import --rui
public class ParsingUtils {
	public final static char quoteChar = '"';
	public final static char seperateChar = ',';
	public final static char spaceChar = ' ';
	public final static char singleQuote = '\'';
	public final static char semicolon = ';';
	public final static char crChar = '\r';
	public final static char lfChar = '\n';
	

	public final static int  inQuote = 1;
	public final static int firstQuoteInQuote = 2;
	public final static int seperate = 3;
	public final static int  inValue = 4;
	public final static int cr = 5;

	public final static String[] sqlKeyword = {"group","order"};

	public static String[] parseCsvLine(String line, int colNum) throws GuardGeneralException {
		return parseCsvLine( line,  colNum, true);
	}
	/*@pre colNum should be larger than 0*/
	//
	public static String[] parseCsvLine(String line, int colNum, boolean checkCol) throws GuardGeneralException {
		String[] ret = null;
		if(colNum>0)
			ret = new String[colNum];
        if (line == null) {
            return null;
        }
        	
        List<String> tokens = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        boolean inQuotes = false;
        int status = seperate;
        int ctr = 0;
        
        boolean error = false;
        int i = 0;
        boolean currentTokenAlreadyAdd = false;
        for (; i < line.length(); i++) {
        	//if(i==70)
        	//	System.out.println("test");
            char c = line.charAt(i);
            switch(c){
            	case quoteChar:
	            	{
	            		switch(status){
	            		  case inValue:
	            			  error = true;
	            			  break;
	            		  case seperate:
	            			  status = inQuote;
	            			  break;
	            		  case inQuote:
	            			  status = firstQuoteInQuote;
	            			  break;
	            		  case firstQuoteInQuote:
	            			  status = inQuote;
	            			  sb.append(quoteChar);
	            			  break; 
	            	   
	            		}            		
	            	}
	            	break;
            	case seperateChar:
	            	{
	            		switch(status){
	            		  case firstQuoteInQuote:
	            		  case inValue:
	            		  case seperate:
	            			  tokens.add(sb.toString());
	            			  sb = new StringBuffer();
	            			  ctr++;
	            			  if(colNum>0&&ctr>=colNum)
	            			  {
	            				  currentTokenAlreadyAdd = true;
	            				  break;
	            			  }
	            			  
	            			  status = seperate;
	            			  break;
	            		  case inQuote:
	            			  sb.append(seperateChar);
	            			  break;          			
	            		}            		
	            	}
	            	break;
	            case spaceChar:
	            	{
	            		switch(status){
	            		  case firstQuoteInQuote:
	            			  status = seperate;
	            		  case seperate:	            			  
	            			  break;
	            		  case inQuote:
	            		  case inValue:
	            			  sb.append(spaceChar);
	            			  break;          			
	            		}            		
	            	}
	            	break;
            	default:
            		switch(status){
	          		  
	          		  case seperate:
	          			  status = inValue;
	          			  sb.append(c);
	          			  break;
	          		  case inQuote:
	          		  case inValue:
	          			  sb.append(c);
	          			  break;
	          		  case firstQuoteInQuote:
	          			  error = true;
	          			  break;	          	   
	          		}
            	
            }
            if(error==true)
        		break;
        }
        if(!currentTokenAlreadyAdd){
        	tokens.add(sb.toString());
        }
        ctr++;
        if(error==true || status==inQuote||(checkCol&&(ctr<colNum)))
        	throw new  GuardGeneralException("Can't parse csv line "+line+" at "+i,GuardGeneralException.INVALID_CSV_FILE);        
        if(colNum<1)
        	ret = new String[tokens.size()];
        return tokens.toArray(ret);
    }
	
	public static String getFirstWordOfSql(String sql){
		String ret=sql.trim();
		int blank = ret.indexOf(' ');
		if(blank>0)
			return ret.substring(0,blank);
		else
			return ret;
	}
	
	public static boolean sqlHasComma(String line){
        int status = inValue;
        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);
            switch(c){
            	case singleQuote:
	            	{
	            		switch(status){
	            		  case inValue:
	            			  status = inQuote;
	            			  break;
	            		  case inQuote:
	            			  status = firstQuoteInQuote;
	            			  break;
	            		  case firstQuoteInQuote:
	            			  status = inQuote;		            			  
	            			  break;		            	   
	            		}            		
	            	}
	            	break;
            	case semicolon:{
            		switch(status){
            		  case inValue:
            		  case firstQuoteInQuote:
            			  return true;
            		  case inQuote:	            			  
            			  break;	            	   
            		} 
            	}
            	default:
            		switch(status){
	          		  case inQuote:
	          		  case inValue:
	          			  break;
	          		  case firstQuoteInQuote:
	          			  status = inValue;
	          			  break;	          	   
	          		}
            	
            }

        }
        return false;
    }
	
	public static String extractWhere(String line){
        StringBuffer sb = new StringBuffer();
		List<String> tokens = getTokens(line);
		boolean whereStart = false;
		for(String token:tokens)
		{
			if(token.equalsIgnoreCase("where"))
			{				
				whereStart = true;
			}else if(token.equalsIgnoreCase("order")||token.equalsIgnoreCase("group"))
			{				
				break;
			}
			if(whereStart)
			{
				if(token.startsWith(String.valueOf(singleQuote)))
					sb.append(token);
				else
					sb.append(token);//toLowerCase());
			}
		}
		return sb.toString();
    }
	
	public static List<String> getTokens(String line){
        int status = seperate;
        List<String> tokens = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);
            switch(c){
            	case singleQuote:
	            	{
	            		switch(status){
	            		  case inValue:
	            		  case seperate:
	            			  status = inQuote;
	            			  sb.append(c);
	            			  break;
	            		  case inQuote:
	            			  status = firstQuoteInQuote;
	            			  sb.append(c);
	            			  break;
	            		  case firstQuoteInQuote:
	            			  status = inQuote;		            			  
	            			  break;		            	   
	            		}            		
	            	}
	            	break;
            	case spaceChar:{
            		switch(status){
            		  case inValue:
            		  case firstQuoteInQuote:
            			  status = inValue;
            			  tokens.add(sb.toString());
            			  sb.delete(0,sb.length());
            			  status = seperate;
            			  break;
            		  case inQuote:
            			  sb.append(c);
            			  break;
            		  case seperate:
            			  break;
            		} 
            	}
            	break;
            	default:
            		switch(status){
	          		  case inQuote:
	          		  case inValue:
	          			  sb.append(c);
	          			  break;
	          		  case seperate:
	          			  sb.append(c);
	          			  status = inValue;
	          			  break;
	          		  case firstQuoteInQuote:
	          			  status = inValue;
	          			  tokens.add(sb.toString());
	          			  sb.delete(0,sb.length());
	          			  sb.append(c);	          			
	          			  break;	          	   
	          		}
            	
            }

        }
        if(sb.length()>0)
        	tokens.add(sb.toString());
        return tokens;
    }
	
	public static String readALine(BufferedReader reader) throws IOException
	{
		StringBuffer sb = new StringBuffer();
		int status = seperate;
		int i = reader.read();
		if(i==-1)
			return null;
		char c = (char)i;

		while(c==crChar||c==lfChar)
		{
			i = reader.read();
			if(i==-1)
				return sb.toString();
			c = (char)i;
		}
		while(true){
			switch(c){
		    	case quoteChar:
		    	{
	        		switch(status){
	        		  
	        		  case seperate:
	        			  status = inQuote;
	        			  break;
	        		  case inQuote:
	        			  status = firstQuoteInQuote;
	        			  break;
	        		  case firstQuoteInQuote:
	        			  status = inQuote;	        			  
	        			  break;        	   
	        		}            		
	        	}
	        	break;
		    	case crChar:
		    	case lfChar:
		    	{
	        		switch(status){   		  
		      		  case seperate:
		      			  return sb.toString();		      			  
		      		  case inQuote:		      			  
		      			  break;
		      		  case firstQuoteInQuote:		      			  
		      			  return sb.toString();	      			        	   
		      		}	    		
		    	}	        	
	    	    default:
			}
			sb.append(c);
			int j = reader.read();
			if(j==-1)
			{
				if(sb.length()>0)
					return sb.toString();
				else
					return null;
			}
			c = (char)j;
		}
		
	}
}
