package util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import toolWindow.LocalToolWindow.Graph;
import toolWindow.LocalToolWindow.entity.Access;
import toolWindow.LocalToolWindow.entity.Edge;
import toolWindow.LocalToolWindow.entity.Node;

import java.util.*;
import java.util.stream.Collectors;

public class PsiUtils {


    public static Set<PsiClass> getAllJavaClasses(Project project, Set<VirtualFile> sourceCodeRoots) {

        Set<PsiJavaFile> virtualFiles=new HashSet<>();

        sourceCodeRoots.forEach(virtualFile -> VfsUtilCore.iterateChildrenRecursively(virtualFile, null, fileOrDir -> {

            if (fileOrDir.isValid() && !fileOrDir.isDirectory() ) {
                if(null!=fileOrDir.getExtension()&&fileOrDir.getExtension().equals("java")){
                    PsiJavaFile psiJavaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(fileOrDir);
                    if (null != psiJavaFile) {
                        virtualFiles.add(psiJavaFile);
                    }
                }

            }
            return true;
        }));

        return virtualFiles.stream().flatMap(psiJavaFile -> Arrays.stream(psiJavaFile.getClasses())).collect(Collectors.toSet());

    }



    public static Set<PsiMethod> getMethodsFromClasses(Set<PsiClass> classes) {
        return  classes.stream().flatMap(e->Arrays.stream(e.getMethods())).collect(Collectors.toSet());

    }
    public static String getSignature(PsiElement element){

        if(element instanceof PsiMethod){
            String params = Arrays.stream(((PsiMethod)element).getParameterList().getParameters()).map(PsiParameter::getName).collect(Collectors.joining(","));
            params = params.isEmpty() ? "" : "(" + params + ")";
            return ((PsiMethod)element).getName()+params;
        }else {
            return ((PsiClass)element).getName()+":constructor";
        }

    }

    //packageName+className
    public static String getMethodPackageClassName(PsiMethod method){

        PsiJavaFile psiJavaFile=((PsiJavaFile) method.getContainingFile());
        String packageName = null != psiJavaFile.getPackageStatement() ? psiJavaFile.getPackageStatement().getPackageName() : "";
        String className = null != method.getContainingClass() ? method.getContainingClass().getQualifiedName() : "";
        return packageName.isBlank() || className.startsWith(packageName) ? className : packageName + "." + className;

    }



    public static  Graph buildGraph(Project project,List<Edge> edgeList) {
        Graph graph=new Graph();
        for(Edge edge:edgeList){

            graph.addEdge(project,edge);
            for(Access access:edge.getAccessList()){
                access.setPsiVariable(getVariableForAccess(project,access));
            }
        }
        GraphUtils.layout(graph);
        return graph;
    }

    public static PsiVariable getVariableForAccess(Project project, Access access){
        // Lorg/apache/maven/plugin/surefire/AbstractSurefireMojo;effectiveForkCount
        String[] fieldInfoRaw=access.getClassSignatureAndFieldName().split(";");
        int index=fieldInfoRaw[0].lastIndexOf('/');
        String packageName=fieldInfoRaw[0].substring(1,index).replace('/','.');
        String classNameRaw=fieldInfoRaw[0].substring(index+1);
        String[] classes=analyzeClassName(classNameRaw);
        String originClass=classes[0];
        String innerClass=classes[1];
        String anonymousClass=classes[2];
        String packageClassName="";
        int dollorIndexOfField=fieldInfoRaw[1].indexOf('$');
        String fieldName=fieldInfoRaw[1];
        if(dollorIndexOfField>0){
            fieldName=fieldInfoRaw[1].substring(dollorIndexOfField+1);
        }
        PsiClass psiClass;
        final PsiVariable[] psiVariables = new PsiVariable[1];

        if(anonymousClass.isEmpty()){
            if(!innerClass.isEmpty()){
                //originClass.innerClass
                packageClassName=packageName + "." +
                        originClass + "." +
                        innerClass;
            }else {
                //originClass
                packageClassName=packageName + "." + originClass;
            }

            psiClass=getPsiClassByName(project,packageClassName);

            psiVariables[0] =psiClass.findFieldByName(fieldName,false);


        }else {
            if(!innerClass.isEmpty()){
                //originClass.innerClass.anonymousClass
                packageClassName=packageName + "." +
                        originClass + "." +
                        innerClass;
                psiClass = getPsiClassByName(project, packageClassName);
            }else {
                //originClass.anonymousClass
                packageClassName = packageName + "." +
                        originClass;
                psiClass = getPsiClassByName(project, packageClassName);

            }
            List<PsiAnonymousClass> anonymousClasses=findAnonymousClasses(psiClass);
            int anonymousIndex=Integer.parseInt(classes[2])-1;

            if(dollorIndexOfField>0 && fieldInfoRaw[1].substring(0,dollorIndexOfField).equals("val")){
                //anonymous class use outer final variable

                PsiElement element = anonymousClasses.get(anonymousIndex).getScope();
                String finalFieldName = fieldName;
                element.accept(new JavaRecursiveElementVisitor() {
                    private PsiLocalVariable psiLocalVariable;
                    @Override
                    public void visitLocalVariable(PsiLocalVariable variable) {
                        super.visitLocalVariable(variable);
                        if(variable.getName().equals(finalFieldName)){
                            psiLocalVariable = variable;
                            psiVariables[0] =  psiLocalVariable;
                        }
                    }
                });
                if(psiVariables[0]==null){
                    for(PsiParameter parameter:((PsiMethod) element).getParameterList().getParameters()){
                        if(parameter.getName().equals(fieldName)){
                            psiVariables[0]=parameter;
                            return psiVariables[0];
                        }
                    }
                }


            }else {
//                Arrays.stream(anonymousClasses.get(anonymousIndex).getFields()).map(e->e.getName()).collect(Collectors.toList()).forEach(System.out::println);

                psiVariables[0] =anonymousClasses.get(anonymousIndex).findFieldByName(fieldName,false);
            }

        }

        return psiVariables[0];
    }

