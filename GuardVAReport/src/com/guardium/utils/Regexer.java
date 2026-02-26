/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;

/**
 * I regex, therefore I am.
 * 
 * @author dtoland on Sep 22, 2006 at 5:01:06 PM
 */
public class Regexer {
	/** Local static logger for class */
	//private static final transient Logger LOG = Logger.getLogger(Regexer.class);

	/** Constant regex metacharacter for "|" */
	public static final transient Metacharacter META_BAR = new Metacharacter("|");

	/** Constant regex metacharacter for "^" */
	public static final transient Metacharacter META_CARET = new Metacharacter("^");

	/** Constant regex metacharacter for ")" */
	public static final transient Metacharacter META_CLOSE_PAREN = new Metacharacter(")");

	/** Constant regex metacharacter for "." */
	public static final transient Metacharacter META_DOT = new Metacharacter(".");

	/** Constant regex metacharacter for "[" */
	public static final transient Metacharacter META_OPEN_BRACKET = new Metacharacter("[");

	/** Constant regex metacharacter for "(" */
	public static final transient Metacharacter META_OPEN_PAREN = new Metacharacter("(");

	/** Constant regex metacharacter for "+" */
	public static final transient Metacharacter META_PLUS = new Metacharacter("+");

	/** Constant regex metacharacter for "?" */
	public static final transient Metacharacter META_QUESTION = new Metacharacter("?");

	/** Constant regex metacharacter for "*" */
	public static final transient Metacharacter META_STAR = new Metacharacter("*");

	/** Constant regex metacharacter for "$" */
	public static final transient Metacharacter META_DOLLAR = new Metacharacter("$");

	/** Constant array of regex metacharacters */
	public static final transient Metacharacter[] METACHARACTER = { META_BAR, META_CARET, META_CLOSE_PAREN, META_DOT, META_OPEN_BRACKET, META_OPEN_PAREN, META_PLUS, META_QUESTION, META_STAR,
			META_DOLLAR };

	/** Constant regex metacharacter for "\" */
	public static final transient Metacharacter BACKSLASH = new Metacharacter("\\");

	/** Constant expression that will ignore escaped characters when placed before them. */
	public static final transient String ESCAPED_PATTERN_PFX = "(?<!\\\\)";

	/** Constant start of symbolic regex */
	protected static final transient String OPEN_SYMBOLIC_EXPRESSION = "\\$\\{";
	/** Constant start of symbolic */
	public static final transient String OPEN_SYMBOLIC = "${";
	/** Constant end of symbolic */
	public static final transient String CLOSE_SYMBOLIC = "}";

	/** Matches "%" or "*", but not "\%" or "\*" */
	private static final Pattern SQL_STRING_WILDCARD = Pattern.compile("(?<!\\\\)%|(?<!\\\\)\\*");

	/** Matches "\\%". */
	private static final Pattern SQL_ESCAPE_PCT_WILDCARD = Pattern.compile("\\\\%|\\\\\\\\%");

	/** Matches "_", but not "\_". */
	public static final Pattern SQL_CHAR_WILDCARD = Pattern.compile("(?<!\\\\)_");

	/** Matches "?", but not "\?". */
	public static final Pattern OS_CHAR_WILDCARD = Pattern.compile("(?<!\\\\)\\?");

	/** Matches "\\_". */
	public static final Pattern SQL_ESCAPE_BAR_WILDCARD = Pattern.compile("\\\\_|\\\\\\\\_");

	/**
	 * Escapes all the defined metacharacters.
	 * 
	 * @param input
	 *           The value to escape all special regex metacharacters.
	 * @return The value with all regex metacharacters escaped
	 * @see #METACHARACTER
	 */
	public static String escapeMetacharacter(String input) {
		String result = input;
		for (int i = 0; i < METACHARACTER.length; i++) {
			Metacharacter meta = METACHARACTER[i];
			result = escapeMetacharacter(result, meta);
		}
		return result;
	}

	public static String escapeMetacharacterWithoutIgnorePreviousEscape(String input) {
		String result = input;
		for (int i = 0; i < METACHARACTER.length; i++) {
			Metacharacter meta = METACHARACTER[i];
			result = escapeMetacharacterWithoutIgnorePreviousEscape(result, meta);
		}
		return result;
	}

