/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

//import org.apache.jcs.access.exception.InvalidArgumentException;

import com.guardium.utils.i18n.SayAppRes;
import com.guardium.utils.runtime.CommandExecutor;

public class FileUtils {
	static private final String GUARD_HOME;
	static private final String GUARD_LOG_DIR;
	static private final String SCRIPTS_DIR;
	static private final String BIN_DIR;
	static private final String TOMCAT_DIR;
	static private final String ETC_DIR;
	static private final String GUARD_DATA_DIR;
	static private final String DYNAMIC_IMAGES_DIR;
	static private final String PORTAL_IMAGES_DIR;
	
	static private final String SEC_COPY_CMD;
	static private final String ZIP_MYSQL_SH;
	static private final String CONSOLE_SOCKET;
	static private final String DYMAMIC_IMAGES_RELATIVE;
	static private final String DB_PD_CMD;
	
	static final int ZIP_BUFFER_SZ = 2048;
	
	static {
		// Directories
		GUARD_HOME         = getEnvDefault("GUARD_HOME",    "/opt/IBM/Guardium/");
		GUARD_LOG_DIR      = getEnvDefault("GUARD_LOG_DIR", GUARD_HOME + "log/");
		SCRIPTS_DIR        = GUARD_HOME  + "scripts/";
		BIN_DIR            = GUARD_HOME  + "bin/";
		TOMCAT_DIR = 	  getEnvDefault("GUARD_TOMCAT_DIR", GUARD_HOME  + "tomcat/");
		ETC_DIR            = getEnvDefault("GUARD_ETC_DIR", GUARD_HOME  + "etc/");
		GUARD_DATA_DIR	   = getEnvDefault("GUARD_DATA_DIR", GUARD_HOME  + "data/");
		
		DYNAMIC_IMAGES_DIR = TOMCAT_DIR  + "webapps/ROOT/dynamic/images/";
		PORTAL_IMAGES_DIR  = TOMCAT_DIR  + "webapps/ROOT/images/";
		
			
		// Specific files
		SEC_COPY_CMD            = SCRIPTS_DIR + "guard_secure_copy.pl";
		ZIP_MYSQL_SH            = BIN_DIR + "guard_archiveTables_wrapper ";
		CONSOLE_SOCKET          = GUARD_HOME  + "run/console.socket";
		DYMAMIC_IMAGES_RELATIVE = "/dynamic/images/";
		DB_PD_CMD = GUARD_HOME + "bin/obstore";
	}
	
	/**
	 * @param env Environment variable
	 * @param defaultValue if env is missing or blank, then use defaultValue
	 * @return slash terminated value
	 */
	static private String getEnvDefault(String env, String defaultValue) {
		String result = System.getenv(env);
		
		if (result == null || result.trim().length() == 0) {
			result = defaultValue;
		}
		else {
			// Make sure there is a trailing slash
			result = result.trim();
			if (result.charAt(result.length()-1) != '/') {
				result += "/";
			}
		}		
		
		return result;
	}
	