    //find psiElement of nodes
    public static void matchNodeAndPsiClass(Project project,Node node) {
            String classNameAndMethodName = node.getClassNameAndMethodName();

            int leftExpansion = classNameAndMethodName.indexOf('(');
            int rightExpansion = classNameAndMethodName.indexOf(')');
            String paramsRaw = classNameAndMethodName.substring(leftExpansion, rightExpansion + 1).replace("/",".").replace("$",".");
            int methodLeftIndex = classNameAndMethodName.lastIndexOf('.');
            int classLeftIndex = classNameAndMethodName.substring(0, methodLeftIndex).lastIndexOf('.');
            String node_MethodName = classNameAndMethodName.substring(methodLeftIndex + 1, leftExpansion);
            String node_PackageClassName = classNameAndMethodName.substring(0, methodLeftIndex);
            String node_PackageName=node_PackageClassName.substring(0,node_PackageClassName.lastIndexOf("."));
            String node_ClassNameRaw = classNameAndMethodName.substring(classLeftIndex + 1, methodLeftIndex);
            String[] classes = analyzeClassName(node_ClassNameRaw);
            String originClass=classes[0];
            String innerClass=classes[1];
            String anonymousClass=classes[2];
            PsiClass psiClass;

            if(anonymousClass.isEmpty()){
                if(!innerClass.isEmpty()){
                    //originClass.innerClass
                    node_PackageClassName=node_PackageName + "." +
                            originClass + "." +
                            innerClass;
                }

                psiClass=getPsiClassByName(project,node_PackageClassName);
                if(node_MethodName.equals("-init-")  || node_MethodName.equals("-clinit-")){
                    node.setPsiElement(psiClass);
                    node.setPackageClassName(node_PackageClassName);
                }else {
                    for (PsiMethod method : psiClass.getMethods()) {
                        if (node_MethodName.equals(method.getName())) {

                            String paramsStringFromPsi=getParamsStringFromPsi(method);


                            if (paramsRaw.equals(paramsStringFromPsi)) {
                                node.setPsiElement(method);
                                node.setPackageClassName(node_PackageClassName);
                                return;
                            }
                        }


                    }
                }
            }else {

                if(!innerClass.isEmpty()){
                    //originClass.innerClass.anonymousClass
                    node_PackageClassName=node_PackageName + "." +
                            originClass + "." +
                            innerClass;
                    psiClass = getPsiClassByName(project, node_PackageClassName);
                }else {
                    //originClass.anonymousClass
                    node_PackageClassName = node_PackageName + "." +
                            originClass;
                    psiClass = getPsiClassByName(project, node_PackageClassName);


                }
                //todo
                List<PsiAnonymousClass> anonymousClasses=findAnonymousClasses(psiClass);
                int anonymousIndex=Integer.parseInt(classes[2])-1;

                for (PsiMethod method : anonymousClasses.get(anonymousIndex).getMethods()) {

                    if (method.getName().equals(node_MethodName)) {

                        if (paramsRaw.equals(getParamsStringFromPsi(method))) {
                            node.setPsiElement(method);
                            node.setPackageClassName(node_PackageClassName);
                            return;
                        }
                    }
                }
            }



    }

