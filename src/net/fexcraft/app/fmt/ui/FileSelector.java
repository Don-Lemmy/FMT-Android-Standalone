package net.fexcraft.app.fmt.ui;

import static org.liquidengine.legui.event.MouseClickEvent.MouseClickAction.CLICK;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Dialog;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.SelectBox;
import org.liquidengine.legui.component.optional.align.HorizontalAlign;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.listener.MouseClickEventListener;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.app.fmt.porters.PorterManager;
import net.fexcraft.app.fmt.porters.PorterManager.ExImPorter;
import net.fexcraft.app.fmt.utils.Settings.Setting;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class FileSelector {
	
	/** For general file needs. */
	public static final void select(String title, String root, String[] type, boolean save, AfterTask task){
		if(!root.endsWith("/")) root += "/";
        try(MemoryStack stack = MemoryStack.stackPush()){
        	PointerBuffer buffer = stack.mallocPointer(type.length - 1); String string = "";
            for(int i = 1; i < type.length; i++) buffer.put(stack.UTF8(type[i])); buffer.flip();
    		if(save) string = TinyFileDialogs.tinyfd_saveFileDialog(title, root, buffer, type[0]);
    		else string = TinyFileDialogs.tinyfd_openFileDialog(title, root, buffer, type[0], false);//Print.console(string);
    		if(string != null && string.trim().length() > 0){
    			boolean ends = false;
    			for(int i = 1; i < type.length; i++){
    				if(string.endsWith(type[i].replace("*", ""))){ ends = true; break; }
    			}
    			if(!ends) string += type[1].replace("*", "");
    			task.process(new File(string));
    		}
    		else task.process(null);
        }
	}
	
	/** For selecting an Ex/Im-Porter first. */
	public static final void select(String title, String root, boolean export, SelectTask task){
        Dialog dialog = new Dialog(UserInterfaceUtils.translate("eximporter." + (export ? "export" : "import") + ".select.title"), 340, 125);
        dialog.setResizable(false); if(!root.endsWith("/")) root += "/"; final String reet = root;
        Label label = new Label(UserInterfaceUtils.translate("eximporter." + (export ? "export" : "import") + ".select.desc"), 10, 10, 320, 20);
        Button okbutton = new Button(UserInterfaceUtils.translate("eximporter." + (export ? "export" : "import") + ".select.continue"), 10, 75, 100, 20);
        SelectBox<String> selbox = new SelectBox<>(10, 40, 320, 24);
        List<ExImPorter> eximporter = PorterManager.getPorters(export);
        selbox.setVisibleCount(8); selbox.setElementHeight(20);
        for(ExImPorter porter : eximporter){ selbox.addElement(porter.getName()); }
        selbox.getSelectionButton().getStyle().setFontSize(20f);
        selbox.getSelectBoxElements().forEach(elm -> {
        	elm.getStyle().setHorizontalAlign(HorizontalAlign.LEFT);
        	elm.getStyle().setFontSize(20f);
        });
        okbutton.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) e -> {
        	if(CLICK == e.getAction()){
        		ExImPorter porter = eximporter.get(selbox.getElementIndex(selbox.getSelection()));
        		String tetle = porter.getExtensions()[0]; dialog.close();
        		SettingsBox.open((export ? "Exporter" : "Importer") + " Settings", porter.getSettings(export), false, (settings) -> {
        	        try(MemoryStack stack = MemoryStack.stackPush()){
        	        	PointerBuffer buffer = stack.mallocPointer(porter.getExtensions().length);
        	            for(String pattern : porter.getExtensions()) buffer.put(stack.UTF8(pattern)); buffer.flip(); String string = "";
        	    		if(export) string = TinyFileDialogs.tinyfd_saveFileDialog(title, reet, buffer, tetle);
        	    		else string = TinyFileDialogs.tinyfd_openFileDialog(title, reet, buffer, tetle, false);//Print.console(string);
        	    		if(string != null && string.trim().length() > 0){
        	    			boolean ends = false;
        	    			for(int i = 1; i < porter.getExtensions().length; i++){
        	    				if(string.endsWith(porter.getExtensions()[i].replace("*", ""))){ ends = true; break; }
        	    			}
        	    			if(!ends) string += porter.getExtensions()[1].replace("*", "");
        	    			task.process(new File(string), porter, settings);
        	    		}
        	    		else task.process(null, porter, settings);
        	        }
        		});
        	}
        });
        dialog.getContainer().add(label);
        dialog.getContainer().add(selbox);
        dialog.getContainer().add(okbutton);
        dialog.show(FMTB.frame);
	}
	
	@FunctionalInterface
	public static interface AfterTask {
		
		public void process(File file);
		
	}
	
	@FunctionalInterface
	public static interface SelectTask {
		
		public void process(File file, ExImPorter porter, Map<String, Setting> list);
		
	}
	
	public static String[] TYPE_PNG = { "Portable Network Graphics", "*.png" };
	public static String[] TYPE_IMG = { "Image Files", "*.png", ".jpg", ".jpeg", ".bmp" };
	public static String[] TYPE_FMTB = { "FMT Save File", "*.fmtb" };

}
