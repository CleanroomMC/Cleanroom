package com.cleanroommc.utils.asm.name;

import com.llamalad7.mixinextras.utils.ASMUtils;
import org.objectweb.asm.tree.FieldNode;

public class FieldName {
    public String mcpName;public String mcpDesc;
    public String srgName;public String srgDesc;
    public String notchName;public String notchDesc;
    FieldName(){}
    public static Builder of(){
        return new Builder();
    }
    public boolean is(FieldNode mn){
        return is(mn.name,mn.desc);
    }
    public boolean is(String name,String desc){
        if (srgName.equals(name))return true;
        else if (mcpName.equals(name) && mcpDesc.equals(desc))return true;
        else return notchName.equals(name) && notchDesc.equals(desc);
    }
    public static class Builder{
        FieldName fieldName;
        public Builder(){
            fieldName=new FieldName();
            fieldName.notchName="<null>";
            fieldName.mcpName="<null>";
            fieldName.srgName="<null>";
            fieldName.notchDesc="<null>";
            fieldName.mcpDesc="<null>";
            fieldName.srgDesc="<null>";
        }
        public Builder mcp(String name,String desc){
            fieldName.mcpName=name;
            fieldName.mcpDesc=desc;
            return this;
        }
        public Builder srg(String name,String desc){
            fieldName.srgName=name;
            fieldName.srgDesc=desc;
            return this;
        }
        public Builder notch(String name,String desc){
            fieldName.notchName=name;
            fieldName.notchDesc=desc;
            return this;
        }
        public FieldName build(){
            return fieldName;
        }
    }
}
