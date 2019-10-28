/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vn.net.cbm.RWRMTN.internal.model;

import java.awt.Color;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import vn.net.cbm.RWRMTN.internal.Base.Disease;
import vn.net.cbm.RWRMTN.internal.Base.Interaction;
import vn.net.cbm.RWRMTN.internal.Base.Node;
import vn.net.cbm.RWRMTN.internal.Base.NodeInteraction;
import vn.net.cbm.RWRMTN.internal.RESTmodel.PMInfo;

/**
 *
 * @author Administrator
 */
public class Common {
	public static String RankMethod;
	public static String NetworkFileName;
	public static String DiseaseFileName;

	// public static Map<String,Set<String>> miRNA2GeneMap;
	public static Map<String, Map<String, Double>> miRNA2Gene2WeightMapMap = new TreeMap<>();
	public static Map<String, Map<String, Double>> Disease2miRNA2WeightMapMap = new TreeMap<>();
	public static Map<String, String> ID2NameMap;
	public static Map<String, PMInfo> PMID_info = new TreeMap<>();

	public static ArrayList<String> NetworkNodeArray = new ArrayList<String>();
	public static Set<String> NetworkNodeSet = new TreeSet<String>();
	public static Map<String, ArrayList<NodeInteraction>> IncomingNodeTable;

	public static ArrayList<Interaction> NetworkInteractionArray = new ArrayList<Interaction>();

	public static ArrayList<String> DiseaseNodeArray = new ArrayList<String>();
	public static Set<String> DiseasekNodeSet = new TreeSet<String>();
	public static ArrayList<Interaction> DiseaseInteractionArray = new ArrayList<Interaction>();

	public static String DiseaseTerm = "";
	public static ArrayList<ArrayList<Double>> AllTPFs = new ArrayList<ArrayList<Double>>(); // True
																								// Positive
																								// Fraction
																								// (Vertical
																								// axis)
	public static ArrayList<ArrayList<Double>> AllPrecisions = new ArrayList<ArrayList<Double>>(); // True
																									// Positive
																									// Fraction
																									// (Vertical
																									// axis)
	public static ArrayList<ArrayList<Double>> AllFPFs = new ArrayList<ArrayList<Double>>(); // False
																								// Positive
																								// Fraction
																								// (Horizontal
																								// axis)

	public static Evaluation Eval = new Evaluation();

	public static boolean isWeighted = true;

	public static ArrayList<String> OriginalNetworkNode;
	public static ArrayList<String> OriginalPhenotypeNetworkNode;
	public static ArrayList<Interaction> OriginalNetwork;
	public static ArrayList<Interaction> OriginalPhenotypeNetwork;
	public static Set<String> NetworkMIMSet = new TreeSet<String>();
	public static ArrayList<Interaction> Mim2GeneNetwork;
	public static ArrayList<Interaction> Gene2MimNetwork;
	public static int numOfEvidence;

	public static void evaluatePerformance(int MaxRank, ArrayList<Integer> HoldoutRanks) {
		Eval = new Evaluation();
		Eval.HoldoutRanks.addAll(HoldoutRanks);
		Eval.MaxRank = MaxRank;
		Eval.calTPFs_FPFs();
		Eval.calcAUC();
	}

	public static CyNetwork getNetworkByName(CyNetworkManager cyNetworkManager, String name) {
		for (CyNetwork net : cyNetworkManager.getNetworkSet()) {
			if (net.getRow(net).get(CyNetwork.NAME, String.class).equals(name)) {
				return net;
			}
		}
		return null;
	}

