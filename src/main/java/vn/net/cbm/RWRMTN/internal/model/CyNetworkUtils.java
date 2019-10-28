package vn.net.cbm.RWRMTN.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;

public class CyNetworkUtils {
	/**
	 * Keeps attributes that are computed by NetworkAnalyzer in this network and can be visualized.
	 *
	 * @param aNetwork
	 *            The selected network.
	 * @param computedAttr
	 *            Set of the names of all attributes computed in the network.
	 * @param cyAttr
	 *            Set of node/edge attributes in Cytoscape.
	 * @param netAnalyzerAttr
	 *            Set of all node/edge attributes computed by NetworkAnalyzer
	 *
	 * @return The computed attribute names in the form of two arrays. The names are placed in the returned
	 *         array in alphabetical order.
	 */
	private static String[][] keepAvailableAttributes(CyNetwork aNetwork, Set<String> computedAttr,
							  CyTable table, Set<String> netAnalyzerAttr)
	{
		final List<String> visualizeAttr = new ArrayList<String>(computedAttr.size() + 1);
		for (final String colName : computedAttr) {
			CyColumn column = table.getColumn(colName);
			
			if (column == null) continue;
			
			if (column.getType() == Double.class || column.getType() == Integer.class)
				visualizeAttr.add(column.getName());
		}
		final List<String> resultNetAnalyzer = new ArrayList<String>(visualizeAttr);
		final List<String> resultOther = new ArrayList<String>(visualizeAttr);
		resultNetAnalyzer.retainAll(netAnalyzerAttr);
		resultOther.removeAll(netAnalyzerAttr);
		Collections.sort(resultNetAnalyzer);
		Collections.sort(resultOther);
		String[][] result = new String[2][];
		result[0] = resultNetAnalyzer.toArray(new String[resultNetAnalyzer.size()]);
		result[1] = resultOther.toArray(new String[resultOther.size()]);
		return result;
	}

	/**
	 * Checks the specified network has values for the given edge attributes.
	 *
	 * @param aNetwork
	 *            Network of interest.
	 * @return Array of all attributes for which all edges in <code>aNetwork</code> have values; an empty
	 *         set if no such attributes are found.
	 */
	
	/**
	 * Checks the specified network has values for the given node attributes.
	 *
	 * @param aNetwork
	 *            Network of interest.
         * @param nodes
         *            Collection of CyNode Cytoscape
	 * @return Array of all attributes for which all nodes in <code>aNetwork</code> have values; an empty
	 *         set if no such attributes are found.
	 */
	
	public static Set<CyEdge> getAllConnectingEdges(CyNetwork aNetwork, Collection<CyNode> nodes) {
		Set<CyEdge> connEdgeSet = new HashSet<CyEdge>();
		ArrayList<CyNode> nl = new ArrayList<CyNode>(nodes);
		for ( int i = 0; i < nl.size(); i++ )
			for ( int j = i+1; j < nl.size(); j++)
				connEdgeSet.addAll( aNetwork.getConnectingEdgeList(nl.get(i), nl.get(j),CyEdge.Type.ANY) );
		return connEdgeSet;
	}
	/**
	 * Gets the number of pair connections between the given set of nodes.
	 * <p>
	 * This method effectively counts the number of edges between nodes in the given set, ignoring self-loops
	 * and multiple edges.
	 * </p>
	 *
	 * @param aNetwork
	 *            Network containing the nodes of interest.
	 * @param aNodeIndices
	 *            Indices of the nodes to be examined.
	 * @param aIgnoreDir
	 *            Flag indicating if connections are undirected.
	 * @return Number of pair connections between the nodes in the given node set. The returned value is
	 *         always in the range <code>[0, n(n-1)/2]</code> for undirected networks (
	 *         <code>aIgnoreDir == true</code>) and <code>[0, n(n-1)]</code> for directed networks (
	 *         <code>aIgnoreDir == true</code>).
	 */
	public static int getPairConnCount(CyNetwork aNetwork, Collection<CyNode> aNodeIndices, boolean aIgnoreDir) {

		Set<CyEdge> connEdgeSet = getAllConnectingEdges(aNetwork, aNodeIndices);

		int edgeCount = connEdgeSet.size();

		List<CyEdge> connEdges = new ArrayList<CyEdge>(connEdgeSet);

		for (int i = 0; i < connEdges.size(); ++i) {
			CyEdge e = connEdges.get(i);

			// Ignore self-loops
			if ( e.getSource() == e.getTarget() ) {
				edgeCount--;
			} else {
				// Ignore multiple edges
				for (int j = i + 1; j < connEdges.size(); ++j) {
					CyEdge ee = connEdges.get(j);
					if ( // directed edges have same source + target
					     ( e.getSource() == ee.getSource() &&
					       e.getTarget() == ee.getTarget() ) ||
						 // or undirected edges have same source + target (if we care)
					     ( aIgnoreDir &&
						   ( e.getSource() == ee.getTarget() &&
						      e.getTarget() == ee.getSource() ) ) ) {

						edgeCount--;
						// TODO I think this break is wrong!
						// What if there are more than two edges?
						break;
					}
				}
			}
		}

		return edgeCount;
	}

