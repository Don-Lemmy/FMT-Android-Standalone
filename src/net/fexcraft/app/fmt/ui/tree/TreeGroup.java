package net.fexcraft.app.fmt.ui.tree;

import static org.liquidengine.legui.event.MouseClickEvent.MouseClickAction.CLICK;

import java.util.HashMap;
import java.util.function.Consumer;

import org.liquidengine.legui.component.Component;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.Panel;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.input.Mouse.MouseButton;
import org.liquidengine.legui.style.color.ColorConstants;

import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.app.fmt.porters.PorterManager;
import net.fexcraft.app.fmt.porters.PorterManager.ExImPorter;
import net.fexcraft.app.fmt.ui.DialogBox;
import net.fexcraft.app.fmt.ui.editor.Editors;
import net.fexcraft.app.fmt.ui.editor.PreviewEditor;
import net.fexcraft.app.fmt.utils.GGR;
import net.fexcraft.app.fmt.utils.HelperCollector;
import net.fexcraft.app.fmt.utils.Settings;
import net.fexcraft.app.fmt.utils.Settings.Setting;
import net.fexcraft.app.fmt.wrappers.GroupCompound;
import net.fexcraft.app.fmt.wrappers.TurboList;
import net.fexcraft.lib.common.math.Vec3f;
import net.fexcraft.lib.common.utils.Print;
import net.fexcraft.lib.mc.utils.Static;

public class TreeGroup extends Panel {
	
	protected GroupCompound compound;
	private boolean animations;
	private TurboList list;
	private TreeBase tree;
	private Label label;

	public TreeGroup(TreeBase base){
		super(0, 0, base.getSize().x - 12, 20); tree = base;
		this.add(label = new Label("group-label", 0, 0, (int)getSize().x, 20));
		Consumer<Boolean> con;
		Settings.THEME_CHANGE_LISTENER.add(con = bool -> {
			label.getStyle().setFont("roboto-bold");
			label.getStyle().setPadding(0, 0, 0, 5);
			label.getStyle().setBorderRadius(0);
			//if(bool){
				label.getStyle().setTextColor(ColorConstants.darkGray());
			//}
			//else{
			//	label.getStyle().setTextColor(ColorConstants.lightGray());
			//}
			getStyle().getBorder().setEnabled(false);
			if(compound != null || list != null) updateColor();
		});
		con.accept(Settings.darktheme());
	}
	
	public TreeGroup(TreeBase base, TurboList group, boolean flag){
		this(base); list = group; updateColor(); animations = flag; if(!flag) Static.halt(0);
		this.add(new TreeIcon((int)getSize().x - 42, 0, "group_visible", () -> {
			list.visible = !list.visible; updateColor();
		}, "visibility"));
		this.add(new TreeIcon((int)getSize().x - 64, 0, "group_edit", () -> {
			Editors.show("group");
		}, "edit"));
		this.add(new TreeIcon((int)getSize().x - 86, 0, "group_minimize", () -> toggle(!list.aminimized), "minimize"));
		label.getListenerMap().addListener(MouseClickEvent.class, listener -> {
			if(listener.getAction() != CLICK || listener.getButton() != MouseButton.MOUSE_BUTTON_LEFT) return;
			boolean sell = list.selected; FMTB.MODEL.clearSelection();
			list.selected = !sell; FMTB.MODEL.updateFields(); FMTB.MODEL.lastselected = null; updateColor();
		});
		this.recalculateSize();
	}
	