	public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
		Set<T> keys = new HashSet<T>();
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				keys.add(entry.getKey());
			}
		}
		return keys;
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static Map<String, Integer> rankByRWR(Map<String, ArrayList<NodeInteraction>> IncomingNodeTable,
			double gamma, Map<String, Double> Priors, Map<String, Double> NodeScore) {
		Map<String, Integer> NodeRank = new TreeMap<String, Integer>();
		try {
			int i, j, k;
			Set<String> NodeSet = IncomingNodeTable.keySet();
			Map<String, Double> priorsl = new TreeMap<String, Double>();

			for (String n : NodeSet) {
				if (Priors.containsKey(n)) {
					priorsl.put(n, Priors.get(n));
				} else {
					priorsl.put(n, 0.0);
				}
			}

			Map<String, Double> probnext0 = new TreeMap<String, Double>();
			Map<String, Double> probnext1 = new TreeMap<String, Double>();

			probnext0 = priorsl;

			int it = 0;

			while (true) {
				probnext1 = new TreeMap<String, Double>();
				double normL2 = 0.0;
				for (String n : NodeSet) {
					double temp = 0.0;
					ArrayList<NodeInteraction> InNodeList = IncomingNodeTable.get(n);

					for (k = 0; k < InNodeList.size(); k++) {
						temp += InNodeList.get(k).Weight * probnext0.get(InNodeList.get(k).Node);
					}
					probnext1.put(n, (1 - gamma) * temp + gamma * priorsl.get(n));
				}
				normL2 = normL2(probnext0, probnext1);
				if (normL2 < Math.pow(10.0, -6.0) || it >= 10)
					break;

				probnext0 = probnext1;
				it++;
			}

			ArrayList<Node> NodeScoreList = new ArrayList<Node>();
			for (Map.Entry<String, Double> e : probnext1.entrySet()) {
				Node n = new Node();
				n.NodeID = e.getKey();

				n.Score = e.getValue();
				NodeScoreList.add(n);
			}
			Common.sortQuickNodeListInDescScore(NodeScoreList);
			int r = 0;
			for (i = 0; i < NodeScoreList.size(); i++) {
				if (NodeScoreList.get(i).NodeID.startsWith("hsa-")) {
					r++;
					NodeScore.put(NodeScoreList.get(i).NodeID, NodeScoreList.get(i).Score);
					NodeRank.put(NodeScoreList.get(i).NodeID, r);
				}
			}
			return NodeRank;
		} catch (Exception e) {
			e.printStackTrace();
			return NodeRank;
		}
	}

	public static ArrayList<Integer> evaluateByRWRMDA(Set<String> AssociatedmiRNASet,
			Map<String, ArrayList<NodeInteraction>> IncomingNodeTable, double gamma) {
		ArrayList<Integer> HoldoutmiRNAList = new ArrayList<Integer>();
		Set<String> miRNASeedSet = new TreeSet<String>();
		Map<String, Integer> HoldoutmiRNA2RankMap = new TreeMap<String, Integer>();

		for (String HoldoutmiRNA : AssociatedmiRNASet) {
			miRNASeedSet.addAll(AssociatedmiRNASet);
			miRNASeedSet.remove(HoldoutmiRNA);
			System.out.println("\t-->miRNASeedSet: " + miRNASeedSet.toString());
			Map<String, Double> Priors = new TreeMap<String, Double>();
			// for(String n: miRNASeedSet){
			// Priors.put(n,(double)1/miRNASeedSet.size());
			// }

			for (Entry<String, ArrayList<NodeInteraction>> e : IncomingNodeTable.entrySet()) {
				if (miRNASeedSet.contains(e.getKey())) {
					Priors.put(e.getKey(), (double) 1 / miRNASeedSet.size());
				} else {
					Priors.put(e.getKey(), 0.0);
				}
			}

			Map<String, Double> Node2ScoreMap = new TreeMap<String, Double>();
			Map<String, Integer> Node2RankMap = Common.rankByRWR(IncomingNodeTable, gamma, Priors, Node2ScoreMap);

			Map<Integer, String> Rank2NodeMap = new TreeMap<Integer, String>();

			for (Map.Entry<String, Integer> e : Node2RankMap.entrySet()) {
				Rank2NodeMap.put(e.getValue(), e.getKey());
			}

			int cr = 0;
			for (Map.Entry<Integer, String> e : Rank2NodeMap.entrySet()) {
				if (!miRNASeedSet.contains(e.getValue())) {
					cr++;
					if (e.getValue().compareTo(HoldoutmiRNA) == 0) {
						HoldoutmiRNAList.add(cr);
						HoldoutmiRNA2RankMap.put(HoldoutmiRNA, cr);
						break;
					}
				}
			}
			System.out.println("\t\t--> HoldoutNode " + HoldoutmiRNA + "\t" + "HoldoutSetRank.get(HoldoutNode): "
					+ HoldoutmiRNA2RankMap.get(HoldoutmiRNA));

		}

		System.out.println("\t--> HoldoutmiRNA2RankMap.keySet().toString(): " + HoldoutmiRNA2RankMap.keySet().toString()
				+ "\t" + "HoldoutmiRNA2RankMap.values().toString(): " + HoldoutmiRNA2RankMap.values().toString());
		return HoldoutmiRNAList;
	}

	public static Map<Integer, String> evaluateByRWRMTN(Map<String, Double> NodeScore, Set<String> AssociatedmiRNASet,
			Map<String, Map<String, Double>> miRNA2GeneMap, Map<String, ArrayList<NodeInteraction>> IncomingNodeTable,
			double alpha, double gamma, ArrayList<String> arrUserMiRNA) {

		Set<String> miRNASeedSet = new TreeSet<String>();

		// for(String HoldoutmiRNA: AssociatedmiRNASet){
		Set<String> SeedSet = new TreeSet<String>();

		miRNASeedSet.addAll(AssociatedmiRNASet);
		// miRNASeedSet.remove(HoldoutmiRNA);

		System.out.println("\t-->miRNASeedSet: " + miRNASeedSet.toString());
		SeedSet.addAll(miRNASeedSet);

		Set<String> TargetGeneSeedSet = new TreeSet<String>();

		for (String m : AssociatedmiRNASet) {
			if (miRNA2GeneMap.containsKey(m)) {
				TargetGeneSeedSet.addAll(miRNA2GeneMap.get(m).keySet());
			}
		}
		System.out.println("\t-->TargetGeneSetSet: " + TargetGeneSeedSet.toString());

		SeedSet.addAll(TargetGeneSeedSet);

		// Map<String, Double> Priors = new TreeMap<String, Double>();
		// for(String n: miRNASeedSet){
		// if(n.contains("hsa")){//miRNA
		// Priors.put(n,alpha*(double)1/miRNASeedSet.size());
		// }else{//Target gene
		// Priors.put(n,(1-alpha)*(double)1/TargetGeneSeedSet.size());
		// }
		// }

		Map<String, Double> Priors = new TreeMap<String, Double>();
		for (Entry<String, ArrayList<NodeInteraction>> e : IncomingNodeTable.entrySet()) {
			if (SeedSet.contains(e.getKey())) {
				if (e.getKey().contains("hsa")) {// miRNA
					Priors.put(e.getKey(), alpha * (double) 1 / miRNASeedSet.size());
				} else {// Target gene
					Priors.put(e.getKey(), (1 - alpha) * (double) 1 / TargetGeneSeedSet.size());
				}
			} else {
				Priors.put(e.getKey(), 0.0);
			}
		}

		// Map<String, Double> NodeScore = new TreeMap<String, Double>();
		Map<String, Integer> NodeRank = Common.rankByRWR(IncomingNodeTable, gamma, Priors, NodeScore);

		Map<Integer, String> RankNode = new TreeMap<Integer, String>();
		if (arrUserMiRNA.size() == 0) {
			for (Entry<String, Integer> e : NodeRank.entrySet()) {
				RankNode.put(e.getValue(), e.getKey());
			}
		} else {
			for (Entry<String, Integer> e : NodeRank.entrySet()) {
				for (String str : arrUserMiRNA) {
					if (str.equals(e.getKey())) {
						RankNode.put(e.getValue(), e.getKey());
					}
				}
			}
		}

		return RankNode;
	}

	public static double normL2(Map<String, Double> vector0, Map<String, Double> vector1) {
		double temp = 0.0;
		for (Map.Entry<String, Double> e : vector0.entrySet()) {
			String n = e.getKey();
			temp += (vector0.get(n) - vector1.get(n)) * (vector0.get(n) - vector1.get(n));
		}
		return Math.sqrt(temp);
	}

	public static Map<String, Set<String>> loadEntity2Elements(String Entity2ElementFile) throws Exception {
		int i, j;

		BufferedReader br = new BufferedReader(new FileReader(Entity2ElementFile));
		String str = null;

		System.out.println("Entity 2 Element association data file is being loaded...!");

		Map<String, Set<String>> Entity2ElementMap = new TreeMap<>();

		while ((str = br.readLine()) != null) {
			// System.out.println(str);
			String[] st = str.split("\t");
			if (st.length < 3)
				continue;
			String EntityID = st[0].trim();
			String[] ElementArr = st[2].split(", ");

			Set<String> ElementSet = new TreeSet<>();
			for (i = 0; i < ElementArr.length; i++) {
				String ElementID = ElementArr[i].trim();
				ElementSet.add(ElementID);
			}
			Entity2ElementMap.put(EntityID, ElementSet);
		}
		br.close();

		System.out.println("Number of Entities: " + Entity2ElementMap.size());

		return Entity2ElementMap;
	}

	public static void preprocessInteractionList(ArrayList<Interaction> Interactions, String By) {
		int i;
		for (i = 0; i < Interactions.size(); i++) {
			if (By.compareTo("NodeSrc") == 0) {
				Interactions.get(i).Index = Interactions.get(i).NodeSrc;
			} else if (By.compareTo("NodeDst") == 0) {
				Interactions.get(i).Index = Interactions.get(i).NodeDst;
			}
		}
	}

	public static void sortQuickInteractionListInAsc(ArrayList<Interaction> Interactions) {

		Common.quickSortInteraction(Interactions, 0, Interactions.size() - 1);
	}

	public static void quickSortInteraction(ArrayList<Interaction> A, int lower, int upper) {
		int i, j;
		String x;
		x = A.get((lower + upper) / 2).Index;
		i = lower;
		j = upper;
		while (i <= j) {
			while (A.get(i).Index.compareTo(x) < 0)
				i++;
			while (A.get(j).Index.compareTo(x) > 0)
				j--;
			if (i <= j) {
				Interaction temp = new Interaction();
				temp = A.get(i);
				A.set(i, A.get(j));
				A.set(j, temp);

				i++;
				j--;
			}
			// System.out.println("i = " + i + ", j = " + j);
		}
		if (j > lower)
			quickSortInteraction(A, lower, j);
		if (i < upper)
			quickSortInteraction(A, i, upper);
	}

	public static ArrayList<Integer> searchUsingBinaryInteraction(String searchterm, ArrayList<Interaction> List) {
		int lo, high;
		lo = 0;
		high = List.size();
		int pos = Common.searchUsingBinaryInteractionDetail(searchterm, List, lo, high);

		ArrayList<Integer> posarr = new ArrayList<Integer>();
		if (pos >= 0) {
			posarr.add(pos);
			int postemp1 = pos;
			int postemp2 = pos;
			boolean exist1, exist2;
			while (true) {
				exist1 = false;
				postemp1++;
				if (postemp1 < List.size() && List.get(postemp1).Index.compareTo(searchterm) == 0) {
					posarr.add(postemp1);
					exist1 = true;
				}
				if (exist1 == false)
					break;
			}
			while (true) {
				exist2 = false;
				postemp2--;
				if (postemp2 >= 0 && List.get(postemp2).Index.compareTo(searchterm) == 0) {
					posarr.add(postemp2);
					exist2 = true;
				}
				if (exist2 == false)
					break;
			}
		}
		return posarr;
	}

	public static int searchUsingBinaryInteractionDetail(String key, ArrayList<Interaction> a, int lo, int hi) {
		// possible key indices in [lo, hi)
		if (hi <= lo)
			return -1;
		int mid = lo + (hi - lo) / 2;
		int cmp = a.get(mid).Index.compareTo(key);
		if (cmp > 0)
			return searchUsingBinaryInteractionDetail(key, a, lo, mid);
		else if (cmp < 0)
			return searchUsingBinaryInteractionDetail(key, a, mid + 1, hi);
		else
			return mid;
	}

	// For simple nodes
	public static void sortQuickNodeListInDescScore(ArrayList<Node> Nodes) {

		Common.quickSortNodeInDescScore(Nodes, 0, Nodes.size() - 1);
	}

	public static void quickSortNodeInDescScore(ArrayList<Node> A, int lower, int upper) {
		int i, j;
		double x;
		x = A.get((lower + upper) / 2).Score;
		i = lower;
		j = upper;
		while (i <= j) {
			while (A.get(i).Score > x)
				i++;
			while (A.get(j).Score < x)
				j--;
			if (i <= j) {
				Node temp = new Node();
				temp = A.get(i);
				A.set(i, A.get(j));
				A.set(j, temp);

				i++;
				j--;
			}
			// System.out.println("i = " + i + ", j = " + j);
		}
		if (j > lower)
			quickSortNodeInDescScore(A, lower, j);
		if (i < upper)
			quickSortNodeInDescScore(A, i, upper);
	}

	public static Map<String, ArrayList<NodeInteraction>> calculateOutgoingNeighbors(ArrayList<Interaction> Network) {
		Map<String, ArrayList<NodeInteraction>> OutgoingNeighbors = new TreeMap<String, ArrayList<NodeInteraction>>();

		int i;
		Set<String> NetworkNodeSet = new TreeSet<String>();
		for (i = 0; i < Network.size(); i++) {
			NetworkNodeSet.add(Network.get(i).NodeSrc);
			NetworkNodeSet.add(Network.get(i).NodeDst);
		}

		Common.preprocessInteractionList(Network, "NodeSrc");
		Common.sortQuickInteractionListInAsc(Network);

		for (Iterator<String> it = NetworkNodeSet.iterator(); it.hasNext();) {
			String n = it.next();
			ArrayList<Integer> posarr = Common.searchUsingBinaryInteraction(n, Network);
			ArrayList<NodeInteraction> neighbors = new ArrayList<NodeInteraction>();
			if (posarr.size() > 0) {

				for (i = 0; i < posarr.size(); i++) {
					Interaction ina = Network.get(posarr.get(i));
					neighbors.add(new NodeInteraction(ina.NodeDst, ina.Weight));
				}
				OutgoingNeighbors.put(n, neighbors);
			} else {
				OutgoingNeighbors.put(n, neighbors);
			}
		}

		return OutgoingNeighbors;
	}

	public static Map<String, ArrayList<NodeInteraction>> calculateIncomingNeighbors(ArrayList<Interaction> Network) {
		Map<String, ArrayList<NodeInteraction>> IncomingNeighbors = new TreeMap<String, ArrayList<NodeInteraction>>();

		int i;
		Set<String> NetworkNodeSet = new TreeSet<String>();
		for (i = 0; i < Network.size(); i++) {
			NetworkNodeSet.add(Network.get(i).NodeSrc);
			NetworkNodeSet.add(Network.get(i).NodeDst);
		}

		Common.preprocessInteractionList(Network, "NodeDst");
		Common.sortQuickInteractionListInAsc(Network);

		for (Iterator<String> it = NetworkNodeSet.iterator(); it.hasNext();) {
			String n = it.next();
			ArrayList<Integer> posarr = Common.searchUsingBinaryInteraction(n, Network);
			ArrayList<NodeInteraction> neighbors = new ArrayList<NodeInteraction>();
			if (posarr.size() > 0) {

				for (i = 0; i < posarr.size(); i++) {
					Interaction ina = Network.get(posarr.get(i));
					neighbors.add(new NodeInteraction(ina.NodeSrc, ina.Weight));
				}
				IncomingNeighbors.put(n, neighbors);
			} else {
				IncomingNeighbors.put(n, neighbors);
			}
		}

		return IncomingNeighbors;
	}

	public static VisualStyle getVisualStyleByName(String styleName, VisualMappingManager vmm) {
		Set<VisualStyle> styles = vmm.getAllVisualStyles();
		for (VisualStyle style : styles) {
			if (style.getTitle().equals(styleName)) {
				// System.out.println("style found in VisualStyles: " +
				// styleName + " == " + style.getTitle());
				return style;
			}
		}
		System.out.println("style [" + styleName + "] not in VisualStyles, default style used.");
		return null;
	}

	public static void applyNetworkVisualStyle(CyNetwork network, CyNetworkView view, String vsNetworkName,
			VisualMappingManager vmm, VisualStyleFactory visualStyleFactory, VisualMappingFunctionFactory vmfFactoryP,
			VisualMappingFunctionFactory vmfFactoryD, VisualMappingFunctionFactory vmfFactoryC) {
		// Check to see if a visual style with this name already exists

		VisualStyle vs = getVisualStyleByName(vsNetworkName, vmm);
		if (null == vs) {
			// if not, create it and add it to the catalog.
			vs = Common.createNetworkVisualStyle(network, vsNetworkName, visualStyleFactory, vmfFactoryP, vmfFactoryD,
					vmfFactoryC);
			vmm.addVisualStyle(vs);
		}
		// Actually apply the visual style
		vs.apply(view);
		vmm.setVisualStyle(vs, view);
	}

	public static VisualStyle createNetworkVisualStyle(CyNetwork network, String vsNetworkName,
			VisualStyleFactory visualStyleFactory, VisualMappingFunctionFactory vmfFactoryP,
			VisualMappingFunctionFactory vmfFactoryD, VisualMappingFunctionFactory vmfFactoryC) {

		VisualStyle visualStyle = visualStyleFactory.createVisualStyle(vsNetworkName);
		// Node settings
		// Node Label
		PassthroughMapping pm = (PassthroughMapping) vmfFactoryP.createVisualMappingFunction("OfficialSymbol",
				String.class, BasicVisualLexicon.NODE_LABEL);
		visualStyle.addVisualMappingFunction(pm);
		// Node color
		// DiscreteMapping disMapping = (DiscreteMapping)
		// vmfFactoryD.createVisualMappingFunction("Role", String.class,
		// BasicVisualLexicon.NODE_FILL_COLOR);
		//
		// disMapping.putMapValue("Training-Gene/Protein", Color.RED);
		// disMapping.putMapValue("Training-Disease", Color.RED);
		// disMapping.putMapValue("Candidate-Gene/Protein", Color.ORANGE);
		// disMapping.putMapValue("Candidate-Disease", Color.ORANGE);
		// disMapping.putMapValue("Unknown-Gene/Protein", Color.GREEN);
		// disMapping.putMapValue("Unknown-Disease", Color.GREEN);
		// visualStyle.addVisualMappingFunction(disMapping);

		int min = 0;
		int max = 0;

		List<CyNode> it = network.getNodeList();// nodesIterator();
		for (CyNode cyNode : it) {
			Integer value = network.getDefaultNodeTable().getRow(cyNode.getSUID()).get("Rank", Integer.class);
			if (value.intValue() < min) {
				min = value.intValue();
			} else if (value.intValue() > max) {
				max = value.intValue();
			}
		}

		// pick 3 points within (min~max)
		double p1 = min + (max - min) / 3.0;
		double p2 = p1 + (max - min) / 3.0;
		double p3 = p2 + (max - min) / 3.0;

		// Create a calculator for "Degree" attribute

		// final Object defaultObj =
		// type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());

		ContinuousMapping cm = (ContinuousMapping) vmfFactoryC.createVisualMappingFunction("Rank", Integer.class,
				BasicVisualLexicon.NODE_FILL_COLOR);

		// Interpolator numToColor = new LinearNumberToColorInterpolator();
		// cm.setInterpolator(numToColor);

		Color underColor = Color.GRAY;
		Color minColor = Color.RED;
		Color midColor = Color.WHITE;
		Color maxColor = Color.GREEN;
		Color overColor = Color.BLUE;

		BoundaryRangeValues bv0 = new BoundaryRangeValues(minColor, minColor, minColor);
		BoundaryRangeValues bv1 = new BoundaryRangeValues(midColor, midColor, midColor);
		BoundaryRangeValues bv2 = new BoundaryRangeValues(maxColor, maxColor, maxColor);
		//
		// Color underColor = Color.GRAY;
		// Color minColor = Color.RED;
		// Color midColor = Color.WHITE;
		// Color maxColor = Color.GREEN;
		// Color overColor = Color.BLUE;
		//
		// BoundaryRangeValues bv0 = new BoundaryRangeValues(Color.RED,
		// Color.WHITE, Color.WHITE);
		// BoundaryRangeValues bv1 = new BoundaryRangeValues(Color.WHITE,
		// Color.WHITE, Color.GREEN);
		// BoundaryRangeValues bv2 = new BoundaryRangeValues(maxColor, maxColor,
		// overColor);

		// Set the attribute point values associated with the boundary values
		cm.addPoint(p1, bv0);
		cm.addPoint(p2, bv1);
		cm.addPoint(p3, bv2);
		visualStyle.addVisualMappingFunction(cm);
		// Node shape
		DiscreteMapping disMapping = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction("Role", String.class,
				BasicVisualLexicon.NODE_SHAPE);

		disMapping.putMapValue("Training-Gene/Protein", NodeShapeVisualProperty.TRIANGLE);
		disMapping.putMapValue("Training-Disease", NodeShapeVisualProperty.DIAMOND);
		disMapping.putMapValue("Candidate-Gene/Protein", NodeShapeVisualProperty.OCTAGON);
		disMapping.putMapValue("Candidate-Disease", NodeShapeVisualProperty.RECTANGLE);
		disMapping.putMapValue("Unknown-Gene/Protein", NodeShapeVisualProperty.ELLIPSE);
		disMapping.putMapValue("Unknown-Disease", NodeShapeVisualProperty.ROUND_RECTANGLE);
		visualStyle.addVisualMappingFunction(disMapping);

		// Node size
		// Calculator nodeSizeCalculator =
		// createNodeColorCalculatorBasedRank(network);
		// nodeAppCalc.setCalculator(nodeSizeCalculator);
		// Edge settings
		// Edge weight
		// disMapping = new
		// DiscreteMapping(ArrowShape.NONE,ObjectMapping.EDGE_MAPPING);
		// disMapping.setControllingAttributeName("interaction", network,
		// false);
		// disMapping.putMapValue(new String("1"), ArrowShape.ARROW);
		// disMapping.putMapValue(new String("-1"), ArrowShape.T);
		// disMapping.putMapValue(new String("0"), ArrowShape.NONE);
		// Calculator edgeTargetArrowCalculator = new BasicCalculator("Example
		// Edge Target Arrow Calc",
		// disMapping,VisualPropertyType.EDGE_TGTARROW_SHAPE);
		// edgeAppCalc.setCalculator(edgeTargetArrowCalculator);
		return visualStyle;
	}

	public static Set<String> getPubMedIDFromPubMedSearch(String term, Map<String, PMInfo> map) {
		Set<String> IDs = new TreeSet<String>();
		numOfEvidence = 0;
		try {

			String NormalizedTerm = URLEncoder.encode(term);
			URL u = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmax=2000&term="
					+ NormalizedTerm);
			System.out.println(u.toString());

			// Connect
			HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
			// Get data
			BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			// Read d
			String line;

			String ID = "";

			while ((line = rd.readLine()) != null) {
				String kw1 = "";
				String kw2 = "";

				kw1 = "<Id>";
				kw2 = "</Id>";
				if (line.contains(kw1) && line.contains(kw2)) {
					ID = line.substring(line.indexOf(kw1) + kw1.length(), line.indexOf(kw2));

					IDs.add(ID);
					PMInfo info = getPMIDinfo(ID);
					System.out.println(info);
					map.put(ID, info);
					PMID_info.put(ID, info);
					numOfEvidence++;
				}
				if (line.contains("</IdList>")) {
					break;
				}
			}

			// close input when finished
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return IDs;
		}

	}

	public static PMInfo getPMIDinfo(String id) {
		String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&retmode=json&id="
				+ URLEncoder.encode(id);

		URL u;
		PMInfo info = null;
		try {
			u = new URL(url);
			HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
			// info=readJsonStream(urlConnection.getInputStream());
			info = readStream(urlConnection.getInputStream());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return info;

	}

	public static PMInfo readStream(InputStream in) {
		PMInfo info = new PMInfo();
		BufferedReader rd = new BufferedReader(new InputStreamReader(in));
		// Read d
		String line;
		String kw1 = "pubdate";
		String kw2 = "name";
		String kw3 = "title";
		String kw4 = "pages";

		try {
			while ((line = rd.readLine()) != null) {
				String str = line.trim();
				if (str.startsWith("\"" + kw1 + "\"")) {
					StringBuilder str1 = new StringBuilder(str.substring(1, str.length() - 2));
					info.setPubdate(str1.substring(str1.lastIndexOf("\"") + 1));
				}
				if (str.startsWith("\"" + kw2 + "\"")) {
					StringBuilder str1 = new StringBuilder(str.substring(1, str.length() - 2));
					info.getAuthors().add(str1.substring(str1.lastIndexOf("\"") + 1));
				}
				if (str.startsWith("\"" + kw3 + "\"")) {
					StringBuilder str1 = new StringBuilder(str.substring(1, str.length() - 2));
					info.setTitle(str1.substring(str1.lastIndexOf("\"") + 1));
				}
				if (str.startsWith("\"" + kw4 + "\"")) {
					StringBuilder str1 = new StringBuilder(str.substring(1, str.length() - 2));
					info.setPages(str1.substring(str1.lastIndexOf("\"") + 1));
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return info;
	}

	public static PMInfo readJsonStream(InputStream in) throws UnsupportedEncodingException {
		PMInfo info = new PMInfo();
		System.out.println("aaaa");
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				System.out.println(name);
				if (name.equals("result")) {
					info = readResult(reader);
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return info;
	}

	private static PMInfo readResult(JsonReader reader) throws IOException {
		ArrayList<String> uuids = new ArrayList<String>();
		PMInfo info = new PMInfo();
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("uids") && reader.peek() != JsonToken.NULL) {
				reader.beginArray();
				while (reader.hasNext()) {
					uuids.add(reader.nextString());
				}
				reader.endArray();
			}

			for (String str : uuids) {
				if (name.equals(str)) {
					reader.beginObject();
					info = readPMInfo(reader);
					reader.endObject();
				}

			}
			System.out.println(uuids);

		}
		reader.endObject();
		return info;
	}

	private static PMInfo readPMInfo(JsonReader reader) throws IOException {
		// TODO Auto-generated method stub
		PMInfo info = new PMInfo();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("pubdate")) {
				info.setPubdate(reader.nextString());
			} else if (name.equals("authors")) {
				info.setAuthors(readAuthors(reader));
			} else if (name.equals("pages")) {
				info.setPages(reader.nextString());
			} else if (name.equals("title")) {
				info.setTitle(reader.nextString());
			} else {
				reader.skipValue();
			}
		}
		System.out.println(info);
		return info;
	}

	private static ArrayList<String> readAuthors(JsonReader reader) throws IOException {
		// TODO Auto-generated method stub
		ArrayList<String> arr = new ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			reader.beginObject();
			while (reader.hasNext()) {
				String authorname = reader.nextName();
				if (authorname.equals("name")) {
					arr.add(reader.nextString());
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		}
		reader.endArray();
		return arr;
	}

}
