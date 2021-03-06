package com.appacitive.core;

import com.appacitive.core.apjson.APJSONArray;
import com.appacitive.core.apjson.APJSONException;
import com.appacitive.core.apjson.APJSONObject;
import com.appacitive.core.exceptions.AppacitiveException;
import com.appacitive.core.exceptions.ValidationException;
import com.appacitive.core.infra.*;
import com.appacitive.core.interfaces.AsyncHttp;
import com.appacitive.core.interfaces.Logger;
import com.appacitive.core.model.*;
import com.appacitive.core.query.AppacitiveQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sathley.
 */
public class AppacitiveObject extends AppacitiveObjectBase implements Serializable, APSerializable {

    public final static Logger LOGGER = APContainer.build(Logger.class);

    public synchronized void setSelf(APJSONObject object) {
        super.setSelf(object);
    }

    public AppacitiveObject(String type) {
        super(type);
    }

    public AppacitiveObject(String type, long objectId) {
        super(type, objectId);
    }

    protected AppacitiveObject() {
    }

    public synchronized APJSONObject getMap() throws APJSONException {
        return super.getMap();
    }

    public void createInBackground(final Callback<AppacitiveObject> callback) {
        LOGGER.info("Creating object of type " + this.getType());
        if ((type == null || this.type.isEmpty()) && (typeId <= 0)) {
            throw new ValidationException("Type and TypeId, both cannot be missing while creating an object.");
        }

        final String url = Urls.ForObject.createObjectUrl(this.type).toString();
        final Map<String, String> headers = Headers.assemble();
        final APJSONObject payload;
        try {
            payload = this.getMap();
        } catch (APJSONException e) {
            throw new RuntimeException(e);
        }
        final AppacitiveObject object = this;
        AsyncHttp asyncHttp = APContainer.build(AsyncHttp.class);
        asyncHttp.put(url, headers, payload.toString(), new APCallback() {
            @Override
            public void success(String result) {

                APJSONObject jsonObject;
                try {
                    jsonObject = new APJSONObject(result);
                } catch (APJSONException e) {
                    throw new RuntimeException(e);
                }
                AppacitiveStatus status = new AppacitiveStatus(jsonObject.optJSONObject("status"));
                if (status.isSuccessful()) {
                    object.setSelf(jsonObject.optJSONObject("object"));
                    if (callback != null) {
                        callback.success(object);
                    }
                } else {
                    if (callback != null)
                        callback.failure(null, new AppacitiveException(status));
                }

            }

            @Override
            public void failure(Exception e) {
                if (callback != null)
                    callback.failure(null, e);
            }
        });
    }

    public static void getInBackground(String type, long objectId, List<String> fields, final Callback<AppacitiveObject> callback) {
        LOGGER.info("Fetching object of type " + type + " and id " + objectId);
        final String url = Urls.ForObject.getObjectUrl(type, objectId, fields).toString();
        final Map<String, String> headers = Headers.assemble();
        AsyncHttp asyncHttp = APContainer.build(AsyncHttp.class);
        asyncHttp.get(url, headers, new APCallback() {
            @Override
            public void success(String result) {

                APJSONObject jsonObject;
                try {
                    jsonObject = new APJSONObject(result);
                } catch (APJSONException e) {
                    throw new RuntimeException(e);
                }
                AppacitiveStatus status = new AppacitiveStatus(jsonObject.optJSONObject("status"));
                if (status.isSuccessful()) {
                    AppacitiveObject object = null;
                    APJSONObject objectJson = jsonObject.optJSONObject("object");
                    if (objectJson != null) {
                        object = new AppacitiveObject();
                        object.setSelf(objectJson);
                    }
                    if (callback != null)
                        callback.success(object);
                } else {
                    if (callback != null)
                        callback.failure(null, new AppacitiveException(status));
                }

            }

            @Override
            public void failure(Exception e) {
                if (callback != null)
                    callback.failure(null, e);
            }
        });
    }

