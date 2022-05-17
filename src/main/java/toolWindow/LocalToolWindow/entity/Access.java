package toolWindow.LocalToolWindow.entity;

import com.intellij.psi.PsiVariable;

public class Access {

    public Access() {

    }

    public Access(String accessType, String classSignatureAndFieldName) {
        this.accessType = accessType;
        this.classSignatureAndFieldName = classSignatureAndFieldName;
    }

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

    public PsiVariable getPsiVariable() {
        return psiVariable;
    }

    public void setPsiVariable(PsiVariable psiVariable) {
        this.psiVariable = psiVariable;
    }

    PsiVariable psiVariable;
    String classSignatureAndFieldName;
    String accessType;


}
