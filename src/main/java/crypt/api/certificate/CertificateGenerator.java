package crypt.api.certificate;

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
}
