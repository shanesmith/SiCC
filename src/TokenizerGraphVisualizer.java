
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.collections15.Transformer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
//import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

//TODO create new ssCCVertexRenderer class based on BasicVertexRenderer with ability to double-line shapes (might also need new RenderContext with new VertexDrawMultiLinesTransformer)

public class TokenizerGraphVisualizer {

	Dimension dimension = new Dimension(500, 500);
	
	int stateSize = 30;
	
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
		graph.addEdge("f", new Pair<String>(myVertices[3], myVertices[3]));
		
		Layout<String,String> layout = new KKLayout<String,String>(graph);
		
		VisualizationImageServer<String,String> viz = new VisualizationImageServer<String, String>(layout, dimension);
		
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
				return new Font("Arial", Font.BOLD, 12);
			}
		});
		viz.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		// Set the vertex to a larger circle
		vizrc.setVertexShapeTransformer(new Transformer<String,Shape>() {
			public Shape transform(String str) {
				return new Ellipse2D.Double(-(stateSize/2), -(stateSize/2), stateSize, stateSize);
			}
		});
		viz.getRenderer().setVertexRenderer(new DoubleStrokeVertexRenderer<String,String>());
		
		
		try {
			Point2D center = new Point2D.Double(dimension.getWidth()/2, dimension.getHeight()/2);
			
			BufferedImage image = (BufferedImage) viz.getImage(center, dimension);
			
			ImageIO.write(image, "png", new File("tokenizer.png"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private class DoubleStrokeVertexRenderer<V,E> extends BasicVertexRenderer<V,E> {
		protected void paintShapeForVertex(RenderContext<V,E> rc, V v, Shape shape) {
	        GraphicsDecorator g = rc.getGraphicsContext();
	        Paint oldPaint = g.getPaint();
	        Paint fillPaint = rc.getVertexFillPaintTransformer().transform(v);
	        if(fillPaint != null) {
	            g.setPaint(fillPaint);
	            g.fill(shape);
	            g.setPaint(oldPaint);
	        }
	        Paint drawPaint = rc.getVertexDrawPaintTransformer().transform(v);
	        if(drawPaint != null) {
	        	g.setPaint(drawPaint);
	        	Stroke oldStroke = g.getStroke();
	        	Stroke stroke = rc.getVertexStrokeTransformer().transform(v);
	        	if(stroke != null) {
	        		g.setStroke(stroke);
	        	}
	        	g.draw(shape);
	        	// Double up!
	        	Rectangle2D bounds = shape.getBounds2D();
	        	AffineTransform scale = AffineTransform.getTranslateInstance(bounds.getCenterX(), bounds.getCenterY());
	        	scale.scale(1.2, 1.2);
	        	scale.translate(-bounds.getCenterX(), -bounds.getCenterY());
	        	g.draw(scale.createTransformedShape(shape));
	        	g.setPaint(oldPaint);
	        	g.setStroke(oldStroke);
	        }
		}
	}
	
}
