/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/*  © Copyright IBM Corp. 2018, 2019                                   */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Executes commands.
 * 
 * If you supply a String command line, it uses CommandLinePattern to parse the string into its args.
 * 
 * If you supply a pre-parsed command line, it will replace environment variables referenced in the command line.
 * 
 * @author sfoley
 *
 */
public class CommandExecutor
{
    private static CommandLinePattern commandLinePattern = new CommandLinePattern();
    private String envp[];
    private File dir;
    private String charSet;
    private final String commandLine;
    private final String parsedCommand[];

    public CommandExecutor(String command[])
    {
        this.parsedCommand = command.clone();
        for (int i = 0; i < command.length; i++)
        {
            parsedCommand[i] = commandLinePattern.replaceEnvVariables(command[i]);
        }
        StringBuilder builder = new StringBuilder();
        for (String s : command)
        {
            s.replaceAll("'", "\\'");
            builder.append("'").append(s).append("' ");
        }
        commandLine = builder.toString();
    }

    public List<String> getParsedCommand()
    {
        return Collections.unmodifiableList(Arrays.asList(parsedCommand));
    }

    public void setEnvironment(String envp[])
    {
        synchronized (parsedCommand)
        {
            this.envp = envp;
        }
    }

    public void setWorkingDir(File dir)
    {
        synchronized (parsedCommand)
        {
            this.dir = dir;
        }
    }

    public void setCharSet(String charSet)
    {
        this.charSet = charSet;
    }

    /**
     * runs this command asynchronously
     * 
     * @return the process object for the command
     * @throws IOException If the command is not found 
     */
    public Process runCommand() throws IOException
    {
        synchronized (parsedCommand)
        {
            return Runtime.getRuntime().exec(parsedCommand, envp, dir);
        }
    }

    public int exec() throws IOException, InterruptedException
    {
        return handleProcess(runCommand(), null, null, null);
    }

    public boolean execBool(Appendable stdOut) throws IOException, InterruptedException
    {
        return exec(stdOut) == 0;
    }

    public boolean execBool(Appendable stdOut, Appendable stdErr) throws IOException, InterruptedException
    {
        return exec(stdOut, stdErr) == 0;
    }

    public int exec(Appendable stdOut) throws IOException, InterruptedException
    {
        return handleProcess(runCommand(), stdOut, null, null);
    }

    public int exec(Appendable stdOut, Appendable stdErr) throws IOException, InterruptedException
    {
        return handleProcess(runCommand(), stdOut, stdErr, null);
    }

    public int exec(Appendable stdOut, Appendable stdErr, CharSequence stdIn) throws IOException, InterruptedException
    {
        return handleProcess(runCommand(), stdOut, stdErr, stdIn);
    }

    public int handleProcess(Process process, Appendable stdOut, Appendable stdErr) throws IOException,
            InterruptedException
    {
        return handleProcess(process, stdOut, stdErr, null);
    }

    private static StreamReader createReader(InputStream stream, Appendable sink, String charSet)
            throws UnsupportedEncodingException
    {
        if (charSet == null)
        {
            return new StreamReader(stream, sink);
        }
        return new StreamReader(stream, sink, charSet);
    }

    private static OutputStreamWriter createWriter(OutputStream stream, String charSet)
            throws UnsupportedEncodingException
    {
        if (charSet == null)
        {
            return new OutputStreamWriter(stream);
        }
        return new OutputStreamWriter(stream, charSet);
    }

    public int handleProcess(Process process, Appendable stdOut, Appendable stdErr, CharSequence stdIn)
            throws IOException, InterruptedException
    {
        String charSet = this.charSet;// read it just once to make this thread-safe
        StreamReader stdOutReader = stdOut == null ? null : createReader(process.getInputStream(), stdOut, charSet);
        StreamReader stdErrReader = stdErr == null ? null : createReader(process.getErrorStream(), stdErr, charSet);
        OutputStreamWriter writer = stdIn == null ? null : createWriter(process.getOutputStream(), charSet);
        return handleProcess(process, stdOutReader, stdErrReader, stdIn, writer);
    }

    private int handleProcess(Process process, StreamReader stdOutReader, StreamReader stdErrReader, CharSequence stdIn,
            OutputStreamWriter writer) throws IOException, InterruptedException, UnsupportedEncodingException
    {
        if (writer != null)
        {
            // note: here we will get an exception at times for processes that don't read from stdin,
            // as the process may be done already
            // but of course, users should not be tryinh to supply stdin to processes that don't want it
            writer.write(stdIn.toString());
            writer.flush();
            writer.close();
        }
        if (stdOutReader != null)
        {
            stdOutReader.readAll();
        }
        if (stdErrReader != null)
        {
            stdErrReader.readAll();
        }
        return process.waitFor();
    }

    @Override
    public String toString()
    {
        return commandLine;
    }
}
