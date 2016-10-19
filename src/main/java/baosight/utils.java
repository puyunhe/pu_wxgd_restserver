package baosight;



import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import sun.misc.BASE64Encoder;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.*;

/**
 * Created by YuNan on 2016/9/8.
 */
public class utils {

    //读取Properties文件参数
    //name:参数名称
    //fileURL:配置文件地址
    public static String getpropertieval(String name,String fileURL){
        String result = "";//返回值
        Properties prop = new Properties();//初始化Properties对象
        try(InputStream in = utils.class.getResourceAsStream(fileURL))//获取文件流
        {
            prop.load(in);//加载配置文件流
            result =  prop.getProperty(name);//读取参数并返回
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();//显示异常
        }
        return result;
    }

    /**
     * jsonarry转list
     * @param sourcearray
     * @return
     */
    public  static List<JSONObject> jsonarray2list(JSONArray sourcearray){
        List<JSONObject> result = new ArrayList<JSONObject>();

        try
        {
            for(int i=0;i<sourcearray.length();i++){
                JSONObject tmp = sourcearray.getJSONObject(i);
                result.add(tmp);
                /*for(Iterator it = tmp.keys(); it.next()){

                }*/
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return  result;

    }


    public static String encryptBasedDes(String data) {
        String DES_KEY=utils.getDESKey(utils.getpropertieval("GUID","/config/dbconfig.properties"));
        String encryptedData = null;
        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            DESKeySpec deskey = new DESKeySpec(DES_KEY.getBytes());
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(deskey);
            // 加密对象
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key, sr);
            // 加密，并把字节数组编码成字符串
            encryptedData = new sun.misc.BASE64Encoder().encode(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
//            log.error("加密错误，错误信息：", e);
            throw new RuntimeException("加密错误，错误信息：", e);
        }
        return encryptedData;
    }


    public static String decryptBasedDes(String cryptData) {
        String DES_KEY=utils.getDESKey(utils.getpropertieval("GUID","/config/dbconfig.properties"));
        String decryptedData = null;
        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            DESKeySpec deskey = new DESKeySpec(DES_KEY.getBytes());
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(deskey);
            // 解密对象
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key, sr);
            // 把字符串解码为字节数组，并解密
            decryptedData = new String(cipher.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(cryptData)));
        } catch (Exception e) {
//            log.error("解密错误，错误信息：", e);
            throw new RuntimeException("解密错误，错误信息：", e);
        }
        return decryptedData;
    }

    /**
     * 取GUID码的其中8位
     * @param GUID
     * @return
     */
    public static String getDESKey(String GUID){
        String eightGUID="";
        char[] charGUID=GUID.toCharArray();
        for (int i=0; i <16 ; i+=2) {
            eightGUID =eightGUID+charGUID[i];
        }
        eightGUID=eightGUID.contains("-")?eightGUID.replace("-","1"):eightGUID;
        return eightGUID;
    }
}


