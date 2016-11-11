package crypt.api.certificate;


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
	 * Initalization of datas from file.
	 * @author Sébastien Pelletier
	 */
	public void initDatas() throws Exception;
	
	/**
	 * Create the certificate.
	 * @author Sébastien Pelletier
	 */
	public Certificate CreateCertificate() throws Exception;

	/**
	 * Create a certificate chain of one certificate.
	 * @author Sébastien Pelletier
	 */
	public Certificate[] CreateChainCertificate() throws Exception;


}
