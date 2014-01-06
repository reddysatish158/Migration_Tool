package com.hugo.source;


//import java.awt.List;
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




public class TestClient 
{
	static Logger logger=Logger.getLogger(TestClient.class);
	static Properties prop=new Properties();
	public static int i;
	public static String file;
	public static String dest;
	public static long records=0;
	public static long totalRecords;
	
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
	
	public static void createClient()
	{
		HttpClient httpClient = new DefaultHttpClient();
		
		httpClient = wrapClient(httpClient);
		String username = prop.getProperty("username");
		String password = prop.getProperty("password");
		String tenantIdentifier = prop.getProperty("tenantIdentfier");
		String ashok = username.trim() + ":" + password.trim();

		// encoding byte array into base 64
		byte[] encoded = Base64.encodeBase64(ashok.getBytes());

		/*System.out.println("Original String: " + ashok);
		System.out.println("Base64 Encoded String : " + new String(encoded));
*/
		JSONObject client = new JSONObject();
		client.put("officeId", bean.getOfficeId());
		client.put("firstname", bean.getFirstname());
		client.put("middlename", bean.getMiddlename());
		client.put("lastname", bean.getLastname());
		client.put("fullname", "");
		client.put("externalId",bean.getExternalid());
		client.put("dateFormat", bean.getDateformat());
		client.put("locale", bean.getLocale());
		client.put("clientCategory", bean.getClientCategory());
		client.put("active", bean.getActive());
		//client.put("activationDate", bean.getActivationDate());
		client.put("addressNo", bean.getAddressNo());
		client.put("street", bean.getStreet());
		//client.put("area",bean.getArea());
		client.put("city", bean.getCity());
		client.put("state", bean.getState());
		client.put("country", bean.getCountry());
		client.put("zipCode", bean.getZipCode());
		client.put("phone", bean.getPhone());
		client.put("email", bean.getEmail());
		client.put("flag","true");
		client.put("login",bean.getLogin());
		client.put("password",bean.getPassword());
		System.out.println("------------" + client.toString());
try{
		StringEntity se = new StringEntity(client.toString());

		HttpPost postRequest1 = new HttpPost(prop.getProperty("clientQuery")
				.trim());

		postRequest1.setHeader("Authorization", "Basic " + new String(encoded));
		postRequest1.setHeader("Content-Type", "application/json");

		postRequest1.addHeader("X-Mifos-Platform-TenantId", tenantIdentifier);
		postRequest1.setEntity(se);

		HttpResponse response1 = httpClient.execute(postRequest1);
		if (response1.getStatusLine().getStatusCode() != 200) {
			logger.error("CNO:"+bean.getCno()+" =Failed : HTTP error code : "
					+ response1.getStatusLine().getStatusCode());
			bean.setResult("failure");

			return;
		}
		BufferedReader br1 = new BufferedReader(new InputStreamReader(
				(response1.getEntity().getContent())));

		String output;
		String clientId = null;

		//logger.info("Output from Server .... \n");
		while ((output = br1.readLine()) != null) {

			//logger.info(output);

		clientId = Util.getStringFromJson("resourceIdentifier", output);

			//logger.info(clientId);

		
			//logger.info(output);
			//logger.info("");
			//logger.info("**********************************");
			//logger.info("client is created");
			//logger.info("clientid is " + clientId);
			bean.setResult("success");
			bean.setClientid(clientId);
			//logger.info("***********************************");

		}
}
catch(Exception e)
{
	System.out.println(e);
}
		httpClient.getConnectionManager().shutdown();

	}

	public static synchronized void readClient(String fileName, String destination) throws FileNotFoundException, IOException 
	{
		file=fileName;
		dest=destination;
		
		
		
		prop.load(new FileInputStream("Migrate.properties"));
		CSVReader csvReader= new CSVReader(new FileReader(fileName));
		String[] row;
		
		
		row =csvReader.readNext();
		
		
			while((row =csvReader.readNext())!=null)
			{
				/*
				long ldate = System.currentTimeMillis();
				String date = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(
						ldate));*/
				
			
				String[] currentRowData=row.clone();
				
				//System.out.println(currentRowData[0]);
				
				bean.setCno(currentRowData[0]);
				bean.setDateformat(currentRowData[1]);
				bean.setLocale(currentRowData[2]);
				//bean.setActivationDate(currentRowData[3]);				
				bean.setFirstname(currentRowData[4]);
				bean.setMiddlename(currentRowData[5]);
				bean.setLastname(currentRowData[6]);
				bean.setFullname(currentRowData[7]);
				String d=currentRowData[8];
				bean.setOfficeId(Double.valueOf(d));
				bean.setExternalid(currentRowData[9]);
				bean.setClientCategory(currentRowData[10]);
				bean.setActive(currentRowData[11]);
				bean.setAddressNo(currentRowData[12]);
				bean.setStreet(currentRowData[13]);
				bean.setCity(currentRowData[14]);
				bean.setState(currentRowData[15]);
				bean.setCountry(currentRowData[16]);
				bean.setZipCode(currentRowData[17]);
				bean.setPhone(currentRowData[18]);
				bean.setEmail(currentRowData[19]);
				bean.setLogin(currentRowData[20]);
				bean.setPassword(currentRowData[21]);
			//	bean.setStartTime(date);
				System.out.println(bean.getCno());
				createClient();
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
		writer.write("CNO,");
		writer.write("Result,");
		writer.write("ClientId,");
		writer.write("\n");
	}
	
		/*long ldate1 = System.currentTimeMillis();
		String date1 = new SimpleDateFormat("HH:mm:ss.SSS")
				.format(new Date(ldate1));
		bean.setLastTime(date1);*/
		writer.write(bean.getCno());
		writer.write(",");
		writer.write(bean.getResult());
		writer.write(",");
		if(bean.getClientid()!=null){ 
		writer.write(bean.getClientid());
		writer.write(",");
		}else
		{
			writer.write(",");
		}
		writer.write("\n");
		bean.setClientid(null);
		bean.setResult(null);
	writer.flush();
	writer.close();
	
	}

}
