package baosight;




import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.io.*;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
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

    //base64字符串转byte[]
    public static byte[] base642byte(String sourcestr){
        byte[] result = null;
        BASE64Decoder decode = new BASE64Decoder();
        try
        {
            result = decode.decodeBuffer(sourcestr);
        }catch (Exception e){

        }
        return  result;
    }
    //byte[]转base64字符串
    public static  String byte2base64(byte[] sourcebyte){
        String result = "";
        BASE64Encoder encoder = new BASE64Encoder();
        try
        {
            result = encoder.encode(sourcebyte);
        }catch (Exception e){

        }
        return  result;
    }

    //文件转byte[]
    public static byte[] file2byte(String filepath){
        byte[] result = null;
        try(FileChannel fc = new RandomAccessFile(filepath, "r").getChannel()) {
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                    fc.size()).load();
            result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
        } catch (Exception e) {

        }
        return result;
    }

    /**
     * byte转换成file
     * @param filepath
     * @param filename
     * @param sourcebyte
     * @return
     */
    public static String byte2file(String filepath,String filename,byte[] sourcebyte) {
        String result = null;

        File file = null;
        String filefullname = filepath + "\\" + filename;//获取文件全名

        File dir = new File(filepath);
        if (!dir.exists() && dir.isDirectory()) {//判断文件目录是否存在
            dir.mkdirs();
        }

        file = new File(filefullname);

        try (
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos)
        ) {
            bos.write(sourcebyte);
            result = filefullname;
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
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


    /**
     * 加密
     * @param data
     * @return
     */
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


    /**
     * 解密
     * @param cryptData
     * @return
     */
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
        //GUID转换成char数组
        char[] charGUID=GUID.toCharArray();
        for (int i=0; i <16 ; i+=2) {
            eightGUID =eightGUID+charGUID[i];
        }
        eightGUID=eightGUID.contains("-")?eightGUID.replace("-","1"):eightGUID;
        return eightGUID;
    }

    /**
     * 创建日志记录到数据库表logtable中
     * @param jsonString
     * @return
     */
    public static String createLog(String jsonString){

        String log="";
        List cols=new ArrayList();
        List vals=new ArrayList();
        try {
            //转换成json并取出所有字段
            JSONObject jsonObject=new JSONObject(jsonString);
            String customerip=jsonObject.getString("cusip");
            String method=jsonObject.getString("method");
            String methodparam=jsonObject.getString("methodparam");
            String result=jsonObject.getString("result");
            String error=jsonObject.getString("error");
            String errtype=jsonObject.getString("errtype");
            //将时间转成字符串
            /*SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dtime = simpleDateFormat.parse(ddate);*/
            //将数据库列名添加进list对象中
            cols.add("dtime");
            cols.add("customerip");
            cols.add("method");
            cols.add("methodparam");
            cols.add("result");
            cols.add("error");
            cols.add("errtype");

            //vals.add(String.format("timestamp '%s'",dtime));
            //将取出的字段值添加进list对象中
            vals.add("to_char(sysdate,'yyyy-mm-dd hh24:mi:ss')");
            vals.add("'"+customerip+"'");
            vals.add("'"+method+"'");
            vals.add("'"+methodparam+"'");
            vals.add("'"+result+"'");
            vals.add("'"+error+"'");
            vals.add("'"+errtype+"'");

            //获取配置文件中的insert语句并拼写
            String sql=String.format(utils.getpropertieval("insert","/config/sqls.properties"),"logtable", join(cols,","),join(vals,","));

            //解密数据库账号
            String user =utils.decryptBasedDes(utils.getpropertieval("s_dbuser","/config/dbconfig.properties"));
            String password =utils.decryptBasedDes(utils.getpropertieval("s_dbpassword","/config/dbconfig.properties"));
            String dbhost =utils.decryptBasedDes(utils.getpropertieval("s_dbname","/config/dbconfig.properties"));

            //执行
            JSONObject jobject=dbhelpser.Excutesql(dbhost, user, password, sql, null, null);
            //System.out.println(jobject.toString());
            if(jobject.get("data")!=null){
                log=jobject.get("data").toString();
            }else {
                log = "-1";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
       return log;
    }

    /**
     * 获取调用请求的IP地址
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request)  {
        String ip  =  request.getHeader( " x-forwarded-for " );
        if (ip  ==   null   ||  ip.length()  ==   0   ||   " unknown " .equalsIgnoreCase(ip))  {
            ip  =  request.getHeader( " Proxy-Client-IP " );
        }
        if (ip  ==   null   ||  ip.length()  ==   0   ||   " unknown " .equalsIgnoreCase(ip))  {
            ip  =  request.getHeader( " WL-Proxy-Client-IP " );
        }
        if (ip  ==   null   ||  ip.length()  ==   0   ||   " unknown " .equalsIgnoreCase(ip))  {
            ip  =  request.getRemoteAddr();
        }
        return  ip;
    }


    /**
     * 给字符与字符之间添加指定的东西
     * @param sourcearray
     * @param joinstr
     * @return
     */
    public  static String join(List<String> sourcearray,String joinstr){
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<sourcearray.size();i++){
            sb.append(sourcearray.get(i));
            if(i==sourcearray.size()-1){
                break;
            }
            sb.append(joinstr);
        }
        return  sb.toString();
    }

    /**
     * MD5加密
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值

            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            throw new RuntimeException("MD5加密出现错误");
        }
    }

    /**
     *查看字符串中位数如果小于16位，从最前面第0位开始用0补足
     * @param src
     * @return
     */
    public static String fillSeats(String src){
        //转换成StringBuffer，效率比StringBuilder更高
        StringBuffer stringBuffer = new StringBuffer(src);
        //转换成char数组
        char[] chars = src.toCharArray();
        while (chars.length<16){
            //如果小于16位在第0位开始用0补足
            stringBuffer.insert(0,0);
            //赋值给char数组对象
            chars = stringBuffer.toString().toCharArray();
        }
        return stringBuffer.toString();
    }
}