    public void deleteInBackground(boolean deleteConnections, final Callback<Void> callback) {
        LOGGER.info("Deleting object of type " + getType() + " and id " + getId());
        final String url = Urls.ForObject.deleteObjectUrl(this.type, this.getId(), deleteConnections).toString();
        final Map<String, String> headers = Headers.assemble();
        AsyncHttp asyncHttp = APContainer.build(AsyncHttp.class);
        asyncHttp.delete(url, headers, new APCallback() {
            @Override
            public void success(String result) {

                APJSONObject jsonObject;
                try {
                    jsonObject = new APJSONObject(result);
                } catch (APJSONException e) {
                    throw new RuntimeException(e);
                }
                AppacitiveStatus status = new AppacitiveStatus(jsonObject.optJSONObject("status"));
                if (status.isSuccessful()) {
                    if (callback != null)
                        callback.success(null);
                } else {
                    if (callback != null)
                        callback.failure(null, new AppacitiveException(status));
                }

            }

            @Override
            public void failure(Exception e) {
                if (callback != null)
                    callback.failure(null, e);
            }
        });
    }

    public static void deleteInBackground(String type, long objectId, boolean deleteConnections, final Callback<Void> callback) {
        LOGGER.info("Deleting object of type " + type + " and id " + objectId);
        final String url = Urls.ForObject.deleteObjectUrl(type, objectId, deleteConnections).toString();
        final Map<String, String> headers = Headers.assemble();
        AsyncHttp asyncHttp = APContainer.build(AsyncHttp.class);
        asyncHttp.delete(url, headers, new APCallback() {
            @Override
            public void success(String result) {

                APJSONObject jsonObject;
                try {
                    jsonObject = new APJSONObject(result);
                } catch (APJSONException e) {
                    throw new RuntimeException(e);
                }
                AppacitiveStatus status = new AppacitiveStatus(jsonObject.optJSONObject("status"));
                if (status.isSuccessful()) {
                    if (callback != null)
                        callback.success(null);
                } else {
                    if (callback != null)
                        callback.failure(null, new AppacitiveException(status));
                }

            }

            @Override
            public void failure(Exception e) {
                if (callback != null)
                    callback.failure(null, e);
            }
        });
    }

    public static void bulkDeleteInBackground(String type, List<Long> objectIds, final Callback<Void> callback) {
        LOGGER.info("Bulk deleting objects of type " + type + " and ids " + StringUtils.joinLong(objectIds, " , "));
        final String url = Urls.ForObject.bulkDeleteObjectUrl(type).toString();
        final Map<String, String> headers = Headers.assemble();

        final List<String> strIds = new ArrayList<String>();
        for (long id : objectIds) {
            strIds.add(String.valueOf(id));
        }

        final APJSONObject payload = new APJSONObject();
        try {
            payload.put("idlist", new APJSONArray(strIds));
        } catch (APJSONException e) {
            throw new RuntimeException(e);
        }
        AsyncHttp asyncHttp = APContainer.build(AsyncHttp.class);
        asyncHttp.post(url, headers, payload.toString(), new APCallback() {
            @Override
            public void success(String result) {

                APJSONObject jsonObject;
                try {
                    jsonObject = new APJSONObject(result);
                } catch (APJSONException e) {
                    throw new RuntimeException(e);
                }
                AppacitiveStatus status = new AppacitiveStatus(jsonObject.optJSONObject("status"));
                if (status.isSuccessful()) {
                    if (callback != null)
                        callback.success(null);
                } else {
                    if (callback != null)
                        callback.failure(null, new AppacitiveException(status));
                }

            }

            @Override
            public void failure(Exception e) {
                if (callback != null)
                    callback.failure(null, e);
            }
        });
    }

