package toolWindow.entity;

public class Access {
    public String getClassSignatureAndFieldName() {
        return classSignatureAndFieldName;
    }

    public void setClassSignatureAndFieldName(String classSignatureAndFieldName) {
        this.classSignatureAndFieldName = classSignatureAndFieldName;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    String classSignatureAndFieldName;
    String accessType;
}
