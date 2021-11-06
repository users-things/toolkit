package ennuo.craftworld.swing;

import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.utilities.StringUtils;

public class SearchParameters {
   public String path;
   public ResourcePtr pointer;
   
   public SearchParameters(String query) {
        this.path = query.toLowerCase().replaceAll("\\s", "");
        if (query.startsWith("res:")) {
            this.pointer = new ResourcePtr();
            String res = this.path.substring(4);
            if (res.startsWith("g"))
                pointer.GUID = StringUtils.getLong(res);
            else if (res.startsWith("h") && res.length() == 41)
                pointer.hash = Bytes.toBytes(res.substring(1));
        }
   }  
}
