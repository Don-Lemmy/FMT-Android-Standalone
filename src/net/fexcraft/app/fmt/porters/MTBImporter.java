package net.fexcraft.app.fmt.porters;

import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.app.fmt.porters.PorterManager.InternalPorter;
import net.fexcraft.app.fmt.ui.general.DialogBox;
import net.fexcraft.app.fmt.utils.TextureManager;
import net.fexcraft.app.fmt.wrappers.*;
import net.fexcraft.lib.common.math.Vec3f;
import net.fexcraft.lib.common.utils.Print;
import net.fexcraft.lib.common.utils.ZipUtil;
import net.fexcraft.lib.tmt.ModelRendererTurbo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Enumeration;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

/**
 * @author EternalBlueFlame, FEX___96
 *
 */
public class MTBImporter extends InternalPorter {
	
	private static String[] extensions = new String[]{ ".mtb" };

    public String[] getExtensions(){
        return extensions;
    }
    
    public String getId(){
    	return "internal_mtb_importer";
    }
    
    public String getName(){
    	return "Internal MTB Importer";
    }

    public static float getFloatFromString(String s){
        return Float.parseFloat(s.replace(",",".").trim());
    }

    static String convertStreamToString(java.io.InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
    }

    public static int getSideFromName(String MrMysterious){
        switch (MrMysterious){
            case "MR_BOTTOM":{return ModelRendererTurbo.MR_BOTTOM;}
            case "MR_LEFT":{return ModelRendererTurbo.MR_LEFT;}
            case "MR_RIGHT":{return ModelRendererTurbo.MR_RIGHT;}
            case "MR_FRONT":{return ModelRendererTurbo.MR_FRONT;}
            case "MR_BACK":{return ModelRendererTurbo.MR_BACK;}
            case "MR_TOP": default:{return ModelRendererTurbo.MR_TOP;}
        }
    }
    
