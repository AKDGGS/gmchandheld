package gov.alaska.gmchandheld;

import java.util.ArrayList;

public class AuditDisplayObjInstance {
    // https://stackoverflow.com/a/19620252
    static AuditDisplayObjInstance obj = null;

    private final ArrayList<String> auditList = new ArrayList<>();
    public ArrayList<String> getAuditList() {
        return auditList;
    }

    private String remark;
    public String getRemark(){return remark;}

    public static AuditDisplayObjInstance getInstance()
    {
        if (obj == null) {
            obj = new AuditDisplayObjInstance();
        }

        return obj;
    }
    public AuditDisplayObjInstance auditDisplayObjInstance;
}
