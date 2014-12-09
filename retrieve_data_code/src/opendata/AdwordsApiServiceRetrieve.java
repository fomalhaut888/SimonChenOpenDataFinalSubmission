package opendata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.ads.adwords.axis.factory.AdWordsServices;
import com.google.api.ads.adwords.axis.v201406.cm.Predicate;
import com.google.api.ads.adwords.axis.v201406.cm.PredicateOperator;
import com.google.api.ads.adwords.axis.v201406.cm.Selector;
import com.google.api.ads.adwords.axis.v201406.mcm.ManagedCustomer;
import com.google.api.ads.adwords.axis.v201406.mcm.ManagedCustomerPage;
import com.google.api.ads.adwords.axis.v201406.mcm.ManagedCustomerServiceInterface;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.client.auth.oauth2.Credential;

public class AdwordsApiServiceRetrieve {
		public List<Map<String, String>> getCustomers(String mccId) 
				throws Exception{
			
				//the argument validation ignored			
				
				Credential oAuth2Credential = new OfflineCredentials.Builder()
		        		.forApi(Api.ADWORDS)
		        		.fromFile()
		        		.build()
		        		.generateCredential();
				// Construct an AdWordsSession.
			    AdWordsSession session = new AdWordsSession.Builder()
			        	.fromFile()
			        	.withOAuth2Credential(oAuth2Credential)
			        	.build();
			    session.setClientCustomerId(mccId);
			    AdWordsServices adWordsServices = new AdWordsServices();
			    ManagedCustomerServiceInterface managedCustomerService = adWordsServices
			    		.get(session, ManagedCustomerServiceInterface.class);
			    
			    // Create selector.
			    Selector selector = new Selector();
			    selector.setFields(new String[]{"CustomerId",  "Name"});
			    selector.setPredicates(new Predicate[] {
			    		new Predicate("CanManageClients",
			    				PredicateOperator.EQUALS, new String[] {"false"})});//Return Customer ,not MCC
			    
			    // Get results.
			    ManagedCustomerPage page = managedCustomerService.get(selector);
			    List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
			    if (page.getEntries() != null) {
						for (ManagedCustomer customer : page.getEntries()) {
								Map<String, String> map = new HashMap<String, String>();
								map.put("customerId", customer.getCustomerId().toString());
								map.put("name", customer.getName());
								resultList.add(map);
						}
						System.out.println("Total " + page.getTotalNumEntries() + " customers");
			    }
				return resultList;
		}
}
