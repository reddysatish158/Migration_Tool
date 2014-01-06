package com.hugo.source;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;


import com.hugo.util.Bean;




public class Main {
	static Logger logger = Logger.getLogger(Main.class);
	static Properties prop = new Properties();
	public static String methodName;
	public static String fileName;
/*	public static XSSFWorkbook wb;*/
	public static int countno;
	public static int endno;
	public static int i;
	public static String destination;
	static Bean bean = new Bean();

	public static void main(String[] args) throws Exception {
		try {
			prop.load(new FileInputStream("Migrate.properties"));
			if (args.length > 0) {
				methodName = args[0];
				fileName = args[1];		
				destination=args[2];
			}

			if (methodName.trim().equalsIgnoreCase("client")) {
				/*String w1 = prop.getProperty("startClientno");
				String w2 = prop.getProperty("endClientno");
				int x1 = Integer.parseInt(w1);
				int x2 = Integer.parseInt(w2);*/
				
			TestClient.readClient(fileName,destination);

			} else if (methodName.trim().equalsIgnoreCase("order")) {
				/*String w1 = prop.getProperty("StartOrderno");
				String w2 = prop.getProperty("endOrderno");
				int x1 = Integer.parseInt(w1);
				int x2 = Integer.parseInt(w2);
				*/
				TestOrder.readOrderFile(fileName,destination);
				
			} else if (methodName.trim().equalsIgnoreCase("invoice")) {
				
				String w1 = prop.getProperty("StartInvoiceno");
				String w2 = prop.getProperty("endInvoiceno");
				int x1 = Integer.parseInt(w1);
				int x2 = Integer.parseInt(w2);
				//TestInvoice.readInvoiceFile(wb,x1,x2);
			}else if (methodName.trim().equalsIgnoreCase("onetimesale")) {
				
				/*String w1 = prop.getProperty("Startonetimesaleno");
				String w2 = prop.getProperty("endonetimesaleno");
				int x1 = Integer.parseInt(w1);
				int x2 = Integer.parseInt(w2);*/
				TestOnetimeSale.readOnetimeSale(fileName,destination);
			}else if (methodName.trim().equalsIgnoreCase("orderdisconnect")) {
				
				String w1 = prop.getProperty("Startorderdisconnno");
				String w2 = prop.getProperty("endorderdisconnno");
				int x1 = Integer.parseInt(w1);
				int x2 = Integer.parseInt(w2);
				//TestOrderDisconnection.readInvoiceFile(wb,x1,x2);
			}else if (methodName.trim().equalsIgnoreCase("orderreconnect")) {
				
				String w1 = prop.getProperty("Startorderreconnno");
				String w2 = prop.getProperty("endorderreconnno");
				int x1 = Integer.parseInt(w1);
				int x2 = Integer.parseInt(w2);
				//TestOrderReconnection.readInvoiceFile(wb,x1,x2);
			}
			else if (methodName.trim().equalsIgnoreCase("clientbalance")) {
				 
		        TestClientBalance.readClientBalanceFile(fileName,destination);	 
			}
			else if (methodName.trim().equalsIgnoreCase("orderrenewal")){
				
				OrderRenewal.readOrderRenewalFile(fileName,destination);
				
			}

		} catch (Exception e) {

			logger.error("failure :Exceptions happen!", e);

		}
	}
}