	public static String escapeMetacharacterForReg(String input) {
		return escapeMetacharacterWithoutIgnorePreviousEscape(escapeMetacharacterWithoutIgnorePreviousEscape(input, BACKSLASH));
	}

	/**
	 * @param input
	 *           The value to escape all special regex metacharacters.
	 * @param meta
	 *           The metacharacter to excape.
	 * @return The value with all regex metacharacter escaped
	 */
	public static String escapeMetacharacter(String input, Metacharacter meta) {
		Matcher match = meta.getPattern().matcher(input);
		String repl = meta.getReplacement();
		return match.replaceAll(repl);
	}

	public static String escapeMetacharacterWithoutIgnorePreviousEscape(String input, Metacharacter meta) {
		Matcher match = Pattern.compile(BACKSLASH + meta.getTypename()).matcher(input);
		String repl = meta.getReplacement();
		return match.replaceAll(repl);
	}

	/**
	 * Will replace the character target value in the input as long as it does not appear with a backslash preceding it.
	 * 
	 * @param input
	 *           The string in which to perform the replacement.
	 * @param target
	 *           The character to replace.
	 * @param replacement
	 *           The replacement value.
	 * @return The input with all non-escaped values of the character replaced.
	 */
	public static String replaceUnescaped(String input, char target, String replacement) {
		String expression = ESCAPED_PATTERN_PFX + target;
		Pattern pattern = Pattern.compile(expression);
		return pattern.matcher(input).replaceAll(replacement);
	}

	/**
	 * @param input
	 *           The text in which to replace the symbolic. Symbols are surrounded by <code>${}</code> for example:
	 *           <code>'This is a ${symbol}.'</code>
	 * @param symbol
	 *           The symbol to replace. Does not need to be surrounded with ${}
	 * @param replacement
	 *           The replacement value.
	 * @return The message with the symbolic replaced by the value.
	 */
	public static String replaceSymbolic(String input, String symbol, Object replacement) {
		String value = String.valueOf(replacement);
		return replaceSymbolic(input, symbol, value);
	}

	/**
	 * @param input
	 *           The text in which to replace the symbolic. Symbols are surrounded by <code>${}</code> for example:
	 *           <code>'This is a ${symbol}.'</code>
	 * @param symbol
	 *           The symbol to replace. Does not need to be surrounded with ${}
	 * @param replacement
	 *           The replacement value.
	 * @return The message with the symbolic replaced by the value.
	 */
	public static String replaceSymbolic(String input, String symbol, String replacement) {
		// nothing to do with an empty message or symbol
		if (Check.isEmpty(input) || Check.isEmpty(symbol)) {
			return input;
		}

		// null replacement values get turned into empty strings
		String substitute = "";
		if (replacement != null) {
			// replacement must be regex expression free
			substitute = escapeMetacharacter(replacement);
		}

		String msg = "";
		try {
			String regex = createSymbol(symbol);
			return input.replaceAll(regex, substitute);

		} catch (PatternSyntaxException e) {
			//if (LOG.isEnabledFor(Level.WARN)) {
				msg = "Could not replace: '" + symbol + "' with: '" + substitute + "' in: '" + input + "'.\n" + e.getPattern() + " - " + e.getDescription();
				//LOG.warn(msg, e);
			//}

		} catch (IllegalArgumentException e) {
			//if (LOG.isEnabledFor(Level.WARN)) {
				msg = "Could not replace: '" + symbol + "' with: '" + substitute + "' in: '" + input + "'.";
				//LOG.warn(msg, e);
			//}
		}
		return input + "\n" + msg;
	}

	/**
	 * Sets the proper symbol delimiters for substition
	 * 
	 * @param symbol
	 *           The symbol that will be properly formatted with delimiters.
	 * @return The symbol properly formatted.
	 */
	public static String createSymbol(String symbol) {
		StringBuffer buf = new StringBuffer();

		// if it already has the regex version for
		if (!symbol.startsWith(OPEN_SYMBOLIC_EXPRESSION)) {
			buf.append(OPEN_SYMBOLIC_EXPRESSION);
		}

		// replace the open delimiter with the regex version
		if (symbol.startsWith(OPEN_SYMBOLIC)) {
			buf.append(symbol.substring(OPEN_SYMBOLIC.length()));
		}
		else {
			buf.append(symbol);
		}

		if (!symbol.endsWith(CLOSE_SYMBOLIC)) {
			buf.append(CLOSE_SYMBOLIC);
		}

		return buf.toString();
	}