    public void updateInBackground(boolean withRevision, final Callback<AppacitiveObject> callback) {
        LOGGER.info("Updating object of type " + getType() + " and id " + getId());
        final String url = Urls.ForObject.updateObjectUrl(this.type, this.getId(), withRevision, this.getRevision()).toString();
        final Map<String, String> headers = Headers.assemble();
        final APJSONObject payload;
        try {
            payload = super.getUpdateCommand();
            payload.put("__acls", this.accessControl.getMap());
        } catch (APJSONException e) {
            throw new RuntimeException(e);
        }
        final AppacitiveObject object = this;
        AsyncHttp asyncHttp = APContainer.build(AsyncHttp.class);
        asyncHttp.post(url, headers, payload.toString(), new APCallback() {
            @Override
            public void success(String result) {

                APJSONObject jsonObject;
                try {
                    jsonObject = new APJSONObject(result);
                } catch (APJSONException e) {
                    throw new RuntimeException(e);
                }
                AppacitiveStatus status = new AppacitiveStatus(jsonObject.optJSONObject("status"));
                if (status.isSuccessful()) {
                    object.setSelf(jsonObject.optJSONObject("object"));
                    if (callback != null) {
                        callback.success(object);
                    }
                } else {
                    if (callback != null)
                        callback.failure(null, new AppacitiveException(status));
                }

            }

            @Override
            public void failure(Exception e) {
                if (callback != null)
                    callback.failure(null, e);
            }
        });
    }

    public void fetchLatestInBackground(final Callback<Void> callback) {
        LOGGER.info("Fetching latest object of type " + getType() + " and id " + getId());
        final String url = Urls.ForObject.getObjectUrl(type, this.getId(), null).toString();
        final Map<String, String> headers = Headers.assemble();
        final AppacitiveObject object = this;
        AsyncHttp asyncHttp = APContainer.build(AsyncHttp.class);
        asyncHttp.get(url, headers, new APCallback() {
            @Override
            public void success(String result) {

                APJSONObject jsonObject;
                try {
                    jsonObject = new APJSONObject(result);
                } catch (APJSONException e) {
                    throw new RuntimeException(e);
                }
                AppacitiveStatus status = new AppacitiveStatus(jsonObject.optJSONObject("status"));
                if (status.isSuccessful()) {
                    object.setSelf(jsonObject.optJSONObject("object"));
                    if (callback != null) {
                        callback.success(null);
                    }
                } else {
                    if (callback != null)
                        callback.failure(null, new AppacitiveException(status));
                }

            }

            @Override
            public void failure(Exception e) {
                if (callback != null)
                    callback.failure(null, e);
            }
        });
    }

    public static void multiGetInBackground(String type, List<Long> objectIds, List<String> fields, final Callback<List<AppacitiveObject>> callback) {
        LOGGER.info("Bulk fetching objects of type " + type + " and ids " + StringUtils.joinLong(objectIds, " , "));
        final String url = Urls.ForObject.multiGetObjectUrl(type, objectIds, fields).toString();
        final Map<String, String> headers = Headers.assemble();
        final List<AppacitiveObject> returnObjects = new ArrayList<AppacitiveObject>();
        AsyncHttp asyncHttp = APContainer.build(AsyncHttp.class);
        asyncHttp.get(url, headers, new APCallback() {
            @Override
            public void success(String result) {

                APJSONObject jsonObject;
                try {
                    jsonObject = new APJSONObject(result);
                } catch (APJSONException e) {
                    throw new RuntimeException(e);
                }
                AppacitiveStatus status = new AppacitiveStatus(jsonObject.optJSONObject("status"));
                if (status.isSuccessful()) {
                    APJSONArray objectsArray = jsonObject.optJSONArray("objects");
                    for (int i = 0; i < objectsArray.length(); i++) {
                        AppacitiveObject object = new AppacitiveObject();
                        object.setSelf(objectsArray.optJSONObject(i));
                        returnObjects.add(object);
                    }
                    if (callback != null)
                        callback.success(returnObjects);
                } else if (callback != null)
                    callback.failure(null, new AppacitiveException(status));

            }

            @Override
            public void failure(Exception e) {
                if (callback != null)
                    callback.failure(null, e);
            }
        });
    }