	/**
	 * Gets all the neighbors of the given node. All types of edges incident on the node are considered -
	 * incoming, outgoing and undirected.
	 * <p>
	 * Note that the node itself is never returned as its neighbor.
	 * </p>
	 *
	 * @param aNetwork
	 *            Network that contains the node of interest - <code>aNode</code>.
	 * @param aNode
	 *            CyNode , whose neighbors are to be found.
	 * @return <code>Set</code> of <code>Node</code> instances, containing all the neighbors of
	 *         <code>aNode</code>; empty set if the node specified is an isolated vertex.
	 * @see #getNeighbors(CyNetwork, CyNode)
	 */
	public static Set<CyNode> getNeighbors(CyNetwork aNetwork, CyNode aNode) {
		return getNeighbors(aNetwork, aNode, aNetwork.getAdjacentEdgeList(aNode, CyEdge.Type.ANY));
	}

	/**
	 * Gets all the neighbors of the given node.
	 * <p>
	 * Note that the node itself is never returned as its neighbor.
	 * </p>
	 *
	 * @param aNetwork
	 *            Network that contains the node of interest - <code>aNode</code>.
	 * @param aNode
	 *            CyNode , whose neighbors are to be found.
	 * @param aIncEdges
	 *            Array of all the edges incident on <code>aNode</code>.
	 * @return <code>Set</code> of <code>Node</code> instances, containing all the neighbors of
	 *         <code>aNode</code>; empty set if the node specified is an isolated vertex.
	 */
	public static Set<CyNode> getNeighbors(CyNetwork aNetwork, CyNode aNode, List<CyEdge> aIncEdges) {
		Set<CyNode> neighborsSet = new HashSet<CyNode>();
		for ( CyEdge e : aIncEdges ) {
			CyNode sourceNode = e.getSource();
			if (sourceNode != aNode) {
				neighborsSet.add(sourceNode);
			} else {
				CyNode targetNode = e.getTarget();
				if (targetNode != aNode) {
					neighborsSet.add(targetNode);
				}
			}
		}
		return neighborsSet;
	}

	/**
	 * Gets all the neighbors of the given node and their frequencies. All types of edges incident on the node
	 * are considered - incoming, outgoing and undirected.
	 * <p>
	 * Note that the node itself is never counted as its neighbor.
	 * </p>
	 *
	 * @param aNetwork
	 *            Network that contains the node of interest - <code>aNode</code>.
	 * @param aNode
	 *            CyNode , whose neighbors are to be found.
	 * @return <code>Map</code> of <code>Node</code> instances as keys, containing all the neighbors of
	 *         <code>aNode</code> and the number of their occurrences as the values, encapsulated in
	 *         <code>MutInteger</code> instances.
	 * @see #getNeighborMap(CyNetwork, CyNode )
	 */
	public static Map<CyNode, MutInteger> getNeighborMap(CyNetwork aNetwork, CyNode aNode) {
		return getNeighborMap(aNetwork, aNode, aNetwork.getAdjacentEdgeList(aNode,CyEdge.Type.ANY));
	}