    private static boolean isDigit(char c){
        return c>='0' && c<='9';
    }
    private static boolean isLetter(char c){
        return c!='$' && !isDigit(c);
    }

    //Finite State Machine
    public static String[] analyzeClassName(String className){
        StringBuilder[] stringBuilders=new StringBuilder[3];
        stringBuilders[0]=new StringBuilder();//class
        stringBuilders[2]=new StringBuilder();//anoy-class
        stringBuilders[1]=new StringBuilder();//inner class

        int state=1;
        for(int i=0;i<className.length();i++){
            char cur=className.charAt(i);

            switch (state){
                case 1:
                    if(cur=='$') state=2;
                    else stringBuilders[0].append(cur);
                    break;
                case 2:
                    if(isDigit(cur)){
                        state=4;
                        stringBuilders[2].append(cur);
                    }
                    if(isLetter(cur)){
                        state=3;
                        stringBuilders[1].append(cur);
                    }
                    break;
                case 3:
                    if(cur=='$') state=2;
                    else stringBuilders[1].append(cur);
                    break;
                case 4:
                    if(isDigit(cur)) stringBuilders[2].append(cur);
                    else System.err.println(className);
                default:
                    stringBuilders[0].append(cur);
            }
        }
        String[] resultStringArr=new String[3];
        for(int i=0;i<stringBuilders.length;i++){
            resultStringArr[i]=stringBuilders[i].toString();
        }
        return resultStringArr;

    }

    public static List<String> getParamsFromPsi(PsiMethod method){
       return Arrays.stream(
                        method.getParameterList().getParameters())
                .map(psiParameter ->
                        psiParameter.getType().getCanonicalText())
                .collect(Collectors.toList());
    }

    public static String getParamsStringFromPsi(PsiMethod method){
        List<String> paramList= Arrays.stream(
                        method.getParameterList().getParameters())
                .map(psiParameter ->
                        psiParameter.getType().getCanonicalText())
                .collect(Collectors.toList());
        StringBuilder sb=new StringBuilder("(");
        for(String s:paramList){
            switch (s){
                case "int":
                    sb.append("I");
                    break;
                case "byte":
                    sb.append("B");
                    break;
                case "byte[]":
                    sb.append("[B");
                    break;
                case "java.lang.String...":
                    sb.append("[Ljava.lang.String;");
                    break;
                case "boolean":
                    sb.append("Z");
                    break;
                case "E":
                    sb.append("Ljava.lang.Enum;");
                    break;
                default:
                    if(s.contains("java.util.Comparator")){
                        sb.append("Ljava.util.Comparator;");
                    }else {
                        sb.append("L").append(s).append(";");
                    }

            }
        }
        sb.append(")");
        return sb.toString();
    }
    public static List<String> getParamsFromString(String paramRaw){

        //         (ILorg/apache/maven/surefire/api/report/RunMode;Ljava/nio/charset/CharsetEncoder;II[Ljava/lang/String;)
        List<String> result=new ArrayList<>();
        if (paramRaw.contains(";")) {
            result = Arrays.stream(paramRaw.substring(1, paramRaw.length() - 1)
                    .replace('$','.')
                    .replace('/', '.').split(";"))
                    .map(e -> e.substring(1)).collect(Collectors.toList());
        }
        return result;
    }
    public static PsiClass getPsiClassByName(Project project, String cls) {
        GlobalSearchScope searchScope = GlobalSearchScope.everythingScope(project);
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        return javaPsiFacade.findClass(cls, searchScope);
    }

    public static List<PsiAnonymousClass> findAnonymousClasses(PsiClass cls){
        PsiElement[] classes = PsiTreeUtil.collectElements(cls, new PsiElementFilter() {
            public boolean isAccepted(PsiElement e) {
                return (e instanceof PsiAnonymousClass) && cls.equals(PsiTreeUtil.getParentOfType(e, PsiClass.class));
            }
        });
        return Arrays.stream(classes).map(e->((PsiAnonymousClass)e)).collect(Collectors.toList());
    }



}
