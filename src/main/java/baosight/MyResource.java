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


    /**用于执行查询的sql语句的接口
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
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
    public String commsearch(String sourcedata,@Context HttpServletRequest request)  {
        String result = "";
        JSONObject errormsg = new JSONObject();
        try {
            JSONObject tmpdata = new JSONObject(sourcedata);
            String sql = tmpdata.getString("sql");
            if(tmpdata.has("pagesize")){
                int pagesize= Integer.parseInt(tmpdata.getString("pagesize")) ;
                int page=Integer.parseInt(tmpdata.getString("page"));
                sql=String.format(utils.getpropertieval("comm_split","/config/sqls.properties"),sql,String.valueOf(pagesize*page),String.valueOf((page-1)*pagesize+1));
            }
            //获取key
           // String DESKey=utils.getDESKey(utils.getpropertieval("GUID","/config/dbconfig.properties"));
            //获取用户名和密码
            String user =utils.decryptBasedDes(utils.getpropertieval("s_dbuser","/config/dbconfig.properties"));
            String password =utils.decryptBasedDes(utils.getpropertieval("s_dbpassword","/config/dbconfig.properties"));
            String dbhost =utils.decryptBasedDes(utils.getpropertieval("s_dbname","/config/dbconfig.properties"));

            JSONObject jresult = dbhelpser.Excutesql(dbhost, user, password, null, null, sql);
            result = jresult.toString();
        } catch (Exception e) {
            try {
                errormsg.accumulate("error",e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            //result = errormsg.toString();
        }
        try {
            JSONObject logobj = new JSONObject();
            logobj.accumulate("cusip",utils.getIpAddr(request));
            logobj.accumulate("method","MyResource.commsearch");
            logobj.accumulate("methodparam",sourcedata);
            logobj.accumulate("result", (errormsg != null) ? "成功" : "失败");
            logobj.accumulate("error",errormsg.has("error")?errormsg.getString("error"):"");
            logobj.accumulate("errtype","MyResource.commsearch");
            utils.createLog(logobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
       }
        return result;
    }

    /*
    * 此接口用于根据网页提供的查询条件传查询后的数据
    * */
    @Path("jsonSearch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String jsonSearch(String jsonString,@Context HttpServletRequest request)  {
        String result="";

        JSONObject errormsg = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            String basictype = jsonObject.getString("basictype");//user，org,job
            String basesql = utils.getpropertieval(String.format("base_%s_sql", basictype), "/config/sqls.properties");

            String conditionstr = "";

            if (jsonObject.has("conditions")) {
                StringBuilder sb = new StringBuilder();
                JSONArray conditions = jsonObject.getJSONArray("conditions");//传过来的条件
                List<JSONObject> list = utils.jsonarray2list(conditions);
                for (int i = 0; i < list.size(); i++) {
                    JSONObject condition = list.get(i);
                    String colname = condition.getString("colname");
                    String colval = condition.getString("colval");
                    String math = condition.has("math") ? condition.getString("math") : "=";
                    String logic = condition.has("logic") ? condition.getString("logic") : "and";
                    String valformat = math.equalsIgnoreCase("like") ? "'%%%s%%'" : "'s'";
                    valformat = colval.startsWith("@TS@") ? "timestamp '%s'" : "'%s'";
                    logic = i == (list.size() - 1) ? "" : logic;
                    sb.append(String.format("%s %s %s %s %s", colname, math, String.format(valformat, colval.replace("@TS@", "")), logic,""));
                }


                if (sb.length() > 0) {
                    sb.insert(0, " where ");
                }
                conditionstr = sb.toString();
            }
            String sqltodo = String.format(basesql, conditionstr);

            JSONObject tmp = new JSONObject();
            tmp.accumulate("sql",sqltodo);
            if(jsonObject.has("pagesize")){
                tmp.accumulate("pagesize",jsonObject.getString("pagesize"));
            }
            if(jsonObject.has("page")){
                tmp.accumulate("page",jsonObject.getString("page"));
            }

            JSONObject jobj =  new JSONObject(commsearch(tmp.toString(),request));
            //JSONObject jobject =dbhelpser.Excutesql(utils.getpropertieval("s_dbname", "/config/dbconfig.properties"), utils.getpropertieval("s_dbuser", "/config/dbconfig.properties"), utils.getpropertieval("s_dbpassword", "/config/dbconfig.properties"), null, null, sqltodo);
            result=jobj.toString();

        } catch (Exception e) {
            try {
                errormsg.accumulate("error",e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            //result = errormsg.toString();
        }

        try {
            JSONObject logobj = new JSONObject();
            logobj.accumulate("cusip",utils.getIpAddr(request));
            logobj.accumulate("method","MyResource.jsonSearch");
            logobj.accumulate("methodparam",jsonString);
            logobj.accumulate("result", (errormsg != null) ? "成功" : "失败");
            logobj.accumulate("error",errormsg.has("error")?errormsg.getString("error"):"");
            logobj.accumulate("errtype","MyResource.jsonSearch");
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
     * 解密的条件多给一个decrypt。如："src":"","decrypt":""
     * @param jsonString
     * @return
     */

    @Path("encryptBasedDES")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String encryptBasedDes(String jsonString){
        String result="";
        JSONObject jsonResult=new JSONObject();
        try {
            jsonResult.accumulate("secretKey",utils.getDESKey(utils.getpropertieval("GUID","/config/dbconfig.properties")));

            JSONObject jsonObject=new JSONObject(jsonString);
            String src=jsonObject.getString("src");
            if(jsonObject.has("decrypt")){
                result=utils.decryptBasedDes(src);
                jsonResult.accumulate("result",result);
                jsonResult.accumulate("source",src);
            }else {
                result=utils.encryptBasedDes(src);
                jsonResult.accumulate("result",result);
                jsonResult.accumulate("source",src);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonResult.toString();
    }

    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String test(String jsonString ,@Context HttpServletRequest request){
        String result=utils.createLog(jsonString);
        //System.out.println(utils.getIpAddr(request));
        return result;
    }

}
