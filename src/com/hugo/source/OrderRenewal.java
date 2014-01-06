package com.hugo.source;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

import com.hugo.source.TestOrder;
import com.hugo.util.Bean;
import com.hugo.util.Util;


public class OrderRenewal {
	static Logger logger=Logger.getLogger(TestOrder.class);
	static Properties prop=new Properties();
    static Bean bean=new Bean();
    public static Long planc;
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
			sr.register(new Scheme("https", ssf, 8443));
			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			return null;
		}

 }
    public static void doOrderRenewal() throws FileNotFoundException, IOException {

		
		HttpClient httpClient = new DefaultHttpClient();

		httpClient = wrapClient(httpClient);
		String username = prop.getProperty("username");
		String password = prop.getProperty("password");
		String tenantIdentifier = prop.getProperty("tenantIdentfier");
		String ashok = username.trim() + ":" + password.trim();
		byte[] encoded = Base64.encodeBase64(ashok.getBytes());
		

		JSONObject order = new JSONObject();
		
		order.put("renewalPeriod", bean.getrenewalPeriod());
		order.put("description", bean.getDiscription());
		

		System.out.println("------------" + order.toString());

		StringEntity se = new StringEntity(order.toString());
		String orderi = bean.getOrderId();
		HttpPost postRequest1 = new HttpPost(prop.getProperty("OrderRenewalPostQuery")
				.trim() + orderi);

		postRequest1.setHeader("Authorization", "Basic " + new String(encoded));
		postRequest1.setHeader("Content-Type", "application/json");

		postRequest1.addHeader("X-Mifos-Platform-TenantId", tenantIdentifier);
		postRequest1.setEntity(se);
		
		HttpResponse response1 = httpClient.execute(postRequest1);
		if (response1.getStatusLine().getStatusCode() != 200) {
			logger.error("Client Id:"+bean.getClientid()+"=Failed : HTTP error code : "
					+ response1.getStatusLine().getStatusCode());
			
			bean.setResult("Failure");
			return;
		}
		BufferedReader br1 = new BufferedReader(new InputStreamReader(
				(response1.getEntity().getContent())));

		String output1;
        String orderId;
		logger.info("Output from Server .... \n");
		while ((output1 = br1.readLine()) != null) {
			
			
			bean.setResult("success");
			

		}

		httpClient.getConnectionManager().shutdown();
	}
    public static void readOrderRenewalFile(String fileName,String destination) throws IOException
    {
    	dest=destination;
   	 file=fileName;
   	  prop.load(new FileInputStream("Migrate.properties"));
   	 CSVReader csvReader=new CSVReader(new FileReader(file));
   	 String[] row;
   	 
   	row =csvReader.readNext();
   		   	
   	 while((row=csvReader.readNext())!= null)
   	 {
   		 String[] currentRowdata=row.clone();
   		 bean.setClientid(currentRowdata[0]);
   		    bean.setOrderId(currentRowdata[1]);
   		    bean.setrenewalPeriod(currentRowdata[2]);
   			bean.setDiscription(currentRowdata[3]);
   		    doOrderRenewal();
   			writeCsv();
   		   			
   			
   	 }
   	 csvReader.close();
   	
    }
    	
    public static synchronized void writeCsv() throws IOException
    {
   	 boolean alreadyExist=new File(dest).exists();
   	 FileWriter writer=new FileWriter(dest,true);
   	 if(!alreadyExist){
   			writer.write("ClientId,");
   			writer.write("Order Id,");
   			writer.write("Result");
   			writer.write("\n");
   	 }
   			writer.write(bean.getClientid());
   			writer.write(",");
   			writer.write(bean.getOrderId());
   			
   			writer.write(",");
   			//Double d=Double.valueOf(bean.getOrderId()); 
   			writer.write(bean.getResult());
   			writer.write("\n");
   			bean.setClientid(null);
   			bean.setOrderId(null);
   			
   			writer.flush();
   			writer.close();
   	
    }
    }
    
