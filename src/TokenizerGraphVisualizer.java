
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.collections15.Transformer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.Point;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

//TODO create new ssCCVertexRenderer class based on BasicVertexRenderer with ability to double-line shapes (might also need new RenderContext with new VertexDrawMultiLinesTransformer)

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
		graph.addEdge("e", new Pair<String>(myVertices[3], myVertices[0]));
		
		Layout<String,String> layout = new KKLayout<String,String>(graph);
		
		VisualizationImageServer<String,String> viz = new VisualizationImageServer<String, String>(layout, new Dimension(1000, 1000));
		
		RenderContext<String,String> vizrc = viz.getRenderContext();
		
		// Paint vertex white
		vizrc.setVertexFillPaintTransformer(new Transformer<String,Paint>() {
			public Paint transform(String str) {
				return Color.WHITE;
			}
		});
		
		// Set the label, bold it and center it
		vizrc.setVertexLabelTransformer(new ToStringLabeller<String>());
		vizrc.setVertexFontTransformer(new Transformer<String,Font>() {
			public Font transform(String str) {
				return new Font("Arial", Font.BOLD, 14);
			}
		});
		viz.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		// Set the vertex to a larger circle
		vizrc.setVertexShapeTransformer(new Transformer<String,Shape>() {
			public Shape transform(String str) {
				return new Ellipse2D.Double(-25, -25, 50, 50);
			}
		});
		
		
		try {
			BufferedImage image = (BufferedImage) viz.getImage(new Point(500, 500), new Dimension(1000, 1000));
			
			ImageIO.write(image, "png", new File("tokenizer.png"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
