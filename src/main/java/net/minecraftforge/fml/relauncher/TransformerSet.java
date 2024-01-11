package net.minecraftforge.fml.relauncher;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public abstract class TransformerSet implements IClassTransformer {
    public TransformerSet(){}
    public abstract void registerTransformer();
    private final Multimap<String,ITransformer> transformers= LinkedHashMultimap.create();
    public void addTransformer(String targetClass,ITransformer transformer){
        transformers.put(targetClass,transformer);
    }
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass!=null && basicClass.length>0){
            try{
                if (transformers.containsKey(transformedName)){
                    ClassReader classReader=new ClassReader(basicClass);
                    ClassNode cn=new ClassNode();
                    classReader.accept(cn, 0);

                    int flag=0;
                    for(ITransformer transformer:transformers.get(transformedName)){
                        flag|=transformer.transform(cn);
                    }

                    ClassWriter classWriter=new ClassWriter(classReader,flag);
                    cn.accept(classWriter);
                    return classWriter.toByteArray();
                }else return basicClass;
            }catch (Exception e){
                e.printStackTrace();
                return basicClass;
            }
        }else return basicClass;
    }
}