    public static void findInBackground(String type, AppacitiveQuery query, List<String> fields, final Callback<PagedList<AppacitiveObject>> callback) {
        LOGGER.info("Searching objects of type " + type);
        final String url = Urls.ForObject.findObjectsUrl(type, query, fields).toString();
        final Map<String, String> headers = Headers.assemble();
        final List<AppacitiveObject> returnObjects = new ArrayList<AppacitiveObject>();
        final PagedList<AppacitiveObject> pagedResult = new PagedList<AppacitiveObject>();
        AsyncHttp asyncHttp = APContainer.build(AsyncHttp.class);
        asyncHttp.get(url, headers, new APCallback() {
            @Override
            public void success(String result) {

                APJSONObject jsonObject;
                try {
                    jsonObject = new APJSONObject(result);
                } catch (APJSONException e) {
                    throw new RuntimeException(e);
                }
                AppacitiveStatus status = new AppacitiveStatus(jsonObject.optJSONObject("status"));
                if (status.isSuccessful()) {
                    APJSONArray objectsArray = jsonObject.optJSONArray("objects");
                    for (int i = 0; i < objectsArray.length(); i++) {
                        AppacitiveObject object = new AppacitiveObject();
                        object.setSelf(objectsArray.optJSONObject(i));
                        returnObjects.add(object);
                    }
                    pagedResult.results = returnObjects;
                    pagedResult.pagingInfo.setSelf(jsonObject.optJSONObject("paginginfo"));
                    if (callback != null)
                        callback.success(pagedResult);
                } else if (callback != null)
                    callback.failure(null, new AppacitiveException(status));

            }

            @Override
            public void failure(Exception e) {
                if (callback != null)
                    callback.failure(null, e);
            }
        });
    }

    public static void findInBetweenTwoObjectsInBackground(String type, long objectAId, String relationA, String labelA, long objectBId, String relationB, String labelB, List<String> fields, final Callback<PagedList<AppacitiveObject>> callback) {
        LOGGER.info("Searching objects of type " + type + " between two objects " + objectAId + " and " + objectBId);
        final String url = Urls.ForObject.findBetweenTwoObjectsUrl(type, objectAId, relationA, labelA, objectBId, relationB, labelB, fields).toString();
        final Map<String, String> headers = Headers.assemble();
        final List<AppacitiveObject> returnObjects = new ArrayList<AppacitiveObject>();
        final PagedList<AppacitiveObject> pagedResult = new PagedList<AppacitiveObject>();
        AsyncHttp asyncHttp = APContainer.build(AsyncHttp.class);
        asyncHttp.get(url, headers, new APCallback() {
            @Override
            public void success(String result) {

                APJSONObject jsonObject;
                try {
                    jsonObject = new APJSONObject(result);
                } catch (APJSONException e) {
                    throw new RuntimeException(e);
                }
                AppacitiveStatus status = new AppacitiveStatus(jsonObject.optJSONObject("status"));
                if (status.isSuccessful()) {
                    APJSONArray objectsArray = jsonObject.optJSONArray("objects");
                    if (objectsArray != null)
                        for (int i = 0; i < objectsArray.length(); i++) {
                            AppacitiveObject object = new AppacitiveObject();
                            object.setSelf(objectsArray.optJSONObject(i));
                            returnObjects.add(object);
                        }
                    pagedResult.results = returnObjects;
                    pagedResult.pagingInfo.setSelf(jsonObject.optJSONObject("paginginfo"));
                    if (callback != null)
                        callback.success(pagedResult);
                } else if (callback != null)
                    callback.failure(null, new AppacitiveException(status));

            }

            @Override
            public void failure(Exception e) {
                if (callback != null)
                    callback.failure(null, e);
            }
        });
    }

