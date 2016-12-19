Vaadin Spreadsheet ${project.version}
================================
Vaadin Spreadsheet is an add-on for the Vaadin Framework. Vaadin Spreadsheet
provides a Vaadin Component for displaying and editing Excel spreadsheets
within a Vaadin application.

Installation
============
The add-on works like normal Vaadin Add-ons. Note, that the package also has
client side extensions, so make sure that you compile your widgetset after
installation!

!!!!! NOTE !!!!!
Widget set compilation will fail unless you have a license for Vaadin
Spreadsheet. A free trial key can be obtained by clicking the big orange "Free
trial key" button on the right hand side of http://vaadin.com/addon/vaadin-spreadsheet
Please find instructions for how to install the license at
https://vaadin.com/directory/help/installing-cval-license

Maven
-----

Dependency snippet for Maven users:

<dependency>
<groupId>com.vaadin</groupId>
<artifactId>vaadin-spreadsheet</artifactId>
<version>${project.version}</version>
</dependency>

The add-on is available in Vaadin Add-Ons repository:

<repository>
<id>vaadin-addons</id>
<url>http://maven.vaadin.com/vaadin-addons</url>
</repository>

Ivy
---

IVY dependency snippet:

<dependency org="com.vaadin" name="vaadin-spreadsheet" rev="${project.version}" conf="default->default" />

Using plain Jar
---------------

If you wan't to use the add-on jar directly, add it to your classpath. Please be sure to 
add all libraries from the lib folder to your classpath also.

Licensing
=========

Vaadin Spreadsheet is a commercial product. After 30 days of evaluation use,
you must either acquire a license or stop using it. More information about
Commercial Vaadin Add-on License is available in LICENSE file or at
https://vaadin.com/license/cval-3.

You may obtain a valid license by subscribing to Vaadin Pro Account at
https://vaadin.com/pro or by purchasing a perpetual license at
https://vaadin.com/directory.

A valid license key is your perpetual license key purchased from Vaadin
Directory or alternatively the email address you use to login to an active
Vaadin Pro Account.

Register your copy of Vaadin Spreadsheet by creating a file named
.vaadin.spreadsheet.developer.license containing the license key in your home
directory or by setting the vaadin.spreadsheet.developer.license=license_key
system property to disable the license warning message.

Vaadin Spreadsheet (version 1.2.0 or later) supports Vaadin Charts, making it
possible to open Excel files with charts in it. To enable this feature, you
need to add a vaadin-spreadsheet-charts dependency to your project. Vaadin
Charts is distributed under the terms of Commercial Vaadin Add-On License
version 3.0("CVALv3"), see https://vaadin.com/license/cval-3 for details. To
use the vaadin-spreadsheet-charts package, you need to have valid Vaadin
Spreadsheet and Vaadin Charts licenses.

Third Party Licensing
=====================

This Add-on component depends on a few external open source libraries, of
which the Apache 2.0 licensed Apache POI library is the most important.
You can find details about these dependencies in the license.html file.

Links
=====

Homepage:
https://vaadin.com/add-ons/spreadsheet

Apache POI Homepage:
http://poi.apache.org/

Code and usage examples:
http://demo.vaadin.com/spreadsheet/

Issue tracker:
http://dev.vaadin.com/

Documentation:
https://vaadin.com/book/vaadin7/-/page/spreadsheet.html

SCM (Git):
https://github.com/vaadin/spreadsheet