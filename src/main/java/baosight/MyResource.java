package baosight;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.*;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {


    /**
     * 用于执行查询的sql语句的接口
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *@Context HttpServletRequest request 获取调用者的IP
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }

    @Path("commsearch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String commsearch(String sourcedata, @Context HttpServletRequest request) {
        String result = "";
        //记录错误信息
        JSONObject errormsg = new JSONObject();
        try {
            //将传入变量转换成JSON类型
            JSONObject tmpdata = new JSONObject(sourcedata);
            //获取sql语句
            String sql = tmpdata.getString("sql");
            //判断传入的数据中是否有须要分页的属性，如果有，组成分页sql
            if (tmpdata.has("pagesize")) {
                int pagesize = Integer.parseInt(tmpdata.getString("pagesize"));
                int page = Integer.parseInt(tmpdata.getString("page"));
                sql = String.format(utils.getpropertieval("comm_split", "/config/sqls.properties"), sql, String.valueOf(pagesize * page), String.valueOf((page - 1) * pagesize + 1));
            }
            //获取key
            // String DESKey=utils.getDESKey(utils.getpropertieval("GUID","/config/dbconfig.properties"));
            //获取用户名和密码
            //JSONObject jresult = dbhelpser.Excutesql("jdbc:oracle:thin:@localhost:1521:orcl", "puyunhe", "tiger", null, null, sql);

            //解密数据库账户和地址
            String user = utils.decryptBasedDes(utils.getpropertieval("s_dbuser", "/config/dbconfig.properties"));
            String password = utils.decryptBasedDes(utils.getpropertieval("s_dbpassword", "/config/dbconfig.properties"));
            String dbhost = utils.decryptBasedDes(utils.getpropertieval("s_dbname", "/config/dbconfig.properties"));

            //连接数据库并执行查询sql
            JSONObject jresult = dbhelpser.Excutesql(dbhost, user, password, null, null, sql);
            //把结果转成String类型
            result = jresult.toString();

        } catch (Exception e) {
            try {
                //将错误信息传入JSON对象
                errormsg.accumulate("error", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            //result = errormsg.toString();
        }
        try {
            //创建JSON对象传入键值对
            JSONObject logobj = new JSONObject();
            //调用者地址
            logobj.accumulate("cusip", utils.getIpAddr(request));
            //调用的方法名称
            logobj.accumulate("method", "MyResource.commsearch");
            //调用的方法的参数
            logobj.accumulate("methodparam", sourcedata.contains("'")?sourcedata.replaceAll("'","@"):sourcedata);//把单引号替换成@符号
            //最后的结果为成功或者失败
            logobj.accumulate("result", (errormsg != null) ? "成功" : "失败");
            //是否有错误信息，有就记录
            logobj.accumulate("error", errormsg.has("error") ? errormsg.getString("error") : "");
            //记录错误类型为调用的哪个方法
            logobj.accumulate("errtype", "MyResource.commsearch");
            //创建日志
            utils.createLog(logobj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
    * 此接口用于根据网页提供的查询条件传查询后的数据
    * 查询条件：basictype(其他可以没有，但这个必须有)：user,org,jog
    *           pagesize，page（分页查询的条件）
    *           conditions（JSONArray对象）：colname，colval，math，logic
    * */
    @Path("jsonSearch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String jsonSearch(String jsonString, @Context HttpServletRequest request) {
        String result = "";
        //记录错误信息
        JSONObject errormsg = new JSONObject();
        try {
            //将传入的数据转换成JSON类型
            JSONObject jsonObject = new JSONObject(jsonString);
            //取出其中的数据
            String basictype = jsonObject.getString("basictype");//user，org,job
            //读取sql语句
            String basesql = utils.getpropertieval(String.format("base_%s_sql", basictype), "/config/sqls.properties");

            String conditionstr = "";
            //condition是JSONArray对象，判断JSON中是否有此对象
            if (jsonObject.has("conditions")) {
                StringBuilder sb = new StringBuilder();
                JSONArray conditions = jsonObject.getJSONArray("conditions");//传过来的条件
                //转成list类型
                List<JSONObject> list = utils.jsonarray2list(conditions);
                //循环取出list中的数据
                for (int i = 0; i < list.size(); i++) {
                    //先取出数据之后放入JSON中
                    JSONObject condition = list.get(i);
                    //依次取出JSON中的对象
                    String colname = condition.getString("colname");
                    String colval = condition.getString("colval");
                    String math = condition.has("math") ? condition.getString("math") : "=";
                    String logic = condition.has("logic") ? condition.getString("logic") : "and";
                    //math的值如果是like，valformat取%%s%，不是取%s
                    String valformat = math.equalsIgnoreCase("like") ? "'%%%s%%'" : "'%s'";
                    //以@TS@开头的字段前加timestamp
                    if(colval.startsWith("@TS@")) {
                        valformat = "timestamp '%s'" ;
                    }
                    //判断逻辑符号是否还有，没有就取空
                    logic = i == (list.size() - 1) ? "" : logic;
                    //拼写语句，valformat的值作一下处理
                    sb.append(String.format("%s %s %s %s %s", colname, math, String.format(valformat, colval.replace("@TS@", "")), logic, ""));
                }
                //判断拼写对象中是否有值，如果有在前面添加where
                if (sb.length() > 0) {
                    sb.insert(0, " where ");
                }
                conditionstr = sb.toString();
            }
            //给读取后的sql赋值
            String sqltodo = String.format(basesql, conditionstr);
            //创建JSON对象，传入sql和分页条件
            JSONObject tmp = new JSONObject();
            tmp.accumulate("sql", sqltodo);
            if (jsonObject.has("pagesize")) {
                tmp.accumulate("pagesize", jsonObject.getString("pagesize"));
            }
            if (jsonObject.has("page")) {
                tmp.accumulate("page", jsonObject.getString("page"));
            }
            //用commsearch方法执行sql,并获取调用者IP
            JSONObject jobj = new JSONObject(commsearch(tmp.toString(), request));
            //JSONObject jobject =dbhelpser.Excutesql(utils.getpropertieval("s_dbname", "/config/dbconfig.properties"), utils.getpropertieval("s_dbuser", "/config/dbconfig.properties"), utils.getpropertieval("s_dbpassword", "/config/dbconfig.properties"), null, null, sqltodo);
            //执行后的结果转成String给返回值赋值
            result = jobj.toString();

        } catch (Exception e) {
            try {
                errormsg.accumulate("error", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            //result = errormsg.toString();
        }

        try {
            JSONObject logobj = new JSONObject();
            logobj.accumulate("cusip", utils.getIpAddr(request));
            logobj.accumulate("method", "MyResource.jsonSearch");
            logobj.accumulate("methodparam", jsonString);
            logobj.accumulate("result", (errormsg != null) ? "成功" : "失败");
            logobj.accumulate("error", errormsg.has("error") ? errormsg.getString("error") : "");
            logobj.accumulate("errtype", "MyResource.jsonSearch");
            //创建日志到数据库
            utils.createLog(logobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * DES加解密
     * 加密只要给加密的内容，如："src":"content"
     * 解密的条件多给一个decrypt，值可以为空。如："src":"","decrypt":""
     *
     * @param jsonString
     * @return
     */

    @Path("encryptBasedDES")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String encryptBasedDes(String jsonString) {
        String result = "";
        //创建JSON一会要返回
        JSONObject jsonResult = new JSONObject();
        try {
            //获取密钥传入JSON
            jsonResult.accumulate("secretKey", utils.getDESKey(utils.getpropertieval("GUID", "/config/dbconfig.properties")));
            //将传入的数据转换成JSON
            JSONObject jsonObject = new JSONObject(jsonString);
            String src = jsonObject.getString("src");
            //判断是否有decrypt，有就调用解密的方法
            if (jsonObject.has("decrypt")) {
                result = utils.decryptBasedDes(src);
                //解密源和解密后的结果传入要返回的JSON
                jsonResult.accumulate("result", result);
                jsonResult.accumulate("source", src);
            } else {
                result = utils.encryptBasedDes(src);
                jsonResult.accumulate("result", result);
                jsonResult.accumulate("source", src);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //返回解密后的JSON
        return jsonResult.toString();
    }

    /**
     * 此代码段为测试日志程序用的
     *
     * @param jsonString
     * @param request
     * @return
     */
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String test(String jsonString, @Context HttpServletRequest request) {
//        String result=utils.createLog(jsonString);
        //System.out.println(utils.getIpAddr(request));
//        String user =utils.decryptBasedDes(utils.getpropertieval("s_dbuser","/config/dbconfig.properties"));
        return utils.getIpAddr(request);
    }

    /**
     * 此为测试代码
     * @param jsonString
     * @param request
     * @return
     */
    @Path("testex")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String testex(String jsonString, @Context HttpServletRequest request) {
        String user = utils.decryptBasedDes(utils.getpropertieval("s_dbuser", "/config/dbconfig.properties"));
        String password = utils.decryptBasedDes(utils.getpropertieval("s_dbpassword", "/config/dbconfig.properties"));
        String dbhost = utils.decryptBasedDes(utils.getpropertieval("s_dbname", "/config/dbconfig.properties"));

        JSONObject jresult = dbhelpser.Excutesql(dbhost, user, password, null, null, "select count(*) from usertable");
        return jresult.toString();
    }


    /**
     * 给出用户ID查询出该用户的所有信息
     * @param jsonString
     * @return
     */
    @Path("getUserInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String getUserInfo(String jsonString,@Context HttpServletRequest request) {
        String result="";
        JSONObject errormsg = new JSONObject();
        try {
            //将传入的数据转换成JSON
            JSONObject jsonObject = new JSONObject(jsonString);
            //获取userid
            String userID = jsonObject.getString("userid");
            //读取和拼写sql
            String sql = utils.getpropertieval("select", "/config/sqls.properties");
            sql = String.format(sql,"usertable","where psnid =", "'"+userID+"'");

            //将sql传入JSON并通过commsearch执行
            JSONObject tmp = new JSONObject();
            tmp.accumulate("sql", sql);
            JSONObject jobject = new JSONObject(commsearch(tmp.toString(),request));
            result=jobject.toString();
        } catch (Exception e) {
            try {
                //获取错误信息
                errormsg.accumulate("error",e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        //添加日志
        try {
            JSONObject logobj = new JSONObject();
            logobj.accumulate("cusip", utils.getIpAddr(request));
            logobj.accumulate("method", "MyResource.getUserInfo");
            logobj.accumulate("methodparam", jsonString);
            logobj.accumulate("result", (errormsg != null) ? "成功" : "失败");
            logobj.accumulate("error", errormsg.has("error") ? errormsg.getString("error") : "");
            logobj.accumulate("errtype", "MyResource.getUserInfo");
            utils.createLog(logobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}