package crypt.api.certificate;

import java.security.KeyPair;
import java.security.cert.Certificate;

/**
 * Use to create Certificate (objet and file (jks)
 * @author Sébastien Pelletier
 *
 */
public interface CertificateGenerator 
{
	/**
	 * Get the configuration file. (containing certificate information).
	 * @param file Path to the configuration file.
	 * @author Sébastien Pelletier
	 */
	public void setConfigFile(String file) throws Exception;

	/**
	 * Get the keyPair used for the certificate.
	 * @author Sébastien Pelletier
	 */
	public void initDatas() throws Exception;
	
	/**
	 * Create the certificate and keys for it.
	 * @author Sébastien Pelletier
	 */
	public Certificate CreateCertificate() throws Exception;

	/**
	 * Create a certificate chain of one certificate.
	 * @author Sébastien Pelletier
	 */
	public Certificate[] CreateChainCertificate() throws Exception;


}
