****************************************************
HOWTO:
****************************************************
To add a new Contributor for testing: 
 * Edit ContributorConfig.java. Copy another site and edit it. Note that:
   ** setHspId9004AndSubIds() needs a value provided by eHealth Ontario for an HSP OID for your organization. Email ehealthstandards@ehealthontario.on.ca to request this.
   ** getMrnPoolOid().add( .. ) also needs a value for an MRN Pool OID from eHealth. See above for hot to get one.
   ** HospitalFacilityNumber is the number that OLIS refers to the hospital by. See the OLIS OIDs to get this number.
   ** Facilities are assigned by us by taking the HspId9004 and adding .100.1, .100.2, etc.
   ** Sending system OIDs are also assigned by us. See a different site to see how it is done. 
 * Run ContributorConfig.java's main() method
 * Rebuild the project
 * Redeploy the project - Anthony can show you how to do this 



****************************************************

Futon Viewer:
http://10.7.7.45:5984/_utils/


Testing MRN view:
------------------------------------------------------------------------------MRN    ---
                                                                              7007469
http://localhost:5984/cgta_input_test_db/_design/application/_view/mrn?key=%227007469%22




