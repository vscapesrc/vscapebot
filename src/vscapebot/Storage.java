package vscapebot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Storage {
	
	static String DATA_DIR_NAME = "vscape";
	static String SLASH = System.getProperty("file.separator");
	static String HOME_PATH = System.getProperty("user.home");
	static String DATA_PATH = HOME_PATH + SLASH + DATA_DIR_NAME;
	
	static File DATA_DIR_FILE = null;
	static String CLIENT_NAME = "vidyascape.jar";
	static String CLIENTVERSION_NAME = "clientVersion.dat";
	
	static String REMOTE_URL = "https://dl.dropboxusercontent.com/u/31306161/vscape/client/vidyascape.jar";
	static String REMOTE_VERSION_URL = "https://dl.dropboxusercontent.com/u/31306161/vscape/clientVersion.dat";
	
	public static File getDataDirFile() throws IOException {
		if(DATA_DIR_FILE == null) {
			File dataDir = new File(DATA_PATH);
			if(dataDir.exists() == false) {
				if(dataDir.mkdir() == false) {
					throw new IOException("Unable to create data directory " + DATA_PATH);
				}
			}
			else if(dataDir.isDirectory() == false) {
				throw new IOException("Data directory path (" + DATA_PATH + ") points to a file");
			}
			
			DATA_DIR_FILE = dataDir;
		}
		
		return DATA_DIR_FILE;
	}
	
	public static String clientJarPath() {
		try {
			return getDataDirFile() + SLASH + CLIENT_NAME;
		} catch (IOException e) {
			return "";
		}
	}
	
	public static String clientVersionPath() {
		try {
			return getDataDirFile() + SLASH + CLIENTVERSION_NAME;
		} catch (IOException e) {
			return "";
		}
	}
	
	public static boolean clientExists(){
		return new File(clientJarPath()).exists();
	}
	
	public static boolean clientVersionExists(){
		return new File(clientVersionPath()).exists();
	}
	
	
	public static String retrieveClientVersion() {
		
		try {
			BufferedReader var27 = new BufferedReader(new InputStreamReader((new URL(REMOTE_VERSION_URL)).openStream()));
			return var27.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static String localClientVersion() {
		String version = "";
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(clientVersionPath())));
			version = br.readLine();
			br.close();
		}
		catch(IOException e) {
			System.err.println("Couldn't get local client version");
		}
		
		if(br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return version;
	}
	
	public static void writeLocalVersion(String version) {
		BufferedWriter bw;
		
		try {
			new File(clientVersionPath()).delete();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(clientVersionPath())));
			bw.write(version);
			bw.newLine();
			bw.flush();
			bw.close();
		}
		catch(IOException e) {
			
		}
	}
	
	public static void downloadClient() throws MalformedURLException, IOException {
		File localClient = new File(clientJarPath());
		
		InputStream is = (new URL(REMOTE_URL)).openStream();
		FileOutputStream fos = new FileOutputStream(localClient);
		
		
		byte[] var2 = new byte[4096];

	      int var4;
	      try {
	         while((var4 = is.read(var2)) != -1) {
	            fos.write(var2, 0, var4);
	         }
	      } catch (IOException var5) {
	         System.err.println("Error downloading client.");
	      }
	      
	      fos.flush();
	      fos.close();
	}
	
	private static final char[] digestCharacters = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	   public static String getLocalClientMD5String() throws Throwable {
	      File localJarFile = new File(clientJarPath());

	      try {
	         Throwable var1 = null;

	         try {
	            FileInputStream var12 = new FileInputStream(localJarFile);

	            String var10000;
	            try {
	               var10000 = getHashString(var12, "MD5");
	            } finally {
	               var12.close();
	            }

	            return var10000;
	         } catch (Throwable var10) {
	            if(var1 == null) {
	               var1 = var10;
	            }

	            throw var1;
	         }
	      } catch (Exception var11) {
	         var11.printStackTrace();
	         return null;
	      }
	   }

	   public static String getRemoteClientMD5String() throws Throwable {
	      try {
	         Throwable var0 = null;

	         try {
	            InputStream is = (new URL(REMOTE_URL)).openStream();

	            String md5String;
	            try {
	               md5String = getHashString(is, "MD5");
	            } finally {
	               if(is != null) {
	                  is.close();
	               }

	            }

	            return md5String;
	         } catch (Throwable var10) {
	            if(var0 == null) {
	               var0 = var10;
	            }

	            throw var0;
	         }
	      } catch (Exception var11) {
	         var11.printStackTrace();
	         return null;
	      }
	   }

	   private static String getHashString(InputStream is, String algorithm) {
	      byte[] var2 = new byte[4096];
	      MessageDigest var3;
	      (var3 = getMessageDigest(algorithm)).reset();

	      int var4;
	      try {
	         while((var4 = is.read(var2)) != -1) {
	            var3.update(var2, 0, var4);
	         }
	      } catch (IOException var5) {
	         System.err.println("Error making a \'" + algorithm + "\' digest on the inputstream");
	      }

	      return md5DigestToString(var3.digest());
	   }

	   private static String md5DigestToString(byte[] var0) {
	      int var1;
	      char[] var2 = new char[(var1 = var0.length) << 1];
	      int var3 = 0;

	      for(int var4 = 0; var4 < var1; ++var4) {
	         var2[var3++] = digestCharacters[var0[var4] >> 4 & 15];
	         var2[var3++] = digestCharacters[var0[var4] & 15];
	      }

	      return new String(var2);
	   }

	   private static MessageDigest getMessageDigest(String algorithm) {
	      MessageDigest var1 = null;

	      try {
	         var1 = MessageDigest.getInstance(algorithm);
	      } catch (NoSuchAlgorithmException var2) {
	         System.err.println("The \'" + algorithm + "\' algorithm is not available");
	      }

	      return var1;
	   }
}
