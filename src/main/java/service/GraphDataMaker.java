package service;



import toolWindow.LocalToolWindow.entity.Access;
import toolWindow.LocalToolWindow.entity.CallResult;
import toolWindow.LocalToolWindow.entity.Edge;
import toolWindow.LocalToolWindow.entity.Node;
import util.SQLiteUtils;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class GraphDataMaker {

    private  Map<String, Map<String, String>> fieldDataGenerate(String dbPath) throws SQLException {
        Connection conn = SQLiteUtils.connectDB(dbPath);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery( "SELECT type, callFlag, classSignature, methodName, methodSignature, threadID, cSignature,oHashcode, fSignature, fHashcode, fName, McSignature, newValue from totalData" );

        // 数据存储结构
        Map<String, Map<String, String>> dataSet = new HashMap<>();

        while(rs.next()) {
            String rowType = rs.getString(1);
            if(rowType.equals("0")) {
                continue;
            }
            String classSignature = rs.getString(7);
            String fieldClass = classSignature.replace('L', ' ').replace('/','.').replace(';', ' ');


            if(rowType.equals("1")) {
                String fieldHashCode = rs.getString(10);
                String fieldName = rs.getString(11);
                Map<String, String> field = new HashMap<>();
                field.put("field_name", fieldName);
                field.put("field_hash_code", fieldHashCode);
                field.put("field_of_class", fieldClass);

                dataSet.put(fieldHashCode, field);
            }
            else if(rowType.equals("2")) {
                String fieldHashCode = rs.getString(13);
                String fieldName = rs.getString(11);
                Map<String, String> field = new HashMap<>();
                field.put("field_name", fieldName);
                field.put("field_hash_code", fieldHashCode);
                field.put("field_of_class", fieldClass);

                dataSet.put(fieldHashCode, field);
            }
        }
        SQLiteUtils.closeResource(conn,stmt,rs);

        return dataSet;
    }


    private  List<CallResult> outerDataAccess(String dbPath, Map<String, Map<String, String>> fieldData, List<String> nodes) throws SQLException {
        Connection conn = SQLiteUtils.connectDB(dbPath);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT caller, callee, accessType, oHashCode, cSignature, fSignature, fHashCode, fName from callGraph");


        List<CallResult> dataAccessRows = new ArrayList<CallResult>();
        List<String> classMethods = new ArrayList<String>();

        while (rs.next()) {
            String caller = rs.getString(1);
            CallResult singleRow = new CallResult(rs);

            if (fieldData.containsKey(singleRow.getfHashCode())) {
                Map<String, String> field = fieldData.get(singleRow.getfHashCode());
                if (!singleRow.getAccessType().equals("null") && !singleRow.getfHashCode().equals("static")
                        && !singleRow.getCaller().startsWith(field.get("field_of_class"))) {
                    dataAccessRows.add(singleRow);
                }

//                if (!classMethods.contains(caller)) {
//                    classMethods.add(caller);
//                }

            }

        }
        rs.close();
        stmt.close();
        conn.close();
        return dataAccessRows;
    }


    // 生成node和edge
    private  void generateEdge(String resultDBPath,List<CallResult> dataAccessResult) throws SQLException {
        // 先过滤相同数据


        // 先生成hash图， 根据被访问的类与类型
        Map<String, Map<String, CallResult>> graphTable = new HashMap<>();
        for(CallResult row: dataAccessResult) {
            String key = row.getcSignature() + row.getfName();

            if(!graphTable.containsKey(key)) {
                Map<String, CallResult> singleMethod = new HashMap<>();
                singleMethod.put(row.getCaller(), row);
                graphTable.put(key, singleMethod);
            }
            Map<String, CallResult> singleMethod = graphTable.get(key);
            if(!singleMethod.containsKey(row.getCaller())) {
                singleMethod.put(row.getCaller(), row);
            } else {
                CallResult tmpNode = graphTable.get(key).get(row.getCaller());
                tmpNode.setAccessType( (row.getAccessType() != "Modify" && tmpNode.getAccessType() != "Modify") ? "Access"  : "Modify")  ;
            }
        }

        // 根据关键词methodA+methodB 作为一个边的关键词key， 对应一条边
        Map<String, Edge> edges = new HashMap<>();
        List<String> visited = new ArrayList<>();

        for(String key: graphTable.keySet()) {
            for(String a: graphTable.get(key).keySet()) {
                for(String b: graphTable.get(key).keySet()) {
                    CallResult nodeA = graphTable.get(key).get(a);
                    CallResult nodeB = graphTable.get(key).get(b);


                    if(visited.contains(nodeA.getCaller() + nodeB.getCaller() + key)) {
                        continue;
                    } else {
                        visited.add(nodeA.getCaller() + nodeB.getCaller() + key);
                    }
                    if(!nodeA.equals(nodeB)) {
                        String edgeKey = nodeA.getCaller() + nodeB.getCaller();
                        Edge edge = null;
                        if (edges.containsKey(edgeKey)) {
                            edge = edges.get(edgeKey);
                        } else {
                            edge = new Edge();
                        }
                        StringBuilder s = new StringBuilder();
                        s.append(nodeA.getAccessType() == "Access" ? "r" : "w");
                        s.append(nodeB.getAccessType()  == "Access" ? "r" : "w");


                        edge.setNodeA(new Node(nodeA.getCaller()));
                        edge.setNodeB(new Node(nodeB.getCaller()));


                        Access access = new Access();
                        access.setClassSignatureAndFieldName(key);
                        access.setAccessType(s.toString());

                        edge.addSA(access);
                        edges.put(edgeKey, edge);
                    }
                }
            }
        }

        insertEdges(resultDBPath,edges);
        insertNodes(resultDBPath,edges);

    }


    private  void insertEdges(String resultDBPath,Map<String, Edge> edges) throws SQLException {
        Connection conn = SQLiteUtils.connectDB(resultDBPath);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DROP TABLE IF EXISTS edge");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS edge(methodA, methodB, accessType, classField)");

        // 插入节点数据
        for(String key: edges.keySet()) {
            Edge edge = edges.get(key);

            for (Access access : edge.getAccessList()) {
                String classField = access.getClassSignatureAndFieldName();

                String sql = "INSERT INTO edge(methodA, methodB, accessType, classField) values (\'"
                        + edge.getNodeA().getClassNameAndMethodName()
                        + "\',\'" + edge.getNodeB().getClassNameAndMethodName()
                        + "\',\'" + access.getAccessType() + "\',\'" + access.getClassSignatureAndFieldName() + "\'" +
                        ");";
                stmt.executeUpdate(sql);
            }
        }
        conn.close();
    }


    // 根据边生成节点，添加进入Nodes中
    private   void insertNodes(String resultDBPath , Map<String, Edge> edges) throws SQLException {
        List<String> exitsNode = new ArrayList<>();

        List<Node> nodes = new ArrayList<>();
        for(String key: edges.keySet()) {
            Edge edge = edges.get(key);
            if(!exitsNode.contains(edge.getNodeA().getClassNameAndMethodName())) {
                nodes.add(edge.getNodeA());
                exitsNode.add(edge.getNodeA().getClassNameAndMethodName());
            }
            if(!exitsNode.contains(edge.getNodeB().getClassNameAndMethodName())) {
                nodes.add(edge.getNodeB());
                exitsNode.add(edge.getNodeB().getClassNameAndMethodName());
            }
        }

        Connection conn = SQLiteUtils.connectDB(resultDBPath);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DROP TABLE IF EXISTS node");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS node(classMethod, _id)");

        // 插入节点数据
        for(Node node: nodes) {
            String sql = "INSERT INTO node(classMethod) values (\'" + node.getClassNameAndMethodName() + "\');";
            stmt.executeUpdate(sql);
        }

        conn.close();
    }


    private  void createCallGraphResultDB(String sourceDatabasePath, String resultDatabasePath){
        HashMap<String, Stack<String>> callTreeCache = new HashMap<String,Stack<String>>();

        Connection connectionSourceDB = null;
        Connection connectionResultDB = null;
        Statement statementSource=null;
        Statement statementResult=null;
        ResultSet rs=null;
        try {
            connectionSourceDB = SQLiteUtils.connectDB(sourceDatabasePath);
            connectionResultDB = SQLiteUtils.connectDB(resultDatabasePath);

            statementSource= connectionSourceDB.createStatement();
            statementResult= connectionResultDB.createStatement();


            statementResult.executeUpdate("drop table if exists callGraph");
            //statementResult.executeUpdate("create table callGraph (caller, callee)");
            statementResult.executeUpdate
                    ("create table callGraph(caller, callee, accessType, oHashCode, cSignature, fSignature, fHashCode, fName)");

            rs= statementSource.executeQuery("select * from totalData");


            String fieldAccessType = null;
            String oHashCode = null;
            String cSignature = null;
            String fSignature = null;
            String fName = null;
            String fHashCode = null;

//            String dbCommand = "insert into callGraph values";
//            int commendCount = 0;

            while(rs.next()) {
                String type = rs.getString(1);

                if(type.equals("0"))
                {

                    String callFlag = rs.getString(2);
                    String className = rs.getString(3)
                            .substring(1, rs.getString(3).length()-1).replace("/", ".");
                    String methodName = rs.getString(4);

                    if(methodName.startsWith("<"))
                        methodName = methodName.replace("<", "-").replace(">", "-");
                    String methodDetail = className+"."+methodName+rs.getString(5);
                    String threadID = rs.getString(6);


                    if(callFlag.equals("E"))
                    {
                        if(!callTreeCache.keySet().contains(threadID))
                        {
                            Stack<String> callTreeStack = new Stack<String>();
                            callTreeStack.push(className);
                            callTreeCache.put(threadID, callTreeStack);
                        }
                        else
                        {
                            // 方法细节
                            callTreeCache.get(threadID).push(methodDetail);
                            // 类级别
//                            callTreeCache.get(threadID).push(className);
                        }
                    }
                    else//callFlag equals "X"
                    {
                        if(callTreeCache.get(threadID).isEmpty())
                            System.err.println(methodDetail);
                        else {
                            methodDetail = callTreeCache.get(threadID).pop();

                            String dbCommand = "insert into callGraph values(";
                            if(!callTreeCache.get(threadID).isEmpty())
                            {
                                dbCommand += "('"+callTreeCache.get(threadID).peek()+"','"+methodDetail + "','"
                                        + fieldAccessType + "','" + oHashCode + "','" + cSignature + "','"
                                        + fSignature + "','" + fHashCode + "','" + fName + "')";
//                                System.out.println(dbCommand);
//                                commendCount++;
//                                if(commendCount > 500) {
//                                    dbCommand += ";";
//                                    commendCount = 0;
//                                    statementResult.executeUpdate(dbCommand);
//                                    dbCommand = "insert into callGraph values";
//                                }

                                fieldAccessType = null;
                                try
                                {
                                    statementResult.executeUpdate(dbCommand);
                                    System.out.println(dbCommand+"  "+" is successfully executed");
                                }
                                catch (SQLException e1)
                                {
                                    System.out.println(e1);
                                }
                            }
                        }

                    }
                }
                else
                // type != 0 means field accessed or modified.
                {
                    oHashCode = rs.getString(9);
                    cSignature = rs.getString(8);
                    fSignature = rs.getString(10);
                    fHashCode = rs.getString(11);
                    fName = rs.getString(12);
                    if(type.equals("1"))
                        fieldAccessType = "Access";
                    else
                        fieldAccessType = "Modify";
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally
        {

            SQLiteUtils.closeResource(connectionSourceDB,statementSource);
            SQLiteUtils.closeResource(connectionResultDB,statementResult,rs);
        }

    }

    /**
     * 从数据库中读取节点和边
     *
     * @param dbName
     * @return
     * @throws SQLException
     */
    public List<Node> queryNodes(String dbName) throws SQLException {
        Connection conn = SQLiteUtils.connectDB(dbName);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT _id, classMethod from node");
        List<Node> nodes = new ArrayList<>();
        while (rs.next()) {
            Node node = new Node(rs.getString(1), rs.getString(2));
            nodes.add(node);
        }
        SQLiteUtils.closeResource(conn,stmt,rs);
        return nodes;
    }


    public Node queryNode(String classField, Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "SELECT _id, classMethod from node WHERE classMethod = \'" + classField + "\';";
        ResultSet rs = stmt.executeQuery(sql);
        Node res = new Node(rs.getString(1), rs.getString(2));
        stmt.close();
        return res;
    }


    public List<Edge> queryEdges(String dbName) throws SQLException {
        Connection conn = SQLiteUtils.connectDB(dbName);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT methodA, methodB, accessType, classField from edge");
        List<Edge> res = new ArrayList<>();
        Map<String, Edge> edges = new HashMap<>();

        while (rs.next()) {
            String method1 = rs.getString(1);
            String method2 = rs.getString(2);

            String accessType = rs.getString(3);
            String classField = rs.getString(4);

            String key = method1 + method2;
            if (edges.containsKey(key)) {
                Edge edge = edges.get(key);
                Access access = new Access(accessType, classField);
                edge.addSA(access);
                edges.put(key, edge);
            } else {
                Edge edge = new Edge();

                edge.setNodeA(queryNode(method1, conn));
                edge.setNodeB(queryNode(method2, conn));

                Access access = new Access(accessType, classField);
                edge.addSA(access);

                edges.put(key, edge);
            }
        }
        for (String edgeKey : edges.keySet()) {
            Edge edge = edges.get(edgeKey);
            res.add(edge);
        }

        SQLiteUtils.closeResource(conn,stmt,rs);
        return res;
    }


    public void run(String sourceDatabasePath, String resultDatabasePath) throws SQLException {

        createCallGraphResultDB(sourceDatabasePath,resultDatabasePath);

        Map<String, Map<String, String>> fieldData = fieldDataGenerate(sourceDatabasePath);

        List<String> nodes = new ArrayList<>();

        List<CallResult> dataAccessResult = outerDataAccess(resultDatabasePath, fieldData, nodes);

        generateEdge(resultDatabasePath,dataAccessResult);
    }

//    public static void main(String[] args) throws SQLException {
//        GraphDataMaker maker = new GraphDataMaker();
//
//        maker.run("/home/kyriepotreler/Projects/maven-surefire/maven-surefire-plugin/TotalData.db",
//                "/home/kyriepotreler/Projects/maven-surefire/maven-surefire-plugin/callGraphResult.db");
//    }


}

