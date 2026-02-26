# dsoc-sc-common

This repository contains the source code for the dsoc-sc-common.jar which contains base classes and utility code used by the IBM Guardium Analyzer Data Connector (needs IBM DSoC Secure Connector).

# How to Build This Project

Be sure to have [**IBM Java 1.8 SDK**](http://w3.hursley.ibm.com/java/jim/ibmsdks/java80/index.html) on your system to build this project.

Clone the dsoc-scripts repository into your workspace at the same level as dsoc-sc-common.

```bash
git clone git@github.ibm.com:Guardium-Cloud/dsoc-scripts.git
```

Then clone and build this repository.  The first time you run gradlew, it will be downloading files it needs for Gradle, unless Gradle is already installed on your system.

```bash
git clone git@github.ibm.com:Guardium-Cloud.dsoc-sc-common.git
cd dsoc-sc-common
./gradlew build
```

The JAR file will be available at *./build/libs/dsoc-sc-common-VERSION.jar* where VERSION is the value of the projVersion property (if set), or the value of project.version (which defaults to 1.2)

To clean your Gradle build simply run:

```bash
./gradlew clean
```

# How to Setup for Eclipse --- IGNORE THIS SECTION ---

Be sure to have [**IBM Java 1.8 SDK**](http://w3.hursley.ibm.com/java/jim/ibmsdks/java80/index.html) on your system to build this project.

Follow the steps above to checkout a build the project.  Then:

1. Setup the project for use in Eclipse development:

```bash
./gradlew eclipse
```

2. In Eclipse, use _File -> Import... -> Existing Projects into Workspace -> Select root directory (set this to the directory containing dsoc-sc-common)_

3. Check the _dsoc-sc-common_ project and click *Finish*

# How to Publish dsoc-sc-common JAR to Artifactory --- IGNORE THIS SECTION ---

We have a Jenkins job built that will automatically build a JAR and publish it to IBM Artifactory, when a GIT Tag is issued. The Job status can be viewed on IBM Security Slack channel [#ras-sec-con-build](https://ibm-security.slack.com/messages/G6H5G6XDW). If you do not have access to this Slack Channel, and want access, notify Josue Diaz or Andrew Becher.

### Prereq - Determine Git Tag Version to use
To get started, you'll want to see what the latest Git Tag version is from the [Releases](https://github.ibm.com/Guardium-Cloud/dsoc-sc-common/releases) page. To keep things clean, try to stick to incremental versions. So if the latest tag on that page is 4, try pushing a Git Tag of 5.

## GIT Tag via Commandline

To create a tag on your local master branch, run:
```
git tag <version tag, e.g. 4,5,6>
```

This will create a local tag with the current state of the master branch you are on. When pushing to upstream, you will need to explicitly say that you want to push your tags to your remote repo:
```
git push upstream <version tag>
```
**Note: upstream is referring to the remote tracking pointing to Guardium-Cloud/dsoc-sc-common repository, and not your private forked repository.**

## GIT Tag via Github Enterprise GUI

1. Click on the _releases_ link on the [dsoc-sc-common](https://github.ibm.com/Guardium-Cloud/dsoc-sc-common) repository page.

2. Click on _Draft a new release_.

3. Fill out the form fields with appropriate title and description for what is in this new JAR. Pay attention that _Tag version_ be filled out with the version determined above (e.g. 1,2,3,4)

4. Always check the _This is a pre-release_ checkbox to say that the JAR is not production-ready.

5. Click _Publish release_ when finished.

## Whitewater detect-secrets

If there are any false positive api/ssh keys identified by this app, follow the steps to mitigate this:
1. Ensure you have ssh key setup with github.ibm.com
2. `pip install --user git+ssh://git@github.ibm.com/Whitewater/whitewater-detect-secrets.git@master#egg=detect-secrets`
3. Default location of installation is `~/Library/Python/<python_version>/bin/detect-secrets`
4. Run tool: `detect-secrets scan --no-keyword-scan > .secrets.baseline`
5. Mark false positives/fix problems: `detect-secrets audit .secrets.baseline`

Taken from: https://w3.ibm.com/w3publisher/detect-secrets/getting-started

