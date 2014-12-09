package opendata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DBAccess {
	
		private final static String JDBC_URL = "jdbc:mysql://127.0.0.1/ADWORDSDB?useUnicode=true&amp;characterEncoding=UTF-8";
		
		private final static String MY_SQL_DRIVER = "com.mysql.jdbc.Driver";
		
		private final static String DB_USER = "root";
		
		private final static String DB_PASSWD = "";
		
		public void writeCustomers(List<Map<String, String>> customers) throws Exception{
				Connection con = null;
				try{
						Class.forName(MY_SQL_DRIVER);
						con = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWD);
						con.setAutoCommit(false);
						String sql = "insert into CUSTOMER(name) values(?)";
						for(Map<String, String> customer: customers){
								PreparedStatement ps = con.prepareStatement(sql, 
										Statement.RETURN_GENERATED_KEYS);
								ps.setString(1, customer.get("newName"));
								ps.executeUpdate();
								ResultSet rs = ps.getGeneratedKeys();
								rs.next();
								String id = rs.getString(1);
								customer.put("id", id);
						}
						con.commit();
				}catch(Exception e){
						System.out.println(e);
						if(con != null){
								try{
										con.rollback();
								}catch(Exception e2){
										System.out.println(e2);
								}
						}
						throw e;
				}finally{
						if(con != null){
								try{
										con.close();
								}catch(Exception e){
										System.out.println(e);
								}
						}
				}
		}

		public void writeDailyData(List<Map<String, Object>> campaigns) throws Exception {
				Connection con = null;
				try{
						Class.forName(MY_SQL_DRIVER);
						con = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWD);
						con.setAutoCommit(false);
						String sql = "insert into DAILY_DATA(customer, campaign_name, data_date, "
								+ "impressions, clicks, ctr, conversions, conv_rate, cost) " 
								+ " values(?,?,?,?,?,?,?,?,?)";
						for(Map<String, Object> campaign: campaigns){
								PreparedStatement ps = con.prepareStatement(sql);
								ps.setLong(1, (Long)campaign.get("customerId"));
								ps.setString(2, (String)campaign.get("fakeCampaignName"));
								ps.setTimestamp(3, new Timestamp(((Date)campaign.get("day")).getTime()));
								ps.setLong(4, (Long)campaign.get("impressions"));
								ps.setLong(5, (Long)campaign.get("clicks"));
								ps.setDouble(6, (Double)campaign.get("ctr"));
								ps.setLong(7, (Long)campaign.get("conversions"));
								ps.setDouble(8, (Double)campaign.get("convRate"));
								ps.setDouble(9, (Double)campaign.get("cost"));
								ps.executeUpdate();
						}
						con.commit();
				}catch(Exception e){
						System.out.println(e);
						if(con != null){
								try{
										con.rollback();
								}catch(Exception e2){
										System.out.println(e2);
								}
						}
						throw e;
				}finally{
						if(con != null){
								try{
										con.close();
								}catch(Exception e){
										System.out.println(e);
								}
						}
				}
		}
		
		public void writeDailyGeoData(List<Map<String, Object>> geoList) throws Exception {
				Connection con = null;
				try{
						Class.forName(MY_SQL_DRIVER);
						con = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWD);
						con.setAutoCommit(false);
						String sql = "insert into DAILY_GEO_DATA(customer, campaign_name, country, data_date, "
								+ "impressions, clicks, ctr, conversions, conv_rate, cost) " 
								+ " values(?,?,?,?,?,?,?,?,?,?)";
						for(Map<String, Object> geo: geoList){
								PreparedStatement ps = con.prepareStatement(sql);
								ps.setLong(1, (Long)geo.get("customerId"));
								ps.setString(2, (String)geo.get("fakeCampaignName"));
								ps.setString(3, (String)geo.get("countryTerritory"));
								ps.setTimestamp(4, new Timestamp(((Date)geo.get("day")).getTime()));
								ps.setLong(5, (Long)geo.get("impressions"));
								ps.setLong(6, (Long)geo.get("clicks"));
								ps.setDouble(7, (Double)geo.get("ctr"));
								ps.setLong(8, (Long)geo.get("conversions"));
								ps.setDouble(9, (Double)geo.get("convRate"));
								ps.setDouble(10, (Double)geo.get("cost"));
								ps.executeUpdate();
						}
						con.commit();
				}catch(Exception e){
						System.out.println(e);
						if(con != null){
								try{
										con.rollback();
								}catch(Exception e2){
										System.out.println(e2);
								}
						}
						throw e;
				}finally{
						if(con != null){
								try{
										con.close();
								}catch(Exception e){
										System.out.println(e);
								}
						}
				}
		}
}
