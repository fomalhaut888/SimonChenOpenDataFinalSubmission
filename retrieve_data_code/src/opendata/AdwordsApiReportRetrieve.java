package opendata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.adwords.lib.jaxb.v201406.DownloadFormat;
import com.google.api.ads.adwords.lib.jaxb.v201406.Predicate;
import com.google.api.ads.adwords.lib.jaxb.v201406.PredicateOperator;
import com.google.api.ads.adwords.lib.jaxb.v201406.ReportDefinition;
import com.google.api.ads.adwords.lib.jaxb.v201406.ReportDefinitionDateRangeType;
import com.google.api.ads.adwords.lib.jaxb.v201406.ReportDefinitionReportType;
import com.google.api.ads.adwords.lib.jaxb.v201406.Selector;
import com.google.api.ads.adwords.lib.utils.ReportDownloadResponse;
import com.google.api.ads.adwords.lib.utils.v201406.ReportDownloader;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.utils.Streams;
import com.google.api.client.auth.oauth2.Credential;
import com.google.common.collect.Lists;

public class AdwordsApiReportRetrieve {
	
		//private final static String START_DATE = "20141001";
		
		//private final static String END_DATE = "20141031";
	
		public List<Map<String, Object>> getDailyData(String customerId){
				Selector selector = new Selector();
				List<String> fieldsList = Lists.newArrayList("CampaignId","CampaignName",
						"Date", "Impressions", "Clicks", "Ctr", "ConversionsManyPerClick",
						"ConversionRateManyPerClick", "Cost");
				selector.getFields().addAll(fieldsList);
				//DateRange dr = new DateRange();
				//dr.setMin(START_DATE);
				//dr.setMax(END_DATE);
				//selector.setDateRange(dr);
				
				Predicate p1 = new Predicate();
			    p1.setField("CampaignStatus");
			    p1.setOperator(PredicateOperator.EQUALS);
			    p1.getValues().add("ENABLED");
			    selector.getPredicates().add(p1);
			    
			    ReportDefinition reportDefinition = new ReportDefinition();
			    reportDefinition.setReportName("Campaign performance report #" + System.currentTimeMillis());
			    //Last Month
			    reportDefinition.setDateRangeType(ReportDefinitionDateRangeType.LAST_MONTH);
			    reportDefinition.setReportType(ReportDefinitionReportType.CAMPAIGN_PERFORMANCE_REPORT);
				reportDefinition.setDownloadFormat(DownloadFormat.XML);
			    reportDefinition.setSelector(selector);
			    
			    InputStream is = null;
			    ByteArrayOutputStream baos = null;
			    ByteArrayInputStream bais = null;
			    DocumentBuilderFactory documentBuilderFactory = null;
			    DocumentBuilder documentBuilder = null;
			    Document document = null;
			    try{
			    		//the argument validation ignored			
					
						Credential oAuth2Credential = new OfflineCredentials.Builder()
				        		.forApi(Api.ADWORDS)
				        		.fromFile()
				        		.build()
				        		.generateCredential();
						// Construct an AdWordsSession.
					    AdWordsSession adSession = new AdWordsSession.Builder()
					        	.fromFile()
					        	.withOAuth2Credential(oAuth2Credential)
					        	.build();
					    adSession.setClientCustomerId(customerId);
					    
					    ReportDownloadResponse response = null;
					    for(int m = 0; m < 5; m++ ){
								try{
										response = new ReportDownloader(adSession).downloadReport(reportDefinition);
										break;//jump out the loop once success to get data
								}catch(Exception e){
										System.out.println(e);
										if(m == (5 - 1)){
												throw e;
										}else{
												Thread.sleep(10 * 1000);
										}
								}
						}
					    
					    is = response.getInputStream();
			    		baos = new ByteArrayOutputStream();
			    		Streams.copy(is, baos);
			    		String xml = new String(baos.toByteArray());
			    		baos.close();
			    		is.close();
			    		System.out.println("Primary xml=" + xml);
			    		
			    		documentBuilderFactory = DocumentBuilderFactory.newInstance();
			    		documentBuilder = documentBuilderFactory.newDocumentBuilder();
			    		bais = new ByteArrayInputStream(xml.getBytes());
			    		document = documentBuilder.parse(bais);
			    		NodeList nodeList = document.getElementsByTagName("row");
			    		System.out.println("nodeList.getLength()=" + nodeList.getLength());
			    		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
			    		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			    		NumberFormat integerFormat = new DecimalFormat("#,##0");
			    		NumberFormat percentFormat = new DecimalFormat("#,##0.00%");
			    		NumberFormat moneyFormat = new DecimalFormat("#,##0.00");
			    		for(int i = 0; i < nodeList.getLength(); i++){
			    				Node node = nodeList.item(i);
			    				NamedNodeMap nnm = node.getAttributes();
			    				
			    				Map<String, Object> map = new HashMap<String, Object>();
			    				map.put("campaignID", nnm.getNamedItem("campaignID").getNodeValue());
			    				map.put("campaign", nnm.getNamedItem("campaign").getNodeValue());
			    				map.put("day", df.parse(nnm.getNamedItem("day").getNodeValue()));
			    				map.put("impressions", integerFormat.parse(nnm.getNamedItem("impressions").getNodeValue()).longValue());
			    				map.put("clicks", integerFormat.parse(nnm.getNamedItem("clicks").getNodeValue()).longValue());
			    				map.put("ctr", percentFormat.parse(nnm.getNamedItem("ctr").getNodeValue()).doubleValue());
			    				map.put("conversions", integerFormat.parse(nnm.getNamedItem("conversions").getNodeValue()).longValue());
			    				map.put("convRate", percentFormat.parse(nnm.getNamedItem("convRate").getNodeValue()).doubleValue());
			    				map.put("cost", moneyFormat.parse(nnm.getNamedItem("cost").getNodeValue()).doubleValue() / 1000000D);
			    				resultList.add(map);
			    		}
			    		
			    		return resultList;
			    }catch (Exception e) {
			    		System.out.println(e);
			    		throw new RuntimeException(e);
			    }finally{
				    	if(baos != null){
					    		try{
					    				baos.close();
					    		}catch(Exception e){
					    				System.out.println(e);
					    		}
				    	}
				    	if(is != null){
					    		try{
					    				is.close();
					    		}catch(Exception e){
					    				System.out.println(e);
					    		}
				    	}
				    	if(bais != null){
					    		try{
					    				bais.close();
					    		}catch(Exception e){
					    				System.out.println(e);
					    		}
				    	}
			    }
		}
		
