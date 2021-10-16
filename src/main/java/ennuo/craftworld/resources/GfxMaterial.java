package ennuo.craftworld.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.structs.gfxmaterial.Box;
import ennuo.craftworld.resources.structs.gfxmaterial.ParameterAnimation;
import ennuo.craftworld.resources.structs.gfxmaterial.Wire;
import ennuo.toolkit.functions.DebugCallbacks;
import ennuo.toolkit.utilities.Globals;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class GfxMaterial {
    public int flags;
    public float alphaTestLevel;
    public byte alphaLayer, alphaMode, shadowCastMode;
    public float bumpLevel, cosinePower,
    reflectionBlur, refractiveIndex,
    refractiveFresnelFalloffPower, refractiveFresnelMultiplier,
    refractiveFresnelOffset, refractiveFresnelShift;
    public byte fuzzLengthAndRefractiveFlag, translucencyDensity,
    fuzzSwirlAngle, fuzzSwirlAmplitude, fuzzLightingBias,
    fuzzLightingScale, iridescenceRoughness;

    byte[][] shaders;
    public ResourcePtr[] textures;

    public byte[] wrapS, wrapT;

    public Box[] boxes;
    public Wire[] wires;

    public int soundEnum;

    public ParameterAnimation[] parameterAnimations;
    
    public GfxMaterial(Data data) {
        flags = data.i32();

        alphaTestLevel = data.f32();
        alphaLayer = data.i8();
        if (data.revision > 0x331)
            alphaMode = data.i8();
        shadowCastMode = data.i8();

        bumpLevel = data.f32();
        cosinePower = data.f32();
        reflectionBlur = data.f32();
        refractiveIndex = data.f32();

        if (data.revision > 0x13003ef) {
            refractiveFresnelFalloffPower = data.f32();
            refractiveFresnelMultiplier = data.f32();
            refractiveFresnelOffset = data.f32();
            refractiveFresnelShift = data.f32();
            fuzzLengthAndRefractiveFlag = data.i8();
            
            if (data.revision > 0x17703ef) {
                translucencyDensity = data.i8();
                fuzzSwirlAngle = data.i8();
                fuzzSwirlAmplitude = data.i8();
                fuzzLightingBias = data.i8();
                fuzzLightingScale = data.i8();
                iridescenceRoughness = data.i8();
            }
        }

        
        int shaderCount = 3;
        if (data.revision >= 0x398) shaderCount = 11;
        else if (data.revision >= 0x353) shaderCount = 8;
        else if (data.revision == 0x272 || data.revision >= 0x336) shaderCount = 4;
        
        int[] offsets = new int[shaderCount + 1];
        for (int i = (data.revision >= 0x398) ? 1 : 0; i < shaderCount + 1; ++i) offsets[i] = data.i32();
        shaders = new byte[shaderCount][];
        System.out.println("Shaders offset = 0x" + Bytes.toHex(data.offset));
        for (int i = 1; i <= shaderCount; ++i)
            shaders[i - 1] = data.bytes(offsets[i] - offsets[i - 1]);

        textures = new ResourcePtr[8];
        for (int i = 0; i < 8; ++i) {
            textures[i] = data.resource(RType.TEXTURE);
            String str;
            if (textures[i] != null) str = textures[i].toString();
            else str = "null";
            System.out.println("Texture [" + i + "]  offset = 0x" + Bytes.toHex(data.offset) + ", value = " + str);
        }

        System.out.println("WrapS offset = 0x" + Bytes.toHex(data.offset));
        wrapS = new byte[data.i32()];
        for (int i = 0; i < wrapS.length; ++i)
            wrapS[i] = data.i8();
        System.out.println("WrapT offset = 0x" + Bytes.toHex(data.offset));
        wrapT = new byte[data.i32()];
        for (int i = 0; i < wrapT.length; ++i)
            wrapT[i] = data.i8();

        System.out.println("Boxes offset = 0x" + Bytes.toHex(data.offset));
        boxes = Box.array(data);
        System.out.println("Wires offset = 0x" + Bytes.toHex(data.offset));
        wires = Wire.array(data);
        
        if (data.revision >= 0x149) {
            System.out.println("SoundEnum offset = 0x" + Bytes.toHex(data.offset));
            soundEnum = data.i32();
        }
        
        if (data.revision >= 0x2a2) {
            System.out.println("ParameterAnimations offset = 0x" + Bytes.toHex(data.offset));
            parameterAnimations = ParameterAnimation.array(data);            
        }

        System.out.println("End Parsing offset = 0x" + Bytes.toHex(data.offset));
    }
    
    public Wire findWireFrom(int box) {
        for (Wire wire: this.wires)
            if (wire.boxFrom == box)
                return wire;
        return null;
    }
    
    public int getOutputBox() {
        for (int i = 0; i < this.boxes.length; ++i) {
            Box box = this.boxes[i];
            if (box.type == Box.BoxType.OUTPUT)
                return i;
        }
        return -1;
    }
    
    public Box getBoxFrom(Wire wire) {
        return this.boxes[wire.boxFrom];
    }
    
    public Box getBoxTo(Wire wire) {
        return this.boxes[wire.boxTo];
    }
    
    public byte[] extractTexture(int index) {
        byte[] data = Globals.extractFile(this.textures[index]);
        if (data == null) return null;
        Texture texture = new Texture(data);
        if (texture.parsed) {
            BufferedImage image = texture.getImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", baos);
                return baos.toByteArray();
            } catch (IOException ex) {
                Logger.getLogger(GfxMaterial.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return null;
    }

}
