
import java.io.File;

import javax.imageio.ImageIO;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.Point;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
//import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

public class TokenizerGraphVisualizer {

	private String[] myVertices = {
		"A", "B", "C", "D"	
	};
	
	public static void main(String[] args) {
		new TokenizerGraphVisualizer(null);
	}
	
	public TokenizerGraphVisualizer(TokenizerDefinition tokendef) {
		
		DirectedSparseMultigraph<String,String> graph = new DirectedSparseMultigraph<String,String>();
		
		for (String v : myVertices) graph.addVertex(v);
		
		graph.addEdge("a", new Pair<String>(myVertices[0], myVertices[1]));
		graph.addEdge("b", new Pair<String>(myVertices[0], myVertices[2]));
		graph.addEdge("c", new Pair<String>(myVertices[1], myVertices[3]));
		graph.addEdge("d", new Pair<String>(myVertices[2], myVertices[3]));
		
		Layout<String,String> layout = new KKLayout<String,String>(graph);
		
		VisualizationImageServer<String,String> viz = new VisualizationImageServer<String, String>(layout, new Dimension(1000, 1000));
		
		try {
			BufferedImage image = (BufferedImage) viz.getImage(new Point(500, 500), new Dimension(1000, 1000));
			
			ImageIO.write(image, "png", new File("tokenizer.png"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
