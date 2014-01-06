package com.hugo.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

import com.hugo.util.Bean;
import com.hugo.util.Util;

public class TestOnetimeSale{
	static Logger logger=Logger.getLogger(TestClient.class);
	static Properties prop=new Properties();
	static Bean bean=new Bean();
	public static String file;
	public static String dest;
	public static int countno;
	public static int endno;
	public static HttpClient wrapClient(HttpClient base) {

		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				@SuppressWarnings("unused")
				public void checkClientTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				@SuppressWarnings("unused")
				public void checkServerTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
		    sr.register(new Scheme("https", ssf,8443));
			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static void createOnetimeSale() throws FileNotFoundException,
	IOException {

HttpClient httpClient = new DefaultHttpClient();

httpClient = wrapClient(httpClient);

String username = prop.getProperty("username");
String password = prop.getProperty("password");
String tenantIdentifier = prop.getProperty("tenantIdentfier");
String ashok = username.trim() + ":" + password.trim();

// encoding byte array into base 64
byte[] encoded = Base64.encodeBase64(ashok.getBytes());

/*
 * System.out.println("Original String: " + ashok);
 * System.out.println("Base64 Encoded String : " + new String(encoded));
 */

JSONObject onetimesale = new JSONObject();

onetimesale.put("dateFormat", bean.getDateformat());
onetimesale.put("locale", bean.getLocale());
onetimesale.put("itemId", bean.getItemId());
onetimesale.put("quantity", bean.getQuantity());
onetimesale.put("chargeCode", bean.getChargeCode());
onetimesale.put("totalPrice", bean.getTotalPrice());
onetimesale.put("unitPrice", bean.getUnitPrice());
onetimesale.put("discountId", bean.getDiscountId());
onetimesale.put("saleDate", bean.getSaleDate());

logger.info("------------" + onetimesale.toString());

StringEntity se = new StringEntity(onetimesale.toString());
Long clientid = (new Double(bean.getClientid())).longValue();
HttpPost postRequest1 = new HttpPost(prop.getProperty(
		"OnetimeSalePostQuery").trim()
		+ clientid);

postRequest1.setHeader("Authorization", "Basic " + new String(encoded));
postRequest1.setHeader("Content-Type", "application/json");

postRequest1.addHeader("X-Mifos-Platform-TenantId","default");
postRequest1.setEntity(se);

HttpResponse response1 = httpClient.execute(postRequest1);

if (response1.getStatusLine().getStatusCode() != 200) {
	logger.error("Client Id:"+bean.getClientid()+" =Failed : HTTP error code : "
			+ response1.getStatusLine().getStatusCode());
	bean.setResult("Failure");
	return;
}

BufferedReader br1 = new BufferedReader(new InputStreamReader(
		(response1.getEntity().getContent())));

String output1;
logger.info("Output from Server .... \n");
while ((output1 = br1.readLine()) != null) {
	logger.info(output1);
	String resourceId = Util.getStringFromJson("resourceIdentifier",output1);

	//Double invoiceAmount = Double.parseDouble(invoiceAmount1);
	logger.info(resourceId.toString());
//	bean.setInvoiceAmount(invoiceAmount);
	bean.setResult("success");
	logger.info("Onetimesale is created");
	bean.setOnetimesaleResource(resourceId);
	
	JSONObject allocation = new JSONObject();
	JSONObject allocation1 = new JSONObject();
	JSONArray 	serialNumber=new JSONArray();
	 //JsonElement element=new JsonElement();
	 
	 
	
	 
	allocation.put("itemMasterId", Integer.parseInt(bean.getItemId()));
	allocation.put("clientId",clientid);
	allocation.put("orderId",resourceId);
	allocation.put("serialNumber", bean.getSerailNumber());
	allocation.put("status","allocated");
	allocation.put("isNewHw","Y");
	
	serialNumber.add(allocation);
	allocation1.put("quantity",Integer.parseInt(bean.getQuantity()));
	allocation1.put("itemMasterId",Integer.parseInt(bean.getItemId()));
	allocation1.put("serialNumber",serialNumber);
	
	logger.info("------------" + allocation1.toString());
	
	StringEntity se1 = new StringEntity(allocation1.toString());
	
	
HttpPost postRequestAllocation= new HttpPost(prop.getProperty("AllocationPostQuery").trim());

postRequestAllocation.setHeader("Authorization", "Basic " + new String(encoded));
postRequestAllocation.setHeader("Content-Type", "application/json");

postRequestAllocation.addHeader("X-Mifos-Platform-TenantId", "default");
postRequestAllocation.setEntity(se1);
	
	HttpResponse responseAllocation = httpClient.execute(postRequestAllocation);

	if (responseAllocation.getStatusLine().getStatusCode() != 200) {
		logger.error("Client Id:"+bean.getClientid()+" =Failed : HTTP error code : "
				+ responseAllocation.getStatusLine().getStatusCode());
		bean.setResult("Failure");
		return;
	}

	 br1 = new BufferedReader(new InputStreamReader(
			(responseAllocation.getEntity().getContent())));


	//logger.info("Output from Server .... \n");
	while ((output1 = br1.readLine()) != null) {
		//logger.info(output1);
			//logger.info(resourceId.toString());
		bean.setResult("success");
		logger.info("hardware allocated");

}

httpClient.getConnectionManager().shutdown();
}
}

	
public static synchronized void readOnetimeSale(String fileName,String destination) throws FileNotFoundException, IOException{
	prop.load(new FileInputStream("Migrate.properties"));
	file=fileName;
	dest=destination;
	
	CSVReader csvReader= new CSVReader(new FileReader(file));
	String[] row;
	row =csvReader.readNext();
	while((row =csvReader.readNext())!=null)
	{
		String[] currentRowData=row.clone();
		
		//System.out.println(currentRowData[0]);
		//Double d = Double.valueOf((currentRowData[0]));
		bean.setClientid(currentRowData[0]);
		bean.setDateformat(currentRowData[1]);
		//bean.setActivationDate(currentRowData[3]);				
		bean.setLocale(currentRowData[2]);
		bean.setitemId(currentRowData[3]);
		bean.setQuantity(currentRowData[4]);
		bean.setSaleDate(currentRowData[5]);
		bean.setChargeCode(currentRowData[6]);
		
		bean.setTotalPrice(currentRowData[7]);
		bean.setUnitPrice(currentRowData[8]);
		bean.setDiscountId(currentRowData[9]);
		bean.setSerailNumber(currentRowData[10]);
		createOnetimeSale();
		writeCsv();
		
}
	csvReader.close();
	
}
public static synchronized void writeCsv() throws IOException{
	boolean alreadyExists = new File(dest).exists();
FileWriter writer=new FileWriter(dest, true);
	
	if(!alreadyExists){
		writer.write("ClientId,");
		writer.write("Result,");
		writer.write("\n");
	}
	writer.write(bean.getClientid());
	writer.write(",");
	writer.write(bean.getResult());
	writer.write("\n");
	bean.setClientid(null);
	bean.setResult(null);
	writer.flush();
	writer.close();
	
}
}


