package config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.jedigames.utils.JediCast;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class Config {
	public static final int WARNNING_MSG_LEN = 1024*1024; //包体大小警告界限：如果包体大于此值，则说明可能存在性能问题。

    public static ConcurrentHashMap<String, String> config=new ConcurrentHashMap<>();
    public static final String[] CONFIG_FILE_LIST = {"config/startconfig/config.json", "config/startconfig/config_server.json"};
    
    /**
     * 判断是否包含某个配置项
     * @param key
     * @return
     */
    public static boolean containsKey(String key) {
		return config.containsKey(key);
	}
    
    /**
     * 获取某个配置,返回String值
     * @param key 
     * @return
     */
    public static String getConfig(String key){
        if(config.containsKey(key)){
            return config.get(key).toString();
        }
        return "";
    }
    
    /**
     * 获取某个配置,并转为boolean值
     * @param key 
     * @return
     */
    public static boolean getConfigBoolean(String key){
        if(config.containsKey(key)){
            return Boolean.parseBoolean(config.get(key));
        }
        return false;
    }
    
    /**
     * 获取某个配置,并转为int值
     * @param key 
     * @return
     */
    public static int getConfigInt(String key){
        if(config.containsKey(key)){
            return JediCast.toInt(config.get(key));
        }
        return 0;
    }
        
    /**
     * 从json文件中获取配置
     */
    public static void initFromJsonFile(){
    	BufferedReader br = null;
    	FileInputStream fis = null;
    	InputStreamReader isr = null;
        try{
            ConcurrentHashMap<String, String> configTmp = new ConcurrentHashMap<>();
            for(String fileName:CONFIG_FILE_LIST){
            	File file = new File(fileName);
            	if(!file.isFile()||!file.exists()){
            		continue;
            	}
            	String absolutePath = file.getAbsolutePath();
	        	fis = new FileInputStream(file);
	        	isr = new InputStreamReader(fis, "UTF-8");
	        	br = new BufferedReader(isr);
	            String jsonString = "";
	            String tempString = null;
	            while ((tempString = br.readLine()) != null) {  
	                jsonString = jsonString + tempString;  
	            }
	            
				JSONObject json = JSON.parseObject(jsonString);
				for (String key : json.keySet()) {
	                if (key.equals("_comment")) {
						continue;
					}
	                if(configTmp.containsKey(key)){
	                	//冲突了报下warning
	                	String errMsg = String.format("Warning: configuration conflict, file[%s]:key[%s]",fileName, key);
	                	System.out.println(errMsg);
	                	continue;
	                }
	                configTmp.put(key, json.get(key).toString());
	            }
            }
            config = configTmp;
        }catch (IOException | JSONException e){
        }finally{
        	safeClose(br);
        	safeClose(isr);
        	safeClose(fis);
        }
    }

	private static void safeClose(FileInputStream fis) {
		if(fis!=null){
            try {
            	fis.close();
			} catch (IOException e) {
			}
    	}
	}

	private static void safeClose(InputStreamReader isr) {
		if(isr!=null){
            try {
            	isr.close();
			} catch (IOException e) {
			}
    	}
	}

	private static void safeClose(BufferedReader br) {
		if(br!=null){
            try {
				br.close();
			} catch (IOException e) {
			}
    	}
	}
}
