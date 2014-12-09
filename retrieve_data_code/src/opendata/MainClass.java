package opendata;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainClass {

	private static final String SAMPLE_MCC_ID = "XXXXXXXXXX";//
	
	public static void main(String[] args) throws Exception {
			//1. Get AdWords customer ids under a MCC.
			AdwordsApiServiceRetrieve sr = new AdwordsApiServiceRetrieve();
			List<Map<String, String>> customersList = sr.getCustomers(SAMPLE_MCC_ID);
			NumberFormat nf = new DecimalFormat("0000");
			NumberFormat nf2 = new DecimalFormat("000");
			for(int i = 0; i < customersList.size(); i++){
					Map<String, String> map = customersList.get(i);
					map.put("newName", "Client" + nf.format(i+1));//set fake name such as Client0001.
			}
			//2. write fake names into table 'CUSTOMER' and return the record id from it.
			//id    name
			// 1    Client0001
			// 2   Client0002
			// .....
			DBAccess dbAccess = new DBAccess();
			dbAccess.writeCustomers(customersList);
			
			AdwordsApiReportRetrieve rr = new AdwordsApiReportRetrieve();
			for(int i = 0; i < customersList.size(); i++){
					Map<String, String> customer = customersList.get(i);
					//Get camapaign data
					List<Map<String,Object>> campaignList = rr.getDailyData(customer.get("customerId"));
					int j = 1;
					Map<String, String> campaignIdToFakeName = new HashMap<String, String>();
					for(Map<String,Object> campaign: campaignList){
							String campaignId = (String)campaign.get("campaignID");
							String fakeCampaignName = "Campaign" + nf.format(i+1) + "_" + nf2.format(j);
							if(campaignIdToFakeName.get(campaignId) == null){
									campaignIdToFakeName.put(campaignId, fakeCampaignName);
									j++;
							}else{
									fakeCampaignName = campaignIdToFakeName.get(campaignId);
							}
							campaign.put("customerId", Long.parseLong(customer.get("id")));
							campaign.put("fakeCampaignName", fakeCampaignName);
					}
					dbAccess.writeDailyData(campaignList);
					
					//Get geo data
					List<Map<String,Object>> geoList = rr.getDailyGeoData(customer.get("customerId"));
					for(Map<String,Object> geo: geoList){
							String campaignId = (String)geo.get("campaignID");
							geo.put("customerId", Long.parseLong(customer.get("id")));
							geo.put("fakeCampaignName", campaignIdToFakeName.get(campaignId));
					}
					dbAccess.writeDailyGeoData(geoList);
			}
	}

}