	@Override
	public GroupCompound importModel(File f){
        try {
            GroupCompound compound = new GroupCompound(f);
            boolean loadtex = ZipUtil.contains(f, "Model.png");
            ZipFile zip = new ZipFile(f);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            InputStream stream = null;
            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                if(entry.getName().equals("Model.txt")){
                    stream = zip.getInputStream(entry); break;
                }
            }
            if(stream == null){
            	FMTB.showDialogbox("Status\nImport Failed, MTB appears corrupt.", "Oh well..", "", DialogBox.NOTHING, null);
                zip.close(); return compound;
            }
            String[] file = convertStreamToString(stream).split("\n"); //Files.readAllLines(stream.toPath());
            for(String s : file){
                String[] parts = s.split("\\u007C");
                parts[0] = parts[0].trim();
                if(parts[0].equals("TexSizeX")){
                    compound.textureSizeX = Integer.parseInt(parts[1].trim());
                }
                else if(parts[0].equals("TexSizeY")){
                    compound.textureSizeY = Integer.parseInt(parts[1]);
                }
                //
                else if(parts[0].equals("ModelAuthor") && parts.length > 1){
                    compound.creators.add(parts[1]);
                }
                else if(parts[0].equals("ModelName") && parts.length > 1){
                	compound.name = parts[1];
                }
                else if(parts[0].equals("Element")){
                    BoxWrapper polygon = null;
                    switch(parts[5]){
                        case "Box":{
                            polygon = new BoxWrapper(compound); break;
                        }
                        case "Shapebox":{
                            polygon = new ShapeboxWrapper(compound).setCoords(
                                new Vec3f(getFloatFromString(parts[20]),getFloatFromString(parts[28]),getFloatFromString(parts[36])),
                                new Vec3f(getFloatFromString(parts[21]),getFloatFromString(parts[29]),getFloatFromString(parts[37])),
                                new Vec3f(getFloatFromString(parts[22]),getFloatFromString(parts[30]),getFloatFromString(parts[38])),
                                new Vec3f(getFloatFromString(parts[23]),getFloatFromString(parts[31]),getFloatFromString(parts[39])),
                                new Vec3f(getFloatFromString(parts[24]),getFloatFromString(parts[32]),getFloatFromString(parts[40])),
                                new Vec3f(getFloatFromString(parts[25]),getFloatFromString(parts[33]),getFloatFromString(parts[41])),
                                new Vec3f(getFloatFromString(parts[26]),getFloatFromString(parts[34]),getFloatFromString(parts[42])),
                                new Vec3f(getFloatFromString(parts[27]),getFloatFromString(parts[35]),getFloatFromString(parts[43]))
                            );
                            break;
                        }
                        case "FlexBox":{
                            polygon = new FlexboxWrapper(compound).setCoords(
                                getFloatFromString(parts[47]),getFloatFromString(parts[48]),
                                getFloatFromString(parts[49]), getFloatFromString(parts[50]),
                                parts[44]
                            );
                            break;
                        }
                        case "Trapezoid":{
                            polygon = new TrapezoidWrapper(compound).setCoords(getFloatFromString(parts[45]),parts[44]);
                            break;
                        }
                        case "FlexTrapezoid":{
                            polygon = new FlexTrapezoidWrapper(compound).setCoords(
                                getFloatFromString(parts[52]), getFloatFromString(parts[53]),getFloatFromString(parts[54]),
                                getFloatFromString(parts[55]), getFloatFromString(parts[55]), getFloatFromString(parts[55]),
                                parts[44]
                            );
                            break;
                        }
                        /*
                        case "Shape":{
                            turbo.addShape3D();
                            break;
                        }
                        */
                    }
                    if(polygon == null) continue;
                    //
                    polygon.name = parts[3];
                    polygon.size = new Vec3f(getFloatFromString(parts[9]), getFloatFromString(parts[10]), getFloatFromString(parts[11]));
                    polygon.off = new Vec3f(getFloatFromString(parts[15]), getFloatFromString(parts[16]), getFloatFromString(parts[17]));
                    polygon.pos = new Vec3f(getFloatFromString(parts[6]), getFloatFromString(parts[7]), getFloatFromString(parts[8]));
                    polygon.textureX = Integer.parseInt(parts[18]);
                    polygon.textureY = Integer.parseInt(parts[19]);
                    //
                    /*polygon.rot = new Vec3f(Math.toDegrees(getFloatFromString(parts[12])), Math.toDegrees(getFloatFromString(parts[13])), Math.toDegrees(getFloatFromString(parts[14])));
                    if(polygon.rot.xCoord != 0){
                        polygon.rot.xCoord *= 0.01745329259;
                    }
                    if(polygon.rot.yCoord != 0){
                        polygon.rot.yCoord *= 0.01745329259;
                    }
                    if(polygon.rot.zCoord != 0){
                        polygon.rot.zCoord *= -0.01745329259;
                    }*/
                    polygon.rot = new Vec3f(getFloatFromString(parts[12]), getFloatFromString(parts[13]), getFloatFromString(parts[14]));
                    polygon.rot.zCoord = -polygon.rot.zCoord;
                    //
                    if(!compound.getCompound().containsKey("group" + parts[4])){
                    	compound.getCompound().put("group" + parts[4], new TurboList("group" + parts[4]));
                    }
                    compound.getCompound().get("group" + parts[4]).add(polygon);
                }
            }
            stream.close();
            if(loadtex){
            	try{
            		BufferedImage image = ImageIO.read(zip.getInputStream(zip.getEntry("Model.png")));
            		boolean transparent = true; Color color = null;
            		for(int x = 0; x < image.getWidth(); x++){
            			for(int y = 0; y < image.getHeight(); y++){
            				color = new Color(image.getRGB(x, y));
            				if(color.getAlpha() > 0 && color.getRed() > 0 && color.getBlue() > 0 && color.getGreen() > 0){
            					transparent = false; break;
            				}
            			}
            		}
            		if(!transparent){
                    	compound.setTexture("./temp/" + compound.name);
                    	TextureManager.loadTextureFromZip(image, "./temp/" + compound.name, false, true);
            		}
            	}
            	catch(Exception e){
            		e.printStackTrace(); Print.console("Could not load texture from MTB.");
            	}
            }
            zip.close(); return compound;
        }
        catch(IOException e){
        	//literally not even possible.
        	return new GroupCompound(f);
        }
	}
	
	@Override
	public String exportModel(GroupCompound compound, File file){
		return "This isn't an exporter as of now.";
	}
	
	@Override
	public boolean isImporter(){
		return true;
	}
	
	@Override
	public boolean isExporter(){
		return false;
	}
    
}