package crypt.impl.certificate;

import crypt.api.certificate.CertificateGenerator;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.Security;
import java.math.BigInteger;
import java.util.Date;

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
	///////////////////////////// private //////////////////////////////////

	private String config_file;        //Configuration file (certificate datas).

	private char[] keystore_password;  //Password for the keysore.
	private KeyStore ks;               //KeyStore that will contain the certificate.
	private KeyPair keys;              //Generated keys for the certificate.

	//Certificate datas.
	private BigInteger serial_number;
	private X500Principal issuerDN;
	//private Date begin;                //Start validity date.
	//private Date end;                  //End validity date.
	private String sign_alg;           //Algorithm used to sign.
	

	////////////////////////////// Public ////////////////////////////////
	
	//// Geters&Seters ////

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
	 */
	@Override
	public void initDatas() throws Exception
	{
		/*
			file pattern : 
			data=value
			data1=value
		*/

		/// Read config file ///
		try
		{
			File file = new File(this.config_file);
			//tester si le fichier existe

			InputStream input_stream              = new FileInputStream(file); 
			InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
			BufferedReader buffered_reader        = new BufferedReader(input_stream_reader);
			String line;

			String name;	//Configuration name.
			String value;   //Value to this name.
			String[] temp;
			

			while( (line = buffered_reader.readLine()) != null )
			{
				if(  !line.isEmpty() && !line.startsWith("#") ) //if not a comment or newline.
				{
					System.out.println(line);
					temp = line.split("=");
					name = temp[0];
					value = temp[1];
					switch(name)
					{
						case "serialnumber" :
							this.serial_number = new BigInteger(value);
							break;
						case "signalgorithm":
							this.sign_alg = value;
							break;
						default:
							System.out.println("Bad configuration file : " + line);
							System.exit(1);
						break;
					}
				}
			}

			buffered_reader.close(); 
		}		
		catch( Exception e )
		{
			System.out.println(e.toString());
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