    public static void  getConnectedObjectsInBackground(String relationType, String objectType, long objectId, AppacitiveQuery query, List<String> fields, final Callback<ConnectedObjectsResponse> callback) {
        LOGGER.info("Searching for connected objects of type " + relationType + "from " + objectId + " of type " + objectType);
        final String url = Urls.ForConnection.getConnectedObjectsUrl(relationType, objectType, objectId, query, fields).toString();
        final Map<String, String> headers = Headers.assemble();
        AsyncHttp asyncHttp = APContainer.build(AsyncHttp.class);
        asyncHttp.get(url, headers, new APCallback() {
            @Override
            public void success(String result) {

                APJSONObject jsonObject;
                try {
                    jsonObject = new APJSONObject(result);
                } catch (APJSONException e) {
                    throw new RuntimeException(e);
                }
                AppacitiveStatus status = new AppacitiveStatus(jsonObject.optJSONObject("status"));
                if (status.isSuccessful()) {
                    ConnectedObjectsResponse connectedObjectsResponse = new ConnectedObjectsResponse(jsonObject.optString("parent"));
                    connectedObjectsResponse.pagingInfo.setSelf(jsonObject.optJSONObject("paginginfo"));
                    connectedObjectsResponse.results = new ArrayList<ConnectedObject>();
                    APJSONArray nodesArray = jsonObject.optJSONArray("nodes");
                    if (nodesArray != null) {
                        for (int i = 0; i < nodesArray.length(); i++) {
                            ConnectedObject connectedObject = new ConnectedObject();
                            APJSONObject nodeObject = nodesArray.optJSONObject(i);
                            if (nodeObject.isNull("__edge") == false) {
                                connectedObject.connection = new AppacitiveConnection();
                                connectedObject.connection.setSelf(nodeObject.optJSONObject("__edge"));
                                nodeObject.remove("__edge");
                            }
                            connectedObject.object = new AppacitiveObject();
                            connectedObject.object.setSelf(nodeObject);
                            connectedObjectsResponse.results.add(connectedObject);
                        }
                    }
                    if (callback != null)
                        callback.success(connectedObjectsResponse);
                } else {
                    if (callback != null)
                        callback.failure(null, new AppacitiveException(status));
                }


            }

            @Override
            public void failure(Exception e) {
                if (callback != null)
                    callback.failure(null, e);
            }
        });
//        Future<Map<String, Object>> future = ExecutorServiceWrapper.submit(new Callable<Map<String, Object>>() {
//            @Override
//            public Map<String, Object> call() throws Exception {
//                return APContainer.build(Http.class).get(url, headers);
//            }
//        });
//        AppacitiveStatus status;
//        boolean isSuccessful;
//        ConnectedObjectsResponse response = null;
//        AppacitiveException exception = null;
//        try {
//            Map<String, Object> responseMap = future.get();
//            status = new AppacitiveStatus((Map<String, Object>) responseMap.get("status"));
//            isSuccessful = status.isSuccessful();
//            if (isSuccessful) {
//
//                response = new ConnectedObjectsResponse(responseMap.get("parent").toString());
//                response.pagingInfo = new PagingInfo((Map<String, Object>) responseMap.get("paginginfo"));
//                List<Object> nodeObjects = (ArrayList<Object>) (responseMap.get("nodes"));
//                response.results = new ArrayList<ConnectedObject>();
//                if (nodeObjects != null)
//                    for (Object n : nodeObjects) {
//                        Map<String, Object> obj_n = (Map<String, Object>) n;
//                        ConnectedObject connectedObject = new ConnectedObject();
//                        if (obj_n.containsKey("__edge")) {
//                            connectedObject.connection = new AppacitiveConnection();
//                            connectedObject.connection.setSelf((Map<String, Object>) obj_n.get("__edge"));
//                            obj_n.remove("__edge");
//                        }
//                        connectedObject.object = new AppacitiveObject("");
//                        connectedObject.object.setSelf(obj_n);
//                        response.results.add(connectedObject);
//                    }
//            } else
//                exception = new AppacitiveException(status);
//        } catch (Exception e) {
//            LOGGER.log(Level.ALL, e.getMessage());
//            if (callback != null) callback.failure(null, e);
//            return;
//        }
//        if (callback != null) {
//            if (isSuccessful)
//                callback.success(response);
//            else
//                callback.failure(null, exception);
//        }
    }

    public void getConnectedObjectsInBackground(String relationType, AppacitiveQuery query, List<String> fields, final Callback<ConnectedObjectsResponse> callback)
    {
        AppacitiveObject.getConnectedObjectsInBackground(relationType, this.getType(), this.getId(), query, fields, callback);
    }
}
