### Connect to Cytoscape via CyREST
#
# This script will show you how to connect to Cytoscape from R using CyREST.  It will also cover
# the installation and check of Cytosacpe Apps and demonstrate some basic functionality of CyREST,
# commands and r2cytoscape.
#
# This is helpful to run PRIOR to workshops and other tutorials to mitigate troubleshooting time.

#### First, install libs
# if any of these do not load properly, please refer to check-library-installation.R for more details:
# https://github.com/cytoscape/cytoscape-automation/tree/master/for-scripters/R
source('https://bioconductor.org/biocLite.R')
ip = installed.packages()
if(!("pacman" %in% ip)) install.packages("pacman")
library(pacman)
if(!("devtools" %in% ip)) install.packages("devtools")
library(devtools)
if(!("r2cytoscape" %in% ip)) install_github('https://github.com/cytoscape/r2cytoscape')
library(r2cytoscape)
if(!("Biobase" %in% ip)) biocLite("Biobase")
library(Biobase)
if(!("GEOquery" %in% ip)) biocLite("GEOquery")
library(GEOquery)
if(!("limma" %in% ip)) biocLite("limma")
library(limma)
if("RJSONIO" %in% ip) print("Success: the RJSONIO lib is installed") else print("Warning: RJSONIO lib is not installed. Please install this lib before proceeding.")
if("igraph" %in% ip) print("Success: the igraph lib is installed") else print("Warning: igraph lib is not installed. Please install this lib before proceeding.")
if("httr" %in% ip) print("Success: the httr lib is installed") else print("Warning: httr lib is not installed. Please install this lib before proceeding.")
if("stringr" %in% ip) print("Success: the stringr lib is installed") else print("Warning: stringr lib is not installed. Please install this lib before proceeding.")
if("XML" %in% ip) print("Success: the XML lib is installed") else print("Warning: XML lib is not installed. Please install this lib before proceeding.")
if("RColorBrewer" %in% ip) print("Success: the RColorBrewer lib is installed") else print("Warning: RColorBrewer lib is not installed. Please install this lib before proceeding.")
#p_load(RJSONIO,httr,XML,r2cytoscape)
p_load(RJSONIO,httr,XML,devtools)

if(exists('command2query',mode='function')) print("Success: r2cytoscape is installed") else print("Warning: r2cytoscape is not installed. Please source this script before proceeding.")

#### Next, setup Cytoscape
# - Launch Cytoscape on your local machine. If you haven't already installed Cytoscape, then download the latest version from http://cytoscape.org.
# - Install the RWRMTN app 
# - Leave Cytoscape running in the background during the remainder of the tutorial.

#### Test connection to Cytoscape
# **port.number** needs to match value of Cytoscape property: rest.port (see Edit>Preferences>Properties...); default = 1234
# port.number = 1234
# base.url = paste('http://localhost:',port.number,'/v1',sep="")
checkCytoscapeVersion()

#### Test installed apps for Cytoscape
if("RWRMTN" %in% commandHelp("")) print("Success: the RWRMTN app is installed") else print("Warning: RWRMTN app is not installed. Please install the RWRMTN app before proceeding.")

###############################
#### Now it gets interesting...
###############################



# r2cytoscape helper functions
help(package=r2cytoscape)

# Open swagger docs for live instances of CyREST API and CyREST-supported commands:
openCySwagger()  # CyREST API
openCySwagger("commands")  # CyREST Commands API

#List available commands and arguments in R. Use "help" to list top level:
commandHelp("help")  

#List **network** commands. Note that "help" is optional:
commandHelp("help network")  

#List arguments for the **network select** command:
commandHelp("help network select")  

#### Syntax reference and helper functions
# Syntax examples. Do not run this chunk of code.

### CyREST direct
# queryURL = paste(base.url,'arg1','arg2','arg3',sep='/') # refer to Swagger for args
# res = GET(queryURL) # GET result object
# res.html = htmlParse(rawToChar(res$content), asText=TRUE)  # parse content as HTML

### Commands via CyREST
# queryURL = command2query('commands and args') # refer to Swagger or Tools>Command Line Dialog in Cytoscape
# res = GET(queryURL) # GET result object
# res.html = htmlParse(rawToChar(res$content), asText=TRUE)  # parse content as HTML
## ...using helper function
# res.list = commandRun('commands and args') # parse list from content HTML


#### Ok, now you are ready to work with some real data!  See advanced tutorials...