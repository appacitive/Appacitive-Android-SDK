package com.appacitive.core.model;

import com.appacitive.core.AppacitiveDevice;
import com.appacitive.core.AppacitiveEntity;
import com.appacitive.core.AppacitiveObject;
import com.appacitive.core.AppacitiveUser;
import com.appacitive.core.apjson.APJSONException;
import com.appacitive.core.apjson.APJSONObject;
import com.appacitive.core.infra.APSerializable;
import com.appacitive.core.infra.SystemDefinedPropertiesHelper;

import java.io.Serializable;

/**
 * Created by sathley.
 */
public class AppacitiveEndpoint implements Serializable, APSerializable {

    public AppacitiveEndpoint() {
    }

    @Override
    public APJSONObject getMap() throws APJSONException {
        APJSONObject nativeMap = new APJSONObject();

        nativeMap.put("label", this.label);
        nativeMap.put("type", this.type);
        nativeMap.put("objectid", String.valueOf(this.objectId));
        if (this.object != null)
            nativeMap.put("object", this.object.getMap());
        if(this.name != null && this.name.isEmpty() == false)
            nativeMap.put("name", name);
        return nativeMap;
    }

    public String label = null;

    public String type = null;

    public AppacitiveObjectBase object = null;

    public long objectId = 0;

    private String name = null;

    protected void setName(String name)
    {
        this.name = name;
    }

    public void setSelf(APJSONObject endpoint) {
        if (endpoint != null) {

            this.label = endpoint.optString("label", null);
            this.type = endpoint.optString("type", null);
            this.objectId = Long.valueOf(endpoint.optString("objectid", "0"));
            APJSONObject object = endpoint.optJSONObject("object");
            if (object != null) {
                if (this.object == null) {
                    String type = object.optString(SystemDefinedPropertiesHelper.type);
                    if (type.equals("user")) {
                        this.object = new AppacitiveUser();
                        this.object.setSelf(object);
                    } else if (type.equals("device")) {
                        this.object = new AppacitiveDevice();
                        this.object.setSelf(object);
                    } else {
                        this.object = new AppacitiveObject(type);
                        this.object.setSelf(object);
                    }
                } else
                    this.object.setSelf(object);
            }
        }
    }
}
