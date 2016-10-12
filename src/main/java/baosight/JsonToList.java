package baosight;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/29.
 */
public class JsonToList {
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

}
