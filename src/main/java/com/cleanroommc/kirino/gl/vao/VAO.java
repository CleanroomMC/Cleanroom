package com.cleanroommc.kirino.gl.vao;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.EBOView;
import com.cleanroommc.kirino.gl.buffer.VBOView;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VAO extends GLDisposable {
    public final int vaoID;

    private final AttributeLayout attributeLayout;
    private final EBOView eboView;
    private final List<VBOView> vboViews = new ArrayList<>();

    public static void bind(int vaoID) {
        GL30.glBindVertexArray(vaoID);
    }

    public void bind() {
        bind(vaoID);
    }

    public VAO(AttributeLayout attributeLayout, EBOView eboView, VBOView... vboViews) {
        vaoID = GL30.glGenVertexArrays();
        this.attributeLayout = attributeLayout;
        this.eboView = eboView;
        this.vboViews.addAll(Arrays.asList(vboViews));

        bind();
        eboView.bind();
        attributeLayout.upload(vboViews);
        bind(0);
        eboView.bind(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GLResourceManager.addDisposable(this);
    }

    @Override
    public int disposePriority() {
        return 100; // earlier than vbo and ebo
    }

    @Override
    public void dispose() {
        GL30.glDeleteVertexArrays(vaoID);
    }
}