	/**
	 * Gets all the neighbors of the given node and their frequencies.
	 * <p>
	 * All types of edges incident on the node are considered - incoming, outgoing and undirected.
	 * </p>
	 * <p>
	 * Note that the node itself is never counted as its neighbor.
	 * </p>
	 *
	 * @param aNetwork
	 *            Network that contains the node of interest - <code>aNode</code>.
	 * @param aNode
	 *            CyNode , whose neighbors are to be found.
	 * @param aIncEdges
	 *            Array of all the edges incident on <code>aNode</code>.
	 * @return <code>Map</code> of <code>Node</code> instances as keys, containing all the neighbors of
	 *         <code>aNode</code> and the number of their occurrences as values, encapsulated in
	 *         <code>MutInteger</code> instances.
	 */
	public static Map<CyNode, MutInteger> getNeighborMap(CyNetwork aNetwork, CyNode aNode, List<CyEdge> aIncEdges) {
		Map<CyNode, MutInteger> m = new HashMap<CyNode, MutInteger>();
		for (int i = 0; i < aIncEdges.size(); i++) {
			CyEdge e = aIncEdges.get(i);
			CyNode n = (e.getSource() == aNode) ? e.getTarget() : e.getSource();
			if (n != aNode) {
				MutInteger count = m.get(n);
				if (count == null)
					m.put(n, new MutInteger(1));
				else
					count.value++;
			}
		}
		return m;
	}


	/**
	 * Performs an inspection on what kind of edges the given network contains.
	 *
	 * @param aNetwork
	 *            Network to be inspected.
	 * @return Results of the inspection, encapsulated in a <code>NetworkInspection</code> instance.
	 *
	 * @throws IllegalArgumentException
	 *             If <code>aNetwork</code> is empty, that is, contains zero nodes.
	 * @throws NullPointerException
	 *             If <code>aNetwork</code> is <code>null</code>.
	 */

	/**
	 * Removes all duplicated edges in the network, not including self-loops.
	 *
	 * @param aNetwork
	 *            Network from which duplicated edges are to be removed.
	 * @param aIgnoreDir
	 *            Flag indicating if edge direction should be ignored. Set this to <code>true</code> if all
	 *            edges are to be treated as undirected.
	 * @param aCreateEdgeAttr
	 *            Flag indicating if an edge attribute representing the number of duplicated edges should be
	 *            created.
	 *
	 * @return Number of edges removed from the network.
	 */


	/**
	 * Saves the number of edges duplicated to aEdge as an edge attribute.
	 *
	 * @param aEdge
	 *            CyEdge for which duplicated edges are saved.
	 * @param aNumEdges
	 *            NUmber of edges duplicated to aEdge.
	 */
	

	/**
	 * Removes all self-loops in a network and returns the number of removed self-loops. All types of edges
	 * are considered - incoming, outgoing and undirected.
	 *
	 * @param aNetwork
	 *            Network from which self-loops are to be removed.
	 * @return Number of removed self-loops.
	 */
	public static int removeSelfLoops(CyNetwork aNetwork) {
		int removedCount = 0;
		List<CyEdge> toRemove = new ArrayList<CyEdge>();
		for ( CyEdge e : aNetwork.getEdgeList()) {
			if (e.getSource() == e.getTarget()) {
				toRemove.add(e);
				removedCount++;
			}
		}
		aNetwork.removeEdges(toRemove);
		return removedCount;
	}
}

