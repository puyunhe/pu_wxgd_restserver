package baosight;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
}


