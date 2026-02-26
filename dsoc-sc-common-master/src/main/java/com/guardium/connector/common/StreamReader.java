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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class StreamReader
{
    private final Appendable output;
    private final InputStreamReader reader;
    boolean done;
    private BufferCharSequence buffer;

    private static class BufferCharSequence implements CharSequence
    {
        final char buffer[];
        int len;

        BufferCharSequence(int capacity)
        {
            buffer = new char[capacity];
        }

        @Override
        public int length()
        {
            return len < 0 ? 0 : len;
        }

        @Override
        public char charAt(int index)
        {
            return buffer[index];
        }

        @Override
        public CharSequence subSequence(int start, int end)
        {
            return new String(buffer, start, end - start);
        }

        @Override
        public String toString()
        {
            return new String(buffer, 0, len);
        }
    }

    public StreamReader(InputStream stream)
    {
        this(stream, new StringBuilder());
    }

    public StreamReader(InputStream stream, Appendable output)
    {
        reader = new InputStreamReader(stream);
        this.output = output;
        buffer = new BufferCharSequence(1024);
    }

    public StreamReader(InputStream stream, Appendable output, String charsetName) throws UnsupportedEncodingException
    {
        reader = new InputStreamReader(stream, charsetName);
        this.output = output;
        buffer = new BufferCharSequence(1024);
    }

    private void read() throws IOException
    {
        buffer.len = reader.read(buffer.buffer, 0, buffer.buffer.length);
        if (buffer.len < 0)
        {
            done = true;
            close();
        }
        else
            synchronized (output)
            {
                output.append(buffer);
            }
    }

    public void close()
    {
        try
        {
            done = true;
            reader.close();
        }
        catch (IOException e)
        {
        }
    }

    /**
     * Reads more bytes
     * 
     * @param block
     *            whether to block when no bytes are available
     * @return
     */
    boolean readMore(boolean block)
    {
        try
        {
            if (!done)
            {
                synchronized (buffer)
                {
                    if (!done && (block || reader.ready()))
                    {
                        read();
                    }
                }
            }
        }
        catch (IOException e)
        {
            done = true;
            close();
        }
        return !done;
    }

    void readAll()
    {
        try
        {
            while (!done)
            {
                synchronized (buffer)
                {
                    if (!done)
                    {
                        read();
                    }
                }
            }
        }
        catch (IOException e)
        {
            done = true;
            close();
        }
    }

    String getOutput()
    {
        synchronized (output)
        {
            return output.toString();
        }
    }
}
