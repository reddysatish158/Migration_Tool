package com.hugo.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

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

public class TestClientBalance {
	static Logger logger=Logger.getLogger(TestClient.class);
	static Properties prop=new Properties();
	public static int i;
	public static String file;
	public static String dest;
	static Bean bean = new Bean();
	
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
public static void createClientBalance() throws Exception{
		
		HttpClient httpClient = new DefaultHttpClient();
		httpClient = wrapClient(httpClient);
		String username = prop.getProperty("username");
		String password = prop.getProperty("password");
		String tenantIdentifier = prop.getProperty("tenantIdentfier");
		String ashok = username.trim() + ":" + password.trim();

		// encoding byte array into base 64
		byte[] encoded = Base64.encodeBase64(ashok.getBytes());
		
		JSONObject balance = new JSONObject();
		balance.put("clientId",bean.getClientid());
		balance.put("balance",bean.getClientBalanceAmount());
		balance.put("locale","en");
		
		System.out.println("------------" + balance.toString());
		StringEntity se = new StringEntity(balance.toString());
		HttpPost postRequest1=new HttpPost(prop.getProperty("clientBalanceQuery").trim());
		postRequest1.setHeader("Authorization", "Basic " + new String(encoded));
		postRequest1.setHeader("Content-Type", "application/json");
		postRequest1.addHeader("X-Mifos-Platform-TenantId", tenantIdentifier);
		postRequest1.setEntity(se);
		HttpResponse response1 = httpClient.execute(postRequest1);
		if(response1.getStatusLine().getStatusCode() != 200) {
		logger.error("Failed: HTTP error code :" + response1.getStatusLine().getStatusCode());
		bean.setResult("failure");
		return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(response1.getEntity().getContent()));
		String output;
		String balanceAmount=null;
		logger.info("output from server.....\n");
		while((output=br.readLine()) != null)
		{
			logger.info(output);
			bean.setResult("Success");
		}
		httpClient.getConnectionManager().shutdown();
		
		
	}
public static void readClientBalanceFile(String fileName, String destination)throws Exception{
	file=fileName;
	dest=destination;
	prop.load(new FileInputStream("Migrate.properties"));
	CSVReader csvReader= new CSVReader(new FileReader(fileName));
	String[] row;
	
	row =csvReader.readNext();
	
	while((row =csvReader.readNext())!=null)
		{
				
			String[] currentRowData=row.clone();
			
			
			bean.setClientid(currentRowData[0]);
			bean.setClientBalanceAmount(currentRowData[1]);
		
			
	
			createClientBalance();
			writeCsv();
		
		
		}
		csvReader.close();
	
}
public static synchronized  void writeCsv() throws IOException
{
	boolean alreadyExists = new File(dest).exists();
	
	//CsvWriter writer=new CsvWriter(new FileWriter(dest,true),',');
	//CsvWriter writer =new CsvWriter(new FileWriter(dest, true),',');
	FileWriter writer=new FileWriter(dest, true);
	
	if(!alreadyExists){
		writer.write("ClientId,");
		
		writer.write("Result");
		
		
		writer.write("\n");
	}
	
	
		
		/*long ldate1 = System.currentTimeMillis();
		String date1 = new SimpleDateFormat("HH:mm:ss.SSS")
				.format(new Date(ldate1));
		bean.setLastTime(date1);*/
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