	/**
	 * Uses a perl script to do a secure copy to/from a remote host
	 * 
	 * @param localFilepath
	 *           <b>String</b> the fully-specified path to the file (which can be a directory) on the <b>local</b>
	 *           machine. File expressions can be used if this is the source.
	 * @param remoteMachine
	 *           <b>String</b> The name of the remote host.
	 * @param remotePath
	 *           <b>String</b> the fully-specified path to the file (which can be a directory) on the <b>remote</b>
	 *           machine. File expressions can be used if this is the source.
	 * @param remoteUser
	 *           <b>String</b> The user on the remote host
	 * @param remotePassword
	 *           <b>String</b> The user's password on the remote host
	 * @param localToRemote
	 *           <b>boolean</b> If <i>true</i>, copy from the local machine to the remote host
	 * @param recursive
	 *           <b>boolean</b> If <i>true</i>, copy recursively
	 * @throws Exception
	 */
	public static void copyRemote(String localPath, String transferMethod, String remoteMachine, String remotePath, String remoteUser, String remotePassword, List<String> files, boolean localToRemote,
			boolean recursive) throws Exception {
		// -d direction -h host -u user -p password -l local_path -r remote_path -f files_list -m method [-c]
		String command = SEC_COPY_CMD;
		String filesList = files.get(0);
		for (int i = 1; i < files.size(); i++)
			filesList += "," + files.get(i);
		command += (localToRemote) ? " -d put" : " -d get";
		command += " -h " + remoteMachine;
		command += " -u " + remoteUser;

		command += " -p " + remotePassword;
		command += " -l " + localPath;
		command += " -r " + remotePath;
		command += " -f " + filesList;
		command += " -m " + transferMethod;
		if (recursive)
			command += " -c";
		String display_command = command.replaceFirst("-p " + remotePassword, "-p {not shown}");
		StringBuffer stdOut = new StringBuffer();
		StringBuffer stdErr = new StringBuffer();
		boolean status = Utils.executeCommand(command.split(" "), null, null, stdOut, stdErr, null);
		if (!status)
			throw new Exception("Problem doing a remote copy [ " + display_command + "]: " + stdErr.toString());
		else if (stdErr.length() > 0)
			AdHocLogger.logDebug("Warnings doing a remote copy [ " + display_command + "]: " + stdErr.toString(), AdHocLogger.LOG_ERRORS);

	}

	public static void removeRemote(String transferMethod, String remoteMachine, String remotePath, String remoteUser, String remotePassword, String file) throws Exception {
		// -d direction -h host -u user -p password -r remote_path -f files_list -D -m method
		String command = SEC_COPY_CMD;
		command += " -h " + remoteMachine;
		command += " -u " + remoteUser;
		command += " -p " + remotePassword;
		command += " -r " + remotePath;
		command += " -D ";
		command += " -f " + file;
		command += " -m " + transferMethod;
		String display_command = command.replaceFirst("-p " + remotePassword, "-p {not shown}");
		StringBuffer stdOut = new StringBuffer();
		StringBuffer stdErr = new StringBuffer();
		boolean status = Utils.executeCommand(command.split(" "), null, null, stdOut, stdErr, null);
		if (!status)
			throw new Exception("Problem doing a remote copy [ " + display_command + "]: " + stdErr.toString());
		else if (stdErr.length() > 0)
			AdHocLogger.logDebug("Warnings deleting a remote archive [ " + display_command + "]: " + stdErr.toString(), AdHocLogger.LOG_ERRORS);
	}

	/**
	 * Use the uid-modified script to zip or unzip tables under mysql
	 * 
	 * @param option
	 *           <b>Character</b> z- zip; u - unzip (extract and remove);e - extract ; a - append
	 * @param zipfile
	 *           <b>String</b> The zip file name (no preceding subdirectories)
	 * @param db
	 *           <b>String</b> The database name under MySql (<i>e.g.: RETRO</i>)
	 * @param pattern
	 *           <b>String</b> The <i>optional</i> list of files to zip - can include patterns.
	 * @throws Exception
	 */
	public static int zipMysqlTableFiles(char option, String zipfile, String db, String pattern) throws Exception {
		if ("zuea".indexOf(option) < 0)
			throw new Exception("Problem zipmysqltables. invalid option - " + option);
		StringBuffer command = new StringBuffer(ZIP_MYSQL_SH);
		command.append(option);
		command.append(" ").append(zipfile);
		command.append(" ").append(db);
		if (pattern != null && !pattern.equals(""))
			command.append(" ").append(pattern);
		StringBuffer stdOut = new StringBuffer();
		StringBuffer stdErr = new StringBuffer();
		Integer processStatus = new Integer(0);
		try {
			Integer status = Utils.executeCommand(command.toString().split(" "), null, null, stdOut, stdErr, null, processStatus);
			if (status.intValue() != 0)
				throw new Exception("Problem zipmysqltables. retStat= " + status + " [ " + command + "]:" + stdErr.toString());
			else if (stdErr.length() > 0)
				AdHocLogger.logDebug("Warnings doing a mysql tables zip [ " + command + "]: " + stdErr.toString(), AdHocLogger.LOG_ERRORS);
            return 0;
		} catch (Exception e) {
			AdHocLogger.logException(e);
            return 1;
		}

	}

