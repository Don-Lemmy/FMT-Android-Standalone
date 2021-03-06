package net.fexcraft.app.fmt.ui.editor;

import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.event.MouseClickEvent.MouseClickAction;

import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.app.fmt.ui.FunctionButton;
import net.fexcraft.app.fmt.ui.field.ColorField;
import net.fexcraft.lib.common.math.RGB;

public class TextureEditor extends EditorBase {
	
	public static RGB CURRENTCOLOR = new RGB(RGB.WHITE);
	public static ColorPanel[] panels = new ColorPanel[18 * 18]; 
	public static ColorField colorfield;
	public static ColorPanel current;
	public static Button face, polygon, group, pencil, picker;
	//
	public static boolean BUCKETMODE;
	private static PaintMode PMODE;
	private static final int rows = 18;
	
	public TextureEditor(){
		super(); int pass = -20;
		EditorWidget palette = new EditorWidget(this, translate("editor.texture.palette"), 0, 0, 0, 0);
		palette.getContainer().add(new Label(translate("editor.texture.palette.inputfield"), 3, pass += 24, 290, 20));
		palette.getContainer().add(colorfield = new ColorField(palette.getContainer(), newval -> updateColor(newval), 4, pass += 24, 290, 20));
		palette.getContainer().add(new Label(translate("editor.texture.palette.large"), 3, pass += 24, 290, 20)); pass += 24;
		byte[] arr = CURRENTCOLOR.toByteArray();
		for(int x = 0; x < rows; x++){
			for(int z = 0; z < rows; z++){
				int y = x * rows + z;
				float e = (1f / (rows * rows)) * y, f = (1f / rows) * z, h = (255 / rows) * x;
				int r = (int)Math.abs((e * (arr[0] + 128)) + ((1 - f) * h));
				int g = (int)Math.abs((e * (arr[1] + 128)) + ((1 - f) * h));
				int l = (int)Math.abs((e * (arr[2] + 128)) + ((1 - f) * h));
				palette.getContainer().add(panels[x + (z * rows)] = new ColorPanel(3 + (x * 16), pass + (z * 16), 16, 16, new RGB(r, g, l)));
			}
		} pass += 268;
		palette.getContainer().add(new Label(translate("editor.texture.palette.horizontal"), 3, pass += 24, 290, 20)); pass += 24;
		for(int i = 0; i < 36; i++){
			float c = i * (1f / 36);
			int r, g, b;
			//
	        if(c >= 0 && c <= (1/6.f)){
	            r = 255;
	            g = (int)(1530 * c);
	            b = 0;
	        }
	        else if( c > (1/6.f) && c <= (1/3.f) ){
	            r = (int)(255 - (1530 * (c - 1/6f)));
	            g = 255;
	            b = 0;
	        }
	        else if( c > (1/3.f) && c <= (1/2.f)){
	            r = 0;
	            g = 255;
	            b = (int)(1530 * (c - 1/3f));
	        }
	        else if(c > (1/2f) && c <= (2/3f)) {
	            r = 0;
	            g = (int)(255 - ((c - 0.5f) * 1530));
	            b = 255;
	        }
	        else if( c > (2/3f) && c <= (5/6f) ){
	            r = (int)((c - (2/3f)) * 1530);
	            g = 0;
	            b = 255;
	        }
	        else if(c > (5/6f) && c <= 1 ){
	            r = 255;
	            g = 0;
	            b = (int)(255 - ((c - (5/6f)) * 1530));
	        }
	        else{ r = 127; g = 127; b = 127; }
			RGB result = new RGB(r, g, b);
			palette.getContainer().add(new ColorPanel(3 + (i * 8), pass, 8, 20, result));
		}
		palette.getContainer().add(new Label(translate("editor.texture.palette.current"), 3, pass += 24, 290, 20));
		palette.getContainer().add(current = new ColorPanel(3, pass += 24, 290, 20, new RGB()));
		palette.setSize(296, pass + 52);
        this.addSub(palette); pass = -20;
        //
		EditorWidget brushes = new EditorWidget(this, translate("editor.texture.brushes"), 0, 0, 0, 0);
		String off = translate("editor.texture.brushes.tool_off");
		brushes.getContainer().add(face = new FunctionButton(format("editor.texture.brushes.face_bucket", off), 3, pass += 24, 290, 20, () -> toggleBucketMode(PaintMode.FACE)));
		brushes.getContainer().add(polygon = new FunctionButton(format("editor.texture.brushes.polygon_bucket", off), 3, pass += 24, 290, 20, () -> toggleBucketMode(PaintMode.POLYGON)));
		brushes.getContainer().add(group = new FunctionButton(format("editor.texture.brushes.group_bucket", off), 3, pass += 24, 290, 20, () -> toggleBucketMode(PaintMode.GROUP)));
		brushes.getContainer().add(pencil = new FunctionButton(format("editor.texture.brushes.pixel_pencil", off), 3, pass += 24, 290, 20, () -> toggleBucketMode(PaintMode.PIXEL)));
		brushes.getContainer().add(picker = new FunctionButton(format("editor.texture.brushes.color_picker", off), 3, pass += 24, 290, 20, () -> toggleBucketMode(PaintMode.COLORPICKER)));
		brushes.setSize(296, pass + 52);
        this.addSub(brushes); pass = -20;
        //
        reOrderWidgets();
	}

