import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Wraps the parsing functionality of the MapDBHandler as an example.
 * You may choose to add to the functionality of this class if you wish.
 * @author Alan Yao
 */
public class GraphDB {

    /** List of all Connected Nodes represented in HashMap. */
    private HashMap<Long, GraphNode> nodeList;
    /** List of Strings mapped to a List of Nodes with name associated. */
    private Hashtable<String, List<GraphNode>> stringNodes;
    /** Trie of locationNames. */
    private Trie stringLocations;
    /** A mapping of cleaned string to its original form. */
    private HashMap<String, String> cleanedStrings;

    /**
     * Example constructor shows how to create and start an XML parser.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        nodeList = new HashMap<>();
        stringNodes = new Hashtable<>();
        stringLocations = new Trie();
        cleanedStrings = new HashMap<>();
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            MapDBHandler mapHandler = new MapDBHandler(this);
            saxParser.parse(inputFile, mapHandler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /** Add a Node to NodeList. */
    public void addNode(Long id, GraphNode node) {
        if (!nodeList.containsKey(id)) {
            nodeList.put(id, node);
        }
    }

    /** Get NodeMapList. */
    public HashMap<Long, GraphNode> getNodeMap() {
        return nodeList;
    }

    /** Map word to Node. */
    public void addWord(String str, GraphNode node) {
        if (!stringNodes.containsKey(str)) {
            ArrayList<GraphNode> container = new ArrayList<>();
            container.add(node);
            stringNodes.put(str, container);
        } else {
            List<GraphNode> copy = stringNodes.get(str);
            if (copy.contains(node)) {
                return;
            }
            copy.add(node);
            stringNodes.put(str, copy);
        }
        if (!cleanedStrings.containsKey(str)) {
            cleanedStrings.put(cleanString(str), str);
        }
        stringLocations.addWord(cleanString(str));
    }

    /** Returns HashTable of strings to List of Nodes. */
    public Hashtable<String, List<GraphNode>> getStringNodes() {
        return stringNodes;
    }

    /** Gets the location with prefix. */
    public List<String> getLocations(String word) {
        List<String> autoFill = stringLocations.autoComplete(word);
        List<String> corrected = new ArrayList<>();
        if (autoFill == null) {
            return corrected;
        }
        for (String str : autoFill) {
            corrected.add(cleanedStrings.get(str));
        }
        return corrected;
    }

    public String getSpecificLocation(String word) {
        return cleanedStrings.get(cleanString(word));
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
    }

}


