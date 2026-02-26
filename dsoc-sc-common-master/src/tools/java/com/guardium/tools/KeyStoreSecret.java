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

package com.guardium.tools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;

import com.guardium.connector.common.crypto.Vault;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

/**
 * Note well: This class must NOT get shipped with the Secure Connector! It is for development use only!
 * 
 * This program can be used by a developer working on the secure-connector or one of it's components. It is used to
 * decrypt the keystore password which is kept in a file called journal1.bin (by default). This program can by run from
 * Eclipse or the command line by doing:
 * 
 * ./gradlew getKsSecret -Parg="--dir <relative path to directory containing entropy.bin and journal1.bin>"
 * 
 * @author abecher, jbisbal
 *
 */
public class KeyStoreSecret
{
    public static void main(String[] args)
    {
        DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        ArgumentBuilder aBuilder = new ArgumentBuilder();
        GroupBuilder gBuilder = new GroupBuilder();

        Argument pathArgument = aBuilder
                .withName("path")
                .withMinimum(1)
                .withMaximum(1)
                .create();

        Option dirOption = oBuilder
                .withLongName("dir")
                .withShortName("d")
                .withDescription("Path relative to . where the entropy and keystore secret files are found")
                .withArgument(pathArgument)
                .create();

        Argument fileArgument = aBuilder
                .withName("file")
                .withMinimum(1)
                .withMaximum(1)
                .create();

        Option entropyFileOption = oBuilder
                .withLongName("entropy-file")
                .withShortName("ef")
                .withDescription("Name of the entropy file")
                .withArgument(fileArgument)
                .create();

        Option secretFileOption = oBuilder
                .withLongName("secret-file")
                .withShortName("sf")
                .withDescription("Name of the file containting the keystore secret")
                .withArgument(fileArgument)
                .create();

        Option quietOption = oBuilder
                .withLongName("verbose")
                .withShortName("v")
                .withDescription("Print additional information")
                .create();

        Group options = gBuilder
                .withOption(dirOption)
                .withOption(entropyFileOption)
                .withOption(secretFileOption)
                .withOption(quietOption)
                .create();

        // configure a HelpFormatter
        HelpFormatter hf = new HelpFormatter();

        // configure a parser
        Parser p = new Parser();
        p.setGroup(options);
        p.setHelpFormatter(hf);
        p.setHelpTrigger("--help");
        CommandLine cl = p.parseAndHelp(args);

        // abort application if no CommandLine was parsed
        if (cl == null)
        {
            System.exit(-1);
        }

        if (cl.hasOption(dirOption))
        {
            if (null == cl.getValue(dirOption))
            {
                hf.printHelp();
                return;
            }

            Path path = Paths.get(cl.getValue(dirOption).toString());
            if (path.endsWith("db"))
            {
                path = path.getParent();
            }

            SecureConnectorDefaultProperties.setHomeDir(path);
        }

        // keystore uses same entropy file as DbSecret
        Path eFile = SecureConnectorDefaultProperties.getEntropyFileName();
        if (cl.hasOption("--entropy-file"))
        {
            eFile = Paths.get((String) cl.getValue("--entropy-file"));
            SecureConnectorDefaultProperties.setEntropyFileName(eFile);
        }

        Path e = SecureConnectorDefaultProperties.getDBDir().resolve(eFile);
        if (!Files.isReadable(e))
        {
            System.out.println("Cannot read entropy file " + e);
            System.exit(-1);
        }

        Path sFile = SecureConnectorDefaultProperties.getKSVaultFileName();
        if (cl.hasOption("--secret-file"))
        {
            sFile = Paths.get((String) cl.getValue("--secret-file"));
            SecureConnectorDefaultProperties.setKSVaultFileName(sFile);
        }

        Path s = SecureConnectorDefaultProperties.getDBDir().resolve(sFile);
        if (!Files.isReadable(s))
        {
            System.out.println("Cannot read secret file " + s);
            System.exit(-1);
        }

        if (cl.hasOption("--verbose"))
        {
            System.out.println("Entropy file: " + SecureConnectorDefaultProperties.getDBDir().resolve(
                    SecureConnectorDefaultProperties.getEntropyFileName()));
            System.out.println("Secret file:  " + SecureConnectorDefaultProperties.getDBDir().resolve(
                    SecureConnectorDefaultProperties.getKSVaultFileName()));
        }

        try
        {
            System.out.println(Vault.getKeyStoreSecret());
        }
        catch (Throwable ex)
        {
            System.out.println("Could not get keystore secret: " + ex.getMessage());
            System.exit(-1);
        }
    }

}
