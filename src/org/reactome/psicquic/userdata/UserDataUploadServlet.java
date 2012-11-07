/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.userdata;


/**
 * 
 * The aim of UserDataUploadServlet is store the user data in a temporal table
 * in order to allow future queries.
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 *
 */
public class UserDataUploadServlet {
//	private static final long serialVersionUID = 1L;
//	private String name = null;
//	private final String tablePrefix = "interactionsDataTable_";
//
//	@Override
//	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
//			throws ServletException, IOException {
//		this.name = req.getParameter("name");
//
//		List<String> data;
//		try {
//			boolean isMultipart = ServletFileUpload.isMultipartContent(req);
//			data = isMultipart ? getMultiPartData(req) : getData(req);
//
//			// if multipartdata function is called, this.name will contain the
//			// name specified for the user
//			if(this.name==null || this.name.isEmpty())
//				throw new Exception("Data name has not been specified");
//		} catch (Exception e) {
//			// TODO: sent an error message to the client
//			//String errorMsg = e.getMessage();
//			return;
//		}
//
//		
//		HttpSession session = req.getSession();
//		DatabaseManagers databaseManagers = getDatabaseManagers(session);
//		TemporaryDatabaseTables dnTemporaryDatabaseTables = databaseManagers.getDnTemporaryDatabaseTables();
//		String tableName = dnTemporaryDatabaseTables.addUnique(tablePrefix + "_" + name);
//		// Synchronise on session as it might be accessed somewhere else in
//		// the application
//		synchronized (session) {
//			Map<String, String> dataMap = getOrCreateSessionNameTableMap(session);
//			dataMap.put(name, tableName);
//		}
//		
//		Connection dncon = null;
//		try {
//			DatabaseTools dnDatabaseTools = databaseManagers.getDnDatabaseTools();
//			dncon = dnDatabaseTools.getConnection(); // auto-closed at end of session
//			createTemporalTable(tableName, dncon);
//			loadTable(tableName, dncon, data, name);
//		} catch (Exception e) {
//	    	System.err.println("UserDataUploadServlet.doPost: WARNING - problem");
//			e.printStackTrace(System.err);
//		}
//		
//		databaseManagers.releaseConnections();
//	}
//
//	@SuppressWarnings("unchecked")
//	private List<String> getMultiPartData(HttpServletRequest req) throws Exception {
//		List<String> data = new ArrayList<String>();
//		BufferedReader in = null;
//
//		try {
//			// Create a factory for disk-based file items
//			FileItemFactory factory = new DiskFileItemFactory();
//			// Create a new file upload handler
//			ServletFileUpload upload = new ServletFileUpload(factory);
//			// Parse the request
//			List<FileItem> items = upload.parseRequest(req);
//
//			for (FileItem fileItem : items) {
//				if (fileItem.isFormField()) {
//					//If is multipart, one of the fileItem will be the field
//					//which contains the name for the data uploaded by the user
//					this.name = fileItem.getString();
//				} else {
//					InputStream is = fileItem.getInputStream();
//					in = new BufferedReader(new InputStreamReader(is));
//					
//					String line;
//					while ((line = in.readLine()) != null) {
//						data.add(line);
//					}
//					in.close();
//
//				}
//			}
//		} catch (Exception e) {
//			throw new Exception("The data you have sent can not be retrieved");
//		} finally {
//			if (in != null) {
//				try {
//					in.close();
//				} catch (Exception e) {
//					System.err.println("UserDataUploader: reader can not be closed");
//					throw new Exception("The server is experimenting problems, please try later");
//				}
//			}
//		}
//		return data;
//	}
//	
//	private List<String> getData(HttpServletRequest req){
//		List<String> data = new ArrayList<String>();
//		for(String line : req.getParameter("data").split("/n")){
//			data.add(line);
//		}
//		return data;
//	}
//
//	@SuppressWarnings("unchecked")
//	private Map<String, String> getOrCreateSessionNameTableMap(HttpSession session){
//		Map<String, String> dataMap = (Map<String, String>) session.getAttribute("nameTableMap");
//		if (dataMap == null){
//			dataMap = new HashMap<String, String>();
//			session.setAttribute("nameTableMap", dataMap);
//		}
//		return dataMap;
//	}
//	
//	private void createTemporalTable(String name, Connection con) throws Exception {
//		try {
//			String sql = "CREATE TABLE " + name + " (DB_ID INT, LINE LONGTEXT)";
//			PreparedStatement prep = con.prepareStatement(sql);
//			prep.execute();
//		} catch (SQLException e) {
//			//TODO: Write a good error message
//			throw new Exception("");
//		}		
//	}
//
//	private void loadTable(String tableName, Connection con, List<String> data, String name) throws Exception {
//		try {
//			String sql = "INSERT INTO " + tableName + " VALUES(?,?)";
//			
//			int id = 0;
//			for(String line : data){
//				PreparedStatement prep = con.prepareStatement(sql);
//				prep.setInt(1, id++);
//				prep.setString(2, line);
//				prep.execute();
//			}
//		} catch (SQLException e) {
//			//TODO: Write a good error message
//			throw new Exception("");
//		}
//	}

}
