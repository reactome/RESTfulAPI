/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.userdata;


/**
 * 
 * The aim of UserDataRetriever object is to retrieve the previously uploaded
 * user data in an indicated temporal table in the database.
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 *
 */
public class UserDataRetriever {
//	private DatabaseTools dnDatabaseTools;
//	
//	/**
//	 * Contains the name of temporal table in the database
//	 */
//	private String tableName;
//
//	/**
//	 * Creates a new UserDataRetriever instance from the following data
//	 * 
//	 * @param dbUtils a reactome DBUtils object
//	 * @param database the name of the database which contains the temporal table
//	 * @param tableName the name of temporal table in the database
//	 */
//	public UserDataRetriever(DatabaseTools dnDatabaseTools, String tableName) {
//		this.dnDatabaseTools = dnDatabaseTools;
//		this.tableName = tableName;
//		
//		//TODO: Decide what is the best way of check the consistency of the values? 
//		
//		//if(nameTableMap == null){
//		//	throw new Exception("No nameTableMap found! Returning");
//		//}
//		
//		//if(tableName == null || tableName.length() ==0){
//		//	throw new Exception("No tablename found in nameTableMap. Returning.");
//		//}
//	}
//	
//	/**
//	 * Returns a list of PSI-MITAB raw data previously upload
//	 * @return a list of PSI-MITAB raw data previously upload
//	 */
//	public List<String> getUserDataResults(){
//		List<String> list = new ArrayList<String>();
//		try {
//			String sql = "SELECT LINE FROM " + tableName;
//			Connection dncon = dnDatabaseTools.getConnection();
//			Statement prep = dncon.createStatement();
//			ResultSet rs = prep.executeQuery(sql);
//	               
//	        while (rs.next())
//	        	list.add(rs.getString(1));
//	        
//	        rs.close();
//	        prep.close();
//		} catch (SQLException e) {
//			System.out.println(e.toString());
//			//TODO: Decide if throwing an exception is a good idea
//		}
//		return list;
//	}
}