	/**
	 * Supports MSDOS and SQL wildcards of:
	 * <ul>
	 * <li>Multiple characters: '%' or '*'</li>
	 * <li>Single character: '_' or '.'</li>
	 * <ul>
	 * Any of the wildcard values can be "escaped" by prefixing them with a '\' to include them in the actual search
	 * expression.
	 * 
	 * @param expression
	 *           The wildcard expression.
	 * @return A wildcard expression converted to a regular expression.
	 */
	public static Pattern convertWildcards(String expression) {
		String value = convertWildcardExpression(expression);

		Pattern pattern = Pattern.compile(value, Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
		return pattern;
	}

	/**
	 * Supports MSDOS and SQL wildcards of:
	 * <ul>
	 * <li>Multiple characters: '%' or '*'</li>
	 * <li>Single character: '_' or '.'</li>
	 * <ul>
	 * Any of the wildcard values can be "escaped" by prefixing them with a '\' to include them in the actual search
	 * expression.
	 * 
	 * @param expression
	 *           The wildcard expression.
	 * @return A wildcard expression converted to a regular expression.
	 */
	public static String convertWildcardExpression(String expression) {
		String value = expression;

		// replace "%" or '"*'" but not "\%" of "\\*" with any string match: ".*"
		value = SQL_STRING_WILDCARD.matcher(value).replaceAll(".*");

		// replace escaped "\\%" or "\%" with "%"
		value = SQL_ESCAPE_PCT_WILDCARD.matcher(value).replaceAll("%");

		// replace "_" but not "\_" with any character match: "."
		value = SQL_CHAR_WILDCARD.matcher(value).replaceAll(".");

		// replace "?" but not "\?" with any character match: "."
		value = OS_CHAR_WILDCARD.matcher(value).replaceAll(".");

		// replace escaped "\\_" or "\_" with "_"
		value = SQL_ESCAPE_BAR_WILDCARD.matcher(value).replaceAll("_");

		return value;
	}

	/**
	 * Wrapper returns a null patten instead of throwing an exception if the expression is null;
	 * 
	 * @param expression
	 *           The expression to compile.
	 * @return The compiled Regexer pattern.
	 */
	public static Pattern compilePattern(String expression) {
		if (Check.isEmpty(expression))
			return null;
		return Pattern.compile(expression);
	}

	/**
	 * Tests to see if a pattern with embedded SQL wildcards matches. Supported wildcards are: '%', '*' for strings or
	 * '_', '.' for characters combining the best of the SQL and MSDOS worlds.<br />
	 * Wildcards may be escaped if the actual value is desired by puting a '\' in front the wildcard. For example:<br />
	 * <ul>
	 * <li>pattern: "under_bar" would match value: "under" + [any single character] + "bar"</li>
	 * <li>pattern: "under\_bar" would match value: "under_bar"</li>
	 * <li>pattern: "under.bar" would match value: "under" + [any single character] + "bar"</li>
	 * <li>pattern: "under\.bar" would match value: "under.bar"</li>
	 * <li>pattern: "7% solution" would match value: "7" + [any string] + " solution"</li>
	 * <li>pattern: "7\% solution" would match value: "7% solution"</li>
	 * <li>pattern: "7* solution" would match value: "7" + [any string] + " solution"</li>
	 * <li>pattern: "7\* solution" would match value: "7* solution"</li>
	 * </ul>
	 * 
	 * @param input
	 *           The string being matched.
	 * @param expression
	 *           The pattern to use for comparison that contains SQL wildcards of '%' and '_', or '*'.
	 * @return Whether the value matched the pattern
	 */
	public static boolean matchWildcards(String input, String expression) {
		Pattern pattern = convertWildcards(expression);
		return matchWildcards(input, pattern);
	}

	/**
	 * Tests to see if a pattern with embedded SQL wildcards matches. Supported wildcards are: '%', '*' for strings or
	 * '_', '.' for characters combining the best of the SQL and MSDOS worlds.<br />
	 * Wildcards may be escaped if the actual value is desired by puting a '\' in front the wildcard. For example:<br />
	 * <ul>
	 * <li>pattern: "under_bar" would match value: "under" + [any single character] + "bar"</li>
	 * <li>pattern: "under\_bar" would match value: "under_bar"</li>
	 * <li>pattern: "under.bar" would match value: "under" + [any single character] + "bar"</li>
	 * <li>pattern: "under\.bar" would match value: "under.bar"</li>
	 * <li>pattern: "7% solution" would match value: "7" + [any string] + " solution"</li>
	 * <li>pattern: "7\% solution" would match value: "7% solution"</li>
	 * <li>pattern: "7* solution" would match value: "7" + [any string] + " solution"</li>
	 * <li>pattern: "7\* solution" would match value: "7* solution"</li>
	 * </ul>
	 * 
	 * @param input
	 *           The string being matched.
	 * @param pattern
	 *           The pattern to use for comparison that contains SQL wildcards of '%' and '_', or '*'.
	 * @return Whether the value matched the pattern
	 */
	public static boolean matchWildcards(String input, Pattern pattern) {
		Matcher matcher = pattern.matcher(input);
		boolean result = matcher.matches();
		return result;
	}

	/**
	 * @param input
	 *           A list of string values.
	 * @param pattern
	 *           The regex pattern
	 * @return True on the first match to the regex pattern. False if no matches
	 */
	public static boolean matchWildcards(List<?> input, Pattern pattern) {
		if (!Check.isEmpty(input)) {
			for (Object value : input) {
				boolean match = matchWildcards(String.valueOf(value), pattern);
				if (match) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param input
	 *           A list of string values.
	 * @param pattern
	 *           The regex pattern
	 * @return number of matches
	 */
	public static int countMatchWildcards(List<?> input, Pattern pattern, List<String> matches) {
		int ret = 0;
		if (!Check.isEmpty(input)) {
			boolean isMatches = matches != null;
			if (isMatches) {
				matches.clear();
			}
			for (Object ovalue : input) {
				String value = String.valueOf(ovalue);
				boolean match = matchWildcards(value, pattern);
				if (match) {
					ret++;
					if (isMatches) {
						matches.add(value);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Where ever the expression contains white space, the input will match as long as there is white space in the same
	 * location. The amount of whitespace and type, in both the input and expression will be ignored.
	 * 
	 * @param input
	 *           The string we are searching in.
	 * @param expression
	 *           The string we are searching for
	 * @return whether the expression matches the input.
	 */
	public static boolean matchSpaceInsensitive(String input, String expression) {
		String value;

		Pattern pattern = Pattern.compile("\\s+");
		value = pattern.matcher(expression).replaceAll("\\\\s+");
		pattern = Pattern.compile(value, Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ);

		Matcher matcher = pattern.matcher(input);
		boolean result = matcher.find();
		return result;
	}

	/**
	 * Jr Dec 2013. Bug 37903. Port from Elkhound. 
	 * When performing Luhn validation, do it on the subset of the input values that match the regex. 
	 * @param input   A list of string values.
	 * @param pattern The regex pattern
	 * @return        The number of matches
	 */
	public static int countMatchRegexAndLuhn(List<?> input, Pattern pattern, List<String> matches) {
		int ret = 0;
		boolean matchRegexFound = false;
		boolean matchLuhnFound = false;
		
		if (!Check.isEmpty(input)) {
			boolean isMatches = matches != null;
			if (isMatches) {
				matches.clear();
			}
		
			for (Object ovalue : input) {
				String value = String.valueOf(ovalue);
				matchRegexFound = matchRegex(value, pattern);
				if (matchRegexFound) {
					matchLuhnFound = isValidLuhn(value);
					if (matchLuhnFound) {
						ret++;
						if (isMatches) {
							matches.add(value);
						}
					}
				}
			}
		}
		return ret;
	}	// end countMatchRegexAndLuhn
	
	/**
	 * Jr - Nov 22, 2013. Bug 37906
	 * CPF (Cadastro de Pessoas Físicas) is a Brazilian ID with 11 digits of the 
	 * format nnn.nnn.nnn-nn where the last 2 digits are check digits. 
	 * First ensure that a match to the regex pattern is found. Then validate the check digits.
	 * @param input   A list of string values
	 * @param pattern The regex pattern for cpf
	 * @return        The number of matches
	 */
	public static int countMatchRegexAndCpf(List<?> input, Pattern pattern, List<String> matches) {
		int ret = 0;
		boolean matchRegexFound = false;
		boolean matchCpfFound = false;
		
		if (!Check.isEmpty(input)) {
			boolean isMatches = matches != null;
			if (isMatches) {
				matches.clear();
			}
		
			for (Object ovalue : input) {
				String value = String.valueOf(ovalue);
				matchRegexFound = matchRegex(value, pattern);
				if (matchRegexFound) {
					matchCpfFound = isValidCpf(value);
					if (matchCpfFound) {
						ret++;
						if (isMatches) {
							matches.add(value);
						}
					}
				}
			}
		}
		return ret;
	}	// end countMatchRegexAndCpf
	
	/**
	 * Jr - Nov 22, 2013. Bug 37906
	 *  CNPJ (Cadastro Nacional de Pessoas Jurídicas) is a Brazilian ID with 14 digits of the 
	 *  format 00.000.000/0001-00 where:
	 *   first 8 numbers is the registration
	 *   next  4 numbers identifies the entity branch (0001 is the default for head quarters)
	 *   last  2 numbers are the check digits 
	 *  First ensure that a match to the regex pattern is found. Then validate the check digits.
	 * @param input   A list of string values
	 * @param pattern The regex pattern for cnpj
	 * @return        The number of matches
	 */
	public static int countMatchRegexAndCnpj(List<?> input, Pattern pattern, List<String> matches) {
			int ret = 0;
			boolean matchRegexFound = false;
			boolean matchCnpjFound = false;
			
			if (!Check.isEmpty(input)) {
				boolean isMatches = matches != null;
				if (isMatches) {
					matches.clear();
				}
			
				for (Object ovalue : input) {
					String value = String.valueOf(ovalue);
					matchRegexFound = matchRegex(value, pattern);
					if (matchRegexFound) {
						matchCnpjFound = isValidCnpj(value);
						if (matchCnpjFound) {
							ret++;
							if (isMatches) {
								matches.add(value);
							}
						}
					}
				}
			}
			return ret;
		}	// end countMatchRegexAndCnpj
		
	/**
	 * @param input
	 *           A list of string values.
	 * @param pattern
	 *           The regex pattern
	 * @return True on the first match to the regex pattern. False if no matchhes
	 */
	public static boolean matchRegex(List<?> input, Pattern pattern) {
		if (!Check.isEmpty(input)) {
			Iterator<?> it = input.iterator();
			while (it.hasNext()) {
				String value = String.valueOf(it.next());
				boolean match = matchRegex(value, pattern);
				if (match) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param input
	 *           A list of string values.
	 * @param pattern
	 *           The regex pattern
	 * @return number of matches
	 */
	public static int countMatchRegex(List<?> input, Pattern pattern, List<String> matches) {
		int ret = 0;
		if (!Check.isEmpty(input)) {
			boolean isMatches = matches != null;
			if (isMatches) {
				matches.clear();
			}
			for (Object ovalue : input) {
				String value = String.valueOf(ovalue);
				boolean match = matchRegex(value, pattern);
				if (match) {
					ret++;
					if (isMatches) {
						matches.add(value);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Tests to see if a string matches a regular expression. Uses <em>'single-line mode'</em> to perform it's match.
	 * 
	 * @param input
	 *           The string being matched.
	 * @param expression
	 *           The regular expression.
	 * @return Whether the string value matched the regex.
	 * @see java.util.regex.Pattern#DOTALL
	 */
	public static boolean matchRegex(String input, String expression) {
		Pattern pattern = Pattern.compile(expression, Pattern.DOTALL | Pattern.CANON_EQ);
		return matchRegex(input, pattern);
	}

	/**
	 * Tests to see if a string matches a regular expression.
	 * 
	 * @param input
	 *           The string being matched.
	 * @param pattern
	 *           The regular expression pattern.
	 * @return Whether the string value matched the regex.
	 */
	public static boolean matchRegex(String input, Pattern pattern) {
		boolean result = pattern.matcher(input).find();
		return result;
	}

	/**
	 * @param input
	 *           A list of string values.
	 * @return True on the first match to Luhn. False if no matches
	 */
	public static boolean matchLuhn(List<?> input) {
		if (!Check.isEmpty(input)) {
			Iterator<?> it = input.iterator();
			while (it.hasNext()) {
				String value = String.valueOf(it.next());
				boolean match = isValidLuhn(value);
				if (match) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param input
	 *           A list of string values.
	 * @return number of matches
	 */
	public static int countMatchLuhn(List<?> input, List<String> matches) {
		int ret = 0;
		if (!Check.isEmpty(input)) {
			boolean isMatches = matches != null;
			if (isMatches) {
				matches.clear();
			}
			for (Object ovalue : input) {
				String value = String.valueOf(ovalue);
				boolean match = isValidLuhn(value);
				if (match) {
					ret++;
					if (isMatches) {
						matches.add(value);
					}
				}
			}
		}

		return ret;
	}

	// -------------------
	// Perform Luhn check
	// -------------------

	public static boolean isValidLuhn(String cardNumber) {
		String digitsOnly = getDigitsOnly(cardNumber);
		int sum = 0;
		int digit = 0;
		int addend = 0;
		boolean timesTwo = false;

		for (int i = digitsOnly.length() - 1; i >= 0; i--) {
			digit = Integer.parseInt(digitsOnly.substring(i, i + 1));
			if (timesTwo) {
				addend = digit * 2;
				if (addend > 9) {
					addend -= 9;
				}
			}
			else {
				addend = digit;
			}
			sum += addend;
			timesTwo = !timesTwo;
		}

		int modulus = sum % 10;
		return modulus == 0;

	}

	/**
	 * Validation of Brazilian ID: Cadastro de Pessoas Físicas (CPF)
	 * Format: 11 digit CPF number, with the last 2 digits generated from the first 9.
	 * Format: nnn.nnn.nnn-nn with optional formatting characters.
	 * @param cpf String to validate
	 * @return true if valid cpf number, false otherwise 
	 */
	public static boolean isValidCpf(String cpf) {
		boolean match = false;
		
		if ((cpf == null) || (cpf.isEmpty()))
			return match;
		String digitsOnly = getDigitsOnly(cpf);
		if (digitsOnly.length() != 11)
			return match;
	
		if ((digitsOnly.equals("00000000000")) || (digitsOnly.equals("11111111111")) || (digitsOnly.equals("22222222222")) ||
			(digitsOnly.equals("33333333333")) || (digitsOnly.equals("44444444444")) || (digitsOnly.equals("55555555555")) ||
			(digitsOnly.equals("66666666666")) || (digitsOnly.equals("77777777777")) || (digitsOnly.equals("88888888888")) ||
			(digitsOnly.equals("99999999999")))
			return match;
		
		if (cpfChecksum(digitsOnly, 9))				// validate first checksum digit
			if (cpfChecksum(digitsOnly, 10))		// validate second checksum digit
				match = true;
		return match;
	}
	
	/**
	 * Calculate CPF check sum digit and validate that it matches the one in the input string
	 * @param id String to validate
	 * @param end position of checksum digit
	 * @return true if valid check sum digit, false otherwise
	 */
	public static boolean cpfChecksum (String id, int end)
	{
		boolean validated = false;
		int start;
		int idDigit;
		int moduloValue;
		int checkDigit;
		int sum = 0;

		for (start=0; start < end; start++){
			idDigit = Integer.parseInt(id.substring(start, start+1)); 
			sum += (idDigit * ((end+1) - start));
		}
		moduloValue = sum % 11;
		if (moduloValue < 2) 
			checkDigit = 0;
		else 
			checkDigit = 11 - moduloValue;
		
		if (checkDigit == Integer.parseInt(id.substring(end, end+1)))
			validated = true;
		
		return validated;
	}
	
	/**
	 * Validation of Brazilian ID: Cadastro Nacional de Pessoas Jurídicas (CNPJ)
	 * Format: 14 digit CNPJ number nn.nnn/nnnn-nn with optional formatting characters, wwhere:
	 *   first 8 numbers are the registration itself
	 *   next  4 numbers identify the entity branch (0001 default for the headquarter)
	 *   last  2 numbers are the check digits generated from the first 12.
	 * @param cnpj String to validate
	 * @return true if valid cnpj number, false otherwise 
	 */
	public static boolean isValidCnpj (String cnpj)
	{
		boolean match = false;
		int firstMultipliers[] = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
		int secondMultipliers[] = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
		
		if ((cnpj == null) || (cnpj.isEmpty()))
			return match;
		String digitsOnly = getDigitsOnly(cnpj);
		if (digitsOnly.length() != 14)
			return match;
		
		if ((digitsOnly.equals("00000000000000")) || (digitsOnly.equals("11111111111111")) || (digitsOnly.equals("22222222222222")) ||
				(digitsOnly.equals("33333333333333")) || (digitsOnly.equals("44444444444444")) || (digitsOnly.equals("55555555555555")) ||
				(digitsOnly.equals("66666666666666")) || (digitsOnly.equals("77777777777777")) || (digitsOnly.equals("88888888888888")) ||
				(digitsOnly.equals("99999999999999")))
				return match;
		
		if (cnpjChecksum(digitsOnly, firstMultipliers))			// validate first checksum digit
			if (cnpjChecksum (digitsOnly, secondMultipliers))	// validate second checksum digit
				match = true;
		return match;
	}
	
	/**
	 * Calculate CNPJ check sum digit and validate that it matches the one in the input string
	 * @param id    String to validate
	 * @param end   position of checksum digit
	 * @return true if valid check sum digit, false otherwise
	 */
	public static boolean cnpjChecksum (String id, int[] multipliers)
	{
		boolean validated = false;
		int start = 0;
		int idDigit;
		int moduloValue;
		int checkDigit;
		int sum = 0;
		int end = multipliers.length;
		
		for (start=0; start < end; start++){
			idDigit = Integer.parseInt(id.substring(start, start+1));
			sum += idDigit * multipliers[start];
		}		
		
		moduloValue = sum % 11;
		if (moduloValue < 2) 
			checkDigit = 0;
		else 
			checkDigit = 11 - moduloValue;
		
		if (checkDigit == Integer.parseInt(id.substring(end, end+1)))
			validated = true;
		
		return validated;
	}
	
	// --------------------------------
	// Filter out non-digit characters
	// --------------------------------

	private static String getDigitsOnly(String s) {
		StringBuffer digitsOnly = new StringBuffer();
		char c;
		for (int i = 0; i < s.length(); i++) {
			c = s.charAt(i);
			if (Character.isDigit(c)) {
				digitsOnly.append(c);
			}
		}
		return digitsOnly.toString();
	}

	/**
	 * Defines Regex metadata characters and the information needed to escaped them when they are embedded in strings
	 * that it is not desired that they trigger regex actions.
	 * 
	 * @author dtoland on Sep 28, 2006 at 5:28:58 PM
	 */
	public static class Metacharacter extends AbstractInnerType {
		// secret java formula for replacing meta characters, escaped thrice-over.
		private static final transient String RPL_PFX = "\\\\\\";
		private Pattern pattern = null;

		/**
		 * @param name
		 *           The name of the metacharacter
		 */
		Metacharacter(String name) {
			super(name);
		}

		/**
		 * @return The regex expression that will match this character when it has not been escaped.
		 */
		public String getExpression() {
			return ESCAPED_PATTERN_PFX + BACKSLASH + this.getTypename();
		}

		/**
		 * @return The pattern that will match this character when it has not been escaped.
		 */
		public Pattern getPattern() {
			if (this.pattern == null) {
				this.pattern = Pattern.compile(this.getExpression());
			}
			return this.pattern;
		}

		/**
		 * @return The replacement value that will escape this character.
		 */
		public String getReplacement() {
			return RPL_PFX + this.getTypename();
		}
	}
}