		public List<Map<String, Object>> getDailyGeoData(String customerId){
				Selector selector = new Selector();
				List<String> fieldsList = Lists.newArrayList("CampaignId","CampaignName",
						"CountryCriteriaId", "Date", "Impressions", "Clicks", "Ctr", 
						"ConversionsManyPerClick", "ConversionRateManyPerClick", "Cost");
				selector.getFields().addAll(fieldsList);
				
				Predicate p1 = new Predicate();
			    p1.setField("CampaignStatus");
			    p1.setOperator(PredicateOperator.EQUALS);
			    p1.getValues().add("ENABLED");
			    selector.getPredicates().add(p1);
			    
			    ReportDefinition reportDefinition = new ReportDefinition();
			    reportDefinition.setReportName("Geo performance report #" + System.currentTimeMillis());
			    //Last Month
			    reportDefinition.setDateRangeType(ReportDefinitionDateRangeType.LAST_MONTH);
			    reportDefinition.setReportType(ReportDefinitionReportType.GEO_PERFORMANCE_REPORT);
				reportDefinition.setDownloadFormat(DownloadFormat.XML);
			    reportDefinition.setSelector(selector);
			    
			    InputStream is = null;
			    ByteArrayOutputStream baos = null;
			    ByteArrayInputStream bais = null;
			    DocumentBuilderFactory documentBuilderFactory = null;
			    DocumentBuilder documentBuilder = null;
			    Document document = null;
			    try{
			    		//the argument validation ignored			
					
						Credential oAuth2Credential = new OfflineCredentials.Builder()
				        		.forApi(Api.ADWORDS)
				        		.fromFile()
				        		.build()
				        		.generateCredential();
						// Construct an AdWordsSession.
					    AdWordsSession adSession = new AdWordsSession.Builder()
					        	.fromFile()
					        	.withOAuth2Credential(oAuth2Credential)
					        	.build();
					    adSession.setClientCustomerId(customerId);
					    
					    ReportDownloadResponse response = null;
					    for(int m = 0; m < 5; m++ ){
								try{
										response = new ReportDownloader(adSession).downloadReport(reportDefinition);
										break;//jump out the loop once success to get data
								}catch(Exception e){
										System.out.println(e);
										if(m == (5 - 1)){
												throw e;
										}else{
												Thread.sleep(10 * 1000);
										}
								}
						}
					    
					    is = response.getInputStream();
			    		baos = new ByteArrayOutputStream();
			    		Streams.copy(is, baos);
			    		String xml = new String(baos.toByteArray());
			    		baos.close();
			    		is.close();
			    		System.out.println("Primary xml=" + xml);
			    		
			    		documentBuilderFactory = DocumentBuilderFactory.newInstance();
			    		documentBuilder = documentBuilderFactory.newDocumentBuilder();
			    		bais = new ByteArrayInputStream(xml.getBytes());
			    		document = documentBuilder.parse(bais);
			    		NodeList nodeList = document.getElementsByTagName("row");
			    		System.out.println("nodeList.getLength()=" + nodeList.getLength());
			    		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
			    		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			    		NumberFormat integerFormat = new DecimalFormat("#,##0");
			    		NumberFormat percentFormat = new DecimalFormat("#,##0.00%");
			    		NumberFormat moneyFormat = new DecimalFormat("#,##0.00");
			    		for(int i = 0; i < nodeList.getLength(); i++){
			    				Node node = nodeList.item(i);
			    				NamedNodeMap nnm = node.getAttributes();
			    				
			    				Map<String, Object> map = new HashMap<String, Object>();
			    				map.put("campaignID", nnm.getNamedItem("campaignID").getNodeValue());
			    				map.put("campaign", nnm.getNamedItem("campaign").getNodeValue());
			    				map.put("countryTerritory", nnm.getNamedItem("countryTerritory").getNodeValue());
			    				map.put("day", df.parse(nnm.getNamedItem("day").getNodeValue()));
			    				map.put("impressions", integerFormat.parse(nnm.getNamedItem("impressions").getNodeValue()).longValue());
			    				map.put("clicks", integerFormat.parse(nnm.getNamedItem("clicks").getNodeValue()).longValue());
			    				map.put("ctr", percentFormat.parse(nnm.getNamedItem("ctr").getNodeValue()).doubleValue());
			    				map.put("conversions", integerFormat.parse(nnm.getNamedItem("conversions").getNodeValue()).longValue());
			    				map.put("convRate", percentFormat.parse(nnm.getNamedItem("convRate").getNodeValue()).doubleValue());
			    				map.put("cost", moneyFormat.parse(nnm.getNamedItem("cost").getNodeValue()).doubleValue() / 1000000D);
			    				resultList.add(map);
			    		}
			    		
			    		return resultList;
			    }catch (Exception e) {
			    		System.out.println(e);
			    		throw new RuntimeException(e);
			    }finally{
				    	if(baos != null){
					    		try{
					    				baos.close();
					    		}catch(Exception e){
					    				System.out.println(e);
					    		}
				    	}
				    	if(is != null){
					    		try{
					    				is.close();
					    		}catch(Exception e){
					    				System.out.println(e);
					    		}
				    	}
				    	if(bais != null){
					    		try{
					    				bais.close();
					    		}catch(Exception e){
					    				System.out.println(e);
					    		}
				    	}
			    }
		}
}
