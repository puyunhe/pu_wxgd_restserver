package baosight;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {


    /**
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
    public String commsearch(String sourcedata) {
        String result = "";

        try {
            JSONObject tmpdata = new JSONObject(sourcedata);
            String sql = tmpdata.getString("sql");
            if(tmpdata.has("pagesize")){
                int pagesize= Integer.parseInt(tmpdata.getString("pagesize")) ;
                int page=Integer.parseInt(tmpdata.getString("page"));
                sql=String.format(utils.getpropertieval("comm_split","/config/sqls.properties"),sql,String.valueOf(pagesize*page),String.valueOf((page-1)*pagesize+1));
            }


            JSONObject jresult = dbhelpser.Excutesql(utils.getpropertieval("s_dbname", "/config/dbconfig.properties"), utils.getpropertieval("s_dbuser", "/config/dbconfig.properties"), utils.getpropertieval("s_dbpassword", "/config/dbconfig.properties"), null, null, sql);
            result = jresult.toString();
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
    public String jsonSearch(String jsonString) {
        String result="";
        try {
            JSONObject jsonObject = new JSONObject(jsonString);


            String basictype = jsonObject.getString("basictype");//user，org,job
            String basesql = utils.getpropertieval(String.format("base_%s_sql", basictype), "/config/sqls.properties");

            String conditionstr = "";

            if (jsonObject.has("conditions")) {
                StringBuilder sb = new StringBuilder();
                JSONArray conditions = jsonObject.getJSONArray("conditions");//传过来的条件
                List<JSONObject> list = JsonToList.jsonarray2list(conditions);
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

            JSONObject jobj =  new JSONObject(commsearch(tmp.toString()));
            //JSONObject jobject =dbhelpser.Excutesql(utils.getpropertieval("s_dbname", "/config/dbconfig.properties"), utils.getpropertieval("s_dbuser", "/config/dbconfig.properties"), utils.getpropertieval("s_dbpassword", "/config/dbconfig.properties"), null, null, sqltodo);
            result=jobj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}