	public TreeGroup(TreeBase base, TurboList group){
		this(base); list = group; updateColor();
		this.add(new TreeIcon((int)getSize().x - 20, 0, "group_delete", () -> {
			DialogBox.showYN(null, () -> { FMTB.MODEL.getGroups().remove(list.id); }, null, "tree.polygon.remove_group", "#" + list.id);
		}, "delete"));
		this.add(new TreeIcon((int)getSize().x - 42, 0, "group_visible", () -> {
			list.visible = !list.visible; updateColor();
		}, "visibility"));
		this.add(new TreeIcon((int)getSize().x - 64, 0, "group_edit", () -> {
			Editors.show("group");
		}, "edit"));
		this.add(new TreeIcon((int)getSize().x - 86, 0, "group_minimize", () -> toggle(!list.minimized), "minimize"));
		label.getListenerMap().addListener(MouseClickEvent.class, listener -> {
			if(listener.getAction() != CLICK || listener.getButton() != MouseButton.MOUSE_BUTTON_LEFT) return;
			boolean sell = list.selected; if(!GGR.isShiftDown()){ FMTB.MODEL.clearSelection(); }
			list.selected = !sell; FMTB.MODEL.updateFields(); FMTB.MODEL.lastselected = null; updateColor();
			GroupCompound.SELECTED_POLYGONS = FMTB.MODEL.countSelectedMRTs();
		});
		this.recalculateSize();
	}
	
	public TreeGroup(TreeBase base, GroupCompound group){
		this(base); compound = group; updateColor();
		this.add(new TreeIcon((int)getSize().x - 20, 0, "group_delete", () -> {
			HelperCollector.LOADED.remove(index()); this.removeFromTree(); tree.reOrderGroups();
		}, "delete"));
		this.add(new TreeIcon((int)getSize().x - 42, 0, "group_visible", () -> {
			compound.visible = !compound.visible; updateColor();
		}, "visibility"));
		this.add(new TreeIcon((int)getSize().x - 64, 0, "group_edit", () -> {
			Editors.show("preview");
		}, "edit"));
		this.add(new TreeIcon((int)getSize().x - 86, 0, "group_clone", () -> {
			GroupCompound newcomp = null, parent = compound;
			if(parent.name.startsWith("fmtb/")){
				newcomp = HelperCollector.loadFMTB(parent.origin);
			}
			else if(parent.name.startsWith("frame/")){
				newcomp = HelperCollector.loadFrame(parent.origin);
			}
			else{
				ExImPorter porter = PorterManager.getPorterFor(parent.origin, false);
				HashMap<String, Setting> map = new HashMap<>();
				porter.getSettings(false).forEach(setting -> map.put(setting.getId(), setting));
				newcomp = HelperCollector.load(parent.file, porter, map);
			}
			if(newcomp == null){ Print.console("Error on creating clone."); return; }
			if(parent.pos != null) newcomp.pos = new Vec3f(parent.pos);
			if(parent.rot != null) newcomp.rot = new Vec3f(parent.rot);
			if(parent.scale != null) newcomp.scale = new Vec3f(parent.scale);
		}, "clone"));
		this.add(new TreeIcon((int)getSize().x - 108, 0, "group_minimize", () -> toggle(!compound.minimized), "minimize"));
		label.getListenerMap().addListener(MouseClickEvent.class, listener -> {
			if(listener.getAction() != CLICK || listener.getButton() != MouseButton.MOUSE_BUTTON_LEFT) return;
			GroupCompound model = HelperCollector.getSelected();
			if(selected()){
				HelperCollector.SELECTED = -1; updateColor();
			}
			else{
				if(HelperCollector.SELECTED > -1) model = HelperCollector.getSelected();
				HelperCollector.SELECTED = index();
			}
			if(model != null) model.button.updateColor(); updateColor();
			model = HelperCollector.getSelected();
			if(model == null){
				PreviewEditor.pos_x.apply(0);
				PreviewEditor.pos_y.apply(0);
				PreviewEditor.pos_z.apply(0);
				PreviewEditor.poss_x.apply(0);
				PreviewEditor.poss_x.apply(0);
				PreviewEditor.poss_x.apply(0);
				PreviewEditor.rot_x.apply(0);
				PreviewEditor.rot_y.apply(0);
				PreviewEditor.rot_z.apply(0);
				PreviewEditor.size_x.apply(1);
				PreviewEditor.size_y.apply(1);
				PreviewEditor.size_z.apply(1);
				PreviewEditor.size16_x.apply(16);
				PreviewEditor.size16_y.apply(16);
				PreviewEditor.size16_z.apply(16);
			}
			else{
				PreviewEditor.pos_x.apply(model.pos == null ? 0 : model.pos.xCoord);
				PreviewEditor.pos_y.apply(model.pos == null ? 0 : model.pos.yCoord);
				PreviewEditor.pos_z.apply(model.pos == null ? 0 : model.pos.zCoord);
				PreviewEditor.poss_x.apply(model.pos == null ? 0 : model.pos.xCoord * 16);
				PreviewEditor.poss_x.apply(model.pos == null ? 0 : model.pos.yCoord * 16);
				PreviewEditor.poss_x.apply(model.pos == null ? 0 : model.pos.zCoord * 16);
				PreviewEditor.rot_x.apply(model.rot == null ? 0 : model.rot.xCoord);
				PreviewEditor.rot_y.apply(model.rot == null ? 0 : model.rot.yCoord);
				PreviewEditor.rot_z.apply(model.rot == null ? 0 : model.rot.zCoord);
				PreviewEditor.size_x.apply(model.scale == null ? 1 : model.scale.xCoord);
				PreviewEditor.size_y.apply(model.scale == null ? 1 : model.scale.yCoord);
				PreviewEditor.size_z.apply(model.scale == null ? 1 : model.scale.zCoord);
				PreviewEditor.size16_x.apply(model.scale == null ? 1 : model.scale.xCoord * 16);
				PreviewEditor.size16_y.apply(model.scale == null ? 1 : model.scale.yCoord * 16);
				PreviewEditor.size16_z.apply(model.scale == null ? 1 : model.scale.zCoord * 16);
			}
		});
		this.recalculateSize();
	}
	
