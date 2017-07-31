About Eclipse OM2M
==================

Eclipse OM2M is an open source OSGi-based implementation of the oneM2M standard .

Prerequisites:
* JAVA 1.7 to run OM2M.
* Apache Maven 3 to build OM2M.

Building OM2M from sources using maven:
* Go to the parent project directory "org.eclipse.om2m"
* Build the OM2M project using the following command:
 > mvn clean install
* The IN-CSE product is generated on the this directory: "om2m/org.eclipse.om2m/org.eclipse.om2m.site.in-cse/target/products/in-cse/<os>/<ws>/<arch>"
* The MN-CSE product is generated on the this directory: "om2m/org.eclipse.om2m/org.eclipse.om2m.site.mn-cse/target/products/mn-cse/<os>/<ws>/<arch>"
* The ASN-CSE product is generated on the this directory: "om2m/org.eclipse.om2m/org.eclipse.om2m.site.asn-cse/target/products/asn-cse/<os>/<ws>/<arch>"

Configure and Start a CSE:
* Go to the CSE product directory.
* You can edit the file "configuration/config.ini" to configure the CSE
* You can start the CSE using the following command:
> java -jar -ea -Declipse.ignoreApp=true -Dosgi.clean=true -Ddebug=true plugins/org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar -console -noExit
* Open your browser, then enter the CSE address (e.g."http://127.0.0.1:8080/webpage" for IN-CSE) to access the CSE web interface.

For more details, see https://wiki.eclipse.org/OM2M/one
