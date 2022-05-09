package toolWindow.LocalToolWindow.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CallResult {
    String caller;

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCallee() {
        return callee;
    }

    public void setCallee(String callee) {
        this.callee = callee;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getoHashCode() {
        return oHashCode;
    }

    public void setoHashCode(String oHashCode) {
        this.oHashCode = oHashCode;
    }

    public String getcSignature() {
        return cSignature;
    }

    public void setcSignature(String cSignature) {
        this.cSignature = cSignature;
    }

    public String getfSignature() {
        return fSignature;
    }

    public void setfSignature(String fSignature) {
        this.fSignature = fSignature;
    }

    public String getfHashCode() {
        return fHashCode;
    }

    public void setfHashCode(String fHashCode) {
        this.fHashCode = fHashCode;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    String callee;
    String accessType;
    String oHashCode;
    String cSignature;
    String fSignature;
    String fHashCode;
    String fName;

    public CallResult(ResultSet rs) throws SQLException {
        caller = rs.getString(1);
        callee = rs.getString(2);
        accessType = rs.getString(3);
        oHashCode = rs.getString(4);
        cSignature = rs.getString(5);
        fSignature = rs.getString(6);
        fHashCode = rs.getString(7);
        fName = rs.getString(8);
    }
}