	public static void updateColor(Integer newval){
		if(newval == null) newval = RGB.WHITE.packed;
		CURRENTCOLOR.packed = newval;
		//
		byte[] arr = CURRENTCOLOR.toByteArray();
		for(int x = 0; x < rows; x++){
			for(int z = 0; z < rows; z++){
				int y = x * rows + z;
				float e = (1f / (rows * rows)) * y, f = (1f / rows) * z, h = (255 / rows) * x;
				int r = (int)Math.abs((e * (arr[0] + 128)) + ((1 - f) * h));
				int g = (int)Math.abs((e * (arr[1] + 128)) + ((1 - f) * h));
				int l = (int)Math.abs((e * (arr[2] + 128)) + ((1 - f) * h));
				panels[x + (z * rows)].setColor(new RGB(r, g, l));
			}
		}
		current.setColor(CURRENTCOLOR);
		colorfield.apply(newval);
	}
	
	public static class ColorPanel extends Panel {
		
		private RGB color;

		public ColorPanel(int x, int y, int w, int h, RGB rgb){
			super(x, y, w, h); color = rgb; setColor(rgb);
			this.getListenerMap().addListener(MouseClickEvent.class, listener -> {
				if(listener.getAction() == MouseClickAction.CLICK){
					updateColor(color.packed);
				}
			});
			this.getStyle().setBorder(null);
			this.getStyle().setBorderRadius(0f);
		}

		public void setColor(RGB rgb){
			this.getStyle().getBackground().setColor(FMTB.rgba(color.packed = rgb.packed));
		}
		
	}

	public static void toggleBucketMode(PaintMode mode){
		if(mode == null){ BUCKETMODE = false; } else{ BUCKETMODE = PMODE == mode ? !BUCKETMODE : true; PMODE = mode; }
		String on = translate("editor.texture.brushes.tool_on"), off = translate("editor.texture.brushes.tool_off");
		face.getTextState().setText(format("editor.texture.brushes.face_bucket", BUCKETMODE && PMODE == PaintMode.FACE ? on : off));
		polygon.getTextState().setText(format("editor.texture.brushes.polygon_bucket", BUCKETMODE && PMODE == PaintMode.POLYGON ? on : off));
		group.getTextState().setText(format("editor.texture.brushes.group_bucket", BUCKETMODE && PMODE == PaintMode.GROUP ? on : off));
		pencil.getTextState().setText(format("editor.texture.brushes.pixel_pencil", BUCKETMODE && PMODE == PaintMode.PIXEL ? on : off));
		picker.getTextState().setText(format("editor.texture.brushes.color_picker", BUCKETMODE && PMODE == PaintMode.COLORPICKER ? on : off));
	}
	
	public static enum PaintMode {
		PIXEL, FACE, POLYGON, GROUP, COLORPICKER;
	}

	public static boolean pixelMode(){
		return PMODE == null ? false : PMODE == PaintMode.PIXEL || PMODE == PaintMode.COLORPICKER;
	}

	public static boolean faceMode(){
		return PMODE == null ? false : PMODE == PaintMode.FACE;
	}

	public static boolean polygonMode(){
		return PMODE == null ? false : PMODE == PaintMode.POLYGON;
	}

	public static boolean groupMode(){
		return PMODE == null ? false : PMODE == PaintMode.GROUP;
	}
	
	public static boolean colorPicker(){
		return PMODE == null ? false : PMODE == PaintMode.COLORPICKER;
	}
	
	public static PaintMode paintMode(){ return PMODE; }

	public static void reset(){
		toggleBucketMode(null); PMODE = null; BUCKETMODE = false;
	}
	
}
