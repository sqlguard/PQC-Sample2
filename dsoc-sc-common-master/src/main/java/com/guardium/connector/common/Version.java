/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/* © Copyright IBM Corp. 2018, 2019                                  */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common;

import com.guardium.connector.common.exceptions.InvalidVersionException;

public class Version implements Comparable<Version>
{
    private int major = 0;
    private int minor = 0;
    private int build = 0;
    String buildType;

    public Version()
    {
    }

    public Version(String version) throws InvalidVersionException
    {
        String[] parts = version.split("\\.", 4);
        if (parts.length < 2)
        {
            throw new InvalidVersionException();
        }

        try
        {
            major = Integer.parseInt(parts[0]);
            minor = Integer.parseInt(parts[1]);
            build = Integer.parseInt(parts[2]);
        }
        catch (NumberFormatException e)
        {
            throw new InvalidVersionException("invalid.version", e.getMessage());
        }

        if (parts.length == 4)
        {
            buildType = parts[3];
        }
    }

    @Override
    public int compareTo(Version o)
    {
        if (null == o)
        {
            // for the purpose of this class, it is illegal to try to compare a given version to null,
            // so, we check for that case explicitly and throw
            throw new NullPointerException();
        };
        
        if (this == o || this.equals(o))
        {
            return 0;
        }

        // we have already tested for equality
        if (this.major != o.major)
        {
            return Integer.compare(this.major, o.major);
        }
        else if (this.minor != o.minor)
        {
            return Integer.compare(this.minor, o.minor);
        }
        else if (this.build != o.build)
        {
            return Integer.compare(this.build, o.build);
        }
        else if ((null != this.buildType && null != o.buildType)
            && !this.buildType.equals(o.buildType))
        {
            return this.buildType.compareTo(o.buildType);
        }
        else if (null != this.buildType && null == o.buildType)
        {
            // if either build type is null, use empty string for comparison
            return this.buildType.compareTo("");
        }
        else if (null == this.buildType && null != o.buildType)
        {
            return new String("").compareTo(o.buildType);
        }

        return 0;
    }

    @Override
    public boolean equals(Object o)
    {
        if (null == o)
        {
            return false;
        }

        if (this == o)
        {
            return true;
        }

        if (!Version.class.isAssignableFrom(o.getClass()))
        {
            return false;
        }

        Version other = (Version) o;
        if (this.major != other.major)
        {
            return false;
        }

        if (this.minor != other.minor)
        {
            return false;
        }

        if (this.build != other.build)
        {
            return false;
        }

        if (null == this.buildType && null == other.buildType)
        {
            return true;
        }
        
        if (null == this.buildType && null != other.buildType)
        {
            return false;
        }
        
        if (null != this.buildType && null == other.buildType)
        {
            return false;
        }

        if (!this.buildType.equals(other.buildType))
        {
            return false;
        }

        return true;
    }

    public int getMajor()
    {
        return major;
    }

    public void setMajor(int major)
    {
        this.major = major;
    }

    public int getMinor()
    {
        return minor;
    }

    public void setMinor(int minor)
    {
        this.minor = minor;
    }

    public int getBuild()
    {
        return build;
    }

    public void setBuild(int build)
    {
        this.build = build;
    }

    public String getBuildType()
    {
        return buildType;
    }

    public void setBuildType(String buildType)
    {
        this.buildType = buildType;
    }

    @Override
    public String toString()
    {
        String version = new String((new Integer(major) + "." + new Integer(minor) + "." + new Integer(build)));
        if (null != buildType && !buildType.isEmpty())
        {
            version += "." + buildType;
        }
        return version;
    }
}