	/**
	 * Opens the file defined by the path name, then reads its entirety, line-by-line, into an ArrayList for subsequent
	 * processing.
	 * 
	 * @param pathname
	 *           <b>String</b> Full file path
	 * @return <b>ArrayList</b> of <b>String</b>s
	 * @throws Exception
	 */
	public static ArrayList<String> readFileLines(String pathname) throws Exception {
		ArrayList<String> lines = new ArrayList<String>();
		File file = new File(pathname);
		if (!(file.isFile() && file.canRead()))
			throw new Exception();
		InputStreamReader fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
		BufferedReader reader = new BufferedReader(fileReader);
		String line = null;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}

		return lines;
	}

	/**
	 * Given the input path to a directory, return a list of filepaths for files whose names match the input file
	 * expression pattern.
	 * 
	 * @param dirPath
	 *           <b>String</b> the path to the Directory
	 * @param patternMatch
	 *           <b>String</b> a file matching expression
	 * @return <b>ArrayList</b> of filepaths
	 */
	public static ArrayList<String> getFilesList(String dirPath, String patternMatch) {
		ArrayList<String> files = new ArrayList<String>();
		if (patternMatch == null || patternMatch.equals(""))
			patternMatch = "*";
		File directory = new File(dirPath);
		FileFilter filter = new PatternFileFilter(patternMatch);
		File[] filesList = null;
		try {
			filesList = directory.listFiles(filter);
		} catch (RuntimeException e) {
			throw new RuntimeException("Unable to get files list for " + dirPath + "." + e.getMessage());
		}
		if (filesList != null) {
			for (int i = 0; i < filesList.length; i++) {
				files.add(filesList[i].getPath());
			}
		}
		return files;
	}

	/**
	 * Given a file (and optional directory), encode that file
	 * 
	 * @param directory
	 *           <b>String</b> (optional) directory path
	 * @param fileName
	 *           <b>String</b> filename (or complete file path)
	 * @param cmdLineInput
	 *           <b>String</b> the password used for encoding
	 * @return <b>String</b> The filepath of the encoded file
	 * @throws Exception
	 */
	public static String encryptFile(String cmdDirectory, String destDirectory, String fileName, String cmdLineInput) throws Exception {
		String newFileName = fileName + ".enc";
		if (destDirectory != null && !destDirectory.equals(""))
			newFileName = destDirectory + newFileName;
		
		// 2011-12-01 sbuschman 27400
		String command = getScript("guardfilecrypt") + " E " + fileName + " " + newFileName; 
		String[] commandArr = command.split(" ");
		StringBuffer stdErr = new StringBuffer();
		File dir = null;
		
		if (cmdDirectory != null && !cmdDirectory.equals("")) {
			dir = new File(cmdDirectory);
		}
		
		if (cmdLineInput == null || !cmdLineInput.endsWith("\n"))
			cmdLineInput += "\n";
		boolean status = Utils.executeCommand(commandArr, null, dir, new StringBuffer(), stdErr, cmdLineInput);
		if (!status)
			throw new Exception("Problem encrypting " + fileName + " " + stdErr.toString());
		else if (stdErr.length() > 0)
			AdHocLogger.logDebug("Warnings when  encrypting " + fileName + ": " + stdErr.toString(), AdHocLogger.LOG_ERRORS);

		return newFileName;
	}

	public static void decryptFile(String directory, String encryptedFileName, String decryptedFileName, String cmdLineInput) throws Exception {
		// echo $pass | /usr/bin/gpg --yes --no-tty --quiet --passphrase-fd 0 --output
		// /var/importdir/export_user_tables.txt -d /var/importdir/export_user_tables.txt.enc

		// 2011-12-01 sbuschman 27400
		String dfn = decryptedFileName + ".dec";
		String command = getScript("guardfilecrypt") + " D " +  encryptedFileName + " " + dfn; 
		String[] commandArr = command.split(" ");
		StringBuffer stdOut = new StringBuffer();
		StringBuffer stdErr = new StringBuffer();
		File dir = null;
		if (directory != null && !directory.equals("")) {
			dir = new File(directory);
		}
		if (!cmdLineInput.endsWith("\n"))
			cmdLineInput += "\n";
		boolean status = Utils.executeCommand(commandArr, null, dir, stdOut, stdErr, cmdLineInput);
		File decryptFile = new File(dfn);
		if (!status || !decryptFile.exists())
			throw new Exception("Problem decrypting " + encryptedFileName + " " + stdErr.toString().replaceAll("\n", " - "));
		else if (stdErr.length() > 0)
			AdHocLogger.logDebug("Warnings when decrypting " + encryptedFileName + ": " + stdErr.toString().replaceAll("\n", " - "), AdHocLogger.LOG_ERRORS);
		decryptFile.renameTo(new File(decryptedFileName));
	}

	/**
	 * Just a convenient place to delete a file, in case we want to later add tests before deletion
	 * 
	 * @param filepath
	 *           <b>String</b> the file path
	 * @return <b>boolean</b> <i>true</i> if successfully deleted
	 */
	public static boolean deleteFile(String filepath) {
		File delFile = new File(filepath);
		return delFile.delete();
	}

	/**
	 * recursively deletes a directory ad all it's content.
	 * 
	 * @param directory
	 */
	public static void rmdir(File directory) {
		if (!directory.exists())
			return;
		if (!directory.isDirectory())
			throw new InvalidParameterException(directory.getAbsolutePath() + " is not a directory");
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory())
				rmdir(file);
			else
				file.delete();
		}
		directory.delete();
	}

	/**
	 * Given the input filepath, create a file from the input set of strings
	 * 
	 * @param fileName
	 *           <b>String</b> the file path
	 * @param strings
	 *           <b>ArrayList</b> of <b>String</b>s to be in the file
	 * @return <b>boolean</b> <i>true</i>: successful creation
	 */
	public static boolean createFileFromStrings(String fileName, ArrayList strings) {
		boolean retVal = true;
		File outFile = new File(fileName);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outFile));

			for (int i = 0; i < strings.size(); i++) {
				out.write((String) strings.get(i));
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			AdHocLogger.logException(e);
			retVal = false;
		}
		return retVal;
	}

	/**
	 * Create a deflated zip file, given the full pathname of the zipfile, and an array of <b>String</b>s containing the
	 * full pathnames of the files to be zipped Returns the checksum
	 * 
	 * @param zipFileName
	 *           <b>String</b> containing the full pathname of the zipfile
	 * @param fileNames
	 *           <b>String[]</b> containing the full pathnames of the files
	 * @return <b>long</b> the checksum of the resulting file
	 * @throws Exception
	 */
	public static long createZipFileWithChecksum(String zipFileName, String[] fileNames) throws Exception {
		BufferedInputStream origin = null;
		CheckedOutputStream checksum = null;
		ZipOutputStream out;
		try {
			FileOutputStream dest = new FileOutputStream(zipFileName);
			checksum = new CheckedOutputStream(dest, new Adler32());
			out = new ZipOutputStream(new BufferedOutputStream(checksum));
			// out = new ZipOutputStream(new
			// BufferedOutputStream(dest));
		} catch (FileNotFoundException e1) {
			throw new Exception("Unable to open zip file " + zipFileName + ": " + e1.getMessage());
		}
		out.setMethod(ZipOutputStream.DEFLATED);
		byte data[] = new byte[ZIP_BUFFER_SZ];
		for (int i = 0; i < fileNames.length; i++) {
			FileInputStream fi = new FileInputStream(fileNames[i]);
			origin = new BufferedInputStream(fi, ZIP_BUFFER_SZ);
			ZipEntry entry = new ZipEntry(fileNames[i].substring(fileNames[i].lastIndexOf(File.separator) + 1));
			try {
				out.putNextEntry(entry);
			} catch (IOException e) {
				throw new Exception("Unable to create zip entry for " + fileNames[i] + ": " + e.getMessage());
			}
			int count;
			try {
				while ((count = origin.read(data, 0, ZIP_BUFFER_SZ)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			} catch (IOException e2) {
				throw new Exception("problem reading/zipping " + fileNames[i] + ": " + e2.getMessage());
			}
		}
		out.close();
		return checksum.getChecksum().getValue();
	}

	/**
	 * Create a deflated zip file, given the full pathname of the zipfile, and an array of <b>String</b>s containing the
	 * full pathnames of the files to be zipped Returns the checksum
	 * 
	 * @param zipFileName
	 *           <b>String</b> containing the full pathname of the zipfile
	 * @param path
	 * @param fileNames
	 *           <b>String[]</b> containing the full pathnames of the files
	 * 
	 * @throws Exception
	 */
	public static void createZipFile(String zipFileName, String path, String[] fileNames) throws Exception {
		BufferedInputStream origin = null;

		ZipOutputStream out;
		try {
			FileOutputStream dest = new FileOutputStream(zipFileName);

			out = new ZipOutputStream(new BufferedOutputStream(dest));
		} catch (FileNotFoundException e1) {
			throw new Exception("Unable to open zip file " + zipFileName + ": " + e1.getMessage());
		}
		out.setMethod(ZipOutputStream.DEFLATED);
		byte data[] = new byte[ZIP_BUFFER_SZ];
		for (int i = 0; i < fileNames.length; i++) {
			FileInputStream fi = new FileInputStream(path + File.separator + fileNames[i]);
			origin = new BufferedInputStream(fi, ZIP_BUFFER_SZ);
			ZipEntry entry = new ZipEntry(zipFileName.substring(zipFileName.lastIndexOf(File.separator), zipFileName.lastIndexOf(".")) + File.separator + fileNames[i]);
			try {
				out.putNextEntry(entry);
			} catch (IOException e) {
				throw new Exception("Unable to create zip entry for " + path + File.separator + fileNames[i] + ": " + e.getMessage());
			}
			int count;
			try {
				while ((count = origin.read(data, 0, ZIP_BUFFER_SZ)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			} catch (IOException e2) {
				throw new Exception("problem reading/zipping " + path + File.separator + fileNames[i] + ": " + e2.getMessage());
			}
		}
		out.close();
	}

	public static boolean isFileADirectory(String pathname) {
		File file = new File(pathname);
		if (file != null && file.isDirectory())
			return true;
		else
			return false;
	}

	/**
	 * Unzip the files that match the input pattern from the input zip file.
	 * 
	 * @param zipFileName
	 *           <b>String</b> The path to the zip file
	 * @param pattern
	 *           <b>String</b> The file pattern expression to filter on
	 * @param wholePath
	 *           <b>boolean</b> If true, filter on the entire path name, rather than just the file name
	 * @param newPath
	 *           <b>String</b> Where to unzip the file to
	 * @return <b>ArrayList</b> of <b>String</b>s: the names of the unzipped files
	 * @throws Exception
	 */
	public static ArrayList<String> unzipFiles(String zipFileName, String pattern, boolean wholePath, String newPath) throws Exception {
		ArrayList<String> unzippedFiles = new ArrayList<String>();
		String outPath;
		String outFile;
		if (pattern == null || pattern.equals(""))
			pattern = "*";

		FileFilter filter = null;
		if (wholePath)
			filter = new PatternPathFilter(pattern);
		else
			filter = new PatternFileFilter(pattern);

		BufferedOutputStream dest = null;
		BufferedInputStream is = null;
		ZipEntry entry;
		ZipFile zipfile;
		try {
			zipfile = new ZipFile(zipFileName);
		} catch (IOException e1) {
			throw new Exception("Unable to open Zip file:  " + e1.getMessage());
		}
		Enumeration e = zipfile.entries();
		while (e.hasMoreElements()) {
			entry = (ZipEntry) e.nextElement();
			if (filter.accept(new File(entry.getName()))) {
				AdHocLogger.logDebug("Entries: " + entry.getName(), AdHocLogger.LOG_DEBUG);
				outFile = entry.getName();
				if (!newPath.endsWith("/"))
					newPath = newPath + "/";
				outFile = newPath + outFile;
				outPath = outFile.substring(0,outFile.lastIndexOf('/') + 1);
				File f = new File(outPath);
					if (f.exists()) {
						if (!f.isDirectory())
							//throw new InvalidArgumentException("bad path");
							throw new Exception("bad path");
					}
					else
						f.mkdirs();
				try {
					is = new BufferedInputStream(zipfile.getInputStream(entry));
					int count;
					byte data[] = new byte[ZIP_BUFFER_SZ];
					FileOutputStream fos = new FileOutputStream(outFile);
					dest = new BufferedOutputStream(fos, ZIP_BUFFER_SZ);
					while ((count = is.read(data, 0, ZIP_BUFFER_SZ)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
					is.close();
					unzippedFiles.add(outFile);
				} catch (Exception ex) {
					throw new Exception("Unable to zip-extract " + entry.getName() + " to " + outFile + ": " + ex.getMessage());
				}
			}
		}
		return unzippedFiles;
	}

	public static void copyFile(File in, File out) throws Exception {
		FileInputStream fis = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		byte[] buf = new byte[1024];
		int i = 0;
		while ((i = fis.read(buf)) != -1) {
			fos.write(buf, 0, i);
		}
		fis.close();
		fos.close();
	}

	/**
	 * Provides a FileFilter that uses Regular Expression pattern matching to restrict the set of files in the output
	 * Auto-converts wildcard "*" to ".*" to match via patterns.
	 */
	static class PatternFileFilter implements FileFilter {
		Pattern pattern;

		public PatternFileFilter(String pattern) {
			//
			pattern = pattern.replaceAll("\\*", ".*");
			this.pattern = Pattern.compile(pattern);
		}

		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) {
				return true;
			}
			else {
				return pattern.matcher(pathname.getName()).matches();
			}
		}
	}

	static class PatternPathFilter implements FileFilter {
		Pattern pattern;

		public PatternPathFilter(String pattern) {
			//
			// pattern = pattern.replaceAll("\\*", ".*");
			this.pattern = Pattern.compile(pattern);
		}

		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) {
				return true;
			}
			else {
				return pattern.matcher(pathname.getPath()).find();
			}
		}
	}

	/**
	 * uses du -sm to determine the disk size used by specified subdirectory. Note calling du in this way sometimes
	 * retunes an empty buffer - to work around this I call it in a loop untill successfull TODO--RUI I copied it from
	 * AggregationUtils.java, Guy asked me to move it; when no permission the command in the other place does not throw
	 * an exception
	 */
	public static long getDiskUsage(String subdir) throws Exception {
		boolean success = false;
		long size = -1;
		int MAX = 4;
		int i = 0;
		while (i < MAX && !success) {
			StringBuffer sb = new StringBuffer();
			String[] command = { "/usr/bin/du", "-sm", subdir };
			StringBuffer pError = new StringBuffer();

			try {
				int status = Utils.execCommand(command, null, null, sb, pError, null);
				if (status != 0) {
					AdHocLogger.logDebug("exit:" + status + " " + pError, AdHocLogger.LOG_DEBUG);
					i++;
					continue;
				}
			} catch (IOException e) {

				AdHocLogger.logException(e);
			} catch (InterruptedException e) {

				AdHocLogger.logException(e);
			}
			// AdHocLogger.logDebug(sb.toString(),AdHocLogger.LOG_DEBUG);

			try {
				size = Long.parseLong(sb.toString().split("[ ,\t]")[0]);
				success = true;
			} catch (NumberFormatException e) {
			}
			i++;
		}
		if (size == -1)
			throw new Exception("Can't get db size.");
		return size;
	}

	public static void main(String[] args) {
		try {
			long l = getDiskUsage("/home/guy/download/ggg");
			System.out.println(l);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static ArrayList<File> getFileList(String dirPath, String patternMatch) {
		ArrayList<File> files = new ArrayList<File>();
		if (patternMatch == null || patternMatch.equals(""))
			patternMatch = "*";
		File directory = new File(dirPath);
		FileFilter filter = new PatternFileFilter(patternMatch);
		File[] filesList = null;
		try {
			filesList = directory.listFiles(filter);
		} catch (RuntimeException e) {
			throw new RuntimeException("Unable to get files list for " + dirPath + "." + e.getMessage());
		}
		if (filesList != null) {
			for (int i = 0; i < filesList.length; i++) {
				if (filesList[i].isFile())
					files.add(filesList[i]);
			}
		}
		return files;
	}

	/**
	 * returns the last tailSize bytes of a file removes the first 'broken' line
	 * 
	 * @param file
	 *           name
	 * @param tailSize
	 */
	public static String tail(String fileName, long tailSize) throws IOException {
		char[] buffer = new char[512];
		int size = 0;
		StringBuffer buf = new StringBuffer((int) tailSize);
		File fh = new File(fileName);
		long fileSize = fh.length();
		BufferedReader sl = new BufferedReader(new FileReader(fh));
		if (fileSize > tailSize)
			sl.skip(fileSize - tailSize);
		while (((size = sl.read(buffer, 0, 512)) != -1)) {
			if (size > 0)
				buf.append(buffer, 0, size);
		}
		return buf.substring(buf.indexOf("\n"));
	}

	/**
	 * Checks existence of file specified by pathName
	 * 
	 * @param pathName
	 * @return If file exists, it will return true; otherwise, it returns false.
	 */
	public static boolean fileExists(String pathName) {
		boolean exists = true;
		if (Check.isEmpty(pathName))
			return exists;
		File file = new File(pathName);
		exists = file.exists();
		return exists;

	}

	/**
	 * Fetch the entire contents of a text file, and return it in a String. This style of implementation does not throw
	 * Exceptions to the caller.
	 * 
	 * @param aFile
	 *           is a file which already exists and can be read.
	 */
	static public String getFileContent(String pathName) throws Exception {
		StringBuffer contents = new StringBuffer();

		if (Check.isEmpty(pathName))
			return new String();
		File file = new File(pathName);
		if (!file.exists())
			throw new Exception(SayAppRes.say("file.notExists", pathName));
		if (!file.isFile())
			throw new Exception(SayAppRes.say("file.notFile", pathName));
		if (!file.canRead())
			throw new Exception(SayAppRes.say("file.cannotRead", pathName));

		/*
		 * Charlies // Use buffering, reading one line at a time // FileReader always assumes default encoding is OK!
		 * BufferedReader reader = new BufferedReader(new FileReader(aFile));
		 */
		InputStreamReader fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
		BufferedReader reader = new BufferedReader(fileReader);

		/*
		 * un comment when testing BufferedWriter bfw = null; String newFileName ="/tmp/readTest.pkg";
		 */
		try {
			/*
			 * un comment when testing FileOutputStream fos = new FileOutputStream(newFileName); bfw = new
			 * BufferedWriter(new OutputStreamWriter(fos,"UTF8"));
			 */
			String line = null; // not declared within while loop

			/*
			 * readLine is a bit quirky: -- It returns the content of a line MINUS the newline. -- It returns null only for
			 * the END of the stream. -- It returns an empty String if two newlines appear in a row.
			 */
			while ((line = reader.readLine()) != null) {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
				/*
				 * un comment when testing bfw.write(line); bfw.write(System.getProperty("line.separator"));
				 */
			}
		} finally {
			reader.close();
			// un comment when testing bfw.close();
		}

		return contents.toString();
	}

	public static boolean isFileLocked(String fileName) throws IllegalArgumentException, ClosedChannelException, OverlappingFileLockException, IOException, Exception {
		File file = null;
		FileChannel channel = null;
		try {
			file = new File(fileName);
			if (!file.exists()) {
				throw new Exception(SayAppRes.say("file.notExists", fileName));
			}
			channel = new RandomAccessFile(file, "rw").getChannel();
			FileLock lock = channel.tryLock();
			return (lock == null);
		} finally {
			if (channel != null)
				channel.close();
		}
	}

	static public String getTomcatRoot(String path) {
		return getPath(TOMCAT_DIR, path);
	}

	public static String getGuardDataDir() {
		return GUARD_DATA_DIR;
	}
	
	public static String getGuardDataDir(String path) {
		return getPath(GUARD_DATA_DIR, path);
	}

	static public String getGuardHome() {
		return getPath(GUARD_HOME, null);
	}
	
	static public String getGuardHome(String path) {
		return getPath(GUARD_HOME, path);
	}

	static public String getScript(String path) {
		return getPath(SCRIPTS_DIR, path);
	}
	
	static public String getBin(String path) {
		return getPath(BIN_DIR, path);
	}
	
	static public String getEtc(String path) {
		return getPath(ETC_DIR, path);
	}
	
	static public String getEtc() {
		return ETC_DIR;
	}
	
	static public String getLog(String path) {
		return getPath(GUARD_LOG_DIR, path);
	}
	
	static private String getPath(String directory, String path) {
		String result = directory != null ? directory : "";
		
		if (path != null && path.length() > 0) {
			result += path;
		}
		
		return result;
	}
	
	static public String getConsoleSocket() {
		return CONSOLE_SOCKET;
	}
	
	static public void sendBroadCast(String group, int port, String msg) throws IOException {
        DatagramSocket socket = new DatagramSocket();

        socket.setBroadcast(true);
        DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), InetAddress.getByName(group), port);
        socket.send(packet);
        
        socket.close();		
	}
	
	static public void sendMagenBroadCast(String msg) throws IOException {
		sendBroadCast("127.255.255.255", 20002, msg);
	}
	
	static public String getDynamicImagesDir() {
		return DYNAMIC_IMAGES_DIR;
	}
	
	static public String getDynamicImages(String path) {
		return getPath(DYNAMIC_IMAGES_DIR, path);
	}

	static public String getDynamicImagesRelative() {
		return DYMAMIC_IMAGES_RELATIVE;
	}
	
	static public String getPortalImagesDir() {
		return PORTAL_IMAGES_DIR;
	}
	
	static public String getDumpDir() {
		return GUARD_DATA_DIR+"dump/";
	}
	
	static public String getImportDir() {
		return GUARD_DATA_DIR+"importdir/";
	}
	
	/*
    public static int guardFileTransfer (String fileName, String ip, String gId, String exportDestDir, String exportDestUser, String exportDestPassword, int exportDestPort ) throws IOException, InterruptedException {	
    	String copyCommand = SayAppRes.say("copyUserTablesCommand");
		copyCommand = copyCommand.replaceAll("~dumpFilename~", fileName);
		copyCommand = copyCommand.replaceAll("~ipAddr~",ip);
		copyCommand = copyCommand.replaceAll("~GID~", gId);
		copyCommand = copyCommand.replaceAll("~U~", exportDestUser);
		copyCommand = copyCommand.replaceAll("~D~", exportDestDir);
		copyCommand = copyCommand.replaceAll("~port~", "" + exportDestPort);
		AdHocLogger.logDebug(copyCommand,AdHocLogger.LOG_DEBUG);
		copyCommand = copyCommand.replaceAll("~P~", escapePasswordString(exportDestPassword));
		CommandExecutor executor = new CommandExecutor(copyCommand);
		int status = executor.exec() == 0 ? 1 : 0;
		return status;
    }
    */
	
	private static String escapePasswordString(String pass)
	{
		StringBuffer sb =  new StringBuffer(pass);
        if(sb.length()>0)
        {          
            int from = 0;
            while(true)
            {
                int index = sb.indexOf("$",from);
                if(index==-1) {
                    break;
                } else {
                    sb.insert(index,'\\');
                    from = index+2;
                }
            }
        }
	    
	    return sb.toString();
	}
	
	public static String getPd() throws IOException, InterruptedException {
		String command[] = new String[] {DB_PD_CMD, "1148546370"};
		CommandExecutor commandExecutor = new CommandExecutor(command);
		StringBuilder result = new StringBuilder();
		StringBuilder stdErr = new StringBuilder();
		try {
			int exitCode = commandExecutor.exec(result, stdErr);
			if(exitCode != 0 || stdErr.length() > 0) {
				String str = commandExecutor + " exited with code " + exitCode + "\nstderr: " + stdErr + "\nstdout: " + result;
				System.err.println(str);
				throw new IOException(str);
			}
		} catch(InterruptedException | IOException e) {
			e.printStackTrace();
			System.err.println(commandExecutor + " exception " + e + "\nstderr: " + stdErr + "\nstdout: " + result);
			throw e;
		}
		return result.toString();
	}
}
    