	public void toggle(boolean bool){
		if(animations) list.aminimized = bool;
		else if(list == null) compound.minimized = bool;
		else list.minimized = bool; recalculateSize();
		getChildComponents().forEach(con -> {
			if(con instanceof SubTreeGroup){
				((SubTreeGroup)con).toggle(!bool);
			}
		});
	}
	
	public void recalculateSize(){
		if(animations){
			this.setSize(this.getSize().x, list.aminimized ? 20 : (list.animations.size() * 22) + 20);
			this.update(); this.updateColor();
		}
		else if(list != null){
			this.setSize(this.getSize().x, list.minimized ? 20 : (list.size() * 22) + 20);
		}
		else{
			this.setSize(this.getSize().x, compound.minimized ? 20 : (compound.getGroups().size() * 22) + 20);
		}
		getChildComponents().forEach(con -> {
			if(con instanceof SubTreeGroup) ((SubTreeGroup)con).refreshPosition();
		});
		tree.reOrderGroups();
	}

	public void removeFromTree(){
		tree.scrollable.getContainer().remove(this); tree.groups.remove(this);
	}
	
	public TreeBase tree(){
		return tree;
	}

	public Component update(){
		label.getTextState().setText(list == null ? compound.name : animations ? "[" + list.animations.size() + "] " + list.id : list.id); return this;
	}
	
	public void updateColor(){
		if(animations){
			int color = 0;
			if(list.animations.isEmpty()) color = list.selected ? list.visible ? 0xfc7900 : 0xffe7d1 : list.visible ? 0xffa14a : 0xd1ac8a;
			else color = list.selected ? list.visible ? 0x2985ba : 0x7eb1cf : list.visible ? 0x28a148 : 0x6bbf81;
			label.getStyle().getBackground().setColor(FMTB.rgba(color));
		}
		else if(list == null) label.getStyle().getBackground().setColor(FMTB.rgba(selected() ? compound.visible ? 0xa37a18 : 0xd6ad4b : compound.visible ? 0x5e75e6 : 0xa4b0ed));
		else label.getStyle().getBackground().setColor(FMTB.rgba(list.selected ? list.visible ? 0xa37a18 : 0xd6ad4b : list.visible ? 0x28a148 : 0x6bbf81));
	}
	
	public boolean selected(){
		return HelperCollector.SELECTED > -1 && HelperCollector.SELECTED == index();
	}
	
	public int index(){
		return HelperCollector.LOADED.indexOf(compound);
	}
	
}
