<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
	<head>
		<style>
			#encounterComponent
			{
			font-family:"Trebuchet MS", Arial, Helvetica, sans-serif;
			width:100%;
			border-collapse:collapse;
			}
			#encounterComponent td, #encounterComponent th 
			{
			font-size:1.2em;
			border:1px solid #98bf21;
			padding:3px 7px 2px 7px;
			}
			#encounterComponent th 
			{
			font-size:1.4em;
			text-align:left;
			padding-top:5px;
			padding-bottom:4px;
			background-color:#A7C942;
			color:#fff;
			}
			#encounterComponent tr.alt td 
			{
			color:#000;
			background-color:#EAF2D3;
			}
		</style>
	</head>
  
  
  <body>
	  <!-- Clinical Validation Instructions could go here...-->
 

<xsl:for-each select="//CONTROLACT/OBSERVATIONCLUSTER/OBSERVATIONCLUSTER_ROW">

		<table id="encounterComponent">		
				<tr>
					<td> Document/Result Status:<xsl:value-of select="STATUSCODE"></xsl:value-of></td>
				</tr>		
				<tr class="alt">
					<td> Document/Result ID:<xsl:value-of select="CLUSTER_ID/CLUSTER_ID_ROW/EXTENSION"></xsl:value-of></td>
				</tr>
				<tr>
					<td> Date/Time:<xsl:value-of select="START_DATE"></xsl:value-of></td>
				</tr>
				<tr class="alt">
					<td> Result Code:<xsl:value-of select="CODES/CODES_ROW/CODE"></xsl:value-of></td>
				</tr>					
		</table>

<br></br>
<br></br>

<table id="encounterComponent">
	<tr><td>Person Details</td></tr>
	<tr class="alt"><td>Name: <xsl:value-of select="PATIENT/PATIENT_ROW/PERSON/PERSON_ROW/NAMES/NAMES_ROW/NAME"></xsl:value-of></td></tr>
	<tr><td>Name Type: <xsl:value-of select="PATIENT/PATIENT_ROW/PERSON/PERSON_ROW/NAMES/NAMES_ROW/NAMETYPE"></xsl:value-of></td></tr>
	<tr class="alt"><td>DOB: <xsl:value-of select="PATIENT/PATIENT_ROW/PERSON/PERSON_ROW/DOB"></xsl:value-of></td></tr>
	<tr><td>Gender: <xsl:value-of select="PATIENT/PATIENT_ROW/PERSON/PERSON_ROW/ENTY_CODES/ENTY_CODES_ROW/CODE"></xsl:value-of></td></tr>
	<tr class="alt"><td>MRN: <xsl:value-of select="PATIENT/PATIENT_ROW/PERSON/PERSON_ROW/ID/ID_ROW/EXTENSION"></xsl:value-of></td></tr>
</table>
	
<br></br>
<br></br>
	<table id="encounterComponent">
		<tr><td>Visit/Encounter ID: <xsl:value-of select="COMPONENTOF/COMPONENTOF_ROW/ENCOUNTER/ENCOUNTER_ROW/ENC_ID/ENC_ID_ROW/EXTENSION"></xsl:value-of></td></tr>
	</table>
<br></br>
<br></br>

<xsl:for-each select="AUTHOR/AUTHOR_ROW">
<table id="encounterComponent">
	<tr class="alt"><td>Prinicipal Result Interpreter/ Author: <xsl:value-of select="PERSON/PERSON_ROW/NAMES/NAMES_ROW/NAME"></xsl:value-of></td></tr>
</table>
<br></br>
<br></br>
</xsl:for-each>

	<xsl:for-each select="INFOREC/INFOREC_ROW">
		<table id="encounterComponent">
			<tr class="alt"><td>Result Copies to: <xsl:value-of select="PERSON/PERSON_ROW/NAMES/NAMES_ROW/NAME"></xsl:value-of></td></tr>
		</table>
		<br></br>
		<br></br>
	</xsl:for-each>
	
<xsl:for-each select="FULLFILEMENTOF/FULLFILEMENTOF_ROW/AUTHOR/AUTHOR_ROW">
	<table id="encounterComponent">
		<tr><td>Ordering Provider: <xsl:value-of select="PERSON/PERSON_ROW/NAMES/NAMES_ROW/NAME"></xsl:value-of></td></tr>
	</table>
	<br></br>
	<br></br>
</xsl:for-each>

	<table id="encounterComponent">
		<tr><td>Document/Result:</td></tr>
	<xsl:for-each select="COMPONENT_OBS/COMPONENT_OBS_ROW/OBSERVATION/OBSERVATION_ROW/VALUE_ED/VALUE_ED_ROW">
		
	<xsl:if test="child::NOTE_TEXT">	
		<tr class="alt">
			<td><pre><xsl:value-of select="NOTE_TEXT"/></pre></td>
		</tr>
	</xsl:if>
	<xsl:if test="child::NOTE_CLOB">	
		<tr class="alt">
			<td><pre><xsl:value-of select="NOTE_CLOB"/></pre></td>
		</tr>
	</xsl:if>
		
	</xsl:for-each>	
		
	</table>


<br></br>
<br></br>

<xsl:for-each select="COMPONENT_OBS/COMPONENT_OBS_ROW/OBSERVATION/OBSERVATION_ROW/PERTINENTINFOS/PERTINENTINFOS_ROW">
<table id="encounterComponent">
	<tr><td>Observation Notes/Comments:</td></tr>
	<tr class="alt"><td><pre><xsl:value-of select="VALUE_ED/VALUE_ED_ROW/NOTE_TEXT"></xsl:value-of></pre></td></tr>
</table>
</xsl:for-each>
	
</xsl:for-each>




  


  </body>
  </html>
</xsl:template>
</xsl:stylesheet>
