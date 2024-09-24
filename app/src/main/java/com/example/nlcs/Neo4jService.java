package com.example.nlcs;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

import static org.neo4j.driver.Values.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4jService {
    private final Driver driver;

    // Initialize the driver and connect to the Neo4j database
    public Neo4jService(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    // Close the driver when done
    public void close() {
        driver.close();
    }

    // Create a master node upon creating a new mind map item in menu
    public void createNode(final String title, final String userId, final String mindMapID, final float x, final float y) {
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("CREATE (n:MindMap {nodeID: apoc.create.uuid(), title: $title, userId: $userId, mindMapID: $mindMapID, x: $x, y: $y, creationTime: timestamp()})",
                        parameters("title", title, "userId", userId, "mindMapID", mindMapID, "x", x, "y", y));
                return null;
            });
        }
    }

    public void updateNodePositionDB(final String nodeID, final float x, final float y) { // ---------------------------------------------------------------------------------------
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                String query = "MATCH (n:MindMap {nodeID: $nodeID}) " +
                        "SET n.x = $x, n.y = $y " +
                        "RETURN n";
                tx.run(query, parameters("nodeID", nodeID, "x", x, "y", y));
                return null;
            });
        }
    }

    
    public void deleteAllNodes(final String mindMapID) {
        try (Session session = driver.session()) {session.writeTransaction(tx -> {
            tx.run("MATCH (n {mindMapID: $mindMapID}) " +
                            "OPTIONAL MATCH (n)-[r]-() " +
                            "DELETE r, n",
                    parameters("mindMapID", mindMapID));
            return null;
        });
        }
    }

    // Fetch all nodes in a mind map, via mindMapID, upon opening a mind map
    // And display all nodes
    public List<Map<String, Object>> fetchNodesByMindMapID(final String mindMapID) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (n:MindMap) WHERE n.mindMapID = $mindMapID RETURN n.title AS title, n.id AS id, n.nodeID AS nodeID, n.x AS x, n.y AS y",
                        parameters("mindMapID", mindMapID));

                // Iterate through the result and add nodes to the list
                while (result.hasNext()) {
                    Record record = result.next();
                    Map<String, Object> node = new HashMap<>();
                    node.put("title", record.get("title").asString());
                    node.put("id", record.get("id").asString());
                    node.put("nodeID", record.get("nodeID").asString());
                    node.put("x", record.get("x").asFloat());
                    node.put("y", record.get("y").asFloat());
                    nodes.add(node);
//                    Log.d("Neo4j Node Data", "Node ID: " + record.get("nodeID").asString() + ", X: " + record.get("x").asFloat() + ", Y: " + record.get("y").asFloat());

                }
                return nodes;
            });
        }
        return nodes;
    }

    // Add a child node to a parent node via context menu
    public Map<String, Object> addChildNode(final String parentNodeID, final String childTitle, final String userId, final String mindMapID, final float x, final float y) {
        Map<String, Object> childNode;
        try (Session session = driver.session()) {
            childNode = session.writeTransaction(tx -> {
                String query = "MATCH (parent:MindMap {nodeID: $parentNodeID}) " +
                        "CREATE (child:MindMap {nodeID: apoc.create.uuid(), title: $childTitle, userId: $userId, mindMapID: $mindMapID, x: $x, y: $y, creationTime: timestamp()}) " +
                        "CREATE (parent)-[:HAS_CHILD]->(child) " +
                        "RETURN child";

                Result result = tx.run(query,
                        parameters("parentNodeID", parentNodeID, "childTitle", childTitle, "userId", userId, "mindMapID", mindMapID, "x", x, "y", y));
                if (result.hasNext()) {
                    return result.single().get("child").asMap();
                }
                return null;
            });
        }
        return childNode;
    }

    // Update node titles via the done button
    public void updateNodeTitles(final List<Map<String, String>> nodeUpdates) {
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                for (Map<String, String> update : nodeUpdates) {
                    String nodeID = update.get("nodeID");
                    String newTitle = update.get("newTitle");
                    tx.run("MATCH (n:MindMap) WHERE n.nodeID = $nodeID SET n.title = $newTitle",
                            parameters("nodeID", nodeID, "newTitle", newTitle));
                }
                return null;
            });
        }
    }

    // Return the first child of a node for deletion
    // Ordered by ascending creation time
    public Map<String, Object> fetchFirstChild(final String parentNodeID) {
        try (Session session = driver.session()){
            return session.readTransaction(tx -> {
                String query = "MATCH (parent:MindMap {nodeID: $parentNodeID})-[:HAS_CHILD]->(child) " +
                        "RETURN child.nodeID AS nodeID, child.title AS title, " +"EXISTS((child)-[:HAS_CHILD]->()) AS hasChildren " +
                        "ORDER BY child.creationTime ASC LIMIT 1";
                Result result = tx.run(query, parameters("parentNodeID", parentNodeID));
                if (result.hasNext()) {
                    return result.single().asMap();
                }
                return null;
            });
        }
    }

    // Delete a node leaf in Neo4j
    public void deleteLeafNode(final String nodeID) {
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (n:MindMap {nodeID: $nodeID}) DETACH DELETE n",
                        parameters("nodeID", nodeID));
                return null;
            });
        }
    }

    // Delete a branch in Neo4j
    public void deleteBranch(final String nodeID) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n:MindMap {nodeID: $nodeID}) " +
                                "OPTIONAL MATCH (n)-[:HAS_CHILD*]->(descendant) " +
                                "DETACH DELETE n, descendant",
                        parameters("nodeID", nodeID));
                return null;
            });
        }
    }

    public Map<String, List<String>> fetchParentChildRelationships(final String mindMapID) {
        Map<String, List<String>> parentChildMap = new HashMap<>();

        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run(
                        "MATCH (parentNode)-[:CHILD_OF]->(childNode) " +
                                "WHERE parentNode.mindMapID = $mindMapID " +
                                "RETURN parentNode.nodeID AS parentID, collect(childNode.nodeID) AS childIDs",
                        parameters("mindMapID", mindMapID)
                );

                while (result.hasNext()) {
                    Record record = result.next();
                    String parentID = record.get("parentID").asString();
                    List<String> childIDs = record.get("childIDs").asList(Value::asString);
                    parentChildMap.put(parentID, childIDs);
                }
                return parentChildMap;
            });
        }

        return parentChildMap;
    }

}