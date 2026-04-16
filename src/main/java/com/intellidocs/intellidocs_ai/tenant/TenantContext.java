package com.intellidocs.intellidocs_ai.tenant;

public class TenantContext {
    //One tenantId stored per thread/request - used to associate requests with the correct tenant in a multi-tenant environment
    //Invisible to all other threads
    private static final ThreadLocal<String> CURRENT_TENANT =
            new ThreadLocal<>();

    public  static void setTenantId(String tenantId){
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId(){
        return CURRENT_TENANT.get();
    }


    // CRITICAL — always call this after the request finishes
    // Virtual threads are pooled — without this, the next request
    // on the same thread inherits the previous tenant's ID
    public static void clear(){
        CURRENT_TENANT.remove();
    }

}
