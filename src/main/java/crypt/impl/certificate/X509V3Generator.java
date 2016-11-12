package crypt.impl.certificate;

import crypt.api.certificate.CertificateGenerator;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.Security;
import java.math.BigInteger;
import java.util.Date;

import java.security.cert.Certificate;
import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.*;


public class X509V3Generator implements CertificateGenerator
{
	/**
	 * Use this methode tu create an instance of X509V3Genertor.
	 * @param config_file Name of the file that will be used by the instance.
	 */
	static public X509V3Generator getInstance(String config_file) throws Exception
	{
		X509V3Generator cert_gen = new X509V3Generator();
		cert_gen.setConfigFile(config_file);
		cert_gen.initDatas();
		return cert_gen;
	}
	///////////////////////////// private //////////////////////////////////

	private String config_file;        //Configuration file (certificate datas).
	private boolean flag = false;	   //To kown if a certifictae has already been created.

	private String keystore_password;  //Password for the keystore. can use toCharArray()
	private String ks_alias;
	private KeyStore ks;               //KeyStore that will contain the certificate.
	private Certificate cert;          //The certificate.
	private KeyPair key_pair;          //Generated keys for the certificate.

	//Certificate datas.
	private BigInteger serial_number;
	private String domain_name;		   //Server's IP.
	//private Date begin;              //Start validity date.
	//private Date end;                //End validity date.
	private String sign_alg;           //Algorithm used to sign.
	

	////////////////////////////// Public ////////////////////////////////
	
	//// Geters&Seters ////


	/**
	 * Initalization of datas from file.
	 * @author Sébastien Pelletier
	 */
	public KeyPair getKeysPair() throws Exception
	{
		if( !this.flag )
		{
			System.out.println("getKeyPair() used wihout certificate genereated");
			System.exit(1);
		}
		return key_pair;
	}
	
