package com.cleanroommc.utils.asm.name;

import org.objectweb.asm.tree.MethodNode;

public class MethodName {
    public String mcpName;public String mcpDesc;
    public String srgName;public String srgDesc;
    public String notchName;public String notchDesc;
    MethodName(){}
    public static Builder of(){
        return new Builder();
    }
    public boolean is(MethodNode mn){
        return is(mn.name,mn.desc);
    }
    public boolean is(String name,String desc){
        if (srgName.equals(name))return true;
        else if (mcpName.equals(name) && mcpDesc.equals(desc))return true;
        else return notchName.equals(name) && notchDesc.equals(desc);
    }
    public static class Builder{
        MethodName methodName;
        public Builder(){
            methodName=new MethodName();
            methodName.notchName="<null>";
            methodName.mcpName="<null>";
            methodName.srgName="<null>";
            methodName.notchDesc="<null>";
            methodName.mcpDesc="<null>";
            methodName.srgDesc="<null>";
        }
        public Builder mcp(String name,String desc){
            methodName.mcpName=name;
            methodName.mcpDesc=desc;
            return this;
        }
        public Builder srg(String name,String desc){
            methodName.srgName=name;
            methodName.srgDesc=desc;
            return this;
        }
        public Builder notch(String name,String desc){
            methodName.notchName=name;
            methodName.notchDesc=desc;
            return this;
        }
        public MethodName build(){
            return methodName;
        }
    }
}