	/**
	 * Get the Keystore password
	 * @author Sébastien Pelletier
	 */
	public String getKsPassword() throws Exception
	{
		return keystore_password;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setConfigFile(String file) throws Exception
	{
		this.config_file = file;
	}
	
	///// Methodes /////

	/**
	 * {@inheritDoc}
	 * file pattern : 
	 * data=value
	 * data1=value1
	 */
	@Override
	public void initDatas() throws Exception
	{
		try
		{
			File file = new File(this.config_file);
			
			//Create file if it does not exists.
			if( !file.exists() )
			{
				createDefaultConfigFile(this.config_file);
			}

			InputStream input_stream              = new FileInputStream(file); 
			InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
			BufferedReader buffered_reader        = new BufferedReader(input_stream_reader);
			String line;

			String name;	//Configuration name.
			String value;   //Value to this name.
			String[] temp;
			

			while( (line = buffered_reader.readLine()) != null )
			{
				if( !line.isEmpty() && !line.startsWith("#") && !line.startsWith(" ") ) //if not a comment or newline.
				{
					line = line.split("#")[0];  //Delete comments.
					line = line.split(" ")[0];  //Delete useless spaces.
					line = line.split("\t")[0]; //Delete useless tabs.
					temp = line.split("=");
					name = temp[0];
					value = temp[1];
					switch(name)
					{
						case "kspassword" :
							this.keystore_password = value;
							break;
						case "ksalias":
							this.ks_alias = value;
							break;
						case "serialnumber" :
							this.serial_number = new BigInteger(value);
							break;
						case "signalgorithm":
							this.sign_alg = value;
							break;
						case "domainname":
							this.domain_name = value;
							break;
						default:
							System.out.println("Bad configuration file : " + line);
							System.exit(1);
						break;
					}
					//System.out.println(name + " = " + value);
				}
			}

			buffered_reader.close(); 
		}		
		catch( Exception e )
		{
			System.out.println(e.toString());
			System.exit(1);
		}
		
	}

	/**
	 * Create the default certificate configuration file.
	 * @param file_name Name of the file that will be created.
	 */
	public void createDefaultConfigFile(String file_name) throws Exception
	{
		String content = "";
		content += "# This configuration file contains values for the certificate\n";
		content += "# used by this application. The certificate will be stored in\n";
		content += "# keystore.jks .\n\n";
		content += "kspassword=123456        #Password for the keystore.\n";
		content += "ksalias=SXP              #Alias for the certificate in the keystore.\n";
		content += "serialnumber=0123456789  #Serial Number.\n";
		content += "signalgorithm=MD5WithRSA #Algorithm used for siging the certificate. (SHA256withRSA)\n";
		content += "domainname=localhost     #IP / domain name of the serveur.\n";

		File file = new File(this.config_file);
		try
		{
			FileWriter file_writer = new FileWriter(file);
			file_writer.write (content);
			file_writer.close();
		}
		catch (IOException exception)
		{
			System.out.println ("Error while creation of default configuration file : " 
								+ this.config_file + "\n" + exception.getMessage() );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Certificate CreateCertificate() throws Exception
	{
		if( !this.flag )
		{
			//Provider custom
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

			//Keys (priv & public) generation.																							
			KeyPairGenerator key_gen = KeyPairGenerator.getInstance("RSA");															
			key_gen.initialize(1024);																												
			KeyPair keys = key_gen.genKeyPair();
			this.key_pair = keys;

			//Création du certificat.																												
			X509V3CertificateGenerator cert_gen = new X509V3CertificateGenerator();

			X500Principal cn = new X500Principal("CN=" + domain_name );																		
			cert_gen.setSerialNumber(this.serial_number);																		
			cert_gen.setIssuerDN(cn);																												
			cert_gen.setNotBefore(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));									
			cert_gen.setNotAfter(new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000));							
			cert_gen.setSubjectDN(cn);																												
			cert_gen.setPublicKey(keys.getPublic());																							
			cert_gen.setSignatureAlgorithm(this.sign_alg);
		
			this.cert = cert_gen.generateX509Certificate(keys.getPrivate(), "BC"); //CA private key (autosigned)
			this.flag = true;
		}
		return this.cert;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Certificate[] CreateChainCertificate() throws Exception
	{
		if( !this.flag )
			this.CreateCertificate();

		Certificate[] cert_chain = new X509Certificate[1];																		
		cert_chain[0] = this.cert;
		return cert_chain;
	}

	/**
	 * Store the created certificate in the specified file (keystore).
	 * @param file_name The keystore file.
	 * #Author Pelletier Sébastien
	 */
	public void StoreInKeystore(String file_name) throws Exception
	{																														
		char[] password = this.keystore_password.toCharArray();																				
		KeyStore.PasswordProtection protected_password = new KeyStore.PasswordProtection(password);	

		//Keystore Creation																												
		KeyStore ks = KeyStore.getInstance("jks");																						
		ks.load(null, password);  //Loading from nothing (null) in order to create a new one.
	
		Certificate[] cert_chain = this.CreateChainCertificate();

		ks.setEntry(this.ks_alias,																														
				new KeyStore.PrivateKeyEntry(key_pair.getPrivate(), cert_chain),														
				new KeyStore.PasswordProtection(password));

		//Storing the created key in the file.																						
		java.io.FileOutputStream fos = null;																								
		try 																																			
		{																																				
			fos = new java.io.FileOutputStream(file_name);																		
			ks.store(fos, password);																											
		}
		finally
		{
			if(fos != null)
				fos.close();
		}
		
	}
	
}
/*
		//Provider custom
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());								
																													 
		//Mot de passe.																															
		char[] password = {'1', '2', '3', '4', '5', '6'};																				
		KeyStore.PasswordProtection protected_password = new KeyStore.PasswordProtection(password);						
																																						
		//Création du keystore.																													
		KeyStore ks = KeyStore.getInstance("jks");																						
		ks.load(null, password);  //Chargement à partir de rien (creation du keystore et non importation).				
																																						
		//Création de la paire de clef.																										
		KeyPairGenerator key_gen = KeyPairGenerator.getInstance("RSA");															
		key_gen.initialize(1024);																												
		KeyPair keys = key_gen.genKeyPair();																								
																																						
		//Création du certificat.																												
		X509V3CertificateGenerator cert_gen = new X509V3CertificateGenerator();													
																																						
		X500Principal cn = new X500Principal("CN=SXP");		// nom de domaine																			
		cert_gen.setSerialNumber(new BigInteger("123456789"));																		
		cert_gen.setIssuerDN(cn);																												
		cert_gen.setNotBefore(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));									
		cert_gen.setNotAfter(new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000));							
		cert_gen.setSubjectDN(cn);																												
		cert_gen.setPublicKey(keys.getPublic());																							
		cert_gen.setSignatureAlgorithm("MD5WithRSA"); //SHA256withRSA																


		X509Certificate[] cert_chain = new X509Certificate[1];																		
		cert_chain[0] = cert_gen.generateX509Certificate(keys.getPrivate(), "BC"); //CA private key (autosigned here)		
																																						
		ks.setEntry("SXP",																														
				new KeyStore.PrivateKeyEntry(keys.getPrivate(), cert_chain),														
				new KeyStore.PasswordProtection(password));
																																						

		//Enregistement du keystore dans un fichier.																						
		java.io.FileOutputStream fos = null;																								
		try 																																			
		{																																				
			fos = new java.io.FileOutputStream("keystore.jks");																		
			ks.store(fos, password);																											
		}
		finally
		{
			if(fos != null)
				fos.close();
		}

*/



